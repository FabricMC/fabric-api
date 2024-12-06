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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentEntrypoint;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;

@Mixin(Chunk.class)
abstract class ChunkMixin implements AttachmentTargetImpl {
	@Shadow
	@Final
	protected ChunkPos pos;

	@Shadow
	public abstract ChunkStatus getStatus();

	@Shadow
	public abstract ChunkPos getPos();

	@Shadow
	public abstract boolean needsSaving();

	@Override
	public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
		return new AttachmentTargetInfo.ChunkTarget(this.pos);
	}

	@Override
	public void fabric_markChanged(AttachmentType<?> type) {
		needsSaving();

		if (type.isPersistent() && this.getStatus().equals(ChunkStatus.EMPTY)) {
			AttachmentEntrypoint.LOGGER.warn(
					"Attaching persistent attachment {} to chunk {} with chunk status EMPTY. Attachment might be discarded.",
					type.identifier(),
					this.getPos()
			);
		}
	}

	@Override
	public boolean fabric_shouldTryToSync() {
		// ProtoChunk or EmptyChunk
		return false;
	}

	@Override
	public DynamicRegistryManager fabric_getDynamicRegistryManager() {
		// Should never happen as this is only used for sync
		throw new UnsupportedOperationException("Chunk does not have a DynamicRegistryManager.");
	}
}
