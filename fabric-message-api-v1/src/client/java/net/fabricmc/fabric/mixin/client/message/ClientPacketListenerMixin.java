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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Hud;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;

/**
 * Mixin to {@link ClientPacketListener} to listen for sending messages and commands and receiving titles.
 * Priority set to 800 to inject before {@code fabric-command-api} so that this api will be called first.
 */
@Mixin(value = ClientPacketListener.class, priority = 800)
public abstract class ClientPacketListenerMixin {
	@WrapOperation(method = "setTitleText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;setTitle(Lnet/minecraft/network/chat/Component;)V"))
	private void fabric_receiveTitle(Hud hud, Component title, Operation<Void> original) {
		if (ClientReceiveMessageEvents.ALLOW_TITLE.invoker().allowReceiveTitle(title)) {
			title = ClientReceiveMessageEvents.MODIFY_TITLE.invoker().modifyReceivedTitle(title);
			ClientReceiveMessageEvents.TITLE.invoker().onReceiveTitle(title);
			original.call(hud, title);
		} else {
			ClientReceiveMessageEvents.TITLE_CANCELED.invoker().onReceiveTitleCanceled(title);
		}
	}

	@WrapOperation(method = "setSubtitleText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;setSubtitle(Lnet/minecraft/network/chat/Component;)V"))
	private void fabric_receiveSubtitle(Hud hud, Component subtitle, Operation<Void> original) {
		if (ClientReceiveMessageEvents.ALLOW_SUBTITLE.invoker().allowReceiveSubtitle(subtitle)) {
			subtitle = ClientReceiveMessageEvents.MODIFY_SUBTITLE.invoker().modifyReceivedSubtitle(subtitle);
			ClientReceiveMessageEvents.SUBTITLE.invoker().onReceiveSubtitle(subtitle);
			original.call(hud, subtitle);
		} else {
			ClientReceiveMessageEvents.SUBTITLE_CANCELED.invoker().onReceiveSubtitleCanceled(subtitle);
		}
	}

	@Inject(method = "handleTitlesClear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;clearTitles()V"))
	private void fabric_clearTitles(ClientboundClearTitlesPacket packet, CallbackInfo ci) {
		ClientReceiveMessageEvents.CLEAR_TITLES.invoker().onClearTitles(packet.shouldResetTimes());
	}

	@Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
	private void fabric_allowSendChatMessage(String _content, CallbackInfo ci, @Local(argsOnly = true) LocalRef<String> content) {
		if (ClientSendMessageEvents.ALLOW_CHAT.invoker().allowSendChatMessage(content.get())) {
			content.set(ClientSendMessageEvents.MODIFY_CHAT.invoker().modifySendChatMessage(content.get()));
			ClientSendMessageEvents.CHAT.invoker().onSendChatMessage(content.get());
		} else {
			ClientSendMessageEvents.CHAT_CANCELED.invoker().onSendChatMessageCanceled(content.get());
			ci.cancel();
		}
	}

	@Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
	private void fabric_allowSendCommandMessage(String _command, CallbackInfo ci, @Local(argsOnly = true) LocalRef<String> command) {
		if (ClientSendMessageEvents.ALLOW_COMMAND.invoker().allowSendCommandMessage(command.get())) {
			command.set(ClientSendMessageEvents.MODIFY_COMMAND.invoker().modifySendCommandMessage(command.get()));
			ClientSendMessageEvents.COMMAND.invoker().onSendCommandMessage(command.get());
		} else {
			ClientSendMessageEvents.COMMAND_CANCELED.invoker().onSendCommandMessageCanceled(command.get());
			ci.cancel();
		}
	}
}
