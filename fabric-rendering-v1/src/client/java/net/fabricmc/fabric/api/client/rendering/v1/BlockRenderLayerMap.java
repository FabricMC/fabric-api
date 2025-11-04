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

package net.fabricmc.fabric.api.client.rendering.v1;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.impl.client.rendering.BlockRenderLayerMapImpl;

/**
 * Use to associate blocks or fluids with block render layer other than default (solid).
 *
 * <p>{@link RenderTypes} control how sprite pixels for fluids and blocks are blended with the scene. Consult the
 * vanilla {@link RenderTypes} implementation for examples.
 *
 * <p>The Fabric Renderer API can be used to control this at a per-quad level via {@code BlendMode}.
 */
public final class BlockRenderLayerMap {
	/**
	 * Map (or re-map) a block to a render layer. Re-mapping is not recommended but if done, last one in wins. Must be
	 * called from client thread prior to world load/rendering. Best practice will be to call from mod's client
	 * initializer.
	 *
	 * @param block the block to be mapped
	 * @param layer the render layer
	 */
	public static void putBlock(Block block, ChunkSectionLayer layer) {
		BlockRenderLayerMapImpl.putBlock(block, layer);
	}

	/**
	 * Map (or re-map) multiple blocks to a render layer. Re-mapping is not recommended but if done, last one in wins.
	 * Must be called from client thread prior to world load/rendering. Best practice will be to call from mod's client
	 * initializer.
	 *
	 * @param layer the render layer
	 * @param blocks the blocks to be mapped
	 */
	public static void putBlocks(ChunkSectionLayer layer, Block... blocks) {
		for (Block block : blocks) {
			putBlock(block, layer);
		}
	}

	/**
	 * Map (or re-map) a fluid to a render layer. Re-mapping is not recommended but if done, last one in wins. Must be
	 * called from client thread prior to world load/rendering. Best practice will be to call from mod's client
	 * initializer.
	 *
	 * @param fluid the fluid to be mapped
	 * @param layer the render layer
	 */
	public static void putFluid(Fluid fluid, ChunkSectionLayer layer) {
		BlockRenderLayerMapImpl.putFluid(fluid, layer);
	}

	/**
	 * Map (or re-map) multiple fluids to a render layer. Re-mapping is not recommended but if done, last one in wins.
	 * Must be called from client thread prior to world load/rendering. Best practice will be to call from mod's client
	 * initializer.
	 *
	 * @param layer the render layer
	 * @param fluids the fluids to be mapped
	 */
	public static void putFluids(ChunkSectionLayer layer, Fluid... fluids) {
		for (Fluid fluid : fluids) {
			putFluid(fluid, layer);
		}
	}

	private BlockRenderLayerMap() {
	}
}
