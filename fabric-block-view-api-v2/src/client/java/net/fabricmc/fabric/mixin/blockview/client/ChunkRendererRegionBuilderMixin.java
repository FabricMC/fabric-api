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

package net.fabricmc.fabric.mixin.blockview.client;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.fabric.impl.blockview.client.RenderDataMapConsumer;

@Mixin(ChunkRendererRegionBuilder.class)
public abstract class ChunkRendererRegionBuilderMixin {
	@Unique
	private static final AtomicInteger ERROR_COUNTER = new AtomicInteger();
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger(ChunkRendererRegionBuilderMixin.class);

	@Inject(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkRendererRegionBuilder;getRenderedChunk(Lnet/minecraft/world/World;III)Lnet/minecraft/client/render/chunk/RenderedChunk;"))
	private void copyDataForChunk(World world, long packedChunkPos, CallbackInfoReturnable<ChunkRendererRegion> cir, @Share("dataMap") LocalRef<Long2ObjectOpenHashMap<Object>> mapRef, @Local(ordinal = 11) int x, @Local(ordinal = 10) int y, @Local(ordinal = 9) int z) {
		// Hash maps in chunks should generally not be modified outside of client thread
		// but does happen in practice, due to mods or inconsistent vanilla behaviors, causing
		// CMEs when we iterate the map. (Vanilla does not iterate these maps when it builds
		// the chunk cache and does not suffer from this problem.)
		//
		// We handle this simply by retrying until it works. Ugly but effective.
		while (true) {
			try {
				mapRef.set(mapChunk(world.getChunk(x, z), ChunkSectionPos.from(packedChunkPos), mapRef.get()));
				break;
			} catch (ConcurrentModificationException e) {
				final int count = ERROR_COUNTER.incrementAndGet();

				if (count <= 5) {
					LOGGER.warn("[Block Entity Render Data] Encountered CME during render region build. A mod is accessing or changing chunk data outside the main thread. Retrying.", e);

					if (count == 5) {
						LOGGER.info("[Block Entity Render Data] Subsequent exceptions will be suppressed.");
					}
				}
			}
		}
	}

	@Inject(method = "build", at = @At(value = "RETURN"))
	private void createDataMap(World world, long l, CallbackInfoReturnable<ChunkRendererRegion> cir, @Share("dataMap") LocalRef<Long2ObjectOpenHashMap<Object>> mapRef) {
		ChunkRendererRegion rendererRegion = cir.getReturnValue();
		Long2ObjectOpenHashMap<Object> map = mapRef.get();

		if (map != null) {
			((RenderDataMapConsumer) rendererRegion).fabric_acceptRenderDataMap(map);
		}
	}

	@Unique
	private static Long2ObjectOpenHashMap<Object> mapChunk(WorldChunk chunk, ChunkSectionPos chunkSectionPos, Long2ObjectOpenHashMap<Object> map) {
		// Skip the math below if the chunk contains no block entities
		if (chunk.getBlockEntities().isEmpty()) {
			return map;
		}

		final int xMin = ChunkSectionPos.getBlockCoord(chunkSectionPos.getSectionX() - 1);
		final int yMin = ChunkSectionPos.getBlockCoord(chunkSectionPos.getSectionY() - 1);
		final int zMin = ChunkSectionPos.getBlockCoord(chunkSectionPos.getSectionZ() - 1);
		final int xMax = ChunkSectionPos.getBlockCoord(chunkSectionPos.getSectionX() + 1);
		final int yMax = ChunkSectionPos.getBlockCoord(chunkSectionPos.getSectionY() + 1);
		final int zMax = ChunkSectionPos.getBlockCoord(chunkSectionPos.getSectionZ() + 1);

		for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
			final BlockPos pos = entry.getKey();

			if (pos.getX() >= xMin && pos.getX() <= xMax
					&& pos.getY() >= yMin && pos.getY() <= yMax
					&& pos.getZ() >= zMin && pos.getZ() <= zMax) {
				final Object data = entry.getValue().getRenderData();

				if (data != null) {
					if (map == null) {
						map = new Long2ObjectOpenHashMap<>();
					}

					map.put(pos.asLong(), data);
				}
			}
		}

		return map;
	}
}
