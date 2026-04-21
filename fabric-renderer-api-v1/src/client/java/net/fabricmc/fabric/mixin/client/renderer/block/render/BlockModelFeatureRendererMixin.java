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

package net.fabricmc.fabric.mixin.client.renderer.block.render;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.BlockModelFeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;
import net.fabricmc.fabric.impl.client.renderer.BlockModelBufferCache;
import net.fabricmc.fabric.impl.client.renderer.QuadConsumers;

@Mixin(BlockModelFeatureRenderer.class)
abstract class BlockModelFeatureRendererMixin {
	@Shadow
	@Final
	private static Direction[] DIRECTIONS;

	@Shadow
	@Final
	private QuadInstance quadInstance;

	@Shadow
	private static void putQuad(PoseStack.Pose pose, BakedQuad quad, QuadInstance instance, int[] tintLayers, VertexConsumer buffer, @Nullable VertexConsumer outlineBuffer) {
	}

	@Unique
	private static void putPartQuads(BlockStateModelPart part, PoseStack.Pose pose, QuadInstance quadInstance, int[] tintLayers, Function<ChunkSectionLayer, RenderType> renderTypeFunction, BlockModelBufferCache bufferCache) {
		for (Direction direction : DIRECTIONS) {
			for (BakedQuad quad : part.getQuads(direction)) {
				RenderType renderType = renderTypeFunction.apply(quad.materialInfo().layer());
				putQuad(pose, quad, quadInstance, tintLayers, bufferCache.getBuffer(renderType), bufferCache.getOutlineBuffer(renderType));
			}
		}

		for (BakedQuad quad : part.getQuads(null)) {
			RenderType renderType = renderTypeFunction.apply(quad.materialInfo().layer());
			putQuad(pose, quad, quadInstance, tintLayers, bufferCache.getBuffer(renderType), bufferCache.getOutlineBuffer(renderType));
		}
	}

	@Inject(method = "renderModels", at = @At("RETURN"))
	private void onReturnRenderModels(SubmitNodeCollection nodeCollection, FeatureFrameContext context, boolean translucent, CallbackInfo ci) {
		BlockModelBufferCache bufferCache = new BlockModelBufferCache((net.minecraft.client.renderer.MultiBufferSource.BufferSource) context.bufferSource(), context.outlineBufferSource());
		QuadConsumers.BlockModel quadConsumer = new QuadConsumers.BlockModel();
		QuadEmitter output = Renderer.get().quadEmitter(quadConsumer);

		for (FabricSubmitNodeCollection.ExtendedBlockModelSubmit submit : nodeCollection.getExtendedBlockModelSubmits()) {
			if (submit.translucent() == translucent) {
				PoseStack.Pose pose = submit.pose();
				int[] tintLayers = submit.tintLayers();
				Function<ChunkSectionLayer, RenderType> renderTypeFunction = submit.renderTypeFunction();

				bufferCache.outlineColor(submit.outlineColor());

				quadInstance.setLightCoords(submit.lightCoords());
				quadInstance.setOverlayCoords(submit.overlayCoords());

				for (BlockStateModelPart part : submit.modelParts()) {
					putPartQuads(part, pose, quadInstance, tintLayers, renderTypeFunction, bufferCache);
				}

				if (submit.mesh() != null) {
					quadConsumer.tintLayers = tintLayers;
					quadConsumer.lightCoords = submit.lightCoords();
					quadConsumer.overlayCoords = submit.overlayCoords();
					quadConsumer.pose = pose;
					quadConsumer.renderTypeFunction = renderTypeFunction;
					quadConsumer.bufferCache = bufferCache;
					submit.mesh().outputTo(output);
				}
			}
		}
	}
}
