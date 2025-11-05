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

package net.fabricmc.fabric.mixin.message;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At("HEAD"), cancellable = true)
	private void onSendChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params, CallbackInfo ci) {
		if (!ServerMessageEvents.ALLOW_CHAT_MESSAGE.invoker().allowChatMessage(message, sender, params)) {
			ci.cancel();
			return;
		}

		ServerMessageEvents.CHAT_MESSAGE.invoker().onChatMessage(message, sender, params);
	}

	@Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Z)V", at = @At("HEAD"), cancellable = true)
	private void onSendGameMessage(Component message, Function<ServerPlayer, Component> playerMessageFactory, boolean overlay, CallbackInfo ci) {
		if (!ServerMessageEvents.ALLOW_GAME_MESSAGE.invoker().allowGameMessage(this.server, message, overlay)) {
			ci.cancel();
			return;
		}

		ServerMessageEvents.GAME_MESSAGE.invoker().onGameMessage(this.server, message, overlay);
	}

	@Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At("HEAD"), cancellable = true)
	private void onSendCommandMessage(PlayerChatMessage message, CommandSourceStack source, ChatType.Bound params, CallbackInfo ci) {
		if (!ServerMessageEvents.ALLOW_COMMAND_MESSAGE.invoker().allowCommandMessage(message, source, params)) {
			ci.cancel();
			return;
		}

		ServerMessageEvents.COMMAND_MESSAGE.invoker().onCommandMessage(message, source, params);
	}
}
