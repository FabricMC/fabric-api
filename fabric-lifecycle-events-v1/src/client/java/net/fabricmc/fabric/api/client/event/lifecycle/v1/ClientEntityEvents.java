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
import net.minecraft.world.entity.Entity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ClientEntityEvents {
	private ClientEntityEvents() {
	}

	/**
	 * Called when an Entity is loaded into a ClientLevel.
	 *
	 * <p>When this event is called, the chunk is already in the level.
	 */
	public static final Event<ClientEntityEvents.Load> ENTITY_LOAD = EventFactory.createArrayBacked(ClientEntityEvents.Load.class, callbacks -> (entity, level) -> {
		for (Load callback : callbacks) {
			callback.onLoad(entity, level);
		}
	});

	/**
	 * Called when an Entity is about to be unloaded from a ClientLevel.
	 *
	 * <p>This event is called before the entity is unloaded from the level.
	 */
	public static final Event<ClientEntityEvents.Unload> ENTITY_UNLOAD = EventFactory.createArrayBacked(ClientEntityEvents.Unload.class, callbacks -> (entity, level) -> {
		for (Unload callback : callbacks) {
			callback.onUnload(entity, level);
		}
	});

	@FunctionalInterface
	public interface Load {
		void onLoad(Entity entity, ClientLevel level);
	}

	@FunctionalInterface
	public interface Unload {
		void onUnload(Entity entity, ClientLevel level);
	}
}
