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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin implements NetworkHandlerExtensions {
	@Shadow
	@Final
	protected MinecraftServer server;

	@Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
	private void handleCustomPayloadReceivedAsync(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
		final CustomPacketPayload payload = packet.payload();

		try {
			boolean handled;

			if (getAddon() instanceof ServerConfigurationNetworkAddon addon) {
				handled = addon.handle(payload);
			} else {
				// Play should be handled in ServerGamePacketListenerImplMixin
				throw new IllegalStateException("Unknown addon");
			}

			if (handled) {
				ci.cancel();
			}
		} catch (RunningOnDifferentThreadException e) {
			this.server.packetProcessor().scheduleIfPossible((ServerCommonPacketListenerImpl) (Object) this, packet);
			ci.cancel();
		}
	}

	@Inject(method = "handlePong", at = @At("HEAD"))
	private void onPlayPong(ServerboundPongPacket packet, CallbackInfo ci) {
		if (getAddon() instanceof ServerConfigurationNetworkAddon addon) {
			addon.onPong(packet.getId());
		}
	}
}
