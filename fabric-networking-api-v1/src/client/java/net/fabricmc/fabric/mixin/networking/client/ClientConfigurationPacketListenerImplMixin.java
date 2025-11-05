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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;

import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.client.ClientConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@Mixin(value = ClientConfigurationPacketListenerImpl.class, priority = 999)
public abstract class ClientConfigurationPacketListenerImplMixin extends ClientCommonPacketListenerImpl implements NetworkHandlerExtensions {
	@Unique
	private ClientConfigurationNetworkAddon addon;

	protected ClientConfigurationPacketListenerImplMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddon(CallbackInfo ci) {
		this.addon = new ClientConfigurationNetworkAddon((ClientConfigurationPacketListenerImpl) (Object) this, this.minecraft);
		// A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
		ClientNetworkingImpl.setClientConfigurationAddon(this.addon);
		this.addon.lateInit();
	}

	@Inject(method = "handleConfigurationFinished", at = @At(value = "NEW", target = "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/network/Connection;Lnet/minecraft/client/multiplayer/CommonListenerCookie;)Lnet/minecraft/client/multiplayer/ClientPacketListener;"))
	public void handleComplete(ClientboundFinishConfigurationPacket packet, CallbackInfo ci) {
		this.addon.handleComplete();
	}

	@Override
	public ClientConfigurationNetworkAddon getAddon() {
		return addon;
	}
}
