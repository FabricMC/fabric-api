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

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Contains server-side events triggered when broadcasting messages.
 */
public final class ServerMessageEvents {
	/**
	 * An event triggered when the server broadcasts a chat message sent by a player,
	 * typically from a client GUI or a player-executed command. Mods can use this to block
	 * the message.
	 *
	 * <p>If a listener returned {@code false}, the message will not be broadcast,
	 * the remaining listeners will not be called (if any), and {@link #CHAT_MESSAGE}
	 * event will not be triggered.
	 *
	 * <p>If the message is from a player-executed command, this will be called
	 * only if {@link #ALLOW_COMMAND_MESSAGE} event did not block the message,
	 * and after triggering {@link #COMMAND_MESSAGE} event.
	 */
	public static final Event<AllowChatMessage> ALLOW_CHAT_MESSAGE = EventFactory.createArrayBacked(AllowChatMessage.class, handlers -> (message, sender, params) -> {
		for (AllowChatMessage handler : handlers) {
			if (!handler.allowChatMessage(message, sender, params)) return false;
		}

		return true;
	});

	/**
	 * An event triggered when the server broadcasts a game message to all players. Game
	 * messages include death messages, join/leave messages, and advancement messages.
	 * Mods can use this to block the message.
	 *
	 * <p>If a listener returned {@code false}, the message will not be broadcast,
	 * the remaining listeners will not be called (if any), and {@link #GAME_MESSAGE}
	 * event will not be triggered.
	 */
	public static final Event<AllowGameMessage> ALLOW_GAME_MESSAGE = EventFactory.createArrayBacked(AllowGameMessage.class, handlers -> (server, message, overlay) -> {
		for (AllowGameMessage handler : handlers) {
			if (!handler.allowGameMessage(server, message, overlay)) return false;
		}

		return true;
	});

	/**
	 * An event triggered when the server broadcasts a command message to all players, such as one
	 * from {@code /me} and {@code /say} (but not ones that specify the recipients like
	 * {@code /msg}). Mods can use this to block the message.
	 *
	 * <p>If a listener returned {@code false}, the message will not be broadcast,
	 * the remaining listeners will not be called (if any), and {@link #COMMAND_MESSAGE}
	 * event will not be triggered.
	 *
	 * <p>If the command is executed by a player and the message is not blocked,
	 * {@link #ALLOW_CHAT_MESSAGE} and {@link #CHAT_MESSAGE} events will also be
	 * triggered after triggering {@link #COMMAND_MESSAGE}.
	 */
	public static final Event<AllowCommandMessage> ALLOW_COMMAND_MESSAGE = EventFactory.createArrayBacked(AllowCommandMessage.class, handlers -> (message, source, params) -> {
		for (AllowCommandMessage handler : handlers) {
			if (!handler.allowCommandMessage(message, source, params)) return false;
		}

		return true;
	});

	/**
	 * An event triggered when the server broadcasts a chat message sent by a player, typically
	 * from a client GUI or a player-executed command. Is not called when {@linkplain
	 * #ALLOW_CHAT_MESSAGE chat messages are blocked}.
	 *
	 * <p>If the message is from a player-executed command, this will be called
	 * only if {@link #ALLOW_COMMAND_MESSAGE} event did not block the message,
	 * and after triggering {@link #COMMAND_MESSAGE} event.
	 */
	public static final Event<ChatMessage> CHAT_MESSAGE = EventFactory.createArrayBacked(ChatMessage.class, handlers -> (message, sender, params) -> {
		for (ChatMessage handler : handlers) {
			handler.onChatMessage(message, sender, params);
		}
	});

	/**
	 * An event triggered when the server broadcasts a game message to all players. Game messages
	 * include death messages, join/leave messages, and advancement messages. Is not called
	 * when {@linkplain #ALLOW_GAME_MESSAGE game messages are blocked}.
	 */
	public static final Event<GameMessage> GAME_MESSAGE = EventFactory.createArrayBacked(GameMessage.class, handlers -> (server, message, overlay) -> {
		for (GameMessage handler : handlers) {
			handler.onGameMessage(server, message, overlay);
		}
	});

	/**
	 * An event triggered when the server broadcasts a command message to all players, such as one
	 * from {@code /me} and {@code /say} (but not ones that specify the recipients like
	 * {@code /msg}). Is not called when {@linkplain #ALLOW_COMMAND_MESSAGE command messages
	 * are blocked}.
	 *
	 * <p>If the command is executed by a player, {@link #ALLOW_CHAT_MESSAGE} and
	 * {@link #CHAT_MESSAGE} events will also be triggered after this event.
	 */
	public static final Event<CommandMessage> COMMAND_MESSAGE = EventFactory.createArrayBacked(CommandMessage.class, handlers -> (message, source, params) -> {
		for (CommandMessage handler : handlers) {
			handler.onCommandMessage(message, source, params);
		}
	});

	private ServerMessageEvents() {
	}

	@FunctionalInterface
	public interface AllowChatMessage {
		/**
		 * Called when the server broadcasts a chat message sent by a player, typically
		 * from a client GUI or a player-executed command. Returning {@code false}
		 * prevents the message from being broadcast and the {@link #CHAT_MESSAGE} event
		 * from triggering.
		 *
		 * <p>If the message is from a player-executed command, this will be called
		 * only if {@link #ALLOW_COMMAND_MESSAGE} event did not block the message,
		 * and after triggering {@link #COMMAND_MESSAGE} event.
		 *
		 * @param message the broadcast message with message decorators applied; use {@code message.getContent()} to get the text
		 * @param sender  the player that sent the message
		 * @param params the {@link ChatType.Bound}
		 * @return {@code true} if the message should be broadcast, otherwise {@code false}
		 */
		boolean allowChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params);
	}

	@FunctionalInterface
	public interface AllowGameMessage {
		/**
		 * Called when the server broadcasts a game message to all players. Game messages
		 * include death messages, join/leave messages, and advancement messages. Returning {@code false}
		 * prevents the message from being broadcast and the {@link #GAME_MESSAGE} event
		 * from triggering.
		 *
		 * @param server the server that sent the message
		 * @param message the broadcast message
		 * @param overlay {@code true} when the message is an overlay
		 * @return {@code true} if the message should be broadcast, otherwise {@code false}
		 */
		boolean allowGameMessage(MinecraftServer server, Component message, boolean overlay);
	}

	@FunctionalInterface
	public interface AllowCommandMessage {
		/**
		 * Called when the server broadcasts a command message to all players, such as one
		 * from {@code /me} and {@code /say} (but not ones that specify the recipients like
		 * {@code /msg}). Returning {@code false} prevents the message from being broadcast
		 * and the {@link #COMMAND_MESSAGE} event from triggering.
		 *
		 * <p>If the command is executed by a player and the message is not blocked,
		 * {@link #ALLOW_CHAT_MESSAGE} and {@link #CHAT_MESSAGE} events will also be
		 * triggered after triggering {@link #COMMAND_MESSAGE}.
		 *
		 * @param message the broadcast message with message decorators applied if applicable; use {@code message.getContent()} to get the text
		 * @param source  the command source that sent the message
		 * @param params the {@link ChatType.Bound}
		 * @return {@code true} if the message should be broadcast, otherwise {@code false}
		 */
		boolean allowCommandMessage(PlayerChatMessage message, CommandSourceStack source, ChatType.Bound params);
	}

	@FunctionalInterface
	public interface ChatMessage {
		/**
		 * Called when the server broadcasts a chat message sent by a player, typically
		 * from a client GUI or a player-executed command. Is not called when {@linkplain
		 * #ALLOW_CHAT_MESSAGE chat messages are blocked}.
		 *
		 * <p>If the message is from a player-executed command, this will be called
		 * only if {@link #ALLOW_COMMAND_MESSAGE} event did not block the message,
		 * and after triggering {@link #COMMAND_MESSAGE} event.
		 *
		 * @param message the broadcast message with message decorators applied; use {@code message.getContent()} to get the text
		 * @param sender  the player that sent the message
		 * @param params the {@link ChatType.Bound}
		 */
		void onChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params);
	}

	@FunctionalInterface
	public interface GameMessage {
		/**
		 * Called when the server broadcasts a game message to all players. Game messages
		 * include death messages, join/leave messages, and advancement messages. Is not called
		 * when {@linkplain #ALLOW_GAME_MESSAGE game messages are blocked}.
		 *
		 * @param server the server that sent the message
		 * @param message the broadcast message
		 * @param overlay {@code true} when the message is an overlay
		 */
		void onGameMessage(MinecraftServer server, Component message, boolean overlay);
	}

	@FunctionalInterface
	public interface CommandMessage {
		/**
		 * Called when the server broadcasts a command message to all players, such as one
		 * from {@code /me} and {@code /say} (but not ones that specify the recipients like
		 * {@code /msg}). Is not called when {@linkplain #ALLOW_COMMAND_MESSAGE command messages
		 * are blocked}.
		 *
		 * <p>If the command is executed by a player, {@link #ALLOW_CHAT_MESSAGE} and
		 * {@link #CHAT_MESSAGE} events will also be triggered after this event.
		 *
		 * @param message the broadcast message with message decorators applied if applicable; use {@code message.getContent()} to get the text
		 * @param source  the command source that sent the message
		 * @param params the {@link ChatType.Bound}
		 */
		void onCommandMessage(PlayerChatMessage message, CommandSourceStack source, ChatType.Bound params);
	}
}
