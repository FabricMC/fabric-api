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

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Contains events that are triggered on the server every tick.
 *
 * <p>A dedicated server may "pause" if no player is present for a
 * certain length of time (by default, 1 minute). See {@code pause-when-empty-seconds}
 * property in {@code server.properties}.
 * When the server is "paused", none of the events here will be invoked.
 */
public final class ServerTickEvents {
	private ServerTickEvents() {
	}

	/**
	 * Called at the start of the server tick.
	 *
	 * <p>When the dedicated server is "paused", this event is not invoked.
	 */
	public static final Event<StartTick> START_SERVER_TICK = EventFactory.createArrayBacked(StartTick.class, callbacks -> server -> {
		for (StartTick event : callbacks) {
			event.onStartTick(server);
		}
	});

	/**
	 * Called at the end of the server tick.
	 *
	 * <p>When the dedicated server is "paused", this event is not invoked.
	 */
	public static final Event<EndTick> END_SERVER_TICK = EventFactory.createArrayBacked(EndTick.class, callbacks -> server -> {
		for (EndTick event : callbacks) {
			event.onEndTick(server);
		}
	});

	/**
	 * Called at the start of a ServerWorld's tick.
	 *
	 * <p>When the dedicated server is "paused", this event is not invoked.
	 */
	public static final Event<StartWorldTick> START_WORLD_TICK = EventFactory.createArrayBacked(StartWorldTick.class, callbacks -> world -> {
		for (StartWorldTick callback : callbacks) {
			callback.onStartTick(world);
		}
	});

	/**
	 * Called at the end of a ServerWorld's tick.
	 *
	 * <p>End of world tick may be used to start async computations for the next tick.
	 *
	 * <p>When the dedicated server is "paused", this event is not invoked.
	 */
	public static final Event<EndWorldTick> END_WORLD_TICK = EventFactory.createArrayBacked(EndWorldTick.class, callbacks -> world -> {
		for (EndWorldTick callback : callbacks) {
			callback.onEndTick(world);
		}
	});

	@FunctionalInterface
	public interface StartTick {
		void onStartTick(MinecraftServer server);
	}

	@FunctionalInterface
	public interface EndTick {
		void onEndTick(MinecraftServer server);
	}

	@FunctionalInterface
	public interface StartWorldTick {
		void onStartTick(ServerLevel world);
	}

	@FunctionalInterface
	public interface EndWorldTick {
		void onEndTick(ServerLevel world);
	}
}
