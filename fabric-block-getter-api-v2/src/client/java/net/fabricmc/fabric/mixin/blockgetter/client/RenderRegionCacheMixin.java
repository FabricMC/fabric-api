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

package net.fabricmc.fabric.mixin.blockgetter.client;

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

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import net.fabricmc.fabric.impl.blockgetter.client.RenderDataMapConsumer;

@Mixin(RenderRegionCache.class)
public abstract class RenderRegionCacheMixin {
	@Unique
	private static final AtomicInteger ERROR_COUNTER = new AtomicInteger();
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger(RenderRegionCacheMixin.class);

	@Inject(method = "createRegion", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderRegionCache;getSectionDataCopy(Lnet/minecraft/world/level/Level;III)Lnet/minecraft/client/renderer/chunk/SectionCopy;"))
	private void copyDataForChunk(ClientLevel level, long sectionNode, CallbackInfoReturnable<RenderSectionRegion> cir, @Share("dataMap") LocalRef<Long2ObjectOpenHashMap<Object>> mapRef, @Local(name = "regionSectionX") int regionSectionX, @Local(name = "regionSectionY") int regionSectionY, @Local(name = "regionSectionZ") int regionSectionZ) {
		// Hash maps in chunks should generally not be modified outside of client thread
		// but does happen in practice, due to mods or inconsistent vanilla behaviors, causing
		// CMEs when we iterate the map. (Vanilla does not iterate these maps when it builds
		// the path navigation region and does not suffer from this problem.)
		//
		// We handle this simply by retrying until it works. Ugly but effective.
		while (true) {
			try {
				mapRef.set(mapChunk(level.getChunk(regionSectionX, regionSectionZ), SectionPos.of(sectionNode), mapRef.get()));
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

	@Inject(method = "createRegion", at = @At(value = "RETURN"))
	private void createDataMap(ClientLevel level, long l, CallbackInfoReturnable<RenderSectionRegion> cir, @Share("dataMap") LocalRef<Long2ObjectOpenHashMap<Object>> mapRef) {
		RenderSectionRegion rendererRegion = cir.getReturnValue();
		Long2ObjectOpenHashMap<Object> map = mapRef.get();

		if (map != null) {
			((RenderDataMapConsumer) rendererRegion).fabric_acceptRenderDataMap(map);
		}
	}

	@Unique
	private static Long2ObjectOpenHashMap<Object> mapChunk(LevelChunk chunk, SectionPos sectionPos, Long2ObjectOpenHashMap<Object> map) {
		// Skip the math below if the chunk contains no block entities
		if (chunk.getBlockEntities().isEmpty()) {
			return map;
		}

		final int xMin = SectionPos.sectionToBlockCoord(sectionPos.x() - 1);
		final int yMin = SectionPos.sectionToBlockCoord(sectionPos.y() - 1);
		final int zMin = SectionPos.sectionToBlockCoord(sectionPos.z() - 1);
		final int xMax = SectionPos.sectionToBlockCoord(sectionPos.x() + 1);
		final int yMax = SectionPos.sectionToBlockCoord(sectionPos.y() + 1);
		final int zMax = SectionPos.sectionToBlockCoord(sectionPos.z() + 1);

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
