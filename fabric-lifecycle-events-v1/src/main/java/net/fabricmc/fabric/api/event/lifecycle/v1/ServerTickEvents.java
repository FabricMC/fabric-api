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
	 * Called at the start of a ServerLevel's tick.
	 *
	 * <p>When the dedicated server is "paused", this event is not invoked.
	 */
	public static final Event<StartLevelTick> START_LEVEL_TICK = EventFactory.createArrayBacked(StartLevelTick.class, callbacks -> level -> {
		for (StartLevelTick callback : callbacks) {
			callback.onStartTick(level);
		}
	});

	/**
	 * Called at the end of a ServerLevel's tick.
	 *
	 * <p>End of level tick may be used to start async computations for the next tick.
	 *
	 * <p>When the dedicated server is "paused", this event is not invoked.
	 */
	public static final Event<EndLevelTick> END_LEVEL_TICK = EventFactory.createArrayBacked(EndLevelTick.class, callbacks -> level -> {
		for (EndLevelTick callback : callbacks) {
			callback.onEndTick(level);
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
	public interface StartLevelTick {
		void onStartTick(ServerLevel level);
	}

	@FunctionalInterface
	public interface EndLevelTick {
		void onEndTick(ServerLevel level);
	}
}
