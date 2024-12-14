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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;

@Mixin(EntityTrackerEntry.class)
abstract class EntityTrackerEntryMixin {
	@Shadow
	@Final
	private Entity entity;

	@Inject(
			method = "startTracking",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;onStartedTrackingBy(Lnet/minecraft/server/network/ServerPlayerEntity;)V"
			)
	)
	private void syncAttachmentsAfterSpawn(ServerPlayerEntity player, CallbackInfo ci) {
		// mixin because the START_TRACKING event triggers before the spawn packet is sent to the client,
		// whereas we want to modify the entity on the client
		List<AttachmentChange> changes = new ArrayList<>();
		((AttachmentTargetImpl) this.entity).fabric_computeInitialSyncChanges(player, changes::add);

		if (!changes.isEmpty()) {
			AttachmentChange.partitionAndSendPackets(changes, player);
		}
	}
}
