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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin extends AbstractChunkHolder {
	@Shadow
	@Final
	private HeightLimitView world;

	@Shadow
	private int lastTickLevel;

	private ChunkHolderMixin(ChunkPos pos) {
		super(pos);
	}

	@Inject(method = "increaseLevel", at = @At("TAIL"))
	private void increaseLevel(ServerChunkLoadingManager chunkLoadingManager, CompletableFuture<OptionalChunk<WorldChunk>> chunkFuture, Executor executor, ChunkLevelType target, CallbackInfo ci) {
		ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange((ServerWorld) world, this.pos, ChunkLevels.getType(this.lastTickLevel), target);
	}

	@Inject(method = "decreaseLevel", at = @At("TAIL"))
	private void decreaseLevel(ServerChunkLoadingManager chunkLoadingManager, ChunkLevelType target, CallbackInfo ci) {
		ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange((ServerWorld) world, this.pos, ChunkLevels.getType(this.lastTickLevel), target);
	}
}
