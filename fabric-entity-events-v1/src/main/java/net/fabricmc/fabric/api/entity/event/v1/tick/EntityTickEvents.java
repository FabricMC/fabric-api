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

package net.fabricmc.fabric.api.entity.event.v1.tick;

import net.minecraft.world.entity.Entity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events related to entity ticking.
 *
 * @apiNote These events may be called from both logical sides. Listeners
 * should add appropriate guards before running side-specific logic.
 */
public final class EntityTickEvents {
	/**
	 * This event is fired at the beginning of {@link Entity#tick()} before any logic has executed.
	 */
	public static final Event<StartTick> START_TICK = EventFactory.createArrayBacked(StartTick.class, callbacks -> entity -> {
		for (StartTick callback : callbacks) {
			callback.startTick(entity);
		}
	});

	/**
	 * This event is fired at the end of {@link Entity#tick()} after the logic has been executed.
	 */
	public static final Event<EndTick> END_TICK = EventFactory.createArrayBacked(EndTick.class, callbacks -> entity -> {
		for (EndTick callback : callbacks) {
			callback.endTick(entity);
		}
	});

	@FunctionalInterface
	public interface StartTick {
		/**
		 * Called at the head of {@link Entity#tick()}.
		 *
		 * @param entity The entity being ticked
		 */
		void startTick(Entity entity);
	}

	@FunctionalInterface
	public interface EndTick {
		/**
		 * Called at the tail of {@link Entity#tick()}.
		 *
		 * @param entity The entity being ticked
		 */
		void endTick(Entity entity);
	}

	private EntityTickEvents() {
	}
}
