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

package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;

import net.fabricmc.fabric.impl.client.rendering.ColorResolverRegistryImpl;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	// Do not use the vanilla map because it is an Object2ObjectArrayMap. Array maps have O(n) retrievals compared to
	// hash maps' O(1) retrievals. If many custom ColorResolvers are registered, this may have a non-negligible
	// performance impact.
	@Unique
	private final Reference2ReferenceMap<ColorResolver, BlockTintCache> customColorCache = ColorResolverRegistryImpl.createCustomCacheMap(resolver -> new BlockTintCache(pos -> calculateBlockTint(pos, resolver)));

	@Shadow
	public abstract int calculateBlockTint(BlockPos pos, ColorResolver colorResolver);

	@Inject(method = "onChunkLoaded(Lnet/minecraft/world/level/ChunkPos;)V", at = @At("RETURN"))
	private void onResetChunkColor(ChunkPos chunkPos, CallbackInfo ci) {
		for (BlockTintCache cache : customColorCache.values()) {
			cache.invalidateForChunk(chunkPos.x, chunkPos.z);
		}
	}

	@Inject(method = "clearTintCaches()V", at = @At("RETURN"))
	private void onReloadColor(CallbackInfo ci) {
		for (BlockTintCache cache : customColorCache.values()) {
			cache.invalidateAll();
		}
	}

	@ModifyExpressionValue(method = "getBlockTint(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/ColorResolver;)I", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2ObjectArrayMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object modifyNullCache(/* BiomeColorCache */ Object cache, BlockPos pos, ColorResolver resolver) {
		if (cache == null) {
			cache = customColorCache.get(resolver);

			if (cache == null) {
				throw new UnsupportedOperationException("ClientWorld.getColor called with unregistered ColorResolver " + resolver);
			}
		}

		return cache;
	}
}
