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

package net.fabricmc.fabric.mixin.command.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

@Mixin(ClientSuggestionProvider.class)
abstract class ClientSuggestionProviderMixin implements FabricClientCommandSource {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Override
	public void sendFeedback(Component message) {
		this.minecraft.gui.getChat().addMessage(message);
		this.minecraft.getNarrator().saySystemChatQueued(message);
	}

	@Override
	public void sendError(Component message) {
		sendFeedback(Component.empty().append(message).withStyle(ChatFormatting.RED));
	}

	@Override
	public Minecraft getClient() {
		return minecraft;
	}

	@Override
	public LocalPlayer getPlayer() {
		return minecraft.player;
	}

	@Override
	public ClientLevel getWorld() {
		return minecraft.level;
	}
}
