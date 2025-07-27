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

package net.fabricmc.fabric.impl.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.CustomClickActionEvents;
import net.fabricmc.fabric.api.networking.v1.CustomClickEventContext;

public final class CustomClickActionsRegistry {
	private static final Map<Identifier, Event<CustomClickActionEvents.CustomClickActionReceived>> REGISTRY = new HashMap<>();

	public static Event<CustomClickActionEvents.CustomClickActionReceived> getOrCreateActionEvent(Identifier id) {
		return REGISTRY.computeIfAbsent(id, idx -> createNewEvent());
	}

	public static void invokeListenerEvent(Identifier id, CustomClickEventContext context, Optional<NbtElement> payload) {
		Event<CustomClickActionEvents.CustomClickActionReceived> event = REGISTRY.get(id);

		if (event != null) {
			event.invoker().handleCustomClickAction(context, payload);
		}
	}

	private static Event<CustomClickActionEvents.CustomClickActionReceived> createNewEvent() {
		return EventFactory.createArrayBacked(
				CustomClickActionEvents.CustomClickActionReceived.class,
				listeners -> (context, payload) -> {
					for (CustomClickActionEvents.CustomClickActionReceived listener : listeners) {
						listener.handleCustomClickAction(context, payload);
					}
				}
		);
	}

	private CustomClickActionsRegistry() {
	}
}
