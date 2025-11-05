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

package net.fabricmc.fabric.mixin.event.lifecycle.client;

import java.util.Map;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;

@Mixin(LevelChunk.class)
abstract class LevelChunkMixin {
	@Shadow
	public abstract Level getLevel();

	@ModifyExpressionValue(
			method = "setBlockEntity",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
	)
	private <V> V onLoadBlockEntity(V removedBlockEntity, BlockEntity blockEntity) {
		// Only fire the load event if the block entity has actually changed
		if (blockEntity != null && blockEntity != removedBlockEntity) {
			if (this.getLevel() instanceof ServerLevel) {
				ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(blockEntity, (ServerLevel) this.getLevel());
			} else if (this.getLevel() instanceof ClientLevel) {
				ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(blockEntity, (ClientLevel) this.getLevel());
			}
		}

		return removedBlockEntity;
	}

	@Inject(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;setRemoved()V", shift = At.Shift.AFTER))
	private void onRemoveBlockEntity(BlockEntity blockEntity, CallbackInfo info, @Local(ordinal = 1) BlockEntity removedBlockEntity) {
		if (removedBlockEntity != null) {
			if (this.getLevel() instanceof ServerLevel) {
				ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removedBlockEntity, (ServerLevel) this.getLevel());
			} else if (this.getLevel() instanceof ClientLevel) {
				ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removedBlockEntity, (ClientLevel) this.getLevel());
			}
		}
	}

	// Use the slice to not redirect codepath where block entity is loaded
	@Redirect(method = "getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"),
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;createBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;")))
	private <K, V> Object onRemoveBlockEntity(Map<K, V> map, K key) {
		@Nullable final V removed = map.remove(key);

		if (removed != null) {
			if (this.getLevel() instanceof ServerLevel) {
				ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload((BlockEntity) removed, (ServerLevel) this.getLevel());
			} else if (this.getLevel() instanceof ClientLevel) {
				ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload((BlockEntity) removed, (ClientLevel) this.getLevel());
			}
		}

		return removed;
	}

	@Inject(method = "removeBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;setRemoved()V"))
	private void onRemoveBlockEntity(BlockPos pos, CallbackInfo ci, @Local @Nullable BlockEntity removed) {
		if (removed != null) {
			if (this.getLevel() instanceof ServerLevel) {
				ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removed, (ServerLevel) this.getLevel());
			} else if (this.getLevel() instanceof ClientLevel) {
				ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removed, (ClientLevel) this.getLevel());
			}
		}
	}
}
