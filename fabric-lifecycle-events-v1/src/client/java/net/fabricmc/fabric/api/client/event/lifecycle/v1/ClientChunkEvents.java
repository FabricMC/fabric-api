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

package net.fabricmc.fabric.api.client.event.lifecycle.v1;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ClientChunkEvents {
	private ClientChunkEvents() {
	}

	/**
	 * Called when a chunk is loaded into a ClientWorld.
	 *
	 * <p>When this event is called, the chunk is already in the world.
	 */
	public static final Event<ClientChunkEvents.Load> CHUNK_LOAD = EventFactory.createArrayBacked(ClientChunkEvents.Load.class, callbacks -> (clientWorld, chunk) -> {
		for (Load callback : callbacks) {
			callback.onChunkLoad(clientWorld, chunk);
		}
	});

	/**
	 * Called when a chunk is about to be unloaded from a ClientWorld.
	 *
	 * <p>When this event is called, the chunk is still present in the world.
	 */
	public static final Event<ClientChunkEvents.Unload> CHUNK_UNLOAD = EventFactory.createArrayBacked(ClientChunkEvents.Unload.class, callbacks -> (clientWorld, chunk) -> {
		for (Unload callback : callbacks) {
			callback.onChunkUnload(clientWorld, chunk);
		}
	});

	@FunctionalInterface
	public interface Load {
		void onChunkLoad(ClientLevel world, LevelChunk chunk);
	}

	@FunctionalInterface
	public interface Unload {
		void onChunkUnload(ClientLevel world, LevelChunk chunk);
	}
}
