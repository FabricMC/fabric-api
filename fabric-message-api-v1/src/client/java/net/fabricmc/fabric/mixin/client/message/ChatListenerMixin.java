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

import net.fabricmc.fabric.api.client.message.v1.ClientLogChatMessageEvents;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
	@Inject(method = "showMessageToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getChat()Lnet/minecraft/client/gui/components/ChatComponent;", ordinal = 0), cancellable = true)
	private void fabric_onSignedChatMessage(ChatType.Bound bind, PlayerChatMessage message, Component decorated, GameProfile sender, boolean onlyShowSecureChat, Instant timeStamp, CallbackInfoReturnable<Boolean> cir) {
		fabric_onChatMessage(decorated, message, sender, bind, timeStamp, cir);
	}

	@Inject(method = "showMessageToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getChat()Lnet/minecraft/client/gui/components/ChatComponent;", ordinal = 1), cancellable = true)
	private void fabric_onFilteredSignedChatMessage(ChatType.Bound bind, PlayerChatMessage message, Component decorated, GameProfile sender, boolean onlyShowSecureChat, Instant timeStamp, CallbackInfoReturnable<Boolean> cir) {
		Component filtered = message.filterMask().applyWithFormatting(message.signedContent());

		if (filtered != null) {
			fabric_onChatMessage(bind.decorate(filtered), message, sender, bind, timeStamp, cir);
		}
	}

	@Inject(method = "lambda$handleDisguisedChatMessage$0", at = @At("HEAD"), cancellable = true)
	private void fabric_onProfilelessChatMessage(ChatType.Bound bind, Component content, Instant timeStamp, CallbackInfoReturnable<Boolean> cir) {
		fabric_onChatMessage(bind.decorate(content), null, null, bind, timeStamp, cir);
	}

	@Unique
	private void fabric_onChatMessage(Component message, @Nullable PlayerChatMessage playerChatMessage, @Nullable GameProfile sender, ChatType.Bound bind, Instant timeStamp, CallbackInfoReturnable<Boolean> cir) {
		if (ClientLogChatMessageEvents.IS_CHAT_ALLOWED.invoker().allowLogChatMessage(message, playerChatMessage, sender, bind, timeStamp)) {
			ClientLogChatMessageEvents.CHAT.invoker().onLogChatMessage(message, playerChatMessage, sender, bind, timeStamp);
		} else {
			ClientLogChatMessageEvents.CHAT_CANCELED.invoker().onLogChatMessageCanceled(message, playerChatMessage, sender, bind, timeStamp);
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true)
	private void fabric_allowGameMessage(Component _message, boolean overlay, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Component> message) {
		if (ClientLogChatMessageEvents.ALLOW_GAME.invoker().allowLogGameMessage(message.get(), overlay)) {
			message.set(ClientLogChatMessageEvents.MODIFY_GAME.invoker().modifyLoggedGameMessage(message.get(), overlay));
			ClientLogChatMessageEvents.GAME.invoker().onLogGameMessage(message.get(), overlay);
		} else {
			ClientLogChatMessageEvents.GAME_CANCELED.invoker().onLogGameMessageCanceled(message.get(), overlay);
			ci.cancel();
		}
	}
}
