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

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ServerLevelEvents {
	/**
	 * Called just after a level is loaded by a Minecraft server.
	 *
	 * <p>This can be used to load level specific metadata or initialize a {@link SavedData} on a server level.
	 */
	public static final Event<Load> LOAD = EventFactory.createArrayBacked(Load.class, callbacks -> (server, level) -> {
		for (Load callback : callbacks) {
			callback.onLevelLoad(server, level);
		}
	});

	/**
	 * Called before a level is unloaded by a Minecraft server.
	 *
	 * <p>This typically occurs after a server has {@link ServerLifecycleEvents#SERVER_STOPPING started shutting down}.
	 * Mods which allow dynamic level (un)registration should call this event so mods can let go of level handles when a level is removed.
	 */
	public static final Event<Unload> UNLOAD = EventFactory.createArrayBacked(Unload.class, callbacks -> (server, level) -> {
		for (Unload callback : callbacks) {
			callback.onLevelUnload(server, level);
		}
	});

	@FunctionalInterface
	public interface Load {
		void onLevelLoad(MinecraftServer server, ServerLevel level);
	}

	@FunctionalInterface
	public interface Unload {
		void onLevelUnload(MinecraftServer server, ServerLevel level);
	}

	private ServerLevelEvents() {
	}
}
