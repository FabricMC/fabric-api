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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerating;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

@Mixin(ChunkGenerating.class)
abstract class ChunkGeneratingMixin {
	@Inject(method = "method_60553", at = @At("TAIL"))
	private static void onChunkLoad(Chunk chunk, ChunkGenerationContext chunkGenerationContext, AbstractChunkHolder chunkHolder, CallbackInfoReturnable<Chunk> callbackInfoReturnable) {
		// We fire the event at TAIL since the chunk is guaranteed to be a WorldChunk then.
		ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(chunkGenerationContext.world(), (WorldChunk) callbackInfoReturnable.getReturnValue());

		if (!(chunk instanceof WrapperProtoChunk)) {
			ServerChunkEvents.CHUNK_GENERATE.invoker().onChunkGenerate(chunkGenerationContext.world(), (WorldChunk) callbackInfoReturnable.getReturnValue());
		}
	}
}
