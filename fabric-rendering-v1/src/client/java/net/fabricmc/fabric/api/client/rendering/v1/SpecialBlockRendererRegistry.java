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

import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.LoadedBlockEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;

import net.fabricmc.fabric.impl.client.rendering.SpecialBlockRendererRegistryImpl;

/**
 * Allows registering special renderers for certain blocks, such that they are used when
 * {@link LoadedBlockEntityModels#render} is invoked. The most common use of this method is through
 * {@link BlockRenderManager#renderBlockAsEntity}, which is used for rendering blocks in minecarts, blocks held by
 * endermen, and other cases.
 */
public final class SpecialBlockRendererRegistry {
	private SpecialBlockRendererRegistry() {
	}

	/**
	 * Assign the given unbaked renderer to the given block. {@link SpecialModelRenderer.Unbaked#getCodec()} will not be
	 * used and can return {@code null}.
	 */
	public static void register(Block block, SpecialModelRenderer.Unbaked unbakedRenderer) {
		SpecialBlockRendererRegistryImpl.register(block, unbakedRenderer);
	}
}
