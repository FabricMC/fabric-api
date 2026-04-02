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

package net.fabricmc.fabric.impl.client.renderer;

import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;

// Workaround for mixin not allowing referencing members of anonymous classes defined within mixins.
// Once that is fixed, this class should be inlined.
public final class QuadConsumers {
	private QuadConsumers() {
	}

	public static class BlockModel implements Consumer<MutableQuadView> {
		public int[] tintLayers;
		public int lightCoords;
		public int overlayCoords;
		public PoseStack.Pose pose;
		public Function<ChunkSectionLayer, RenderType> renderTypeFunction;
		public BlockModelBufferCache bufferCache;

		@Override
		public void accept(MutableQuadView quad) {
			if (quad.emissive()) {
				quad.lightmap(LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT);
			} else {
				quad.minLightmap(lightCoords);
			}

			int tintIndex = quad.tintIndex();

			if (tintIndex != -1 && tintIndex < tintLayers.length) {
				quad.multiplyColor(tintLayers[tintIndex]);
			}

			RenderType renderType = renderTypeFunction.apply(quad.chunkLayer());
			quad.buffer(overlayCoords, pose, bufferCache.getBuffer(renderType));
			VertexConsumer outlineBuffer = bufferCache.getOutlineBuffer(renderType);

			if (outlineBuffer != null) {
				quad.buffer(overlayCoords, pose, outlineBuffer);
			}
		}
	}

	public static class BreakingBlockModel implements Consumer<MutableQuadView> {
		public PoseStack.Pose pose;
		public VertexConsumer buffer;

		@Override
		public void accept(MutableQuadView quad) {
			quad.lightmap(LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT);
			quad.buffer(OverlayTexture.NO_OVERLAY, pose, buffer);
		}
	}
}
