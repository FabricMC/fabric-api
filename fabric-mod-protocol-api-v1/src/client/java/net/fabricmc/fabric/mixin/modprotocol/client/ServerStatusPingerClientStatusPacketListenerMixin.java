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

package net.fabricmc.fabric.mixin.modprotocol.client;

import java.util.List;
import java.util.Objects;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;

import net.fabricmc.fabric.impl.modprotocol.ModProtocolHolder;
import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;
import net.fabricmc.fabric.impl.modprotocol.RemoteModProtocol;
import net.fabricmc.fabric.impl.modprotocol.client.FabricServerData;

@Mixin(targets = "net/minecraft/client/multiplayer/ServerStatusPinger$1")
public class ServerStatusPingerClientStatusPacketListenerMixin {
	@Shadow
	@Final
	ServerData val$data;

	@Inject(method = "handleStatusResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/status/ServerStatus;players()Ljava/util/Optional;"))
	private void handleModdedVersionChecks(ClientboundStatusResponsePacket packet, CallbackInfo ci) {
		ModProtocolHolder holder = ModProtocolHolder.of(packet.status());

		List<RemoteModProtocol> protocols = Objects.requireNonNullElse(holder.fabric$getModProtocol(), List.of());
		ModProtocolManager.ValidationResult result = ModProtocolManager.validateClient(protocols);

		if (result.isSuccess() || !holder.fabric$getAlwaysCompatible()) {
			((FabricServerData) this.val$data).fabric$setModProtocolsState(protocols, result);
		} else {
			((FabricServerData) this.val$data).fabric$setModProtocolsState(protocols, new ModProtocolManager.ValidationResult(result.supportedProtocols(), List.of(), List.of(), false));
		}
	}
}
