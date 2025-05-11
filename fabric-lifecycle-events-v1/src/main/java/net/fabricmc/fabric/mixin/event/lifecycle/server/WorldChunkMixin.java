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

package net.fabricmc.fabric.mixin.event.lifecycle.server;

import java.util.Map;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;

/**
 * This is a server only mixin for good reason:
 * Since all block entity tracking is now on the world chunk, we inject into WorldChunk.
 * In order to prevent client logic from being loaded due to the mixin, we have a mixin for the client and this one for the server.
 */
@Mixin(WorldChunk.class)
abstract class WorldChunkMixin {
	@Shadow
	public abstract World getWorld();

	@ModifyExpressionValue(
			method = "setBlockEntity",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
	)
	private <V> V onLoadBlockEntity(V removedBlockEntity, BlockEntity blockEntity) {
		// Only fire the load event if the block entity has actually changed
		if (blockEntity != null && blockEntity != removedBlockEntity) {
			if (this.getWorld() instanceof ServerWorld) {
				ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(blockEntity, (ServerWorld) this.getWorld());
			}
		}

		return removedBlockEntity;
	}

	@Inject(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;markRemoved()V", shift = At.Shift.AFTER))
	private void onRemoveBlockEntity(BlockEntity blockEntity, CallbackInfo info, @Local(ordinal = 1) BlockEntity removedBlockEntity) {
		if (this.getWorld() instanceof ServerWorld) {
			ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removedBlockEntity, (ServerWorld) this.getWorld());
		}
	}

	// Use the slice to not redirect codepath where block entity is loaded
	@Redirect(method = "getBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/WorldChunk$CreationType;)Lnet/minecraft/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"),
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;createBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;")))
	private <K, V> Object onRemoveBlockEntity(Map<K, V> map, K key) {
		@Nullable final V removed = map.remove(key);

		if (removed != null && this.getWorld() instanceof ServerWorld) {
			ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload((BlockEntity) removed, (ServerWorld) this.getWorld());
		}

		return removed;
	}

	@Inject(method = "removeBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;markRemoved()V"))
	private void onRemoveBlockEntity(BlockPos pos, CallbackInfo ci, @Local @Nullable BlockEntity removed) {
		if (removed != null && this.getWorld() instanceof ServerWorld) {
			ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removed, (ServerWorld) this.getWorld());
		}
	}
}
