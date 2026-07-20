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

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

/// Represents a resource pack whose resources are mutable.
public interface MutablePackResources extends PackResources {
	/// Puts a resource into the resource pack's root.
	///
	/// @param fileName the name of the file
	/// @param resource the resource content
	/// @see #putResource(PackType, Identifier, byte[])
	///@see #putResource(String, Supplier)
	/// @see #putResourceAsync(String, Function)
	void putResource(String fileName, byte[] resource);

	/// Puts a resource into the resource pack for the given side and path.
	///
	/// @param type     the resource type
	/// @param id       the path of the resource
	/// @param resource the resource content
	/// @see #putResource(String, byte[])
	/// @see #putResource(PackType, Identifier, Supplier)
	/// @see #putResourceAsync(PackType, Identifier, Function)
	void putResource(PackType type, Identifier id, byte[] resource);

	/// Puts a resource into the resource pack's root.
	///
	/// @param fileName the name of the file
	/// @param resource the supplier of the resource content
	/// @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	/// @see #putResource(PackType, Identifier, Supplier)
	/// @see #putResource(String, byte[])
	/// @see #putResourceAsync(String, Function)
	void putResource(String fileName, Supplier<byte[]> resource);

	/// Puts a resource into the resource pack for the given side and path.
	///
	/// @param type     the resource type
	/// @param id       the path of the resource
	/// @param resource the supplier of the resource content
	/// @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	/// @see #putResource(String, Supplier)
	/// @see #putResource(PackType, Identifier, byte[])
	/// @see #putResourceAsync(PackType, Identifier, Function)
	void putResource(PackType type, Identifier id, Supplier<byte[]> resource);

	/// Puts a resource into the resource pack's root asynchronously.
	///
	/// @param fileName        the name of the file
	/// @param resourceFactory the factory of the resource content
	/// @return the future
	/// @see #putResourceAsync(PackType, Identifier, Function)
	/// @see #putResource(String, byte[])
	/// @see #putResource(String, Supplier)
	Future<byte[]> putResourceAsync(String fileName, Function<String, byte[]> resourceFactory);

	/// Puts a resource into the resource pack for the given side and path asynchronously.
	///
	/// @param type            the resource type
	/// @param id              the path of the resource
	/// @param resourceFactory the factory of the resource content
	/// @return the future
	/// @see #putResourceAsync(String, Function)
	/// @see #putResource(PackType, Identifier, byte[])
	/// @see #putResource(PackType, Identifier, Supplier)
	Future<byte[]> putResourceAsync(PackType type, Identifier id, Function<Identifier, byte[]> resourceFactory);

	/// Puts a text resource into the resource pack's root.
	///
	/// @param fileName the name of the file
	/// @param text     the resource content
	/// @see #putResource(String, byte[])
	default void putText(String fileName, String text) {
		this.putResource(fileName, text.getBytes(StandardCharsets.UTF_8));
	}

	/// Puts a text resource into the resource pack for the given side and path.
	///
	/// @param type the resource type
	/// @param id   the path of the resource
	/// @param text the resource content
	/// @see #putResource(PackType, Identifier, byte[])
	default void putText(PackType type, Identifier id, String text) {
		this.putResource(type, id, text.getBytes(StandardCharsets.UTF_8));
	}

	/// Puts a text resource into the resource pack's root.
	///
	/// @param fileName     the name of the file
	/// @param textSupplier the supplier of the resource content
	/// @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	/// @see #putResource(String, Supplier)
	default void putText(String fileName, Supplier<String> textSupplier) {
		this.putResource(fileName, () -> textSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	/// Puts a text resource into the resource pack for the given side and path.
	///
	/// @param type         the resource type
	/// @param id           the path of the resource
	/// @param textSupplier the supplier of the resource content
	/// @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	/// @see #putResource(PackType, Identifier, Supplier)
	default void putText(PackType type, Identifier id, Supplier<String> textSupplier) {
		this.putResource(type, id, () -> textSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	/// Puts a text resource into the resource pack's root asynchronously.
	///
	/// @param fileName    the name of the file
	/// @param textFactory the factory of the resource content
	/// @return the future
	/// @see #putResourceAsync(String, Function)
	default Future<byte[]> putTextAsync(String fileName, Function<String, String> textFactory) {
		return this.putResourceAsync(fileName, textFactory.andThen(text -> text.getBytes(StandardCharsets.UTF_8)));
	}

	/// Puts a text resource into the resource pack for the given side and path asynchronously.
	///
	/// @param type        the resource type
	/// @param id          the path of the resource
	/// @param textFactory the factory of the resource content
	/// @return the future
	/// @see #putResourceAsync(PackType, Identifier, Function)
	default Future<byte[]> putTextAsync(PackType type, Identifier id, Function<Identifier, String> textFactory) {
		return this.putResourceAsync(type, id, textFactory.andThen(text -> text.getBytes(StandardCharsets.UTF_8)));
	}

	/// Puts a JSON resource into the resource pack's root.
	///
	/// @param fileName the name of the file
	/// @param codec    the codec to serialize the value into JSON
	/// @param value    the resource content
	/// @see #putResource(String, byte[])
	default <T> void putJson(String fileName, Codec<T> codec, T value) {
		this.putText(fileName, codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow().toString());
	}

	/// Puts a JSON resource into the resource pack for the given side and path.
	///
	/// @param type  the resource type
	/// @param id    the path of the resource
	/// @param codec the codec to serialize the value into JSON
	/// @param value the resource content
	/// @see #putResource(PackType, Identifier, byte[])
	default <T> void putJson(PackType type, Identifier id, Codec<T> codec, T value) {
		this.putText(type, id, codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow().toString());
	}

	/// Puts a JSON resource into the resource pack's root.
	///
	/// @param fileName      the name of the file
	/// @param codec         the codec to serialize the value into JSON
	/// @param valueSupplier the supplier of the resource content
	/// @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	/// @see #putResource(String, Supplier)
	default <T> void putJson(String fileName, Codec<T> codec, Supplier<T> valueSupplier) {
		this.putText(fileName, () -> codec.encodeStart(JsonOps.INSTANCE, valueSupplier.get()).getOrThrow().toString());
	}

	/// Puts a JSON resource into the resource pack for the given side and path.
	///
	/// @param type          the resource type
	/// @param id            the path of the resource
	/// @param codec         the codec to serialize the value into JSON
	/// @param valueSupplier the supplier of the resource content
	/// @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	/// @see #putResource(PackType, Identifier, Supplier)
	default <T> void putJson(PackType type, Identifier id, Codec<T> codec, Supplier<T> valueSupplier) {
		this.putText(type, id, () -> codec.encodeStart(JsonOps.INSTANCE, valueSupplier.get()).getOrThrow().toString());
	}

	/// Puts a JSON resource into the resource pack's root asynchronously.
	///
	/// @param fileName     the name of the file
	/// @param codec        the codec to serialize the value into JSON
	/// @param valueFactory the factory of the resource content
	/// @return the future
	/// @see #putResourceAsync(String, Function)
	default <T> Future<byte[]> putJsonAsync(String fileName, Codec<T> codec, Function<String, T> valueFactory) {
		return this.putTextAsync(fileName, valueFactory.andThen(
				value -> codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow().toString()
		));
	}

	/// Puts a JSON resource into the resource pack for the given side and path asynchronously.
	///
	/// @param type         the resource type
	/// @param id           the path of the resource
	/// @param codec        the codec to serialize the value into JSON
	/// @param valueFactory the factory of the resource content
	/// @return the future
	/// @see #putResourceAsync(PackType, Identifier, Function)
	default <T> Future<byte[]> putJsonAsync(PackType type, Identifier id, Codec<T> codec, Function<Identifier, T> valueFactory) {
		return this.putTextAsync(type, id, valueFactory.andThen(
				value -> codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow().toString()
		));
	}

	/// Clears the resource of a specific resource type.
	///
	/// @param type the resource type
	void clearResources(PackType type);

	/// Clears all the resources from memory.
	void clearResources();
}
