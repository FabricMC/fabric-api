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

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.level.block.state.BlockState;

public final class RenderLayerHelper {
	private RenderLayerHelper() {
	}

	/**
	 * Same logic as {@link net.minecraft.client.renderer.ItemBlockRenderTypes#getMovingBlockRenderType}, but accepts a {@link ChunkSectionLayer} instead of a
	 * {@link BlockState}.
	 */
	public static RenderType getMovingBlockLayer(ChunkSectionLayer layer) {
		return switch (layer) {
		case SOLID -> RenderTypes.solidMovingBlock();
		case CUTOUT -> RenderTypes.cutoutMovingBlock();
		case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
		case TRIPWIRE -> RenderTypes.tripwireMovingBlock();
		};
	}

	/**
	 * Same logic as {@link net.minecraft.client.renderer.ItemBlockRenderTypes#getRenderType}, but accepts a {@link ChunkSectionLayer} instead of a
	 * {@link BlockState}.
	 */
	public static RenderType getEntityBlockLayer(ChunkSectionLayer layer) {
		return layer == ChunkSectionLayer.TRANSLUCENT ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockSheet();
	}

	/**
	 * Wraps the given provider, converting {@link ChunkSectionLayer}s to render layers using
	 * {@link #getMovingBlockLayer(ChunkSectionLayer)}.
	 */
	public static BlockVertexConsumerProvider movingDelegate(MultiBufferSource vertexConsumers) {
		return layer -> vertexConsumers.getBuffer(RenderLayerHelper.getMovingBlockLayer(layer));
	}

	/**
	 * Wraps the given provider, converting {@link ChunkSectionLayer}s to render layers using
	 * {@link #getEntityBlockLayer(ChunkSectionLayer)}.
	 */
	public static BlockVertexConsumerProvider entityDelegate(MultiBufferSource vertexConsumers) {
		return layer -> vertexConsumers.getBuffer(RenderLayerHelper.getEntityBlockLayer(layer));
	}
}
