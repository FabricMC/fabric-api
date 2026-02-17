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

package net.fabricmc.fabric.mixin.client.indigo.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessSubmitNodeCollection;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.ItemRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.MeshItemSubmit;

@Mixin(ItemFeatureRenderer.class)
abstract class ItemFeatureRendererMixin {
	@Shadow
	@Final
	private PoseStack poseStack;

	@Unique
	private final ItemRenderContext itemRenderContext = new ItemRenderContext();

	@Inject(method = "renderSolid", at = @At("RETURN"))
	private void onReturnRenderSolid(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, CallbackInfo ci) {
		for (MeshItemSubmit submit : ((AccessSubmitNodeCollection) nodeCollection).fabric_getMeshItemSubmits()) {
			poseStack.pushPose();
			poseStack.last().set(submit.pose());

			itemRenderContext.renderItem(
					submit.displayContext(),
					poseStack,
					bufferSource,
					submit.lightCoords(),
					submit.overlayCoords(),
					submit.tintLayers(),
					submit.quads(),
					submit.mesh(),
					submit.renderType(),
					submit.renderTypeGetter(),
					submit.foilType(),
					false,
					false
			);

			if (submit.outlineColor() != 0) {
				outlineBufferSource.setColor(submit.outlineColor());
				itemRenderContext.renderItem(
						submit.displayContext(),
						poseStack,
						outlineBufferSource,
						submit.lightCoords(),
						submit.overlayCoords(),
						submit.tintLayers(),
						submit.quads(),
						submit.mesh(),
						submit.renderType(),
						submit.renderTypeGetter(),
						ItemStackRenderState.FoilType.NONE,
						true,
						false
				);
			}

			poseStack.popPose();
		}
	}

	@Inject(method = "renderTranslucent", at = @At("RETURN"))
	private void onReturnRenderTranslucent(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, CallbackInfo ci) {
		for (MeshItemSubmit submit : ((AccessSubmitNodeCollection) nodeCollection).fabric_getMeshItemSubmits()) {
			poseStack.pushPose();
			poseStack.last().set(submit.pose());

			itemRenderContext.renderItem(
					submit.displayContext(),
					poseStack,
					bufferSource,
					submit.lightCoords(),
					submit.overlayCoords(),
					submit.tintLayers(),
					submit.quads(),
					submit.mesh(),
					submit.renderType(),
					submit.renderTypeGetter(),
					submit.foilType(),
					false,
					true
			);

			if (submit.outlineColor() != 0) {
				outlineBufferSource.setColor(submit.outlineColor());
				itemRenderContext.renderItem(
						submit.displayContext(),
						poseStack,
						outlineBufferSource,
						submit.lightCoords(),
						submit.overlayCoords(),
						submit.tintLayers(),
						submit.quads(),
						submit.mesh(),
						submit.renderType(),
						submit.renderTypeGetter(),
						ItemStackRenderState.FoilType.NONE,
						true,
						true
				);
			}

			poseStack.popPose();
		}
	}
}
