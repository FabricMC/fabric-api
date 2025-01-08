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

package net.fabricmc.fabric.mixin.attachment.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;

import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;

@Mixin(ClientPlayNetworkHandler.class)
abstract class ClientPlayNetworkHandlerMixin {
	@WrapOperation(
			method = "onPlayerRespawn",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;"
			),
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;startWorldLoading(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen$WorldEntryReason;)V")
			)
	)
	private void copyAttachmentsOnClientRespawn(MinecraftClient client, ClientPlayerEntity newPlayer, Operation<Void> init, PlayerRespawnS2CPacket packet, @Local(ordinal = 0) ClientPlayerEntity oldPlayer) {
		/*
		 * The KEEP_ATTRIBUTES flag is not set on a death respawn, and set in all other cases
		 */
		AttachmentTargetImpl.transfer(oldPlayer, newPlayer, !packet.hasFlag(PlayerRespawnS2CPacket.KEEP_ATTRIBUTES));
		init.call(client, newPlayer);
	}
}
