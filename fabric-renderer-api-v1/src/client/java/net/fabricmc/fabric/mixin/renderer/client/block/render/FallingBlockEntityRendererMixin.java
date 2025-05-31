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

package net.fabricmc.fabric.mixin.renderer.client.block.render;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FallingBlockEntityRenderer;
import net.minecraft.client.render.entity.state.FallingBlockEntityRenderState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.FallingBlockEntity;

import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;

@Mixin(FallingBlockEntityRenderer.class)
abstract class FallingBlockEntityRendererMixin extends EntityRenderer<FallingBlockEntity, FallingBlockEntityRenderState> {
	@Shadow
	@Final
	private BlockRenderManager blockRenderManager;

	private FallingBlockEntityRendererMixin(EntityRendererFactory.Context context) {
		super(context);
	}

	// Support multi-render layer models.
	@Overwrite
	public void render(FallingBlockEntityRenderState renderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light) {
		BlockState blockState = renderState.blockState;

		if (blockState.getRenderType() == BlockRenderType.MODEL) {
			matrixStack.push();
			matrixStack.translate(-0.5, 0.0, -0.5);

			BlockStateModel model = blockRenderManager.getModel(blockState);
			long seed = blockState.getRenderingSeed(renderState.fallingBlockPos);
			blockRenderManager.getModelRenderer().render(renderState, model, blockState, renderState.currentPos, matrixStack, RenderLayerHelper.movingDelegate(vertexConsumers), false, seed, OverlayTexture.DEFAULT_UV);

			matrixStack.pop();
			super.render(renderState, matrixStack, vertexConsumers, light);
		}
	}
}
