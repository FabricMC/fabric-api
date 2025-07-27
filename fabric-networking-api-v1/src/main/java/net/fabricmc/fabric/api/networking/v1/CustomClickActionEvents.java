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
import java.util.Optional;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.networking.CustomClickActionsRegistry;

/**
 * Events for listening to {@linkplain net.minecraft.text.ClickEvent.Custom custom click actions}, such as those invoked
 * from a custom dialog.
 */
public final class CustomClickActionEvents {
	/**
	 * Gets an event that is invoked on the server when a custom click event is received. The returned event will only
	 * be invoked when a click event is received with the given ID.
	 *
	 * @param id The of the ID click event to listen to.
	 * @return Returns an event that will be invoked when a click event with the given ID is received during the PLAY
	 * phase.
	 */
	public static Event<CustomClickActionReceived> customClickActionReceivedEvent(Identifier id) {
		Objects.requireNonNull(id, "ID cannot be null");
		return CustomClickActionsRegistry.getOrCreateActionEvent(id);
	}

	@FunctionalInterface
	public interface CustomClickActionReceived {
		/**
		 * Handles a custom click event on the server from a given context.
		 *
		 * @param context The context of the event, contains the handler responsible for the action and the payload.
		 *                Will either be an instance of {@link CustomClickEventContext.Play} or
		 *                {@link CustomClickEventContext.Configuration}, depending on when this event was invoked. This
		 *                can be checked using switch-statement pattern matching (see testmod if unfamiliar with this
		 *                syntax).
		 * @param payload The payload received with this event. If no payload is received, then this payload will be
		 *                empty.
		 */
		void handleCustomClickAction(CustomClickEventContext context, Optional<NbtElement> payload);
	}

	private CustomClickActionEvents() {
	}
}
