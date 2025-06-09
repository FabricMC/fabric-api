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
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;

public final class RenderLayerHelper {
	private RenderLayerHelper() {
	}

	/**
	 * Same logic as {@link RenderLayers#getMovingBlockLayer}, but accepts a {@link BlockRenderLayer} instead of a
	 * {@link BlockState}.
	 */
	public static RenderLayer getMovingBlockLayer(BlockRenderLayer layer) {
		return switch (layer) {
		case SOLID -> RenderLayer.getSolid();
		case CUTOUT_MIPPED -> RenderLayer.getCutoutMipped();
		case CUTOUT -> RenderLayer.getCutout();
		case TRANSLUCENT -> RenderLayer.getTranslucentMovingBlock();
		case TRIPWIRE -> RenderLayer.getTripwire();
		};
	}

	/**
	 * Same logic as {@link RenderLayers#getEntityBlockLayer}, but accepts a {@link BlockRenderLayer} instead of a
	 * {@link BlockState}.
	 */
	public static RenderLayer getEntityBlockLayer(BlockRenderLayer layer) {
		return layer == BlockRenderLayer.TRANSLUCENT ? TexturedRenderLayers.getItemEntityTranslucentCull() : TexturedRenderLayers.getEntityCutout();
	}

	/**
	 * Wraps the given provider, converting {@link BlockRenderLayer}s to render layers using
	 * {@link #getMovingBlockLayer(BlockRenderLayer)}.
	 */
	public static BlockVertexConsumerProvider movingDelegate(VertexConsumerProvider vertexConsumers) {
		return layer -> vertexConsumers.getBuffer(RenderLayerHelper.getMovingBlockLayer(layer));
	}

	/**
	 * Wraps the given provider, converting {@link BlockRenderLayer}s to render layers using
	 * {@link #getEntityBlockLayer(BlockRenderLayer)}.
	 */
	public static BlockVertexConsumerProvider entityDelegate(VertexConsumerProvider vertexConsumers) {
		return layer -> vertexConsumers.getBuffer(RenderLayerHelper.getEntityBlockLayer(layer));
	}
}
