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
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.feature.BlockFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.fabricmc.fabric.impl.renderer.BatchingRenderCommandQueueExtension;
import net.fabricmc.fabric.impl.renderer.DelegatingBlockVertexConsumerProviderImpl;
import net.fabricmc.fabric.impl.renderer.ExtendedBlockCommand;
import net.fabricmc.fabric.impl.renderer.ExtendedBlockStateModelCommand;

@Mixin(BlockFeatureRenderer.class)
abstract class BlockFeatureRendererMixin {
	@Shadow
	@Final
	private PoseStack poseStack;

	// Support multi-render layer models (MovingBlockCommand).
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0))
	private void beforeRenderMovingBlocks(SubmitNodeCollection queue, MultiBufferSource.BufferSource vertexConsumers, BlockRenderDispatcher blockRenderManager, OutlineBufferSource outlineVertexConsumers, CallbackInfo ci, @Local Iterator<SubmitNodeStorage.MovingBlockSubmit> iterator) {
		while (iterator.hasNext()) {
			SubmitNodeStorage.MovingBlockSubmit command = iterator.next();
			MovingBlockRenderState renderState = command.movingBlockRenderState();
			BlockState blockState = renderState.blockState;
			BlockStateModel model = blockRenderManager.getBlockModel(blockState);
			long seed = blockState.getSeed(renderState.randomSeedPos);
			poseStack.pushPose();
			poseStack.mulPose(command.pose());
			blockRenderManager.getModelRenderer().render(renderState, model, blockState, renderState.blockPos, poseStack, RenderLayerHelper.movingDelegate(vertexConsumers), false, seed, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}

	// Support ExtendedBlockCommand and ExtendedBlockStateModelCommand.
	@Inject(method = "render", at = @At("RETURN"))
	private void onReturnRender(SubmitNodeCollection queue, MultiBufferSource.BufferSource vertexConsumers, BlockRenderDispatcher blockRenderManager, OutlineBufferSource outlineVertexConsumers, CallbackInfo ci) {
		DelegatingBlockVertexConsumerProviderImpl blockVertexConsumerProvider = new DelegatingBlockVertexConsumerProviderImpl();

		for (ExtendedBlockCommand command : ((BatchingRenderCommandQueueExtension) queue).fabric_getExtendedBlockCommands()) {
			poseStack.pushPose();
			poseStack.last().set(command.matricesEntry());
			blockRenderManager.renderBlockAsEntity(command.state(), poseStack, vertexConsumers, command.lightCoords(), command.overlayCoords(), command.blockView(), command.pos());

			if (command.outlineColor() != 0) {
				outlineVertexConsumers.setColor(command.outlineColor());
				blockRenderManager.renderBlockAsEntity(command.state(), poseStack, outlineVertexConsumers, command.lightCoords(), command.overlayCoords(), command.blockView(), command.pos());
			}

			poseStack.popPose();
		}

		for (ExtendedBlockStateModelCommand command : ((BatchingRenderCommandQueueExtension) queue).fabric_getExtendedBlockStateModelCommands()) {
			blockVertexConsumerProvider.renderLayerFunction = command.renderLayerFunction();
			blockVertexConsumerProvider.vertexConsumerProvider = vertexConsumers;
			FabricBlockModelRenderer.render(command.matricesEntry(), blockVertexConsumerProvider, command.model(), command.r(), command.g(), command.b(), command.lightCoords(), command.overlayCoords(), command.blockView(), command.pos(), command.state());

			if (command.outlineColor() != 0) {
				outlineVertexConsumers.setColor(command.outlineColor());
				blockVertexConsumerProvider.vertexConsumerProvider = outlineVertexConsumers;
				FabricBlockModelRenderer.render(command.matricesEntry(), blockVertexConsumerProvider, command.model(), command.r(), command.g(), command.b(), command.lightCoords(), command.overlayCoords(), command.blockView(), command.pos(), command.state());
			}
		}
	}
}
