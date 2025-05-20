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

package net.fabricmc.fabric.api.blockrenderlayer.v1;

import net.minecraft.block.Block;
import net.minecraft.class_11515;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.fluid.Fluid;

import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl;

/**
 * Use to associate blocks or fluids with block render layer other than default.
 *
 * <p>{@link RenderLayers} control how sprite pixels for fluids and blocks are blended
 * with the scene. Consult the vanilla {@link RenderLayers} implementation for examples.
 *
 * <p>The Fabric Renderer API can be used to control this at a per-quad level at the code
 * via {@code BlendMode}.
 *
 * <p>Client-side only.
 */
public interface BlockRenderLayerMap {
	BlockRenderLayerMap INSTANCE = new BlockRenderLayerMapImpl();

	/**
	 * Map (or re-map) a block state with a render layer.  Re-mapping is not recommended but if done, last one in wins.
	 * Must be called from client thread prior to world load/rendering. Best practice will be to call from mod's client initializer.
	 *
	 * @param block Identifies block to be mapped.
	 * @param renderLayer Render layer.  Should be one of the layers used for terrain rendering.
	 */
	void putBlock(Block block, RenderLayer renderLayer);

	/**
	 * Map (or re-map) multiple block states with a render layer.  Re-mapping is not recommended but if done, last one in wins.
	 * Must be called from client thread prior to world load/rendering. Best practice will be to call from mod's client initializer.
	 *
	 * @param renderLayer Render layer.  Should be one of the layers used for terrain rendering.
	 * @param blocks Identifies blocks to be mapped.
	 */
	void putBlocks(RenderLayer renderLayer, Block... blocks);

	/**
	 * Map (or re-map) a fluid state with a render layer.  Re-mapping is not recommended but if done, last one in wins.
	 * Must be called from client thread prior to world load/rendering. Best practice will be to call from mod's client initializer.
	 *
	 * @param fluid Identifies fluid to be mapped.
	 * @param renderLayer Render layer.  Should be one of the layers used for terrain rendering.
	 */
	void putFluid(Fluid fluid, class_11515 renderLayer);

	/**
	 * Map (or re-map) multiple fluid states with a render layer.  Re-mapping is not recommended but if done, last one in wins.
	 * Must be called from client thread prior to world load/rendering. Best practice will be to call from mod's client initializer.
	 *
	 * @param renderLayer Render layer.  Should be one of the layers used for terrain rendering.
	 * @param fluids Identifies fluids to be mapped.
	 */
	void putFluids(class_11515 renderLayer, Fluid... fluids);
}
