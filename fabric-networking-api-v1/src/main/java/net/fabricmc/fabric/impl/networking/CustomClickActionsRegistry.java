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
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.CustomClickActionEvents;
import net.fabricmc.fabric.api.networking.v1.CustomClickEventContext;

public final class CustomClickActionsRegistry {
	private static final Map<Identifier, Event<CustomClickActionEvents.NamedCustomClickActionReceived>> REGISTRY = new HashMap<>();

	public static Event<CustomClickActionEvents.NamedCustomClickActionReceived> getOrCreateActionEvent(Identifier id) {
		return REGISTRY.computeIfAbsent(
				id,
				idx -> {
					return EventFactory.createArrayBacked(
							CustomClickActionEvents.NamedCustomClickActionReceived.class,
							listeners -> context -> {
								for (CustomClickActionEvents.NamedCustomClickActionReceived listener : listeners) {
									listener.handleCustomClickAction(context);
								}
							}
					);
				}
		);
	}

	public static void invokeListenerEvent(Identifier id, CustomClickEventContext context) {
		CustomClickActionEvents.ON_ANY_CUSTOM_CLICK_ACTION_RECEIVED.invoker().handleCustomClickAction(id, context);

		Event<CustomClickActionEvents.NamedCustomClickActionReceived> event = REGISTRY.get(id);

		if (event != null) {
			event.invoker().handleCustomClickAction(context);
		}
	}

	public record PlayContextImpl(
			ServerPlayNetworkHandler handler,
			Optional<NbtElement> payload
	) implements CustomClickEventContext.Play {
	}

	public record ConfigurationContextImpl(
			ServerConfigurationNetworkHandler handler,
			Optional<NbtElement> payload
	) implements CustomClickEventContext.Configuration {
	}

	private CustomClickActionsRegistry() {
	}
}
