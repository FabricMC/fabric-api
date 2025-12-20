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

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;

import net.fabricmc.fabric.api.client.message.v1.ClientHandleChatInputEvents;

/**
 * Mixin to {@link ClientPacketListener} to listen for sending messages and commands.
 * Priority set to 800 to inject before {@code fabric-command-api} so that this api will be called first.
 */
@Mixin(value = ClientPacketListener.class, priority = 800)
public abstract class ClientPacketListenerMixin {
	@Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
	private void fabric_allowLogChatMessage(String _content, CallbackInfo ci, @Local(argsOnly = true) LocalRef<String> content) {
		if (ClientHandleChatInputEvents.IS_CHAT_ALLOWED.invoker().allowLogChatMessage(content.get())) {
			content.set(ClientHandleChatInputEvents.MODIFY_CHAT.invoker().modifyLogChatMessage(content.get()));
			ClientHandleChatInputEvents.CHAT.invoker().onLogChatMessage(content.get());
		} else {
			ClientHandleChatInputEvents.CHAT_CANCELED.invoker().onLogChatMessageCanceled(content.get());
			ci.cancel();
		}
	}

	@Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
	private void fabric_allowLogCommandMessage(String _command, CallbackInfo ci, @Local(argsOnly = true) LocalRef<String> command) {
		if (ClientHandleChatInputEvents.ALLOW_COMMAND.invoker().allowLogCommandMessage(command.get())) {
			command.set(ClientHandleChatInputEvents.MODIFY_COMMAND.invoker().modifyLogCommandMessage(command.get()));
			ClientHandleChatInputEvents.COMMAND.invoker().onLogCommandMessage(command.get());
		} else {
			ClientHandleChatInputEvents.COMMAND_CANCELED.invoker().onLogCommandMessageCanceled(command.get());
			ci.cancel();
		}
	}
}
