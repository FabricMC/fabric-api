package net.fabricmc.fabric.mixin.event.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
