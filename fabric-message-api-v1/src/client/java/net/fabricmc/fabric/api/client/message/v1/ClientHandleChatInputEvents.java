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

package net.fabricmc.fabric.api.client.message.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Contains client-side events triggered when sending messages.
 */
public final class ClientHandleChatInputEvents {
	private ClientHandleChatInputEvents() {
	}

	/**
	 * An event triggered when the client is about to send a chat message,
	 * typically from a client GUI. Mods can use this to block the message.
	 *
	 * <p>If a listener returned {@code false}, the message will not be sent,
	 * the remaining listeners will not be called (if any), and
	 * {@link #CHAT_CANCELED} will be triggered instead of {@link #MODIFY_CHAT}.
	 */
	public static final Event<IsChatAllowed> IS_CHAT_ALLOWED = EventFactory.createArrayBacked(IsChatAllowed.class, listeners -> (message) -> {
		for (IsChatAllowed listener : listeners) {
			if (!listener.allowLogChatMessage(message)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * An event triggered when the client is about to send a command,
	 * which is whenever the player executes a command
	 * including client commands registered with {@code fabric-command-api}.
	 * Mods can use this to block the message.
	 * The command string does not include a slash at the beginning.
	 *
	 * <p>If a listener returned {@code false}, the command will not be sent,
	 * the remaining listeners will not be called (if any), and
	 * {@link #COMMAND_CANCELED} will be triggered instead of {@link #MODIFY_COMMAND}.
	 */
	public static final Event<AllowCommand> ALLOW_COMMAND = EventFactory.createArrayBacked(AllowCommand.class, listeners -> (command) -> {
		for (AllowCommand listener : listeners) {
			if (!listener.allowLogCommandMessage(command)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * An event triggered when the client sends a chat message,
	 * typically from a client GUI. Is not called when {@linkplain
	 * #IS_CHAT_ALLOWED chat messages are blocked}.
	 * Mods can use this to modify the message.
	 * Use {@link #CHAT} if not modifying the message.
	 */
	public static final Event<ModifyChat> MODIFY_CHAT = EventFactory.createArrayBacked(ModifyChat.class, listeners -> (message) -> {
		for (ModifyChat listener : listeners) {
			message = listener.modifyLogChatMessage(message);
		}

		return message;
	});

	/**
	 * An event triggered when the client sends a command,
	 * which is whenever the player executes a command
	 * including client commands registered with {@code fabric-command-api}.
	 * Is not called when {@linkplain #ALLOW_COMMAND command messages are blocked}.
	 * The command string does not include a slash at the beginning.
	 * Mods can use this to modify the command.
	 * Use {@link #COMMAND} if not modifying the command.
	 */
	public static final Event<ModifyCommand> MODIFY_COMMAND = EventFactory.createArrayBacked(ModifyCommand.class, listeners -> (command) -> {
		for (ModifyCommand listener : listeners) {
			command = listener.modifyLogCommandMessage(command);
		}

		return command;
	});

	/**
	 * An event triggered when the client sends a chat message,
	 * typically from a client GUI. Is not called when {@linkplain
	 * #IS_CHAT_ALLOWED chat messages are blocked}.
	 * Mods can use this to listen to the message.
	 */
	public static final Event<Chat> CHAT = EventFactory.createArrayBacked(Chat.class, listeners -> (message) -> {
		for (Chat listener : listeners) {
			listener.onLogChatMessage(message);
		}
	});

	/**
	 * An event triggered when the client sends a command,
	 * which is whenever the player executes a command
	 * including client commands registered with {@code fabric-command-api}.
	 * Is not called when {@linkplain #ALLOW_COMMAND command messages are blocked}.
	 * The command string does not include a slash at the beginning.
	 * Mods can use this to listen to the command.
	 */
	public static final Event<Command> COMMAND = EventFactory.createArrayBacked(Command.class, listeners -> (command) -> {
		for (Command listener : listeners) {
			listener.onLogCommandMessage(command);
		}
	});

	/**
	 * An event triggered when sending a chat message is canceled with {@link #IS_CHAT_ALLOWED}.
	 */
	public static final Event<ChatCanceled> CHAT_CANCELED = EventFactory.createArrayBacked(ChatCanceled.class, listeners -> (message) -> {
		for (ChatCanceled listener : listeners) {
			listener.onLogChatMessageCanceled(message);
		}
	});

	/**
	 * An event triggered when sending a command is canceled with {@link #ALLOW_COMMAND}.
	 * The command string does not include a slash at the beginning.
	 */
	public static final Event<CommandCanceled> COMMAND_CANCELED = EventFactory.createArrayBacked(CommandCanceled.class, listeners -> (command) -> {
		for (CommandCanceled listener : listeners) {
			listener.onLogCommandMessageCanceled(command);
		}
	});

	@FunctionalInterface
	public interface IsChatAllowed {
		/**
		 * Called when the client is about to send a chat message,
		 * typically from a client GUI. Returning {@code false}
		 * prevents the message from being sent, and
		 * {@link #CHAT_CANCELED} will be triggered instead of {@link #MODIFY_CHAT}.
		 *
		 * @param message the message that will be sent to the server
		 * @return {@code true} if the message should be sent, otherwise {@code false}
		 */
		boolean allowLogChatMessage(String message);
	}

	@FunctionalInterface
	public interface AllowCommand {
		/**
		 * Called when the client is about to send a command,
		 * which is whenever the player executes a command
		 * including client commands registered with {@code fabric-command-api}.
		 * Returning {@code false} prevents the command from being sent, and
		 * {@link #COMMAND_CANCELED} will be triggered instead of {@link #MODIFY_COMMAND}.
		 * The command string does not include a slash at the beginning.
		 *
		 * @param command the command that will be sent to the server, without a slash at the beginning.
		 * @return {@code true} if the command should be sent, otherwise {@code false}
		 */
		boolean allowLogCommandMessage(String command);
	}

	@FunctionalInterface
	public interface ModifyChat {
		/**
		 * Called when the client sends a chat message,
		 * typically from a client GUI. Is not called when {@linkplain
		 * #IS_CHAT_ALLOWED chat messages are blocked}.
		 * Use {@link #CHAT} if not modifying the message.
		 *
		 * @param message the message that will be sent to the server
		 * @return the modified message that will be sent to the server
		 */
		String modifyLogChatMessage(String message);
	}

	@FunctionalInterface
	public interface ModifyCommand {
		/**
		 * Called when the client sends a command,
		 * which is whenever the player executes a command
		 * including client commands registered with {@code fabric-command-api}.
		 * Is not called when {@linkplain #ALLOW_COMMAND command messages are blocked}.
		 * The command string does not include a slash at the beginning.
		 * Use {@link #COMMAND} if not modifying the command.
		 *
		 * @param command the command that will be sent to the server, without a slash at the beginning.
		 * @return the modified command that will be sent to the server, without a slash at the beginning.
		 */
		String modifyLogCommandMessage(String command);
	}

	@FunctionalInterface
	public interface Chat {
		/**
		 * Called when the client sends a chat message,
		 * typically from a client GUI. Is not called when {@linkplain
		 * #IS_CHAT_ALLOWED chat messages are blocked}.
		 *
		 * @param message the message that will be sent to the server
		 */
		void onLogChatMessage(String message);
	}

	@FunctionalInterface
	public interface Command {
		/**
		 * Called when the client sends a command,
		 * which is whenever the player executes a command
		 * including client commands registered with {@code fabric-command-api}.
		 * Is not called when {@linkplain #ALLOW_COMMAND command messages are blocked}.
		 * The command string does not include a slash at the beginning.
		 *
		 * @param command the command that will be sent to the server, without a slash at the beginning.
		 */
		void onLogCommandMessage(String command);
	}

	@FunctionalInterface
	public interface ChatCanceled {
		/**
		 * Called when sending a chat message is canceled with {@link #IS_CHAT_ALLOWED}.
		 *
		 * @param message the message that is canceled from being sent to the server
		 */
		void onLogChatMessageCanceled(String message);
	}

	@FunctionalInterface
	public interface CommandCanceled {
		/**
		 * Called when sending a command is canceled with {@link #ALLOW_COMMAND}.
		 * The command string does not include a slash at the beginning.
		 *
		 * @param command the command that is being sent to the server, without a slash at the beginning.
		 */
		void onLogCommandMessageCanceled(String command);
	}
}
