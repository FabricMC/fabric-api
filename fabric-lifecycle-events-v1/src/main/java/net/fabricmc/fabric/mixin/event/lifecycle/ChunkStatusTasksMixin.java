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

package net.fabricmc.fabric.mixin.event.lifecycle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.WorldGenContext;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.impl.event.lifecycle.FullChunkStatusEventTracker;

@Mixin(ChunkStatusTasks.class)
abstract class ChunkStatusTasksMixin {
	@Unique
	private static final FullChunkStatus[] fabric_FULL_CHUNK_STATUSES = FullChunkStatus.values(); // values() clones the internal array each call, so cache the return

	@Inject(method = "lambda$full$0", at = @At("TAIL"))
	private static void onChunkLoad(ChunkAccess chunk, WorldGenContext worldGenContext, GenerationChunkHolder chunkHolder, CallbackInfoReturnable<ChunkAccess> callbackInfoReturnable) {
		LevelChunk levelChunk = (LevelChunk) callbackInfoReturnable.getReturnValue();

		boolean generated = !(chunk instanceof ImposterProtoChunk);

		// We fire the event at TAIL since the chunk is guaranteed to be a LevelChunk then.
		ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(worldGenContext.level(), levelChunk, generated);

		if (generated) {
			ServerChunkEvents.CHUNK_GENERATE.invoker().onChunkGenerate(worldGenContext.level(), levelChunk);
		}

		// Handles the case where the chunk becomes accessible from being completely unloaded, only fires if chunkHolder has been set to at least that full chunk status
		FullChunkStatusEventTracker chunkStatusTracker = (FullChunkStatusEventTracker) chunkHolder;

		for (int i = chunkStatusTracker.fabric_getCurrentEventFullChunkStatus().ordinal(); i < chunkHolder.getFullStatus().ordinal(); i++) {
			FullChunkStatus oldStatus = fabric_FULL_CHUNK_STATUSES[i];
			FullChunkStatus newStatus = fabric_FULL_CHUNK_STATUSES[i+1];
			ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.invoker().onFullChunkStatusChange(worldGenContext.level(), levelChunk, oldStatus, newStatus);
			chunkStatusTracker.fabric_setCurrentEventFullChunkStatus(newStatus);
		}
	}
}
