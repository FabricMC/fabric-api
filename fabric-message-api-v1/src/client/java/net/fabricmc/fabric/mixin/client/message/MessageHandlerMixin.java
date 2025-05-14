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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {
	@Inject(method = "processChatMessageInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;getChatHud()Lnet/minecraft/client/gui/hud/ChatHud;", ordinal = 0), cancellable = true)
	private void fabric_onSignedChatMessage(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
		fabric_onChatMessage(decorated, message, sender, params, receptionTimestamp, cir);
	}

	@Inject(method = "processChatMessageInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;getChatHud()Lnet/minecraft/client/gui/hud/ChatHud;", ordinal = 1), cancellable = true)
	private void fabric_onFilteredSignedChatMessage(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
		Text filtered = message.filterMask().getFilteredText(message.getSignedContent());

		if (filtered != null) {
			fabric_onChatMessage(params.applyChatDecoration(filtered), message, sender, params, receptionTimestamp, cir);
		}
	}

	@Inject(method = "method_45745", at = @At("HEAD"), cancellable = true)
	private void fabric_onProfilelessChatMessage(MessageType.Parameters params, Text content, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
		fabric_onChatMessage(params.applyChatDecoration(content), null, null, params, receptionTimestamp, cir);
	}

	@Unique
	private void fabric_onChatMessage(Text message, @Nullable SignedMessage signedMessage, @Nullable GameProfile sender, MessageType.Parameters params, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
		if (ClientReceiveMessageEvents.ALLOW_CHAT.invoker().allowReceiveChatMessage(message, signedMessage, sender, params, receptionTimestamp)) {
			ClientReceiveMessageEvents.CHAT.invoker().onReceiveChatMessage(message, signedMessage, sender, params, receptionTimestamp);
		} else {
			ClientReceiveMessageEvents.CHAT_CANCELED.invoker().onReceiveChatMessageCanceled(message, signedMessage, sender, params, receptionTimestamp);
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
	private void fabric_allowGameMessage(Text _message, boolean overlay, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Text> message) {
		if (ClientReceiveMessageEvents.ALLOW_GAME.invoker().allowReceiveGameMessage(message.get(), overlay)) {
			message.set(ClientReceiveMessageEvents.MODIFY_GAME.invoker().modifyReceivedGameMessage(message.get(), overlay));
			ClientReceiveMessageEvents.GAME.invoker().onReceiveGameMessage(message.get(), overlay);
		} else {
			ClientReceiveMessageEvents.GAME_CANCELED.invoker().onReceiveGameMessageCanceled(message.get(), overlay);
			ci.cancel();
		}
	}
}
