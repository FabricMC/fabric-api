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
import net.fabricmc.fabric.impl.event.lifecycle.ChunkLevelTypeEventTracker;

@Mixin(ChunkStatusTasks.class)
abstract class ChunkStatusTasksMixin {
	@Unique
	private static final FullChunkStatus[] fabric_CHUNK_LEVEL_TYPES = FullChunkStatus.values(); // values() clones the internal array each call, so cache the return

	@Inject(method = "method_60553", at = @At("TAIL"))
	private static void onChunkLoad(ChunkAccess chunk, WorldGenContext chunkGenerationContext, GenerationChunkHolder chunkHolder, CallbackInfoReturnable<ChunkAccess> callbackInfoReturnable) {
		LevelChunk worldChunk = (LevelChunk) callbackInfoReturnable.getReturnValue();

		// We fire the event at TAIL since the chunk is guaranteed to be a WorldChunk then.
		ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(chunkGenerationContext.level(), worldChunk);

		if (!(chunk instanceof ImposterProtoChunk)) {
			ServerChunkEvents.CHUNK_GENERATE.invoker().onChunkGenerate(chunkGenerationContext.level(), worldChunk);
		}

		// Handles the case where the chunk becomes accessible from being completed unloaded, only fires if chunkHolder has been set to at least that level type
		ChunkLevelTypeEventTracker levelTypeTracker = (ChunkLevelTypeEventTracker) chunkHolder;

		for (int i = levelTypeTracker.fabric_getCurrentEventLevelType().ordinal(); i < chunkHolder.getFullStatus().ordinal(); i++) {
			FullChunkStatus oldLevelType = fabric_CHUNK_LEVEL_TYPES[i];
			FullChunkStatus newLevelType = fabric_CHUNK_LEVEL_TYPES[i+1];
			ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange(chunkGenerationContext.level(), worldChunk, oldLevelType, newLevelType);
			levelTypeTracker.fabric_setCurrentEventLevelType(newLevelType);
		}
	}
}
