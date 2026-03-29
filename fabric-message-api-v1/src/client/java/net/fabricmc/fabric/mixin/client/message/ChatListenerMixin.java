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

package net.fabricmc.fabric.mixin.client.message;

import java.time.Instant;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
	@Inject(method = "showMessageToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getChat()Lnet/minecraft/client/gui/components/ChatComponent;", ordinal = 0), cancellable = true)
	private void fabric_onSignedChatMessage(ChatType.Bound boundChatType, PlayerChatMessage message, Component decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
		fabric_onChatMessage(decorated, message, sender, boundChatType, receptionTimestamp, cir);
	}

	@Inject(method = "showMessageToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getChat()Lnet/minecraft/client/gui/components/ChatComponent;", ordinal = 1), cancellable = true)
	private void fabric_onFilteredSignedChatMessage(ChatType.Bound boundChatType, PlayerChatMessage message, Component decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
		Component filtered = message.filterMask().applyWithFormatting(message.signedContent());

		if (filtered != null) {
			fabric_onChatMessage(boundChatType.decorate(filtered), message, sender, boundChatType, receptionTimestamp, cir);
		}
	}

	@Inject(method = "lambda$handleDisguisedChatMessage$0", at = @At("HEAD"), cancellable = true)
	private void fabric_onProfilelessChatMessage(ChatType.Bound boundChatType, Component content, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
		fabric_onChatMessage(boundChatType.decorate(content), null, null, boundChatType, receptionTimestamp, cir);
	}

	@Unique
	private void fabric_onChatMessage(Component message, @Nullable PlayerChatMessage signedMessage, @Nullable GameProfile sender, ChatType.Bound boundChatType, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
		if (ClientReceiveMessageEvents.ALLOW_CHAT.invoker().allowReceiveChatMessage(message, signedMessage, sender, boundChatType, receptionTimestamp)) {
			ClientReceiveMessageEvents.CHAT.invoker().onReceiveChatMessage(message, signedMessage, sender, boundChatType, receptionTimestamp);
		} else {
			ClientReceiveMessageEvents.CHAT_CANCELED.invoker().onReceiveChatMessageCanceled(message, signedMessage, sender, boundChatType, receptionTimestamp);
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true)
	private void fabric_allowSystemMessage(Component _message, boolean remote, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Component> message) {
		if (ClientReceiveMessageEvents.ALLOW_GAME.invoker().allowReceiveGameMessage(message.get(), false)) {
			message.set(ClientReceiveMessageEvents.MODIFY_GAME.invoker().modifyReceivedGameMessage(message.get(), false));
			ClientReceiveMessageEvents.GAME.invoker().onReceiveGameMessage(message.get(), false);
		} else {
			ClientReceiveMessageEvents.GAME_CANCELED.invoker().onReceiveGameMessageCanceled(message.get(), false);
			ci.cancel();
		}
	}

	@Inject(method = "handleOverlay", at = @At("HEAD"), cancellable = true)
	private void fabric_allowOverlayMessage(Component _message, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Component> message) {
		if (ClientReceiveMessageEvents.ALLOW_GAME.invoker().allowReceiveGameMessage(message.get(), true)) {
			message.set(ClientReceiveMessageEvents.MODIFY_GAME.invoker().modifyReceivedGameMessage(message.get(), true));
			ClientReceiveMessageEvents.GAME.invoker().onReceiveGameMessage(message.get(), true);
		} else {
			ClientReceiveMessageEvents.GAME_CANCELED.invoker().onReceiveGameMessageCanceled(message.get(), true);
			ci.cancel();
		}
	}
}
