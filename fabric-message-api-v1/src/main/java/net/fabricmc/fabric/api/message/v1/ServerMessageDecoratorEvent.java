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

package net.fabricmc.fabric.api.message.v1;

import java.util.Objects;

import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * A class for registering a {@link ChatDecorator}. Check the message decorator documentation
 * for how message decorators work. Unlike other events, this uses a functional interface that is
 * provided by the vanilla game.
 *
 * <p>This event uses phases to provide better mod compatibilities between mods that add custom
 * content and styling. Message decorators with the styling phase will always apply after the ones
 * with the content phase. When registering the message decorator, it is recommended to choose one
 * of the phases from this interface and pass that to the {@link Event#register(Identifier, Object)}
 * function. If not given, the message decorator will run in the default phase, which is between
 * the content phase and the styling phase.
 *
 * <p>Example of registering a content phase message decorator:
 *
 * <pre>{@code
 * ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.CONTENT_PHASE, (sender, message) -> {
 *     // Add smiley face. Has to copy() to get a MutableText with siblings and styles.
 *     return message.copy().append(" :)");
 * });
 * }</pre>
 *
 * <p>Example of registering a styling phase message decorator:
 *
 * <pre>{@code
 * ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.STYLING_PHASE, (sender, message) -> {
 *     // Apply orange color to messages sent by server operators
 *     if (sender != null && sender.server.getPlayerManager().isOperator(sender.getGameProfile())) {
 *         return message.copy().styled(style -> style.withColor(0xFFA500));
 *     }
 *     return message;
 * });
 * }</pre>
 */
public final class ServerMessageDecoratorEvent {
	private ServerMessageDecoratorEvent() {
	}

	/**
	 * The content phase of the event, passed when registering a message decorator. Use this when
	 * the decorator modifies the text content of the message.
	 */
	public static final Identifier CONTENT_PHASE = Identifier.fromNamespaceAndPath("fabric", "content");
	/**
	 * The styling phase of the event, passed when registering a message decorator. Use this when
	 * the decorator only modifies the styling of the message with the text intact.
	 */
	public static final Identifier STYLING_PHASE = Identifier.fromNamespaceAndPath("fabric", "styling");

	public static final Event<ChatDecorator> EVENT = EventFactory.createWithPhases(ChatDecorator.class, decorators -> (sender, message) -> {
		Component decorated = message;

		for (ChatDecorator decorator : decorators) {
			decorated = handle(decorator.decorate(sender, decorated), decorator);
		}

		return decorated;
	}, CONTENT_PHASE, Event.DEFAULT_PHASE, STYLING_PHASE);

	private static <T extends Component> T handle(T decorated, ChatDecorator decorator) {
		String decoratorName = decorator.getClass().getName();
		return Objects.requireNonNull(decorated, "message decorator %s returned null".formatted(decoratorName));
	}
}
