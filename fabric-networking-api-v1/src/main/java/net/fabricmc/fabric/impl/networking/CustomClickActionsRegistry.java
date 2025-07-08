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

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.CustomClickActionEvents;

public final class CustomClickActionsRegistry<T extends CustomClickActionEvents.CommonContext> {
	public static final CustomClickActionsRegistry<CustomClickActionEvents.PlayContext> PLAY_REGISTRY = new CustomClickActionsRegistry<>();
	public static final CustomClickActionsRegistry<CustomClickActionEvents.ConfigContext> CONFIG_REGISTRY = new CustomClickActionsRegistry<>();

	private final Map<Identifier, Event<CustomClickActionEvents.ClickActionReceived<T>>> registry = new HashMap<>();

	public Event<CustomClickActionEvents.ClickActionReceived<T>> getOrCreateActionEvent(Identifier id) {
		return this.registry.computeIfAbsent(
				id,
				idx -> {
					return EventFactory.createArrayBacked(
							CustomClickActionEvents.ClickActionReceived.class,
							listeners -> context -> {
								for (CustomClickActionEvents.ClickActionReceived<T> listener : listeners) {
									listener.handleCustomClickAction(context);
								}
							}
					);
				}
		);
	}

	public void invokeListenerEvent(Identifier id, T context) {
		Event<CustomClickActionEvents.ClickActionReceived<T>> event = this.registry.get(id);

		if (event != null) {
			event.invoker().handleCustomClickAction(context);
		}
	}

	public record PlayContextImpl(
			ServerPlayNetworkHandler handler,
			@Nullable NbtElement payload
	) implements CustomClickActionEvents.PlayContext {
	}

	public record ConfigContextImpl(
			ServerConfigurationNetworkHandler handler,
			@Nullable NbtElement payload
	) implements CustomClickActionEvents.ConfigContext {
	}

	private CustomClickActionsRegistry() {
	}
}
