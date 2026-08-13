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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public class ChatGameTest {
	private static final String MARKER = "fabric message api gametest";
	private static final List<String> FIRED_EVENTS = new CopyOnWriteArrayList<>();

	static {
		ServerMessageEvents.COMMAND_MESSAGE.register((message, source, params) -> {
			if (message.signedContent().contains(MARKER)) FIRED_EVENTS.add("command");
		});
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
			if (message.signedContent().contains(MARKER)) FIRED_EVENTS.add("allow_chat");
			return true;
		});
		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			if (message.signedContent().contains(MARKER)) FIRED_EVENTS.add("chat");
		});
	}

	/**
	 * A command message sent by a player must trigger the chat events after the
	 * command events, as documented in {@link ServerMessageEvents}.
	 */
	@GameTest
	public void playerCommandMessageTriggersChatEvents(GameTestHelper helper) {
		FIRED_EVENTS.clear();
		ServerPlayer player = FakePlayer.get(helper.getLevel());
		helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "/me " + MARKER);

		helper.succeedWhen(() -> helper.assertTrue(
				FIRED_EVENTS.equals(List.of("command", "allow_chat", "chat")),
				Component.literal("Expected [command, allow_chat, chat] for a player-executed /me, got " + FIRED_EVENTS)
		));
	}
}
