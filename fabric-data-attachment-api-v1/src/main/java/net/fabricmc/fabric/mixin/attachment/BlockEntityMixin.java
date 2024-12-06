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

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.fabricmc.fabric.impl.attachment.sync.s2c.AttachmentSyncPayloadS2C;

@Mixin(BlockEntity.class)
abstract class BlockEntityMixin implements AttachmentTargetImpl {
	@Shadow
	@Final
	protected BlockPos pos;
	@Shadow
	@Nullable
	protected World world;

	@Shadow
	public abstract void markDirty();

	@Shadow
	public abstract boolean hasWorld();

	@Inject(
			method = "read",
			at = @At("RETURN")
	)
	private void readBlockEntityAttachments(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
		this.fabric_readAttachmentsFromNbt(nbt, registryLookup);
	}

	@Inject(
			method = "createNbt",
			at = @At("RETURN")
	)
	private void writeBlockEntityAttachments(RegistryWrapper.WrapperLookup wrapperLookup, CallbackInfoReturnable<NbtCompound> cir) {
		this.fabric_writeAttachmentsToNbt(cir.getReturnValue(), wrapperLookup);
	}

	@Override
	public void fabric_markChanged(AttachmentType<?> type) {
		this.markDirty();
	}

	@Override
	public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
		return new AttachmentTargetInfo.BlockEntityTarget(this.pos);
	}

	@Override
	public void fabric_syncChange(AttachmentType<?> type, AttachmentSyncPayloadS2C payload) {
		PlayerLookup.tracking((BlockEntity) (Object) this)
				.forEach(player -> {
					if (((AttachmentTypeImpl<?>) type).syncPredicate().test(this, player)) {
						AttachmentSync.trySync(payload, player);
					}
				});
	}

	@Override
	public boolean fabric_shouldTryToSync() {
		// Persistent attachments are read at a time with no world
		return !this.hasWorld() || !this.world.isClient();
	}

	@Override
	public DynamicRegistryManager fabric_getDynamicRegistryManager() {
		return this.world.getRegistryManager();
	}
}
