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

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ServerChunkEvents {
	private ServerChunkEvents() {
	}

	/**
	 * Called when a chunk is loaded into a ServerWorld.
	 *
	 * <p>When this event is called, the chunk is already in the world.
	 *
	 * <p>Note that this event is not called for chunks that become accessible without previously being unloaded.
	 *
	 * @see ServerChunkEvents#CHUNK_LEVEL_TYPE_CHANGE
	 */
	public static final Event<ServerChunkEvents.Load> CHUNK_LOAD = EventFactory.createArrayBacked(ServerChunkEvents.Load.class, callbacks -> (serverWorld, chunk) -> {
		for (Load callback : callbacks) {
			callback.onChunkLoad(serverWorld, chunk);
		}
	});

	/**
	 * Called when a newly generated chunk is loaded into a ServerWorld.
	 *
	 * <p>When this event is called, the chunk is already in the world.
	 */
	public static final Event<ServerChunkEvents.Generate> CHUNK_GENERATE = EventFactory.createArrayBacked(ServerChunkEvents.Generate.class, callbacks -> (serverWorld, chunk) -> {
		for (Generate callback : callbacks) {
			callback.onChunkGenerate(serverWorld, chunk);
		}
	});

	/**
	 * Called when a chunk is unloaded from a ServerWorld.
	 *
	 * <p>When this event is called, the chunk is still present in the world.
	 *
	 * <p>Note that the server typically unloads chunks when the chunk's level goes above 45 (and not immediately when the chunk becomes inaccessible).
	 * To know when a chunk first becomes inaccessible, see {@link ServerChunkEvents#CHUNK_LEVEL_TYPE_CHANGE}.
	 */
	public static final Event<ServerChunkEvents.Unload> CHUNK_UNLOAD = EventFactory.createArrayBacked(ServerChunkEvents.Unload.class, callbacks -> (serverWorld, chunk) -> {
		for (Unload callback : callbacks) {
			callback.onChunkUnload(serverWorld, chunk);
		}
	});

	/**
	 * Called when a chunk's actual ticking behavior is about to align with its updated {@link ChunkLevelType}.
	 *
	 * <p>When this event is being called:
	 * <ul>
	 * <li>The chunk's {@link WorldChunk#getLevelType()} has already changed.</li>
	 * <li>Entities within the chunk are not guaranteed to be accessible.</li>
	 * <li>The chunk's corresponding level type future in {@link ChunkHolder} is not guaranteed to be done.</li>
	 * <li>When transitioning from {@link ChunkLevelType#INACCESSIBLE} to {@link ChunkLevelType#FULL}, calling {@link ServerChunkManager#getChunkFutureSyncOnMainThread(int, int, ChunkStatus, boolean)} to fetch the current chunk at {@link ChunkStatus#FULL} status results in undefined behavior.</li>
	 * </ul>
	 */
	public static final Event<LevelTypeChange> CHUNK_LEVEL_TYPE_CHANGE = EventFactory.createArrayBacked(LevelTypeChange.class, (world, chunk, oldLevelType, newLevelType) -> { }, callbacks -> (serverWorld, chunk, oldLevelType, newLevelType) -> {
		for (LevelTypeChange callback : callbacks) {
			callback.onChunkLevelTypeChange(serverWorld, chunk, oldLevelType, newLevelType);
		}
	});

	@FunctionalInterface
	public interface Load {
		void onChunkLoad(ServerWorld world, WorldChunk chunk);
	}

	@FunctionalInterface
	public interface Generate {
		void onChunkGenerate(ServerWorld world, WorldChunk chunk);
	}

	@FunctionalInterface
	public interface Unload {
		void onChunkUnload(ServerWorld world, WorldChunk chunk);
	}

	@FunctionalInterface
	public interface LevelTypeChange {
		void onChunkLevelTypeChange(ServerWorld world, WorldChunk chunk, ChunkLevelType oldLevelType, ChunkLevelType newLevelType);
	}
}
