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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Future;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;

import net.fabricmc.fabric.impl.resource.pack.InMemoryPackResourcesImpl;

/// Represents an in-memory resource pack.
///
/// The resources of this pack are stored in memory instead of it being on-disk.
public abstract class InMemoryPackResources implements MutablePackResources {
	private final InMemoryPackResourcesImpl implementation = new InMemoryPackResourcesImpl(this);

	@Override
	public @Nullable IoSupplier<InputStream> getRootResource(String... path) {
		return this.implementation.getRootResource(path);
	}

	@Override
	public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier id) {
		return this.implementation.getResource(type, id);
	}

	@Override
	public void listResources(PackType type, String namespace, String directory, ResourceOutput consumer) {
		this.implementation.listResources(type, namespace, directory, consumer);
	}

	@Override
	public @Unmodifiable Set<String> getNamespaces(PackType type) {
		return this.implementation.getNamespaces(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> @Nullable T getMetadataSection(MetadataSectionType<T> metadataSectionType) throws IOException {
		return this.implementation.getMetadataSection(metadataSectionType);
	}

	@Override
	public void close() {
		this.implementation.close();
	}

	@Override
	public void putResource(String fileName, byte[] resource) {
		this.implementation.putResource(fileName, resource);
	}

	@Override
	public void putResource(PackType type, Identifier id, byte[] resource) {
		this.implementation.putResource(type, id, resource);
	}

	@Override
	public void putResourceAsync(String fileName, Future<byte[]> future) {
		this.implementation.putResourceAsync(fileName, future);
	}

	@Override
	public void putResourceAsync(PackType type, Identifier id, Future<byte[]> future) {
		this.implementation.putResourceAsync(type, id, future);
	}

	@Override
	public void clearResources(PackType type) {
		this.implementation.clearResources(type);
	}

	@Override
	public void clearResources() {
		this.implementation.clearResources();
	}

	/// Dumps the content of this resource pack into the given path.
	///
	/// @param path the path to dump the resources into
	public void dumpTo(Path path) {
		this.implementation.dumpTo(path);
	}

	/// Represents an in-memory resource pack with a static location.
	public static class Located extends InMemoryPackResources {
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
