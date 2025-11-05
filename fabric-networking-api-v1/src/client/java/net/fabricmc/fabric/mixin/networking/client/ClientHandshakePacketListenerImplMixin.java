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

package net.fabricmc.fabric.mixin.networking.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;

import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.client.ClientLoginNetworkAddon;
import net.fabricmc.fabric.impl.networking.payload.PacketByteBufLoginQueryRequestPayload;

@Mixin(ClientHandshakePacketListenerImpl.class)
abstract class ClientHandshakePacketListenerImplMixin implements NetworkHandlerExtensions {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private Connection connection;

	@Unique
	private ClientLoginNetworkAddon addon;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddon(CallbackInfo ci) {
		this.addon = new ClientLoginNetworkAddon((ClientHandshakePacketListenerImpl) (Object) this, this.minecraft);
		// A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
		this.addon.lateInit();
	}

	@Inject(method = "handleCustomQuery", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER), cancellable = true)
	private void handleQueryRequest(ClientboundCustomQueryPacket packet, CallbackInfo ci) {
		if (packet.payload() instanceof PacketByteBufLoginQueryRequestPayload payload) {
			if (this.addon.handlePacket(packet)) {
				ci.cancel();
			} else {
				payload.data().skipBytes(payload.data().readableBytes());
			}
		}
	}

	@Override
	public ClientLoginNetworkAddon getAddon() {
		return this.addon;
	}
}
