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

package net.fabricmc.fabric.api.resource.v1.client.pack;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import net.fabricmc.fabric.api.resource.v1.pack.MutablePackResources;
import net.fabricmc.fabric.impl.resource.client.NativeImageUtils;

/// Represents a client-exclusive extension of [MutablePackResources].
public interface ClientMutablePackResources extends MutablePackResources {
	/// Puts an image resource into the resource pack's root.
	///
	/// @param fileName the name of the file
	/// @param image    the resource content
	/// @see #putResource(String, byte[])
	default void putImage(String fileName, NativeImage image) throws IOException {
		this.putResource(fileName, NativeImageUtils.toBytes(image));
	}

	/// Puts an image resource into the resource pack for the given path in the `assets` directory.
	///
	/// @param id    the path of the resource
	/// @param image the resource content
	/// @see #putResource(PackType, Identifier, byte[])
	default void putImage(Identifier id, NativeImage image) throws IOException {
		this.putResource(PackType.CLIENT_RESOURCES, id, NativeImageUtils.toBytes(image));
	}

	/// Puts an image resource into the resource pack's root.
	///
	/// @param fileName      the name of the file
	/// @param imageSupplier the supplier of the resource content
	/// @see #putResource(String, Supplier)
	default void putImage(String fileName, Supplier<NativeImage> imageSupplier) {
		this.putResource(fileName, () -> {
			try (NativeImage image = imageSupplier.get()) {
				return NativeImageUtils.toBytes(image);
			} catch (IOException e) {
				throw new RuntimeException("Failed to serialize image for file " + fileName, e);
			}
		});
	}

	/// Puts an image resource into the resource pack for the given path in the `assets` directory.
	///
	/// @param id            the path of the resource
	/// @param imageSupplier the supplier of the resource content
	/// @see #putResource(PackType, Identifier, Supplier)
	default void putImage(Identifier id, Supplier<NativeImage> imageSupplier) {
		this.putResource(PackType.CLIENT_RESOURCES, id, () -> {
			try (NativeImage image = imageSupplier.get()) {
				return NativeImageUtils.toBytes(image);
			} catch (IOException e) {
				throw new RuntimeException("Failed to serialize image for resource " + id, e);
			}
		});
	}

	/// Puts an image resource into the resource pack's root asynchronously.
	///
	/// @param fileName     the name of the file
	/// @param imageFactory the factory of the resource content
	/// @see #putResourceAsync(String, Function)
	default Future<byte[]> putImageAsync(String fileName, Function<String, NativeImage> imageFactory) {
		return this.putResourceAsync(fileName, imageFactory.andThen(image -> {
			try (image) {
				return NativeImageUtils.toBytes(image);
			} catch (IOException e) {
				throw new RuntimeException("Failed to serialize image for file " + fileName, e);
			}
		}));
	}

	/// Puts an image resource into the resource pack for the given path in the `assets` directory asynchronously.
	///
	/// @param id           the path of the resource
	/// @param imageFactory the factory of the resource content
	/// @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	/// @see #putResourceAsync(PackType, Identifier, Function)
	default Future<byte[]> putImageAsync(
			Identifier id, Function<Identifier, NativeImage> imageFactory
	) {
		return this.putResourceAsync(PackType.CLIENT_RESOURCES, id, imageFactory.andThen(image -> {
			try (image) {
				return NativeImageUtils.toBytes(image);
			} catch (IOException e) {
				throw new RuntimeException("Failed to serialize image for resource " + id, e);
			}
		}));
	}
}
