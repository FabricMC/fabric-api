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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.SerializedChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;

@Mixin(SerializedChunk.class)
abstract class SerializedChunkMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("SerializedChunkMixin");

	// Adding a mutable record field like this is likely a bad idea, but I cannot see a better way.
	@Unique
	@Nullable
	private NbtCompound attachmentNbtData;

	@Inject(method = "fromNbt", at = @At("RETURN"))
	private static void storeAttachmentNbtData(HeightLimitView heightLimitView, DynamicRegistryManager dynamicRegistryManager, NbtCompound nbt, CallbackInfoReturnable<SerializedChunk> cir, @Share("attachmentDataNbt") LocalRef<NbtCompound> attachmentDataNbt) {
		final SerializedChunk serializer = cir.getReturnValue();

		if (serializer == null) {
			return;
		}

		//noinspection SimplifyOptionalCallChains
		NbtCompound attachmentNbtData = nbt.getCompound(AttachmentTarget.NBT_ATTACHMENT_KEY).orElse(null);

		if (attachmentNbtData != null) {
			((SerializedChunkMixin) (Object) serializer).attachmentNbtData = attachmentNbtData;
		}
	}

	@Inject(method = "convert", at = @At("RETURN"))
	private void setAttachmentDataInChunk(ServerWorld serverWorld, PointOfInterestStorage pointOfInterestStorage, StorageKey storageKey, ChunkPos chunkPos, CallbackInfoReturnable<ProtoChunk> cir) {
		ProtoChunk chunk = cir.getReturnValue();

		if (chunk != null && attachmentNbtData != null) {
			var nbt = new NbtCompound();
			nbt.put(AttachmentTarget.NBT_ATTACHMENT_KEY, attachmentNbtData);

			try (ErrorReporter.Logging reporter = new ErrorReporter.Logging(LOGGER)) {
				ReadView readView = NbtReadView.get(reporter, serverWorld.getRegistryManager(), nbt);
				((AttachmentTargetImpl) chunk).fabric_readAttachmentsFromNbt(readView);
			}
		}
	}

	@Inject(method = "fromChunk", at = @At("RETURN"))
	private static void storeAttachmentNbtData(ServerWorld world, Chunk chunk, CallbackInfoReturnable<SerializedChunk> cir) {
		try (ErrorReporter.Logging reporter = new ErrorReporter.Logging(LOGGER)) {
			NbtWriteView writeView = NbtWriteView.create(reporter, world.getRegistryManager());
			((AttachmentTargetImpl) chunk).fabric_writeAttachmentsToNbt(writeView);

			//noinspection SimplifyOptionalCallChains
			NbtCompound attachmentNbtData = writeView.getNbt().getCompound(AttachmentTarget.NBT_ATTACHMENT_KEY).orElse(null);

			if (attachmentNbtData != null) {
				((SerializedChunkMixin) (Object) cir.getReturnValue()).attachmentNbtData = attachmentNbtData;
			}
		}
	}

	@Inject(method = "serialize", at = @At("RETURN"))
	private void writeChunkAttachments(CallbackInfoReturnable<NbtCompound> cir) {
		if (attachmentNbtData != null) {
			cir.getReturnValue().put(AttachmentTarget.NBT_ATTACHMENT_KEY, attachmentNbtData);
		}
	}
}
