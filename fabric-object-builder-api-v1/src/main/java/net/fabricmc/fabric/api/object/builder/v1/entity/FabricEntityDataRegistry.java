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

package net.fabricmc.fabric.api.object.builder.v1.entity;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.object.builder.FabricEntityDataRegistryImpl;

/// Allows registering custom [EntityDataSerializer]s in a reliable way.
public final class FabricEntityDataRegistry {
	private FabricEntityDataRegistry() {
	}

	/// Registers a [EntityDataSerializer] using the given ID. Use this instead of
	/// [net.minecraft.network.syncher.EntityDataSerializers#registerSerializer(EntityDataSerializer)] as the vanilla method exclusively uses integer
	/// IDs, which can result in desyncs and errors with custom handlers. This method is guaranteed to work reliably.
	///
	/// Handlers registered with this method will have an associated integer ID as well, which can be used with
	/// [net.minecraft.network.syncher.EntityDataSerializers#getSerializer(int)] and [net.minecraft.network.syncher.EntityDataSerializers#getSerializedId(EntityDataSerializer)].
	/// However, the integer ID of a given custom handler registered through this method may change on registry sync.
	/// The integer IDs of vanilla handlers are guaranteed to remain constant.
	public static void register(Identifier id, EntityDataSerializer<?> handler) {
		FabricEntityDataRegistryImpl.register(id, handler);
	}

	/// Retrieves the handler for the given ID, or `null` if it does not exist.
	@Nullable
	public static EntityDataSerializer<?> get(Identifier id) {
		return FabricEntityDataRegistryImpl.get(id);
	}

	/// Retrieves the ID for the given handler, or `null` if the handler was not registered with
	/// [#register(Identifier, EntityDataSerializer)].
	@Nullable
	public static Identifier getId(EntityDataSerializer<?> handler) {
		return FabricEntityDataRegistryImpl.getId(handler);
	}
}
