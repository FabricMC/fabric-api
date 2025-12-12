/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.resource.pack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.FileUtil;

import net.fabricmc.fabric.api.resource.v1.pack.ModPackResources;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class ModNioPackResources implements PackResources, ModPackResources {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModNioPackResources.class);
	private static final Pattern RESOURCE_PACK_PATH = Pattern.compile("[a-z0-9-_.]+");
	private static final FileSystem DEFAULT_FS = FileSystems.getDefault();

	private final String id;
	private final ModContainer mod;
	private final List<Path> basePaths;
	private final PackType type;
	private final PackActivationType activationType;
	private final Map<PackType, Set<String>> namespaces;
	private final PackLocationInfo metadata;
	/**
	 * Whether the pack is bundled and loaded by default, as opposed to registered built-in packs.
	 * @see ModResourcePackUtil#appendModResourcePacks(List, PackType, String)
	 */
	private final boolean modBundled;

	@Nullable
	public static ModNioPackResources create(String id, ModContainer mod, String subPath, PackType type, PackActivationType activationType, boolean modBundled) {
		List<Path> rootPaths = mod.getRootPaths();
		List<Path> paths;

		if (subPath == null) {
			paths = rootPaths;
		} else {
			paths = new ArrayList<>(rootPaths.size());

			for (Path path : rootPaths) {
				path = path.toAbsolutePath().normalize();
				Path childPath = path.resolve(subPath.replace("/", path.getFileSystem().getSeparator())).normalize();

				if (!childPath.startsWith(path) || !exists(childPath)) {
					continue;
				}

				paths.add(childPath);
			}
		}

		if (paths.isEmpty()) return null;

		String packId = subPath != null && modBundled ? id + "_" + subPath : id;
		Component displayName = subPath == null
				? Component.translatable("pack.name.fabricMod", mod.getMetadata().getName())
				: Component.translatable("pack.name.fabricMod.subPack", mod.getMetadata().getName(), Component.translatable("resourcePack." + subPath + ".name"));
		PackLocationInfo metadata = new PackLocationInfo(
				packId,
				displayName,
				ModResourcePackCreator.RESOURCE_PACK_SOURCE,
				Optional.of(new KnownPack(ModResourcePackCreator.VANILLA, packId, mod.getMetadata().getVersion().getFriendlyString()))
		);
		var ret = new ModNioPackResources(packId, mod, paths, type, activationType, modBundled, metadata);

		return ret.getNamespaces(type).isEmpty() ? null : ret;
	}

	private ModNioPackResources(String id, ModContainer mod, List<Path> paths, PackType type, PackActivationType activationType, boolean modBundled, PackLocationInfo metadata) {
		this.id = id;
		this.mod = mod;
		this.basePaths = paths;
		this.type = type;
		this.activationType = activationType;
		this.modBundled = modBundled;
		this.namespaces = readNamespaces(paths, mod.getMetadata().getId());
		this.metadata = metadata;
	}

	@Override
	public ModNioPackResources createOverlay(String overlay) {
		// See DirectoryResourcePack.
		return new ModNioPackResources(this.id, this.mod, this.basePaths.stream().map(
				path -> path.resolve(overlay)
		).toList(), this.type, this.activationType, this.modBundled, this.metadata);
	}

	public static Map<PackType, Set<String>> readNamespaces(List<Path> paths, String modId) {
		var ret = new EnumMap<PackType, Set<String>>(PackType.class);

		for (PackType type : PackType.values()) {
			Set<String> namespaces = null;

			for (Path path : paths) {
				Path dir = path.resolve(type.getDirectory());
				if (!Files.isDirectory(dir)) continue;

				String separator = path.getFileSystem().getSeparator();

				try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
					for (Path p : ds) {
						if (!Files.isDirectory(p)) continue;

						String s = p.getFileName().toString();
						// s may contain trailing slashes, remove them
						s = s.replace(separator, "");

						if (!RESOURCE_PACK_PATH.matcher(s).matches()) {
							LOGGER.warn("Fabric NioResourcePack: ignored invalid namespace: {} in mod ID {}", s, modId);
							continue;
						}

						if (namespaces == null) namespaces = new HashSet<>();

						namespaces.add(s);
					}
				} catch (IOException e) {
					LOGGER.warn("getNamespaces in mod " + modId + " failed!", e);
				}
			}

			ret.put(type, namespaces != null ? namespaces : Collections.emptySet());
		}

		return ret;
	}

	private Path getPath(String filename) {
		if (this.hasAbsentNs(filename)) return null;

		for (Path basePath : this.basePaths) {
			Path childPath = basePath.resolve(filename.replace("/", basePath.getFileSystem().getSeparator())).toAbsolutePath().normalize();

			if (childPath.startsWith(basePath) && exists(childPath)) {
				return childPath;
			}
		}

		return null;
	}

	private static final String resPrefix = PackType.CLIENT_RESOURCES.getDirectory() + "/";
	private static final String dataPrefix = PackType.SERVER_DATA.getDirectory() + "/";

	private boolean hasAbsentNs(String filename) {
		int prefixLen;
		PackType type;

		if (filename.startsWith(resPrefix)) {
			prefixLen = resPrefix.length();
			type = PackType.CLIENT_RESOURCES;
		} else if (filename.startsWith(dataPrefix)) {
			prefixLen = dataPrefix.length();
			type = PackType.SERVER_DATA;
		} else {
			return false;
		}

		int nsEnd = filename.indexOf('/', prefixLen);
		if (nsEnd < 0) return false;

		return !this.namespaces.get(this.type).contains(filename.substring(prefixLen, nsEnd));
	}

	private IoSupplier<InputStream> openFile(String filename) {
		Path path = this.getPath(filename);

		if (path != null && Files.isRegularFile(path)) {
			return () -> Files.newInputStream(path);
		}

		if (ModPackResourcesUtil.containsDefault(filename, this.modBundled)) {
			return () -> ModPackResourcesUtil.openDefault(this.mod, this.type, filename);
		}

		return null;
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getRootResource(String... pathSegments) {
		FileUtil.validatePath(pathSegments);

		return this.openFile(String.join("/", pathSegments));
	}

	@Override
	@Nullable
	public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
		final Path path = this.getPath(getFilename(type, id));
		return path == null ? null : IoSupplier.create(path);
	}

	@Override
	public void listResources(PackType type, String namespace, String path, ResourceOutput visitor) {
		if (!this.namespaces.getOrDefault(type, Collections.emptySet()).contains(namespace)) {
			return;
		}

		for (Path basePath : this.basePaths) {
			String separator = basePath.getFileSystem().getSeparator();
			Path nsPath = basePath.resolve(type.getDirectory()).resolve(namespace);
			Path searchPath = nsPath.resolve(path.replace("/", separator)).normalize();
			if (!exists(searchPath)) continue;

			try {
				Files.walkFileTree(searchPath, new SimpleFileVisitor<>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
						String filename = nsPath.relativize(file).toString().replace(separator, "/");
						Identifier identifier = Identifier.tryBuild(namespace, filename);

						if (identifier == null) {
							LOGGER.error("Invalid path in mod resource-pack {}: {}:{}, ignoring", id, namespace, filename);
						} else {
							visitor.accept(identifier, IoSupplier.create(file));
						}

						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				LOGGER.warn("findResources at " + path + " in namespace " + namespace + ", mod " + mod.getMetadata().getId() + " failed!", e);
			}
		}
	}

	@Override
	public Set<String> getNamespaces(PackType type) {
		return this.namespaces.getOrDefault(type, Set.of());
	}

	@Override
	public <T> T getMetadataSection(MetadataSectionType<T> metaReader) throws IOException {
		try (InputStream is = Objects.requireNonNull(this.openFile("pack.mcmeta")).get()) {
			return AbstractPackResources.getMetadataFromStream(metaReader, is, this.metadata);
		}
	}

	@Override
	public PackLocationInfo location() {
		return this.metadata;
	}

	@Override
	public void close() {
	}

	@Override
	public ModMetadata getFabricModMetadata() {
		return this.mod.getMetadata();
	}

	public PackActivationType getActivationType() {
		return this.activationType;
	}

	@Override
	public String packId() {
		return this.id;
	}

	private static boolean exists(Path path) {
		// NIO Files.exists is notoriously slow when checking the file system
		return path.getFileSystem() == DEFAULT_FS ? path.toFile().exists() : Files.exists(path);
	}

	private static String getFilename(PackType type, Identifier id) {
		return String.format(Locale.ROOT, "%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath());
	}
}
