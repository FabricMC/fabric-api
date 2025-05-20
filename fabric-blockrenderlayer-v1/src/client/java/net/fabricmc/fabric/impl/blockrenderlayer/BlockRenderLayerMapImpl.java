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

package net.fabricmc.fabric.impl.blockrenderlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import net.minecraft.block.Block;
import net.minecraft.class_11515;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;

public class BlockRenderLayerMapImpl implements BlockRenderLayerMap {
	public BlockRenderLayerMapImpl() { }

	@Override
	public void putBlock(Block block, RenderLayer renderLayer) {
		if (block == null) throw new IllegalArgumentException("Request to map null block to RenderLayer");
		if (renderLayer == null) throw new IllegalArgumentException("Request to map block " + block.toString() + " to null RenderLayer");

		blockHandler.accept(block, renderLayer);
	}

	@Override
	public void putBlocks(RenderLayer renderLayer, Block... blocks) {
		for (Block block : blocks) {
			putBlock(block, renderLayer);
		}
	}

	@Override
	public void putFluid(Fluid fluid, class_11515 renderLayer) {
		if (fluid == null) throw new IllegalArgumentException("Request to map null fluid to RenderLayer");
		if (renderLayer == null) throw new IllegalArgumentException("Request to map fluid " + fluid.toString() + " to null RenderLayer");

		fluidHandler.accept(fluid, renderLayer);
	}

	@Override
	public void putFluids(class_11515 renderLayer, Fluid... fluids) {
		for (Fluid fluid : fluids) {
			putFluid(fluid, renderLayer);
		}
	}

	private static final Map<Block, RenderLayer> BLOCK_RENDER_LAYER_MAP = new HashMap<>();
	private static final Map<Fluid, class_11515> FLUID_RENDER_LAYER_MAP = new HashMap<>();

	// These consumers initially add to the maps above, and then are later set (when initialize is called) to insert straight into the target map.
	private static BiConsumer<Block, RenderLayer> blockHandler = BLOCK_RENDER_LAYER_MAP::put;
	private static BiConsumer<Fluid, class_11515> fluidHandler = FLUID_RENDER_LAYER_MAP::put;

	public static void initialize(BiConsumer<Block, RenderLayer> blockHandlerIn, BiConsumer<Fluid, class_11515> fluidHandlerIn) {
		// Add all the preexisting render layers
		BLOCK_RENDER_LAYER_MAP.forEach(blockHandlerIn);
		FLUID_RENDER_LAYER_MAP.forEach(fluidHandlerIn);

		// Set the handlers to directly accept later additions
		blockHandler = blockHandlerIn;
		fluidHandler = fluidHandlerIn;
	}
}
