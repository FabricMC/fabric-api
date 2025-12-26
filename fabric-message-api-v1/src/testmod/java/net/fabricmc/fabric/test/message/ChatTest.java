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

package net.fabricmc.fabric.test.message;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public class ChatTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatTest.class);

	@Override
	public void onInitialize() {
		Executor ioWorkerExecutor = Util.ioPool();

		// Basic content phase testing
		ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.CONTENT_PHASE, (sender, message) -> {
			if (message.getString().contains("tater")) {
				return message.copy().append(" :tiny_potato:");
			}

			return message;
		});

		// Content phase testing, with variable info
		ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.CONTENT_PHASE, (sender, message) -> {
			if (message.getString().contains("random")) {
				return Component.nullToEmpty(String.valueOf(RandomSource.create().nextIntBetweenInclusive(0, 100)));
			}

			return message;
		});

		// Basic styling phase testing
		ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.STYLING_PHASE, (sender, message) -> {
			if (sender != null && sender.hasInfiniteMaterials()) {
				return message.copy().withStyle(style -> style.withColor(0xFFA500));
			}

			return message;
		});

		// ServerMessageEvents
		ServerMessageEvents.CHAT_MESSAGE.register(
				(message, sender, params) -> LOGGER.info("ChatTest: {} sent \"{}\"", sender, message)
		);
		ServerMessageEvents.GAME_MESSAGE.register(
				(server, message, overlay) -> LOGGER.info("ChatTest: server sent \"{}\"", message)
		);
		ServerMessageEvents.COMMAND_MESSAGE.register(
				(message, source, params) -> LOGGER.info("ChatTest: command sent \"{}\"", message)
		);

		// ServerMessageEvents blocking
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(
				(message, sender, params) -> !message.signedContent().contains("sadtater")
		);
		ServerMessageEvents.ALLOW_GAME_MESSAGE.register((server, message, overlay) -> {
			if (message.getContents() instanceof TranslatableContents translatable) {
				return !translatable.getKey().startsWith("death.attack.badRespawnPoint.");
			}

			return true;
		});
		ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register(
				(message, source, params) -> !message.signedContent().contains("sadtater")
		);
	}
}
