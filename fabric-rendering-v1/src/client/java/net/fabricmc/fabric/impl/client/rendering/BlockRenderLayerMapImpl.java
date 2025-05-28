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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import net.minecraft.block.Block;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.fluid.Fluid;

public final class BlockRenderLayerMapImpl {
	private static final Map<Block, BlockRenderLayer> BLOCK_RENDER_LAYER_MAP = new HashMap<>();
	private static final Map<Fluid, BlockRenderLayer> FLUID_RENDER_LAYER_MAP = new HashMap<>();

	// These consumers initially add to the maps above, and then are later set (when setup is called) to insert straight into the target map.
	private static BiConsumer<Block, BlockRenderLayer> blockHandler = BLOCK_RENDER_LAYER_MAP::put;
	private static BiConsumer<Fluid, BlockRenderLayer> fluidHandler = FLUID_RENDER_LAYER_MAP::put;

	public static void putBlock(Block block, BlockRenderLayer layer) {
		Objects.requireNonNull(block, "block must not be null");
		Objects.requireNonNull(layer, "render layer must not be null");

		blockHandler.accept(block, layer);
	}

	public static void putFluid(Fluid fluid, BlockRenderLayer layer) {
		Objects.requireNonNull(fluid, "fluid must not be null");
		Objects.requireNonNull(layer, "render layer must not be null");

		fluidHandler.accept(fluid, layer);
	}

	public static void setup(BiConsumer<Block, BlockRenderLayer> vanillaBlockHandler, BiConsumer<Fluid, BlockRenderLayer> vanillaFluidHandler) {
		// Add all the preexisting render layers
		BLOCK_RENDER_LAYER_MAP.forEach(vanillaBlockHandler);
		FLUID_RENDER_LAYER_MAP.forEach(vanillaFluidHandler);

		// Set the handlers to directly accept later additions
		blockHandler = vanillaBlockHandler;
		fluidHandler = vanillaFluidHandler;
	}

	private BlockRenderLayerMapImpl() {
	}
}
