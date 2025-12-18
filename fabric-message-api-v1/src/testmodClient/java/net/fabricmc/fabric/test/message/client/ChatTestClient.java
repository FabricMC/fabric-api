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

package net.fabricmc.fabric.test.message.client;

import com.mojang.brigadier.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.chat.Component;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientLogChatMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientHandleChatInputEvents;

public class ChatTestClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatTestClient.class);

	@Override
	public void onInitializeClient() {
		//Register test client commands
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(ClientCommandManager.literal("block").then(ClientCommandManager.literal("send").executes(context -> {
			throw new AssertionError("This client command should be blocked!");
		}))));
		//Register the modified result command from ClientHandleChatInputEvents#MODIFY_COMMAND to ensure that MODIFY_COMMAND executes before the client command api
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(ClientCommandManager.literal("sending").then(ClientCommandManager.literal("modified").then(ClientCommandManager.literal("command").then(ClientCommandManager.literal("message").executes(context -> {
			LOGGER.info("Command modified by ClientHandleChatInputEvents#MODIFY_COMMAND successfully processed by fabric client command api");
			return Command.SINGLE_SUCCESS;
		}))))));
		//Test client send message events
		ClientHandleChatInputEvents.IS_CHAT_ALLOWED.register((message) -> {
			if (message.contains("block send")) {
				LOGGER.info("Blocked chat message: " + message);
				return false;
			}

			return true;
		});
		ClientHandleChatInputEvents.MODIFY_CHAT.register((message) -> {
			if (message.contains("modify send")) {
				LOGGER.info("Modifying chat message: " + message);
				return "sending modified chat message";
			}

			return message;
		});
		ClientHandleChatInputEvents.CHAT.register((message -> LOGGER.info("Sent chat message: " + message)));
		ClientHandleChatInputEvents.CHAT_CANCELED.register((message) -> LOGGER.info("Canceled sending chat message: " + message));
		//Test client send command events
		ClientHandleChatInputEvents.ALLOW_COMMAND.register((command) -> {
			if (command.contains("block send")) {
				LOGGER.info("Blocked command message: " + command);
				return false;
			}

			return true;
		});
		ClientHandleChatInputEvents.MODIFY_COMMAND.register((command) -> {
			if (command.contains("modify send")) {
				LOGGER.info("Modifying command message: " + command);
				return "sending modified command message";
			}

			return command;
		});
		ClientHandleChatInputEvents.COMMAND.register((command -> LOGGER.info("Sent command message: " + command)));
		ClientHandleChatInputEvents.COMMAND_CANCELED.register((command) -> LOGGER.info("Canceled sending command message: " + command));
		//Test client receive message events
		ClientLogChatMessageEvents.IS_CHAT_ALLOWED.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			if (message.getString().contains("block receive")) {
				LOGGER.info("Blocked receiving chat message: " + message.getString());
				return false;
			}

			return true;
		});
		ClientLogChatMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> LOGGER.info("Received chat message sent by {} at time {}: {}", sender == null ? "null" : sender.name(), receptionTimestamp.toEpochMilli(), message.getString()));
		ClientLogChatMessageEvents.CHAT_CANCELED.register((message, signedMessage, sender, params, receptionTimestamp) -> LOGGER.info("Cancelled receiving chat message sent by {} at time {}: {}", sender == null ? "null" : sender.name(), receptionTimestamp.toEpochMilli(), message.getString()));
		//Test client receive game message events
		ClientLogChatMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			if (message.getString().contains("block receive")) {
				LOGGER.info("Blocked receiving game message: " + message.getString());
				return false;
			}

			return true;
		});
		ClientLogChatMessageEvents.MODIFY_GAME.register((message, overlay) -> {
			if (message.getString().contains("modify receive")) {
				LOGGER.info("Modifying received game message: " + message.getString());
				return Component.nullToEmpty("modified receiving game message");
			}

			return message;
		});
		ClientLogChatMessageEvents.GAME.register((message, overlay) -> LOGGER.info("Received game message with overlay {}: {}", overlay, message.getString()));
		ClientLogChatMessageEvents.GAME_CANCELED.register((message, overlay) -> LOGGER.info("Cancelled receiving game message with overlay {}: {}", overlay, message.getString()));
	}
}
