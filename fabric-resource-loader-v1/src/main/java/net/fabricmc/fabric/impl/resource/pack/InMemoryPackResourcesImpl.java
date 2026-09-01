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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.InclusiveRange;

import net.fabricmc.fabric.api.resource.v1.pack.InMemoryPackResources;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;

/// Represents an in-memory resource pack.
///
/// The resources of this pack are stored in memory instead of it being on-disk.
public final class InMemoryPackResourcesImpl {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final boolean DUMP = TriState.fromSystemProperty("fabric.resource_loader.debug.pack.dump_from_in_memory")
			.orElse(FabricLoader.getInstance().isDevelopmentEnvironment());
	private final Map<Identifier, Supplier<byte[]>> assets = new ConcurrentHashMap<>();
	private final Map<Identifier, Supplier<byte[]>> data = new ConcurrentHashMap<>();
	private final Map<String, Supplier<byte[]>> root = new ConcurrentHashMap<>();

	private final InMemoryPackResources parent;

	public InMemoryPackResourcesImpl(InMemoryPackResources parent) {
		this.parent = parent;
	}

	public @Nullable IoSupplier<InputStream> getRootResource(String... path) {
		String actualPath = String.join("/", path);

		return this.openResource(this.root, actualPath);
	}

	public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier id) {
		return this.openResource(this.getResourceMap(type), id);
	}

	private <T> @Nullable IoSupplier<InputStream> openResource(Map<T, Supplier<byte[]>> map, T key) {
		Supplier<byte[]> supplier = map.get(key);

		if (supplier == null) {
			return null;
		}

		byte[] bytes = supplier.get();

		if (bytes == null) {
			return null;
		}

		return () -> new ByteArrayInputStream(bytes);
	}

	public void listResources(PackType type, String namespace, String directory, PackResources.ResourceOutput consumer) {
		String normalizedDirectory = this.normalizeDirectory(directory);

		this.getResourceMap(type).entrySet().stream()
				.filter(entry -> entry.getKey().getNamespace().equals(namespace) && entry.getKey().getPath().startsWith(normalizedDirectory))
				.forEach(entry -> {
					byte[] bytes = entry.getValue().get();

					if (bytes != null) {
						consumer.accept(entry.getKey(), () -> new ByteArrayInputStream(bytes));
					}
				});
	}

	private String normalizeDirectory(String directory) {
		if (directory.endsWith("/")) {
			return directory;
		} else {
			return directory + '/';
		}
	}

	public @Unmodifiable Set<String> getNamespaces(PackType type) {
		return this.getResourceMap(type).keySet().stream()
				.map(Identifier::getNamespace)
				.collect(Collectors.toUnmodifiableSet());
	}

	@SuppressWarnings("unchecked")
	public <T> @Nullable T getMetadataSection(MetadataSectionType<T> metadataSectionType) throws IOException {
		if (!this.root.containsKey(PackResources.PACK_META)) {
			if (metadataSectionType == PackMetadataSection.CLIENT_TYPE) {
				return (T) new PackMetadataSection(
						Component.translatable("pack.description.fabric.virtual"),
						new InclusiveRange<>(SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES))
				);
			} else if (metadataSectionType == PackMetadataSection.SERVER_TYPE) {
				return (T) new PackMetadataSection(
						Component.translatable("pack.description.fabric.virtual"),
						new InclusiveRange<>(SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA))
				);
			}
		}

		IoSupplier<InputStream> resource = this.getRootResource(PackResources.PACK_META);
		if (resource == null) return null;

		try (var stream = resource.get()) {
			return ResourceMetadata.fromJsonStream(stream).getSection(metadataSectionType).orElse(null);
		}
	}

	public void close() {
		if (DUMP) {
			this.dumpAll();
		}
	}

	public void putResource(String fileName, byte[] resource) {
		this.root.put(fileName, () -> resource);
	}

	public void putResource(PackType type, Identifier id, byte[] resource) {
		this.getResourceMap(type).put(id, () -> resource);
	}

	private void putResource(String fileName, Supplier<byte[]> resource) {
		this.root.put(fileName, Suppliers.memoize(resource::get));
	}

	private void putResource(PackType type, Identifier id, Supplier<byte[]> resource) {
		this.getResourceMap(type).put(id, Suppliers.memoize(resource::get));
	}

	public void putResourceAsync(String fileName, Future<byte[]> future) {
		this.putResource(fileName, () -> {
			try {
				return future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public void putResourceAsync(PackType type, Identifier id, Future<byte[]> future) {
		this.putResource(type, id, () -> {
			try {
				return future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public void clearResources(PackType type) {
		this.getResourceMap(type).clear();
	}

	public void clearResources() {
		this.root.clear();
		this.clearResources(PackType.CLIENT_RESOURCES);
		this.clearResources(PackType.SERVER_DATA);
	}

	/// Dumps the content of this resource pack into the given path.
	///
	/// @param path the path to dump the resources into
	public void dumpTo(Path path) {
		try {
			Files.createDirectories(path);

			this.root.forEach((p, resource) -> this.dumpResource(path, p, resource.get()));
			this.assets.forEach((p, resource) ->
					this.dumpResource(path, "%s/%s/%s".formatted(PackType.CLIENT_RESOURCES.getDirectory(), p.getNamespace(), p.getPath()), resource.get())
			);
			this.data.forEach((p, resource) ->
					this.dumpResource(path, "%s/%s/%s".formatted(PackType.SERVER_DATA.getDirectory(), p.getNamespace(), p.getPath()), resource.get())
			);
		} catch (IOException e) {
			LOGGER.error("Failed to write resource pack dump from pack {} to {}.", this.parent.location(), path, e);
		}
	}

	private void dumpAll() {
		this.dumpTo(Paths.get("debug", "packs", this.parent.packId()));
	}

	private void dumpResource(Path parentPath, String resourcePath, byte[] resource) {
		try {
			Path p = parentPath.resolve(resourcePath);
			Files.createDirectories(p.getParent());
			Files.write(p, resource, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("Failed to write resource pack dump from pack {}.", this.parent.packId(), e);
		}
	}

	private Map<Identifier, Supplier<byte[]>> getResourceMap(PackType type) {
		return switch (type) {
		case CLIENT_RESOURCES -> this.assets;
		case SERVER_DATA -> this.data;
		};
	}
}
