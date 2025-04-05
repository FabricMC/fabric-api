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

import static net.minecraft.server.world.ChunkLevelType.BLOCK_TICKING;
import static net.minecraft.server.world.ChunkLevelType.FULL;
import static net.minecraft.server.world.ChunkLevelType.INACCESSIBLE;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
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

	@Unique
	private static final ChunkLevelType[] CHUNK_LEVEL_TYPES = ChunkLevelType.values(); // values() clones the internal array each call, so cache the return

	private ChunkHolderMixin(ChunkPos pos) {
		super(pos);
	}

	/**
	 * Really means decrease level (chunk load type promotion).
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

		Runnable runnable = () -> {
			if (this.getLevelType().isAfter(target)) { // If chunk level type got demoted before promotion event fires, then don't fire it. This almost never happens tho.
				ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange(serverWorld, this.pos, previous, target);
			}
		};

		chunkFuture.thenAccept(optionalChunk -> optionalChunk.ifPresent(worldChunk -> {
			if (!server.isOnThread()) {
				server.send(new ServerTask(server.getTicks()-10, runnable)); // ensure the server thread runs it within this tick
			} else {
				runnable.run();
			}
		}));
	}

	/**
	 * Really means increase level (chunk load type demotion).
	 */
	@Inject(method = "decreaseLevel", at = @At("TAIL"))
	private void decreaseLevel(ServerChunkLoadingManager chunkLoadingManager, ChunkLevelType target, CallbackInfo ci) {
		ChunkLevelType previous = ChunkLevels.getType(this.lastTickLevel);
		ServerWorld serverWorld = (ServerWorld) world;

		for (int i = previous.ordinal(); i > target.ordinal(); i--) {
			ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange(serverWorld, this.pos, CHUNK_LEVEL_TYPES[i], CHUNK_LEVEL_TYPES[i-1]);
		}
	}
}
