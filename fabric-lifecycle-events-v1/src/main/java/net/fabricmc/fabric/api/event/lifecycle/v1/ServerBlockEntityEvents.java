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

package net.fabricmc.fabric.api.event.lifecycle.v1;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ServerBlockEntityEvents {
	private ServerBlockEntityEvents() {
	}

	/**
	 * Called when an BlockEntity is loaded into a ServerLevel.
	 *
	 * <p>When this is event is called, the block entity is already in the level.
	 * However, its data might not be loaded yet, so don't rely on it.
	 */
	public static final Event<ServerBlockEntityEvents.Load> BLOCK_ENTITY_LOAD = EventFactory.createArrayBacked(ServerBlockEntityEvents.Load.class, callbacks -> (blockEntity, level) -> {
		for (Load callback : callbacks) {
			callback.onLoad(blockEntity, level);
		}
	});

	/**
	 * Called when an BlockEntity is about to be unloaded from a ServerLevel.
	 *
	 * <p>When this event is called, the block entity is still present on the level.
	 */
	public static final Event<Unload> BLOCK_ENTITY_UNLOAD = EventFactory.createArrayBacked(ServerBlockEntityEvents.Unload.class, callbacks -> (blockEntity, level) -> {
		for (Unload callback : callbacks) {
			callback.onUnload(blockEntity, level);
		}
	});

	@FunctionalInterface
	public interface Load {
		void onLoad(BlockEntity blockEntity, ServerLevel level);
	}

	@FunctionalInterface
	public interface Unload {
		void onUnload(BlockEntity blockEntity, ServerLevel level);
	}
}
