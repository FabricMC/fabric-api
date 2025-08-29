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

import java.util.Iterator;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.FallingBlockCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;

import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;

@Mixin(FallingBlockCommandRenderer.class)
abstract class FallingBlockCommandRendererMixin {
	@Shadow
	@Final
	private MatrixStack matrices;

	// Support multi-render layer models (FallingBlockCommand).
	@Inject(method = "render", at = @At(value = "INVOKE", target = "java/util/Iterator.hasNext()Z", remap = false, ordinal = 0))
	private void beforeRenderFallingBlocks(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers, BlockRenderManager blockRenderManager, OutlineVertexConsumerProvider outlineVertexConsumerProvider, CallbackInfo ci, @Local Iterator<OrderedRenderCommandQueueImpl.MovingBlockCommand> iterator) {
		while (iterator.hasNext()) {
			OrderedRenderCommandQueueImpl.MovingBlockCommand fallingBlockCommand = iterator.next();
			MovingBlockRenderState renderState = fallingBlockCommand.movingBlockRenderState();
			BlockState blockState = renderState.blockState;
			BlockStateModel model = blockRenderManager.getModel(blockState);
			long seed = blockState.getRenderingSeed(renderState.fallingBlockPos);
			matrices.push();
			matrices.multiplyPositionMatrix(fallingBlockCommand.matricesEntry());
			blockRenderManager.getModelRenderer().render(renderState, model, blockState, renderState.entityBlockPos, matrices, RenderLayerHelper.movingDelegate(vertexConsumers), false, seed, OverlayTexture.DEFAULT_UV);
			matrices.pop();
		}
	}
}
