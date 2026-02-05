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

import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;

@Mixin(BlockEntity.class)
abstract class BlockEntityMixin implements AttachmentTargetImpl {
	@Shadow
	public abstract void setChanged();

	@Shadow
	@Final
	protected BlockPos worldPosition;

	@Shadow
	public abstract boolean hasLevel();

	@Shadow
	@Nullable
	protected Level level;

	@Inject(
			method = "loadWithComponents",
			at = @At("RETURN")
	)
	private void readBlockEntityAttachments(ValueInput input, CallbackInfo ci) {
		this.fabric_readAttachmentsFromNbt(input);
	}

	@Inject(
			method = "saveWithoutMetadata(Lnet/minecraft/world/level/storage/ValueOutput;)V",
			at = @At(value = "TAIL")
	)
	private void writeBlockEntityAttachments(ValueOutput output, CallbackInfo ci) {
		this.fabric_writeAttachmentsToNbt(output);
	}

	@Override
	public void fabric_markChanged(AttachmentType<?> type) {
		if (this.level instanceof ServerLevel serverLevel) {
			ChunkHolder chunkHolder = serverLevel.getChunkSource().chunkMap.getUpdatingChunkIfPresent(ChunkPos.pack(this.worldPosition));

			// If chunkHolder is null, then chunk is probably unloaded/unloading.
			// calling setChanged() may start loading the chunk again, causing an infinite loop of chunk loading/unloading.
			// so just do nothing.
			if (chunkHolder == null) {
				return;
			}

			CompletableFuture<ChunkResult<LevelChunk>> chunkFuture = chunkHolder.getFullChunkFuture();

			if (chunkFuture.isDone()) {
				// If chunk is already loaded successfully, then call setChanged() immediately
				chunkFuture.thenAccept(chunkResult -> chunkResult.ifSuccess(_ -> this.setChanged()));
			} else {
				// Otherwise setChanged() is called after to avoid deadlocking the server thread
				MinecraftServer server = serverLevel.getServer();
				server.schedule(server.wrapRunnable(() -> fabric_markChanged(type)));
			}
		} else {
			this.setChanged();
		}
	}

	@Override
	public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
		return new AttachmentTargetInfo.BlockEntityTarget(this.worldPosition);
	}

	@Override
	public void fabric_syncChange(AttachmentType<?> type, AttachmentChange change) {
		if (this.level instanceof ServerLevel serverLevel) {
			serverLevel.getChunkSource().blockChanged(this.worldPosition);
		}
	}

	@Override
	public boolean fabric_shouldTryToSync() {
		// Persistent attachments are read at a time with no level
		return !this.hasLevel() || !this.level.isClientSide();
	}

	@Override
	public boolean fabric_shouldDeferSync() {
		return true;
	}

	@Override
	public RegistryAccess fabric_getRegistryAccess() {
		return this.level.registryAccess();
	}
}
