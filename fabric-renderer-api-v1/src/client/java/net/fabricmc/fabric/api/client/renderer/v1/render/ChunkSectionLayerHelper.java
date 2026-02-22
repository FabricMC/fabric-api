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

package net.fabricmc.fabric.api.client.renderer.v1.render;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public final class ChunkSectionLayerHelper {
	private ChunkSectionLayerHelper() {
	}

	/**
	 * Wraps the given provider, converting {@link ChunkSectionLayer}s to render types using
	 * {@link ItemBlockRenderTypes#getMovingBlockRenderType(ChunkSectionLayer)}.
	 */
	public static BlockMultiBufferSource movingDelegate(MultiBufferSource bufferSource) {
		return layer -> bufferSource.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(layer));
	}

	/**
	 * Wraps the given provider, converting {@link ChunkSectionLayer}s to render types using
	 * {@link ItemBlockRenderTypes#getRenderType(ChunkSectionLayer)}.
	 */
	public static BlockMultiBufferSource entityDelegate(MultiBufferSource bufferSource) {
		return layer -> bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(layer));
	}
}
