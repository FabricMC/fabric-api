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

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.object.builder.FabricTrackedDataRegistryImpl;

/**
 * Allows registering custom {@link TrackedDataHandler}s in a reliable way.
 */
public final class FabricTrackedDataRegistry {
	private FabricTrackedDataRegistry() {
	}

	/**
	 * Registers a {@link TrackedDataHandler} using the given ID. Use this instead of
	 * {@link TrackedDataHandlerRegistry#register(TrackedDataHandler)} as the vanilla method exclusively uses integer
	 * IDs, which can result in desyncs and errors with custom handlers. This method is guaranteed to work reliably.
	 *
	 * <p>Handlers registered with this method will have an associated integer ID as well, which can be used with
	 * {@link TrackedDataHandlerRegistry#get(int)} and {@link TrackedDataHandlerRegistry#getId(TrackedDataHandler)}.
	 * However, the integer ID of a given custom handler registered through this method may change on registry sync.
	 * The integer IDs of vanilla handlers are guaranteed to remain constant.
	 */
	public static void register(Identifier id, TrackedDataHandler<?> handler) {
		FabricTrackedDataRegistryImpl.register(id, handler);
	}

	/**
	 * Retrieves the handler for the given ID, or {@code null} if it does not exist.
	 */
	@Nullable
	public static TrackedDataHandler<?> get(Identifier id) {
		return FabricTrackedDataRegistryImpl.get(id);
	}

	/**
	 * Retrieves the ID for the given handler, or {@code null} if the handler was not registered with
	 * {@link #register(Identifier, TrackedDataHandler)}.
	 */
	@Nullable
	public static Identifier getId(TrackedDataHandler<?> handler) {
		return FabricTrackedDataRegistryImpl.getId(handler);
	}
}
