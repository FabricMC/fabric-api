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

import net.fabricmc.fabric.api.client.renderer.v1.render.ChunkSectionLayerHelper;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricModelBlockRenderer;
import net.fabricmc.fabric.impl.client.renderer.DelegatingBlockMultiBufferSourceImpl;
import net.fabricmc.fabric.impl.client.renderer.ExtendedBlockModelSubmit;
import net.fabricmc.fabric.impl.client.renderer.ExtendedBlockSubmit;
import net.fabricmc.fabric.impl.client.renderer.SubmitNodeCollectionExtension;

@Mixin(BlockFeatureRenderer.class)
abstract class BlockFeatureRendererMixin {
	@Shadow
	@Final
	private PoseStack poseStack;

	// Support multi-chunk layer models (MovingBlockSubmit).
	@Inject(method = "renderMovingBlockSubmits", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0))
	private void beforeRenderMovingBlocks(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderDispatcher, boolean translucent, CallbackInfo ci, @Local Iterator<SubmitNodeStorage.MovingBlockSubmit> iterator) {
		while (iterator.hasNext()) {
			SubmitNodeStorage.MovingBlockSubmit command = iterator.next();
			MovingBlockRenderState renderState = command.movingBlockRenderState();
			BlockState blockState = renderState.blockState;
			BlockStateModel model = blockRenderDispatcher.getBlockModel(blockState);
			long seed = blockState.getSeed(renderState.randomSeedPos);
			poseStack.pushPose();
			poseStack.mulPose(command.pose());
			blockRenderDispatcher.getModelRenderer().render(renderState, model, blockState, renderState.blockPos, poseStack, ChunkSectionLayerHelper.movingDelegate(
					bufferSource), false, seed, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}

	// Support ExtendedBlockSubmit and ExtendedBlockModelSubmit.
	@Inject(method = "renderBlockSubmits", at = @At("RETURN"))
	private void onReturnRender(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderDispatcher, OutlineBufferSource outlineBufferSource, boolean translucent, CallbackInfo ci) {
		DelegatingBlockMultiBufferSourceImpl blockMultiBufferSource = new DelegatingBlockMultiBufferSourceImpl();

		for (ExtendedBlockSubmit submit : ((SubmitNodeCollectionExtension) nodeCollection).fabric_getExtendedBlockSubmits()) {
			poseStack.pushPose();
			poseStack.last().set(submit.pose());
			blockRenderDispatcher.renderBlockAsEntity(
					submit.state(), poseStack,
					bufferSource, submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos());

			if (submit.outlineColor() != 0) {
				outlineBufferSource.setColor(submit.outlineColor());
				blockRenderDispatcher.renderBlockAsEntity(
						submit.state(), poseStack,
						outlineBufferSource, submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos());
			}

			poseStack.popPose();
		}

		for (ExtendedBlockModelSubmit submit : ((SubmitNodeCollectionExtension) nodeCollection).fabric_getExtendedBlockModelSubmits()) {
			blockMultiBufferSource.renderTypeFunction = submit.renderTypeFunction();
			blockMultiBufferSource.multiBufferSource = bufferSource;
			FabricModelBlockRenderer.render(
					submit.pose(),
					blockMultiBufferSource, submit.model(), submit.r(), submit.g(), submit.b(), submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos(), submit.state());

			if (submit.outlineColor() != 0) {
				outlineBufferSource.setColor(submit.outlineColor());
				blockMultiBufferSource.multiBufferSource = outlineBufferSource;
				FabricModelBlockRenderer.render(
						submit.pose(),
						blockMultiBufferSource, submit.model(), submit.r(), submit.g(), submit.b(), submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos(), submit.state());
			}
		}
	}
}
