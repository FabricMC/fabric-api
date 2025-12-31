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

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ServerChunkEvents {
	private ServerChunkEvents() {
	}

	/// Called when a chunk is loaded into a ServerLevel.
	///
	/// When this event is called, the chunk is already in the level.
	///
	/// Note that this event is not called for chunks that become accessible without previously being unloaded.
	///
	/// @see ServerChunkEvents#FULL_CHUNK_STATUS_CHANGE
	public static final Event<ServerChunkEvents.Load> CHUNK_LOAD = EventFactory.createArrayBacked(ServerChunkEvents.Load.class, callbacks -> (serverLevel, chunk) -> {
		for (Load callback : callbacks) {
			callback.onChunkLoad(serverLevel, chunk);
		}
	});

	/// Called when a newly generated chunk is loaded into a ServerLevel.
	///
	/// When this event is called, the chunk is already in the level.
	public static final Event<ServerChunkEvents.Generate> CHUNK_GENERATE = EventFactory.createArrayBacked(ServerChunkEvents.Generate.class, callbacks -> (serverLevel, chunk) -> {
		for (Generate callback : callbacks) {
			callback.onChunkGenerate(serverLevel, chunk);
		}
	});

	/// Called when a chunk is unloaded from a ServerLevel.
	///
	/// When this event is called, the chunk is still present in the level.
	///
	/// Note that the server typically unloads chunks when the chunk's load level goes above [ChunkLevel#MAX_LEVEL]
	/// (and not immediately when the chunk becomes inaccessible). To know when a chunk first becomes inaccessible, see
	/// [ServerChunkEvents#FULL_CHUNK_STATUS_CHANGE].
	public static final Event<ServerChunkEvents.Unload> CHUNK_UNLOAD = EventFactory.createArrayBacked(ServerChunkEvents.Unload.class, callbacks -> (serverLevel, chunk) -> {
		for (Unload callback : callbacks) {
			callback.onChunkUnload(serverLevel, chunk);
		}
	});

	/// Called when a chunk's actual ticking behavior is about to align with its updated [FullChunkStatus].
	///
	/// When this event is being called:
	///
	///   - The chunk's [LevelChunk#getFullStatus()] has already changed.
	///   - Entities within the chunk are not guaranteed to be accessible.
	///   - The chunk's corresponding full chunk status future in [ChunkHolder] is not guaranteed to be done.
	///   - When transitioning from [FullChunkStatus#INACCESSIBLE] to [FullChunkStatus#FULL], calling [ServerChunkCache#getChunkFuture(int, int, ChunkStatus, boolean)] to fetch the current chunk at [ChunkStatus#FULL] status results in undefined behavior.
	///
	public static final Event<FullChunkStatusChange> FULL_CHUNK_STATUS_CHANGE = EventFactory.createArrayBacked(FullChunkStatusChange.class, (level, chunk, oldChunkStatus, newChunkStatus) -> { }, callbacks -> (serverLevel, chunk, oldChunkStatus, newChunkStatus) -> {
		for (FullChunkStatusChange callback : callbacks) {
			callback.onFullChunkStatusChange(serverLevel, chunk, oldChunkStatus, newChunkStatus);
		}
	});

	@FunctionalInterface
	public interface Load {
		void onChunkLoad(ServerLevel level, LevelChunk chunk);
	}

	@FunctionalInterface
	public interface Generate {
		void onChunkGenerate(ServerLevel level, LevelChunk chunk);
	}

	@FunctionalInterface
	public interface Unload {
		void onChunkUnload(ServerLevel level, LevelChunk chunk);
	}

	@FunctionalInterface
	public interface FullChunkStatusChange {
		void onFullChunkStatusChange(ServerLevel level, LevelChunk chunk, FullChunkStatus oldChunkStatus, FullChunkStatus newChunkStatus);
	}
}
