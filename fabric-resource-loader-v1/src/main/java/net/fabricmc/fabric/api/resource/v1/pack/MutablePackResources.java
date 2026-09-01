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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

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
	/// @see #putResourceAsync(String, Future)
	void putResource(String fileName, byte[] resource);

	/// Puts a resource into the resource pack for the given side and path.
	///
	/// @param type the resource type
	/// @param id the path of the resource
	/// @param resource the resource content
	/// @see #putResource(String, byte[])
	/// @see #putResourceAsync(PackType, Identifier, Future)
	void putResource(PackType type, Identifier id, byte[] resource);

	/// Puts a resource into the resource pack's root asynchronously.
	///
	/// @param fileName the name of the file
	/// @param future the future of the resource content
	/// @see #putResourceAsync(PackType, Identifier, Future)
	/// @see #putResource(String, byte[])
	void putResourceAsync(String fileName, Future<byte[]> future);

	/// Puts a resource into the resource pack for the given side and path asynchronously.
	///
	/// @param type the resource type
	/// @param id the path of the resource
	/// @param future the future of the resource content
	/// @see #putResourceAsync(String, Future)
	/// @see #putResource(PackType, Identifier, byte[])
	void putResourceAsync(PackType type, Identifier id, Future<byte[]> future);

	/// Puts a text resource into the resource pack's root.
	///
	/// @param fileName the name of the file
	/// @param text the resource content
	/// @see #putResource(String, byte[])
	default void putText(String fileName, String text) {
		this.putResource(fileName, text.getBytes(StandardCharsets.UTF_8));
	}

	/// Puts a text resource into the resource pack for the given side and path.
	///
	/// @param type the resource type
	/// @param id the path of the resource
	/// @param text the resource content
	/// @see #putResource(PackType, Identifier, byte[])
	default void putText(PackType type, Identifier id, String text) {
		this.putResource(type, id, text.getBytes(StandardCharsets.UTF_8));
	}

	/// Puts a text resource into the resource pack's root asynchronously.
	///
	/// @param fileName the name of the file
	/// @param future the future of the resource content
	/// @see #putResourceAsync(String, Future)
	default void putTextAsync(String fileName, CompletableFuture<String> future) {
		this.putResourceAsync(fileName, future.thenApply(text -> text.getBytes(StandardCharsets.UTF_8)));
	}

	/// Puts a text resource into the resource pack for the given side and path asynchronously.
	///
	/// @param type the resource type
	/// @param id the path of the resource
	/// @param future the future of the resource content
	/// @see #putResourceAsync(PackType, Identifier, Future)
	default void putTextAsync(PackType type, Identifier id, CompletableFuture<String> future) {
		this.putResourceAsync(type, id, future.thenApply(text -> text.getBytes(StandardCharsets.UTF_8)));
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

	/// Puts a JSON resource into the resource pack's root asynchronously.
	///
	/// @param fileName the name of the file
	/// @param codec the codec to serialize the value into JSON
	/// @param future the future of the resource content
	/// @see #putResourceAsync(String, Future)
	default <T> void putJsonAsync(String fileName, Codec<T> codec, CompletableFuture<T> future) {
		this.putTextAsync(fileName, future.thenApply(
				value -> codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow().toString()
		));
	}

	/// Puts a JSON resource into the resource pack for the given side and path asynchronously.
	///
	/// @param type the resource type
	/// @param id the path of the resource
	/// @param codec the codec to serialize the value into JSON
	/// @param future the future of the resource content
	/// @see #putResourceAsync(PackType, Identifier, Future)
	default <T> void putJsonAsync(PackType type, Identifier id, Codec<T> codec, CompletableFuture<T> future) {
		this.putTextAsync(type, id, future.thenApply(
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
