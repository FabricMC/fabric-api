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

package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import java.util.List;
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

import net.fabricmc.fabric.api.client.renderer.v1.render.submit.ExtendedBlockModelSubmit;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;

public class ExtendedBlockModelFeatureRenderer extends RenderTypeFeatureRenderer<ExtendedBlockModelSubmit> {
	private static final Direction[] DIRECTIONS = Direction.values();
	private final QuadInstance quadInstance = new QuadInstance();

	private final BufferCache bufferCache = new BufferCache();
	private final MutableQuadViewImpl emitter = new MutableQuadViewImpl() {
		{
			data = new int[EncodingFormat.TOTAL_STRIDE];
			clear();
		}

		@Override
		protected void emitDirectly() {
			bufferQuad(this);
		}
	};

	private ExtendedBlockModelSubmit submit;

	@Override
	protected void buildGroup(FeatureFrameContext context, List<ExtendedBlockModelSubmit> submits) {
		BufferCache bufferCache = this.bufferCache;
		MutableQuadViewImpl emitter = this.emitter;

		for (ExtendedBlockModelSubmit submit : submits) {
			bufferCache.prepare(submit.renderTypeFunction(), submit.sheetedDecalPose());

			quadInstance.setLightCoords(submit.lightCoords());
			quadInstance.setOverlayCoords(submit.overlayCoords());

			for (BlockStateModelPart part : submit.modelParts()) {
				putPartQuads(part, submit.pose(), quadInstance, submit.tintColor(), submit.tintLayers(), bufferCache);
			}

			if (submit.mesh() != null) {
				this.submit = submit;
				submit.mesh().outputTo(emitter);
			}
		}

		bufferCache.clear();
		submit = null;
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

	private void bufferQuad(MutableQuadViewImpl quad) {
		VertexConsumer buffer = bufferCache.getBuffer(quad.chunkLayer());

		if (buffer == null) {
			return;
		}

		if (quad.emissive()) {
			quad.lightmap(LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT);
		} else {
			quad.minLightmap(submit.lightCoords());
		}

		int[] tintLayers = submit.tintLayers();
		int baseTintColor = submit.tintColor();

		int tintIndex = quad.tintIndex();
		boolean useTintLayer = tintIndex != -1 && tintIndex < tintLayers.length;
		quad.multiplyColor(useTintLayer ? ARGB.multiply(baseTintColor, tintLayers[tintIndex]) : baseTintColor);
		quad.buffer(submit.overlayCoords(), submit.pose(), buffer);
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

		public void clear() {
			renderTypeFunction = null;
			sheetedDecalPose = null;
			lastLayer = null;
			lastBuffer = null;
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
}
