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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.state.NetworkState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.UntrackedNetworkHandler;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@Mixin(value = ServerPlayNetworkHandler.class, priority = 999)
abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler implements NetworkHandlerExtensions {
	@Unique
	private ServerPlayNetworkAddon addon;

	ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData arg) {
		super(server, connection, arg);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddon(CallbackInfo ci) {
		this.addon = new ServerPlayNetworkAddon((ServerPlayNetworkHandler) (Object) this, connection, server);

		if (!(this instanceof UntrackedNetworkHandler)) {
			// A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
			this.addon.lateInit();
		}
	}

	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void handleCustomPayloadReceivedAsync(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		try {
			if (getAddon().handle(packet.payload())) {
				ci.cancel();
			}
		} catch (OffThreadException e) {
			this.server.getPacketApplyBatcher().add(this, packet);
			ci.cancel();
		}
	}

	@WrapOperation(method = "onAcknowledgeReconfiguration", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;transitionInbound(Lnet/minecraft/network/state/NetworkState;Lnet/minecraft/network/listener/PacketListener;)V"))
	private <T extends PacketListener> void onAcknowledgeReconfiguration(ClientConnection instance, NetworkState<T> state, T packetListener, Operation<Void> original) {
		original.call(instance, state, packetListener);

		ServerConfigurationNetworkHandler networkHandler = (ServerConfigurationNetworkHandler) packetListener;
		ServerNetworkingImpl.getAddon(networkHandler).setReconfiguring();

		if (addon.requestedReconfigure()) {
			networkHandler.sendConfigurations();
		}
	}

	@Override
	public ServerPlayNetworkAddon getAddon() {
		return this.addon;
	}
}
