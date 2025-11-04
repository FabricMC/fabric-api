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

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
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
	private void readBlockEntityAttachments(ValueInput view, CallbackInfo ci) {
		this.fabric_readAttachmentsFromNbt(view);
	}

	@Inject(
			method = "saveWithoutMetadata(Lnet/minecraft/world/level/storage/ValueOutput;)V",
			at = @At(value = "TAIL")
	)
	private void writeBlockEntityAttachments(ValueOutput view, CallbackInfo ci) {
		this.fabric_writeAttachmentsToNbt(view);
	}

	@Override
	public void fabric_markChanged(AttachmentType<?> type) {
		this.setChanged();
	}

	@Override
	public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
		return new AttachmentTargetInfo.BlockEntityTarget(this.worldPosition);
	}

	@Override
	public void fabric_syncChange(AttachmentType<?> type, AttachmentChange change) {
		PlayerLookup.tracking((BlockEntity) (Object) this)
				.forEach(player -> {
					if (((AttachmentTypeImpl<?>) type).syncPredicate().test(this, player)) {
						AttachmentSync.trySync(change, player);
					}
				});
	}

	@Override
	public boolean fabric_shouldTryToSync() {
		// Persistent attachments are read at a time with no world
		return !this.hasLevel() || !this.level.isClientSide();
	}

	@Override
	public RegistryAccess fabric_getDynamicRegistryManager() {
		return this.level.registryAccess();
	}
}
