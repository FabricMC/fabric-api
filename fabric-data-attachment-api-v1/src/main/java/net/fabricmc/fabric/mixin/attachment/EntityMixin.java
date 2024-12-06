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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.fabricmc.fabric.impl.attachment.sync.s2c.AttachmentSyncPayloadS2C;

@Mixin(Entity.class)
abstract class EntityMixin implements AttachmentTargetImpl {
	@Shadow
	private int id;

	@Shadow
	public abstract World getWorld();

	@Inject(
			at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"),
			method = "readNbt"
	)
	private void readEntityAttachments(NbtCompound nbt, CallbackInfo cir) {
		this.fabric_readAttachmentsFromNbt(nbt, getWorld().getRegistryManager());
	}

	@Inject(
			at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"),
			method = "writeNbt"
	)
	private void writeEntityAttachments(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		this.fabric_writeAttachmentsToNbt(nbt, getWorld().getRegistryManager());
	}

	@Override
	public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
		return new AttachmentTargetInfo.EntityTarget(this.id);
	}

	@Override
	public void fabric_syncChange(AttachmentType<?> type, AttachmentSyncPayloadS2C payload) {
		if (!this.getWorld().isClient()) {
			AttachmentSyncPredicate predicate = ((AttachmentTypeImpl<?>) type).syncPredicate();

			if ((Object) this instanceof ServerPlayerEntity self && predicate.test(this, self)) {
				// Players do not track themselves
				AttachmentSync.trySync(payload, self);
			}

			PlayerLookup.tracking((Entity) (Object) this)
					.forEach(player -> {
						if (predicate.test(this, player)) {
							AttachmentSync.trySync(payload, player);
						}
					});
		}
	}

	@Override
	public boolean fabric_shouldTryToSync() {
		return !this.getWorld().isClient();
	}

	@Override
	public DynamicRegistryManager fabric_getDynamicRegistryManager() {
		return this.getWorld().getRegistryManager();
	}
}
