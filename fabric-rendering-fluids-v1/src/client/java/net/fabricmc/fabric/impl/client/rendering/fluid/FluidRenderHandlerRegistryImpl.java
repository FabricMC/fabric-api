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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.BiomeKeys;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;

public class FluidRenderHandlerRegistryImpl implements FluidRenderHandlerRegistry {
	private final Map<Fluid, FluidRenderHandler> handlers = new IdentityHashMap<>();
	private final Map<Fluid, FluidRenderHandler> modHandlers = new IdentityHashMap<>();
	private final ConcurrentMap<Block, Boolean> overlayBlocks = new ConcurrentHashMap<>();

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
		overlayBlocks.put(block, transparent);
	}

	@Override
	public boolean isBlockTransparent(Block block) {
		return overlayBlocks.computeIfAbsent(block, k -> k instanceof TranslucentBlock || k instanceof LeavesBlock);
	}

	public void onFluidRendererReload(FluidRenderer renderer, Sprite[] waterSprites, Sprite[] lavaSprites, Sprite waterOverlay) {
		FluidRenderingImpl.setVanillaRenderer(renderer);

		WaterRenderHandler.INSTANCE.updateSprites(waterSprites, waterOverlay);
		LavaRenderHandler.INSTANCE.updateSprites(lavaSprites);

		SpriteAtlasTexture texture = MinecraftClient.getInstance()
				.getBakedModelManager()
				.getAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);

		for (FluidRenderHandler handler : handlers.values()) {
			handler.reloadTextures(texture);
		}
	}

	private static class WaterRenderHandler implements FluidRenderHandler {
		public static final WaterRenderHandler INSTANCE = new WaterRenderHandler();

		/**
		 * The water color of {@link BiomeKeys#OCEAN}.
		 */
		private static final int DEFAULT_WATER_COLOR = 0x3f76e4;

		private final Sprite[] sprites = new Sprite[3];

		@Override
		public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
			return sprites;
		}

		@Override
		public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
			if (view != null && pos != null) {
				return BiomeColors.getWaterColor(view, pos);
			} else {
				return DEFAULT_WATER_COLOR;
			}
		}

		public void updateSprites(Sprite[] waterSprites, Sprite waterOverlay) {
			sprites[0] = waterSprites[0];
			sprites[1] = waterSprites[1];
			sprites[2] = waterOverlay;
		}
	}

	private static class LavaRenderHandler implements FluidRenderHandler {
		public static final LavaRenderHandler INSTANCE = new LavaRenderHandler();

		private Sprite[] sprites;

		@Override
		public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
			return sprites;
		}

		public void updateSprites(Sprite[] lavaSprites) {
			sprites = lavaSprites;
		}
	}
}
