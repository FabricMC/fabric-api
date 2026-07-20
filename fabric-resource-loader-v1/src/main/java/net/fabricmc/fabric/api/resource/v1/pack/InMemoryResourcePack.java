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

package net.fabricmc.fabric.api.resource.v1.pack;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.InclusiveRange;

import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;

/// Represents an in-memory resource pack.
///
/// The resources of this pack are stored in memory instead of it being on-disk.
public abstract class InMemoryResourcePack implements MutablePackResources {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ExecutorService EXECUTOR_SERVICE;
	private static final boolean DUMP = TriState.fromSystemProperty("fabric.resource_loader.debug.pack.dump_from_in_memory")
			.orElse(FabricLoader.getInstance().isDevelopmentEnvironment());
	private static final String VIRTUAL_ASYNC_THREADS_PROPERTY = "fabric.resource_loader.pack.virtual_async_threads";
	private final Map<Identifier, Supplier<byte[]>> assets = new ConcurrentHashMap<>();
	private final Map<Identifier, Supplier<byte[]>> data = new ConcurrentHashMap<>();
	private final Map<String, Supplier<byte[]>> root = new ConcurrentHashMap<>();

	@Override
	public @Nullable IoSupplier<InputStream> getRootResource(String... path) {
		String actualPath = String.join("/", path);

		return this.openResource(this.root, actualPath);
	}

	@Override
	public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier id) {
		return this.openResource(this.getResourceMap(type), id);
	}

	protected <T> @Nullable IoSupplier<InputStream> openResource(Map<T, Supplier<byte[]>> map, T key) {
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

	@Override
	public void listResources(PackType type, String namespace, String startingPath, ResourceOutput consumer) {
		this.getResourceMap(type).entrySet().stream()
				.filter(entry -> entry.getKey().getNamespace().equals(namespace) && entry.getKey().getPath().startsWith(startingPath))
				.forEach(entry -> {
					byte[] bytes = entry.getValue().get();

					if (bytes != null) {
						consumer.accept(entry.getKey(), () -> new ByteArrayInputStream(bytes));
					}
				});
	}

	@Override
	public @Unmodifiable Set<String> getNamespaces(PackType type) {
		return this.getResourceMap(type).keySet().stream()
				.map(Identifier::getNamespace)
				.collect(Collectors.toUnmodifiableSet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> @Nullable T getMetadataSection(MetadataSectionType<T> metadataSectionType) throws IOException {
		if (!this.root.containsKey(PackResources.PACK_META)) {
			if (metadataSectionType == PackMetadataSection.CLIENT_TYPE) {
				return (T) new PackMetadataSection(
						Component.literal("A virtual resource pack."),
						new InclusiveRange<>(SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES))
				);
			} else if (metadataSectionType == PackMetadataSection.SERVER_TYPE) {
				return (T) new PackMetadataSection(
						Component.literal("A virtual resource pack."),
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

	@Override
	public void close() {
		if (DUMP) {
			this.dumpAll();
		}
	}

	@Override
	public void putResource(String fileName, byte[] resource) {
		this.root.put(fileName, () -> resource);
	}

	@Override
	public void putResource(PackType type, Identifier id, byte[] resource) {
		this.getResourceMap(type).put(id, () -> resource);
	}

	@Override
	public void putResource(String fileName, Supplier<byte[]> resource) {
		this.root.put(fileName, Suppliers.memoize(resource::get));
	}

	@Override
	public void putResource(PackType type, Identifier id, Supplier<byte[]> resource) {
		this.getResourceMap(type).put(id, Suppliers.memoize(resource::get));
	}

	@Override
	public Future<byte[]> putResourceAsync(String fileName, Function<String, byte[]> resourceFactory) {
		Future<byte[]> future = EXECUTOR_SERVICE.submit(() -> resourceFactory.apply(fileName));
		this.putResource(fileName, () -> {
			try {
				return future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		});
		return future;
	}

	@Override
	public Future<byte[]> putResourceAsync(PackType type, Identifier id, Function<Identifier, byte[]> resourceFactory) {
		Future<byte[]> future = EXECUTOR_SERVICE.submit(() -> resourceFactory.apply(id));
		this.putResource(type, id, () -> {
			try {
				return future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		});
		return future;
	}

	@Override
	public void clearResources(PackType type) {
		this.getResourceMap(type).clear();
	}

	@Override
	public void clearResources() {
		this.root.clear();
		this.clearResources(PackType.CLIENT_RESOURCES);
		this.clearResources(PackType.SERVER_DATA);
	}

	/**
	 * Dumps the content of this resource pack into the given path.
	 *
	 * @param path the path to dump the resources into
	 */
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
			LOGGER.error("Failed to write resource pack dump from pack {} to {}.", this.location(), path, e);
		}
	}

	protected void dumpAll() {
		this.dumpTo(Paths.get("debug", "packs", this.packId()));
	}

	protected void dumpResource(Path parentPath, String resourcePath, byte[] resource) {
		try {
			Path p = parentPath.resolve(resourcePath);
			Files.createDirectories(p.getParent());
			Files.write(p, resource, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("Failed to write resource pack dump from pack {}.", this.packId(), e);
		}
	}

	private Map<Identifier, Supplier<byte[]>> getResourceMap(PackType type) {
		return switch (type) {
		case CLIENT_RESOURCES -> this.assets;
		case SERVER_DATA -> this.data;
		};
	}

	static {
		int threads = Math.max(Runtime.getRuntime().availableProcessors() / 2 - 1, 1);
		String threadsOverride = System.getProperty(VIRTUAL_ASYNC_THREADS_PROPERTY);

		if (threadsOverride != null) {
			try {
				threads = Integer.parseInt(threadsOverride);
			} catch (NumberFormatException e) {
				LOGGER.error("Could not use the number provided by the property \"{}\": ", VIRTUAL_ASYNC_THREADS_PROPERTY, e);
			}
		}

		EXECUTOR_SERVICE = Executors.newFixedThreadPool(
				threads,
				new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Fabric-Resource-Loader-Virtual-Pack-Worker-%s").build()
		);
	}

	/// Represents an in-memory resource pack with a static location.
	public static class Located extends InMemoryResourcePack {
		private final PackLocationInfo location;

		public Located(PackLocationInfo location) {
			this.location = location;
		}

		@Override
		public PackLocationInfo location() {
			return this.location;
		}
	}
}
