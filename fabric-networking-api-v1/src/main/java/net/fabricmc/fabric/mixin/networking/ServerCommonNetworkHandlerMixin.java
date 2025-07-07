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

package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.CustomClickActionListener;
import net.fabricmc.fabric.impl.networking.CustomClickEventHandlerRegistry;

import net.minecraft.network.packet.c2s.common.CustomClickActionC2SPacket;

import net.minecraft.server.network.ServerPlayNetworkHandler;

import net.minecraft.server.network.ServerPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;

import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements NetworkHandlerExtensions {
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void handleCustomPayloadReceivedAsync(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		final CustomPayload payload = packet.payload();

		boolean handled;

		if (getAddon() instanceof ServerConfigurationNetworkAddon addon) {
			handled = addon.handle(payload);
		} else {
			// Play should be handled in ServerPlayNetworkHandlerMixin
			throw new IllegalStateException("Unknown addon");
		}

		if (handled) {
			ci.cancel();
		}
	}

	@Inject(method = "onPong", at = @At("HEAD"))
	private void onPlayPong(CommonPongC2SPacket packet, CallbackInfo ci) {
		if (getAddon() instanceof ServerConfigurationNetworkAddon addon) {
			addon.onPong(packet.getParameter());
		}
	}

	@WrapMethod(method = "onCustomClickAction")
	protected void overrideCustomClickAction(CustomClickActionC2SPacket packet, Operation<Void> original) {
		original.call(packet);
	}

	@Mixin(ServerPlayNetworkHandler.class)
	private abstract static class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandlerMixin {
		@Shadow
		public ServerPlayerEntity player;

		@Override
		protected void overrideCustomClickAction(CustomClickActionC2SPacket packet, Operation<Void> original) {
			super.overrideCustomClickAction(packet, original);
			CustomClickEventHandlerRegistry.invokeListenerEvent(packet.id(), this.player, packet.payload());
		}
	}
}
