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

import static net.minecraft.server.level.FullChunkStatus.BLOCK_TICKING;
import static net.minecraft.server.level.FullChunkStatus.ENTITY_TICKING;
import static net.minecraft.server.level.FullChunkStatus.FULL;
import static net.minecraft.server.level.FullChunkStatus.INACCESSIBLE;

import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.impl.event.lifecycle.FullChunkStatusEventTracker;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin extends GenerationChunkHolder implements FullChunkStatusEventTracker {
	@Shadow
	@Final
	private LevelHeightAccessor levelHeightAccessor;

	@Shadow
	private int oldTicketLevel;

	@Unique
	private static final FullChunkStatus[] fabric_FULL_CHUNK_STATUSES = FullChunkStatus.values(); // values() clones the internal array each call, so cache the return

	@Unique
	private FullChunkStatus fabric_currentEventFullChunkStatus = INACCESSIBLE;

	private ChunkHolderMixin(ChunkPos pos) {
		super(pos);
	}

	/**
	 * Handles INACCESSIBLE -> FULL for chunks that are immediately loaded and available. {@link ChunkStatusTasksMixin} handles the rest.
	 */
	@Inject(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;addSaveDependency(Ljava/util/concurrent/CompletableFuture;)V", shift = At.Shift.AFTER, ordinal = 0))
	private void updateFutures$inaccessibleToFull(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
		if (this.getChunkIfPresentUnchecked(ChunkStatus.FULL) instanceof LevelChunk && this.fabric_currentEventFullChunkStatus == INACCESSIBLE) { // prevent duplicate events with ChunkStatusTasksMixin
			ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.invoker().onFullChunkStatusChange((ServerLevel) levelHeightAccessor, (LevelChunk) this.getChunkIfPresentUnchecked(ChunkStatus.FULL), INACCESSIBLE, FULL);
			this.fabric_currentEventFullChunkStatus = FULL;
		}
	}

	/**
	 * Handles FULL -> BLOCK_TICKING.
	 */
	@Inject(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;addSaveDependency(Ljava/util/concurrent/CompletableFuture;)V", shift = At.Shift.AFTER, ordinal = 1))
	private void updateFutures$fullToBlockTicking(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
		if (fabric_currentEventFullChunkStatus == FULL) { // if INACCESSIBLE->FULL did not fire immediately, then ChunkStatusTasksMixin will handle this later.
			ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.invoker().onFullChunkStatusChange((ServerLevel) levelHeightAccessor, (LevelChunk) this.getChunkIfPresentUnchecked(ChunkStatus.FULL), FULL, BLOCK_TICKING);
			this.fabric_currentEventFullChunkStatus = BLOCK_TICKING;
		}
	}

	/**
	 * Handles BLOCK_TICKING -> ENTITY_TICKING.
	 */
	@Inject(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;addSaveDependency(Ljava/util/concurrent/CompletableFuture;)V", shift = At.Shift.AFTER, ordinal = 2))
	private void updateFutures$blockTickingToEntityTicking(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
		if (fabric_currentEventFullChunkStatus == BLOCK_TICKING) { // if INACCESSIBLE->FULL->BLOCK_TICKING did not fire immediately, then ChunkStatusTasksMixin will handle this later.
			ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.invoker().onFullChunkStatusChange((ServerLevel) levelHeightAccessor, (LevelChunk) this.getChunkIfPresentUnchecked(ChunkStatus.FULL), BLOCK_TICKING, ENTITY_TICKING);
			this.fabric_currentEventFullChunkStatus = ENTITY_TICKING;
		}
	}

	/**
	 * Fire right before onFullChunkStatusChange() is called.
	 */
	@Inject(method = "demoteFullChunk", at = @At("HEAD"))
	private void decreaseLevel(ChunkMap chunkMap, FullChunkStatus target, CallbackInfo ci) {
		FullChunkStatus previous = ChunkLevel.fullStatus(this.oldTicketLevel);
		ServerLevel serverLevel = (ServerLevel) levelHeightAccessor;

		for (int i = previous.ordinal(); i > target.ordinal(); i--) {
			FullChunkStatus oldStatus = fabric_FULL_CHUNK_STATUSES[i];
			FullChunkStatus newStatus = fabric_FULL_CHUNK_STATUSES[i-1];
			if (this.fabric_currentEventFullChunkStatus.isOrAfter(oldStatus)) { // if a promotion event got cancelled or never finished, then do _not_ fire an equivalent demotion event
				ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.invoker().onFullChunkStatusChange(serverLevel, (LevelChunk) this.getChunkIfPresentUnchecked(ChunkStatus.FULL), oldStatus, newStatus);
				this.fabric_currentEventFullChunkStatus = newStatus;
			}
		}
	}

	@Override
	public void fabric_setCurrentEventFullChunkStatus(FullChunkStatus chunkStatus) {
		this.fabric_currentEventFullChunkStatus = chunkStatus;
	}

	@Override
	public FullChunkStatus fabric_getCurrentEventFullChunkStatus() {
		return this.fabric_currentEventFullChunkStatus;
	}
}
