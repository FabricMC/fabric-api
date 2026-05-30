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
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
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

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;

public class ExtendedBlockModelFeatureRenderer extends RenderTypeFeatureRenderer<FabricSubmitNodeCollection.ExtendedBlockModelSubmit> {
	private static final Direction[] DIRECTIONS = Direction.values();
	private final QuadInstance quadInstance = new QuadInstance();

	@Override
	protected void buildGroup(FeatureFrameContext context, List<FabricSubmitNodeCollection.ExtendedBlockModelSubmit> submits) {
		QuadConsumers.BlockModel quadConsumer = new QuadConsumers.BlockModel();
		QuadEmitter output = Renderer.get().quadEmitter(quadConsumer);

		for (FabricSubmitNodeCollection.ExtendedBlockModelSubmit submit : submits) {
			PoseStack.Pose pose = submit.pose();
			int[] tintLayers = submit.tintLayers();
			Function<ChunkSectionLayer, @Nullable RenderType> renderTypeFunction = submit.renderTypeFunction();

			quadInstance.setLightCoords(submit.lightCoords());
			quadInstance.setOverlayCoords(submit.overlayCoords());

			for (BlockStateModelPart part : submit.modelParts()) {
				putPartQuads(part, pose, quadInstance, submit.tintColor(), tintLayers, renderTypeFunction);
			}

			if (submit.mesh() != null) {
				quadConsumer.tintLayers = tintLayers;
				quadConsumer.lightCoords = submit.lightCoords();
				quadConsumer.overlayCoords = submit.overlayCoords();
				quadConsumer.pose = pose;
				quadConsumer.renderTypeFunction = renderTypeFunction;
				quadConsumer.vertexConsumerFunction = this::getVertexBuilder;
				submit.mesh().outputTo(output);
			}
		}
	}

	private void putPartQuads(BlockStateModelPart part, PoseStack.Pose pose, QuadInstance quadInstance, int baseTintColor, int[] tintLayers, Function<ChunkSectionLayer, @Nullable RenderType> renderTypeFunction) {
		for (Direction direction : DIRECTIONS) {
			for (BakedQuad quad : part.getQuads(direction)) {
				RenderType renderType = renderTypeFunction.apply(quad.materialInfo().layer());

				if (renderType == null) {
					continue;
				}

				putQuad(pose, quad, quadInstance, baseTintColor, tintLayers, this.getVertexBuilder(renderType));
			}
		}

		for (BakedQuad quad : part.getQuads(null)) {
			RenderType renderType = renderTypeFunction.apply(quad.materialInfo().layer());

			if (renderType == null) {
				continue;
			}

			putQuad(pose, quad, quadInstance, baseTintColor, tintLayers, this.getVertexBuilder(renderType));
		}
	}

	private static void putQuad(PoseStack.Pose pose, BakedQuad quad, QuadInstance instance, int baseTintColor, int[] tintLayers, VertexConsumer buffer) {
		int tintIndex = quad.materialInfo().tintIndex();
		boolean useTintLayer = tintIndex != -1 && tintIndex < tintLayers.length;
		instance.setColor(useTintLayer ? ARGB.multiply(baseTintColor, tintLayers[tintIndex]) : baseTintColor);
		buffer.putBakedQuad(pose, quad, instance);
	}
}
