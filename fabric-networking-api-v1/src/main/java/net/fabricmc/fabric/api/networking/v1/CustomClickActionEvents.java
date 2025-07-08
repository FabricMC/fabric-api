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

package net.fabricmc.fabric.api.networking.v1;

import java.util.Objects;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.networking.CustomClickActionsRegistry;

/**
 * Events for listening to {@linkplain net.minecraft.text.ClickEvent.Custom custom click actions}, such as from a
 * dialog.
 */
public final class CustomClickActionEvents {
	/**
	 * Gets an event that is invoked on the server when a custom click event is received during the PLAY phase. The
	 * returned event will only be invoked when a click event is received with the given ID.
	 *
	 * @param id The of the ID click event to listen to.
	 * @return Returns an event that will be invoked when a click event with the given ID is received during the PLAY
	 * phase.
	 */
	public static Event<ClickActionReceived<CustomClickEventContext.Play>> playClickActionEvent(Identifier id) {
		Objects.requireNonNull(id, "ID cannot be null");
		return CustomClickActionsRegistry.PLAY_REGISTRY.getOrCreateActionEvent(id);
	}

	/**
	 * Gets an event that is invoked on the server when a custom click event is received during the CONFIGURATION phase.
	 * The returned event will only be invoked when a click event is received with the given ID.
	 *
	 * @param id The of the ID click event to listen to.
	 * @return Returns an event that will be invoked when a click event with the given ID is received during the
	 * CONFIGURATION phase.
	 */
	public static Event<ClickActionReceived<CustomClickEventContext.Configuration>> configurationClickActionEvent(Identifier id) {
		Objects.requireNonNull(id, "ID cannot be null");
		return CustomClickActionsRegistry.CONFIGURATION_REGISTRY.getOrCreateActionEvent(id);
	}

	@FunctionalInterface
	public interface ClickActionReceived<T extends CustomClickEventContext> {
		/**
		 * Handles a custom click event on the server from a given context.
		 * @param context The context of the event, contains the handler responsible for the action and the payload.
		 */
		void handleCustomClickAction(T context);
	}

	private CustomClickActionEvents() {
	}
}
