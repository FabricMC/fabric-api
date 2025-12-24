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

package net.fabricmc.fabric.impl.event.lifecycle;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelLifecycleEvents;

public final class LifecycleEventsImpl implements ModInitializer {
	@Override
	public void onInitialize() {
		// Part of impl for block entity events
		ServerChunkLifecycleEvents.CHUNK_LOAD.register((level, chunk) -> {
			((LoadedChunksCache) level).fabric_markLoaded(chunk);
		});

		ServerChunkLifecycleEvents.CHUNK_UNLOAD.register((level, chunk) -> {
			((LoadedChunksCache) level).fabric_markUnloaded(chunk);
		});

		// Fire block entity unload events.
		// This handles the edge case where going through a portal will cause block entities to unload without warning.
		ServerChunkLifecycleEvents.CHUNK_UNLOAD.register((level, chunk) -> {
			for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
				ServerBlockEntityLifecycleEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, level);
			}
		});

		// We use the world unload event so worlds that are dynamically hot(un)loaded get (block) entity unload events fired when shut down.
		ServerLevelLifecycleEvents.UNLOAD.register((server, level) -> {
			for (LevelChunk chunk : ((LoadedChunksCache) level).fabric_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ServerBlockEntityLifecycleEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, level);
				}
			}

			for (Entity entity : level.getAllEntities()) {
				ServerEntityLifecycleEvents.ENTITY_UNLOAD.invoker().onUnload(entity, level);
			}
		});
	}
}
