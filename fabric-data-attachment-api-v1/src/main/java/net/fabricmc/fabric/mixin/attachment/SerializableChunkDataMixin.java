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

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;

@Mixin(SerializableChunkData.class)
abstract class SerializableChunkDataMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("SerializableChunkDataMixin");

	// Adding a mutable record field like this is likely a bad idea, but I cannot see a better way.
	@Unique
	@Nullable
	private CompoundTag attachmentNbtData;

	@Inject(method = "parse", at = @At("RETURN"))
	private static void storeAttachmentNbtData(LevelHeightAccessor heightLimitView, PalettedContainerFactory arg, CompoundTag nbt, CallbackInfoReturnable<SerializableChunkData> cir, @Share("attachmentDataNbt") LocalRef<CompoundTag> attachmentDataNbt) {
		final SerializableChunkData serializer = cir.getReturnValue();

		if (serializer == null) {
			return;
		}

		//noinspection SimplifyOptionalCallChains
		CompoundTag attachmentNbtData = nbt.getCompound(AttachmentTarget.NBT_ATTACHMENT_KEY).orElse(null);

		if (attachmentNbtData != null) {
			((SerializableChunkDataMixin) (Object) serializer).attachmentNbtData = attachmentNbtData;
		}
	}

	@Inject(method = "read", at = @At("RETURN"))
	private void setAttachmentDataInChunk(ServerLevel serverWorld, PoiManager pointOfInterestStorage, RegionStorageInfo storageKey, ChunkPos chunkPos, CallbackInfoReturnable<ProtoChunk> cir) {
		ProtoChunk chunk = cir.getReturnValue();

		if (chunk != null && attachmentNbtData != null) {
			var nbt = new CompoundTag();
			nbt.put(AttachmentTarget.NBT_ATTACHMENT_KEY, attachmentNbtData);

			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
				ValueInput readView = TagValueInput.create(reporter, serverWorld.registryAccess(), nbt);
				((AttachmentTargetImpl) chunk).fabric_readAttachmentsFromNbt(readView);
			}
		}
	}

	@Inject(method = "copyOf", at = @At("RETURN"))
	private static void storeAttachmentNbtData(ServerLevel world, ChunkAccess chunk, CallbackInfoReturnable<SerializableChunkData> cir) {
		try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
			TagValueOutput writeView = TagValueOutput.createWithContext(reporter, world.registryAccess());
			((AttachmentTargetImpl) chunk).fabric_writeAttachmentsToNbt(writeView);

			//noinspection SimplifyOptionalCallChains
			CompoundTag attachmentNbtData = writeView.buildResult().getCompound(AttachmentTarget.NBT_ATTACHMENT_KEY).orElse(null);

			if (attachmentNbtData != null) {
				((SerializableChunkDataMixin) (Object) cir.getReturnValue()).attachmentNbtData = attachmentNbtData;
			}
		}
	}

	@Inject(method = "write", at = @At("RETURN"))
	private void writeChunkAttachments(CallbackInfoReturnable<CompoundTag> cir) {
		if (attachmentNbtData != null) {
			cir.getReturnValue().put(AttachmentTarget.NBT_ATTACHMENT_KEY, attachmentNbtData);
		}
	}
}
