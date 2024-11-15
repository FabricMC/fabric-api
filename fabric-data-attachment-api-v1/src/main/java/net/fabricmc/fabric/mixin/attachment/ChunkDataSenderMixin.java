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

package net.fabricmc.fabric.mixin.attachment;

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;

@Mixin(ChunkDataSender.class)
abstract class ChunkDataSenderMixin {
	@WrapOperation(
			method = "sendChunkBatches",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/network/ChunkDataSender;sendChunkData(Lnet/minecraft/server/network/ServerPlayNetworkHandler;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;)V"
			)
	)
	private void sendInitialAttachmentData(ServerPlayNetworkHandler handler, ServerWorld world, WorldChunk chunk, Operation<Void> original, ServerPlayerEntity player) {
		original.call(handler, world, chunk);
		// do a wrap operation so this packet is sent *after* the chunk ones
		List<AttachmentChange> changes = new ArrayList<>();
		((AttachmentTargetImpl) chunk).fabric_computeInitialSyncChanges(player, changes::add);

		if (!changes.isEmpty()) {
			AttachmentChange.partitionAndSendPackets(changes, player);
		}
	}
}
