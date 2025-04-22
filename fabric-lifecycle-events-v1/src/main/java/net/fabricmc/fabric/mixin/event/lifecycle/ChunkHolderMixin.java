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
import static net.minecraft.server.world.ChunkLevelType.ENTITY_TICKING;
import static net.minecraft.server.world.ChunkLevelType.FULL;
import static net.minecraft.server.world.ChunkLevelType.INACCESSIBLE;

import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.impl.event.lifecycle.ChunkLevelTypeEventTracker;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin extends AbstractChunkHolder implements ChunkLevelTypeEventTracker {
	@Shadow
	@Final
	private HeightLimitView world;

	@Shadow
	private int lastTickLevel;

	@Unique
	private static final ChunkLevelType[] fabric_CHUNK_LEVEL_TYPES = ChunkLevelType.values(); // values() clones the internal array each call, so cache the return

	@Unique
	private ChunkLevelType fabric_currentEventLevelType = INACCESSIBLE;

	private ChunkHolderMixin(ChunkPos pos) {
		super(pos);
	}

	/**
	 * Handles INACCESSIBLE -> FULL for chunks that are immediately loaded and available. {@link ChunkGeneratingMixin} handles the rest.
	 */
	@Inject(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;combineSavingFuture(Ljava/util/concurrent/CompletableFuture;)V", shift = At.Shift.AFTER, ordinal = 0))
	private void updateFutures$inaccessibleToFull(ServerChunkLoadingManager chunkLoadingManager, Executor executor, CallbackInfo ci) {
		if (this.getUncheckedOrNull(ChunkStatus.FULL) instanceof WorldChunk && this.fabric_currentEventLevelType == INACCESSIBLE) { // prevent duplicate events with ChunkGeneratingMixin
			ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange((ServerWorld) world, (WorldChunk) this.getUncheckedOrNull(ChunkStatus.FULL), INACCESSIBLE, FULL);
			this.fabric_currentEventLevelType = FULL;
		}
	}

	/**
	 * Handles FULL -> BLOCK_TICKING.
	 */
	@Inject(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;combineSavingFuture(Ljava/util/concurrent/CompletableFuture;)V", shift = At.Shift.AFTER, ordinal = 1))
	private void updateFutures$fullToBlockTicking(ServerChunkLoadingManager chunkLoadingManager, Executor executor, CallbackInfo ci) {
		if (fabric_currentEventLevelType == FULL) { // if INACCESSIBLE->FULL did not fire immediately, then ChunkGeneratingMixin will handle this later.
			ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange((ServerWorld) world, (WorldChunk) this.getUncheckedOrNull(ChunkStatus.FULL), FULL, BLOCK_TICKING);
			this.fabric_currentEventLevelType = BLOCK_TICKING;
		}
	}

	/**
	 * Handles BLOCK_TICKING -> ENTITY_TICKING.
	 */
	@Inject(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;combineSavingFuture(Ljava/util/concurrent/CompletableFuture;)V", shift = At.Shift.AFTER, ordinal = 2))
	private void updateFutures$blockTickingToEntityTicking(ServerChunkLoadingManager chunkLoadingManager, Executor executor, CallbackInfo ci) {
		if (fabric_currentEventLevelType == BLOCK_TICKING) { // if INACCESSIBLE->FULL->BLOCK_TICKING did not fire immediately, then ChunkGeneratingMixin will handle this later.
			ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange((ServerWorld) world, (WorldChunk) this.getUncheckedOrNull(ChunkStatus.FULL), BLOCK_TICKING, ENTITY_TICKING);
			this.fabric_currentEventLevelType = ENTITY_TICKING;
		}
	}

	/**
	 * Really means increase level (chunk load type demotion). Fire right before onChunkStatusChange() is called.
	 */
	@Inject(method = "decreaseLevel", at = @At("HEAD"))
	private void decreaseLevel(ServerChunkLoadingManager chunkLoadingManager, ChunkLevelType target, CallbackInfo ci) {
		ChunkLevelType previous = ChunkLevels.getType(this.lastTickLevel);
		ServerWorld serverWorld = (ServerWorld) world;

		for (int i = previous.ordinal(); i > target.ordinal(); i--) {
			ChunkLevelType oldLevelType = fabric_CHUNK_LEVEL_TYPES[i];
			ChunkLevelType newLevelType = fabric_CHUNK_LEVEL_TYPES[i-1];
			if (this.fabric_currentEventLevelType.isAfter(oldLevelType)) { // if a promotion event got cancelled or never finished, then do _not_ fire an equivalent demotion event
				ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.invoker().onChunkLevelTypeChange(serverWorld, (WorldChunk) this.getUncheckedOrNull(ChunkStatus.FULL), oldLevelType, newLevelType);
				this.fabric_currentEventLevelType = newLevelType;
			}
		}
	}

	@Override
	public void fabric_setCurrentEventLevelType(ChunkLevelType levelType) {
		this.fabric_currentEventLevelType = levelType;
	}

	@Override
	public ChunkLevelType fabric_getCurrentEventLevelType() {
		return this.fabric_currentEventLevelType;
	}
}
