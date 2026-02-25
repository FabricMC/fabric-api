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

package net.fabricmc.fabric.impl.client.rendering.fluid;

import java.util.IdentityHashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;

public class FluidRenderHandlerRegistryImpl implements FluidRenderHandlerRegistry {
	private final Map<Fluid, FluidRenderHandler> handlers = new IdentityHashMap<>();
	private final Map<Fluid, FluidRenderHandler> modHandlers = new IdentityHashMap<>();
	private final Object2BooleanMap<Block> transparencyForOverlay = new Object2BooleanOpenHashMap<>();

	{
		handlers.put(Fluids.WATER, WaterRenderHandler.INSTANCE);
		handlers.put(Fluids.FLOWING_WATER, WaterRenderHandler.INSTANCE);
		handlers.put(Fluids.LAVA, LavaRenderHandler.INSTANCE);
		handlers.put(Fluids.FLOWING_LAVA, LavaRenderHandler.INSTANCE);
	}

	public FluidRenderHandlerRegistryImpl() {
	}

	@Override
	@Nullable
	public FluidRenderHandler get(Fluid fluid) {
		return handlers.get(fluid);
	}

	@Override
	@Nullable
	public FluidRenderHandler getOverride(Fluid fluid) {
		return modHandlers.get(fluid);
	}

	@Override
	public void register(Fluid fluid, FluidRenderHandler renderer) {
		handlers.put(fluid, renderer);
		modHandlers.put(fluid, renderer);
	}

	@Override
	public void setBlockTransparency(Block block, boolean transparent) {
		transparencyForOverlay.put(block, transparent);
	}

	@Override
	public boolean isBlockTransparent(Block block) {
		return transparencyForOverlay.getOrDefault(block, block instanceof HalfTransparentBlock || block instanceof LeavesBlock);
	}

	public Map<Fluid, ChunkSectionLayer> onFluidRendererReload(SpriteGetter spriteGetter, LiquidBlockRenderer renderer, TextureAtlasSprite[] waterSprites, TextureAtlasSprite[] lavaSprites, TextureAtlasSprite waterOverlay) {
		FluidRenderingImpl.setVanillaRenderer(renderer);

		WaterRenderHandler.INSTANCE.updateSprites(waterSprites, waterOverlay);
		LavaRenderHandler.INSTANCE.updateSprites(lavaSprites);

		Map<Fluid, ChunkSectionLayer> fluidChunkSectionLayers = new IdentityHashMap<>();

		// Multiple fluids may share the same handler, so we need to avoid reloading the same handler multiple times.
		Map<FluidRenderHandler, ChunkSectionLayer> loadedHandlers = new IdentityHashMap<>();

		for (Map.Entry<Fluid, FluidRenderHandler> entry : handlers.entrySet()) {
			ChunkSectionLayer chunkSectionLayer = loadedHandlers.get(entry.getValue());

			if (chunkSectionLayer == null) {
				chunkSectionLayer = entry.getValue().reloadTextures(spriteGetter);
				loadedHandlers.put(entry.getValue(), chunkSectionLayer);
			}

			fluidChunkSectionLayers.put(entry.getKey(), chunkSectionLayer);
		}

		return fluidChunkSectionLayers;
	}

	private static class WaterRenderHandler implements FluidRenderHandler {
		public static final WaterRenderHandler INSTANCE = new WaterRenderHandler();

		/**
		 * The water color of {@link Biomes#OCEAN}.
		 */
		private static final int DEFAULT_WATER_COLOR = 0x3f76e4;

		private final TextureAtlasSprite[] sprites = new TextureAtlasSprite[3];

		@Override
		public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, FluidState state) {
			return sprites;
		}

		@Override
		public int getFluidColor(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, FluidState state) {
			if (level != null && pos != null) {
				return BiomeColors.getAverageWaterColor(level, pos);
			} else {
				return DEFAULT_WATER_COLOR;
			}
		}

		public void updateSprites(TextureAtlasSprite[] waterSprites, TextureAtlasSprite waterOverlay) {
			sprites[0] = waterSprites[0];
			sprites[1] = waterSprites[1];
			sprites[2] = waterOverlay;
		}
	}

	private static class LavaRenderHandler implements FluidRenderHandler {
		public static final LavaRenderHandler INSTANCE = new LavaRenderHandler();

		private TextureAtlasSprite[] sprites;

		@Override
		public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, FluidState state) {
			return sprites;
		}

		public void updateSprites(TextureAtlasSprite[] lavaSprites) {
			sprites = lavaSprites;
		}
	}
}
