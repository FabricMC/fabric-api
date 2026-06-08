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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.ExtendedBlockModelSubmit;

public class ExtendedBlockModelFeatureRenderer extends RenderTypeFeatureRenderer<ExtendedBlockModelSubmit> {
	private static final Direction[] DIRECTIONS = Direction.values();
	private final QuadInstance quadInstance = new QuadInstance();

	@Override
	protected void buildGroup(FeatureFrameContext context, List<ExtendedBlockModelSubmit> submits) {
		BufferCache bufferCache = new BufferCache();
		QuadConsumer quadConsumer = new QuadConsumer(bufferCache);
		QuadEmitter output = Renderer.get().quadEmitter(quadConsumer);

		for (ExtendedBlockModelSubmit submit : submits) {
			bufferCache.prepare(submit.renderTypeFunction(), submit.sheetedDecalPose());

			quadInstance.setLightCoords(submit.lightCoords());
			quadInstance.setOverlayCoords(submit.overlayCoords());

			for (BlockStateModelPart part : submit.modelParts()) {
				putPartQuads(part, submit.pose(), quadInstance, submit.tintColor(), submit.tintLayers(), bufferCache);
			}

			if (submit.mesh() != null) {
				quadConsumer.pose = submit.pose();
				quadConsumer.tintLayers = submit.tintLayers();
				quadConsumer.lightCoords = submit.lightCoords();
				quadConsumer.overlayCoords = submit.overlayCoords();
				quadConsumer.baseTintColor = submit.tintColor();
				submit.mesh().outputTo(output);
			}
		}
	}

	private void putPartQuads(BlockStateModelPart part, PoseStack.Pose pose, QuadInstance quadInstance, int baseTintColor, int[] tintLayers, BufferCache bufferCache) {
		for (Direction direction : DIRECTIONS) {
			for (BakedQuad quad : part.getQuads(direction)) {
				VertexConsumer buffer = bufferCache.getBuffer(quad.materialInfo().layer());

				if (buffer == null) {
					continue;
				}

				putQuad(pose, quad, quadInstance, baseTintColor, tintLayers, buffer);
			}
		}

		for (BakedQuad quad : part.getQuads(null)) {
			VertexConsumer buffer = bufferCache.getBuffer(quad.materialInfo().layer());

			if (buffer == null) {
				continue;
			}

			putQuad(pose, quad, quadInstance, baseTintColor, tintLayers, buffer);
		}
	}

	private static void putQuad(PoseStack.Pose pose, BakedQuad quad, QuadInstance instance, int baseTintColor, int[] tintLayers, VertexConsumer buffer) {
		int tintIndex = quad.materialInfo().tintIndex();
		boolean useTintLayer = tintIndex != -1 && tintIndex < tintLayers.length;
		instance.setColor(useTintLayer ? ARGB.multiply(baseTintColor, tintLayers[tintIndex]) : baseTintColor);
		buffer.putBakedQuad(pose, quad, instance);
	}

	private class BufferCache {
		private Function<ChunkSectionLayer, @Nullable RenderType> renderTypeFunction;
		private PoseStack.@Nullable Pose sheetedDecalPose;

		@Nullable
		private ChunkSectionLayer lastLayer;
		@Nullable
		private VertexConsumer lastBuffer;

		public void prepare(Function<ChunkSectionLayer, @Nullable RenderType> renderTypeFunction, PoseStack.@Nullable Pose sheetedDecalPose) {
			this.renderTypeFunction = renderTypeFunction;
			this.sheetedDecalPose = sheetedDecalPose;
			lastLayer = null;
		}

		@Nullable
		public VertexConsumer getBuffer(ChunkSectionLayer layer) {
			if (layer != lastLayer) {
				lastLayer = layer;
				RenderType renderType = renderTypeFunction.apply(layer);

				if (renderType == null) {
					lastBuffer = null;
				} else {
					VertexConsumer buffer = getVertexBuilder(renderType);
					lastBuffer = sheetedDecalPose != null ? new SheetedDecalTextureGenerator(buffer, sheetedDecalPose, 1.0F) : buffer;
				}
			}

			return lastBuffer;
		}
	}

	private class QuadConsumer implements Consumer<MutableQuadView> {
		private final BufferCache bufferCache;

		public PoseStack.Pose pose;
		public int[] tintLayers;
		public int lightCoords;
		public int overlayCoords;
		public int baseTintColor;

		QuadConsumer(BufferCache bufferCache) {
			this.bufferCache = bufferCache;
		}

		@Override
		public void accept(MutableQuadView quad) {
			VertexConsumer buffer = bufferCache.getBuffer(quad.chunkLayer());

			if (buffer == null) {
				return;
			}

			if (quad.emissive()) {
				quad.lightmap(LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT);
			} else {
				quad.minLightmap(lightCoords);
			}

			int tintIndex = quad.tintIndex();
			boolean useTintLayer = tintIndex != -1 && tintIndex < tintLayers.length;
			quad.multiplyColor(useTintLayer ? ARGB.multiply(baseTintColor, tintLayers[tintIndex]) : baseTintColor);
			quad.buffer(overlayCoords, pose, buffer);
		}
	}
}
