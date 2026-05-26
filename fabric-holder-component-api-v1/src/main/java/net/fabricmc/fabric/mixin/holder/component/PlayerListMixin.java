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

package net.fabricmc.fabric.mixin.holder.component;

import net.fabricmc.fabric.impl.holder.component.sync.ClientboundUpdateComponentsPayload;

import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.players.PlayerList;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.holder.component.sync.HolderComponentSynchronization;

import java.util.List;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Shadow
	@Final
	private LayeredRegistryAccess<RegistryLayer> registries;

	@Shadow
	@Final
	private List<ServerPlayer> players;

	@Inject(method = "reloadResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V", shift = At.Shift.AFTER))
	private void sendComponents(CallbackInfo ci) {
		Packet<ClientCommonPacketListener> payload = ServerPlayNetworking.createClientboundPacket(
				HolderComponentSynchronization.serialize(registries)
		);

		for (ServerPlayer player : this.players) {
			if (ClientboundUpdateComponentsPayload.shouldSend(player)) {
				player.connection.send(payload);
			}
		}
	}
}
