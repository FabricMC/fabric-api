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

package net.fabricmc.fabric.api.renderer.v1.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;

public final class RenderLayerHelper {
	private RenderLayerHelper() {
	}

	/**
	 * Same logic as {@link RenderLayers#getMovingBlockLayer}, but accepts a {@link RenderLayer} from
	 * {@link RenderLayer#getBlockLayers()} instead of a {@link BlockState}.
	 */
	public static RenderLayer getMovingBlockLayer(RenderLayer chunkRenderLayer) {
		return chunkRenderLayer == RenderLayer.getTranslucent() ? RenderLayer.getTranslucentMovingBlock() : chunkRenderLayer;
	}

	/**
	 * Same logic as {@link RenderLayers#getEntityBlockLayer}, but accepts a {@link RenderLayer} from
	 * {@link RenderLayer#getBlockLayers()} instead of a {@link BlockState}.
	 */
	public static RenderLayer getEntityBlockLayer(RenderLayer chunkRenderLayer) {
		return chunkRenderLayer == RenderLayer.getTranslucent() ? TexturedRenderLayers.getItemEntityTranslucentCull() : TexturedRenderLayers.getEntityCutout();
	}

	/**
	 * Wraps the given provider, converting {@linkplain RenderLayer#getBlockLayers() block layers} to render layers
	 * using {@link #getMovingBlockLayer(RenderLayer)}.
	 */
	public static VertexConsumerProvider movingDelegate(VertexConsumerProvider vertexConsumers) {
		return chunkRenderLayer -> vertexConsumers.getBuffer(RenderLayerHelper.getMovingBlockLayer(chunkRenderLayer));
	}

	/**
	 * Wraps the given provider, converting {@linkplain RenderLayer#getBlockLayers() block layers} to render layers
	 * using {@link #getEntityBlockLayer(RenderLayer)}.
	 */
	public static VertexConsumerProvider entityDelegate(VertexConsumerProvider vertexConsumers) {
		return chunkRenderLayer -> vertexConsumers.getBuffer(RenderLayerHelper.getEntityBlockLayer(chunkRenderLayer));
	}
}
