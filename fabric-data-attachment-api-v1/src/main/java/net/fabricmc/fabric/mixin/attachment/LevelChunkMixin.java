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

import java.util.Map;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;

@Mixin(LevelChunk.class)
abstract class LevelChunkMixin extends AttachmentTargetsMixin implements AttachmentTargetImpl {
	@Shadow
	@Final
	Level level;

	@Shadow
	public abstract Map<BlockPos, BlockEntity> getBlockEntities();

	@Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V", at = @At("TAIL"))
	private void transferProtoChunkAttachment(ServerLevel world, ProtoChunk protoChunk, LevelChunk.PostLoadProcessor entityLoader, CallbackInfo ci) {
		AttachmentTargetImpl.transfer(protoChunk, this, false);
	}

	@Override
	public void fabric_computeInitialSyncChanges(ServerPlayer player, Consumer<AttachmentChange> changeOutput) {
		super.fabric_computeInitialSyncChanges(player, changeOutput);

		for (BlockEntity be : this.getBlockEntities().values()) {
			((AttachmentTargetImpl) be).fabric_computeInitialSyncChanges(player, changeOutput);
		}
	}

	@Override
	public void fabric_syncChange(AttachmentType<?> type, AttachmentChange change) {
		if (this.level instanceof ServerLevel serverWorld) {
			// can't shadow from Chunk because this already extends a supermixin
			PlayerLookup.tracking(serverWorld, ((ChunkAccess) (Object) this).getPos())
					.forEach(player -> {
						if (((AttachmentTypeImpl<?>) type).syncPredicate().test(this, player)) {
							AttachmentSync.trySync(change, player);
						}
					});
		}
	}

	@Override
	public boolean fabric_shouldTryToSync() {
		return !this.level.isClientSide();
	}

	@Override
	public RegistryAccess fabric_getDynamicRegistryManager() {
		return level.registryAccess();
	}
}
