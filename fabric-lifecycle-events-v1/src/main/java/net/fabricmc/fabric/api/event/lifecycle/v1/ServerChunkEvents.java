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

import net.minecraft.server.world.ServerWorld;
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
	 */
	public static final Event<ServerChunkEvents.Unload> CHUNK_UNLOAD = EventFactory.createArrayBacked(ServerChunkEvents.Unload.class, callbacks -> (serverWorld, chunk) -> {
		for (Unload callback : callbacks) {
			callback.onChunkUnload(serverWorld, chunk);
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
}
