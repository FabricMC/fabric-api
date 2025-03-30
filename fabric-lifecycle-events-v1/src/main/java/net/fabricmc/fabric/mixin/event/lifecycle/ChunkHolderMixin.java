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

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;

import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.server.world.ChunkLevelType.*;
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

	/**
	 * Really means decrease level (chunk load type promotion)
	 */
	@Inject(method = "increaseLevel", at = @At("TAIL"))
	private void increaseLevel(ServerChunkLoadingManager chunkLoadingManager, CompletableFuture<OptionalChunk<WorldChunk>> chunkFuture, Executor executor, ChunkLevelType target, CallbackInfo ci) {
		ServerWorld serverWorld = (ServerWorld) world;
		MinecraftServer server = serverWorld.getServer();
		ChunkLevelType previous = switch (target) {
			case INACCESSIBLE -> throw new IllegalStateException("target must at least be FULL");
			case FULL -> INACCESSIBLE;
			case BLOCK_TICKING -> FULL;
			case ENTITY_TICKING -> BLOCK_TICKING;
		};
		Runnable runnable = () -> ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange(serverWorld, this.pos, previous, target);
		chunkFuture.thenAccept(optionalChunk -> optionalChunk.ifPresent(worldChunk -> {
			if (!server.isOnThread()) {
				LoggerFactory.getLogger(ChunkHolderMixin.class).warn("{} {} -> {} INITIALLY NOT ON SERVER THREAD", this.pos, previous, target); // temp check
				server.send(new ServerTask(server.getTicks()-10, runnable)); // ensure the server thread runs it within this tick
			}
			else runnable.run();
		}));
	}

	/**
	 * Really means increase level (chunk load type demotion)
	 */
	@Inject(method = "decreaseLevel", at = @At("TAIL"))
	private void decreaseLevel(ServerChunkLoadingManager chunkLoadingManager, ChunkLevelType target, CallbackInfo ci) {
		ChunkLevelType previous = ChunkLevels.getType(this.lastTickLevel);
		if (previous.isAfter(ENTITY_TICKING) && !target.isAfter(ENTITY_TICKING)) ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange((ServerWorld) world, this.pos, ENTITY_TICKING, BLOCK_TICKING);
		if (previous.isAfter(BLOCK_TICKING) && !target.isAfter(BLOCK_TICKING)) ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange((ServerWorld) world, this.pos, BLOCK_TICKING, FULL);
		if (previous.isAfter(FULL) && !target.isAfter(FULL)) ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange((ServerWorld) world, this.pos, FULL, INACCESSIBLE);
	}
}
