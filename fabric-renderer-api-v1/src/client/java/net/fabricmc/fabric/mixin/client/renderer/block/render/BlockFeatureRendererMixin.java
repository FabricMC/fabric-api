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

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.BlockFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.render.BlockMultiBufferSource;
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
	@Overwrite
	private void renderMovingBlockSubmits(final SubmitNodeCollection nodeCollection, final MultiBufferSource.BufferSource bufferSource, final BlockRenderDispatcher blockRenderDispatcher, final boolean translucent) {
		BlockMultiBufferSource blockBufferSource = ChunkSectionLayerHelper.movingDelegate(bufferSource);
		Predicate<ChunkSectionLayer> layerFilter = translucent ? layer -> layer == ChunkSectionLayer.TRANSLUCENT : layer -> layer != ChunkSectionLayer.TRANSLUCENT;

		for (SubmitNodeStorage.MovingBlockSubmit submit : nodeCollection.getMovingBlockSubmits()) {
			MovingBlockRenderState renderState = submit.movingBlockRenderState();
			BlockState blockState = renderState.blockState;
			BlockStateModel model = blockRenderDispatcher.getBlockModel(blockState);
			long seed = blockState.getSeed(renderState.randomSeedPos);
			poseStack.pushPose();
			poseStack.mulPose(submit.pose());
			blockRenderDispatcher.getModelRenderer().tesselateBlock(renderState, model, blockState, renderState.blockPos, poseStack, blockBufferSource, layerFilter, false, seed, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}

	// Support multi-chunk layer models (BlockSubmit) and ExtendedBlockSubmit.
	@Overwrite
	private void renderBlockSubmits(final SubmitNodeCollection nodeCollection, final MultiBufferSource.BufferSource bufferSource, final BlockRenderDispatcher blockRenderDispatcher, final OutlineBufferSource outlineBufferSource, final boolean translucent) {
		Predicate<ChunkSectionLayer> layerFilter = translucent ? layer -> layer == ChunkSectionLayer.TRANSLUCENT : layer -> layer != ChunkSectionLayer.TRANSLUCENT;

		for (SubmitNodeStorage.BlockSubmit submit : nodeCollection.getBlockSubmits()) {
			poseStack.pushPose();
			poseStack.last().set(submit.pose());
			blockRenderDispatcher.renderSingleBlock(submit.state(), poseStack, bufferSource, layerFilter, submit.lightCoords(), submit.overlayCoords(), EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO);

			if (submit.outlineColor() != 0) {
				outlineBufferSource.setColor(submit.outlineColor());
				blockRenderDispatcher.renderSingleBlock(submit.state(), poseStack, outlineBufferSource, layerFilter, submit.lightCoords(), submit.overlayCoords(), EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO);
			}

			poseStack.popPose();
		}

		for (ExtendedBlockSubmit submit : ((SubmitNodeCollectionExtension) nodeCollection).fabric_getExtendedBlockSubmits()) {
			poseStack.pushPose();
			poseStack.last().set(submit.pose());
			blockRenderDispatcher.renderSingleBlock(
					submit.state(), poseStack,
					bufferSource, layerFilter, submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos());

			if (submit.outlineColor() != 0) {
				outlineBufferSource.setColor(submit.outlineColor());
				blockRenderDispatcher.renderSingleBlock(
						submit.state(), poseStack,
						outlineBufferSource, layerFilter, submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos());
			}

			poseStack.popPose();
		}
	}

	// Support ExtendedBlockModelSubmit.
	@Inject(method = "renderBlockModelSubmits", at = @At("RETURN"))
	private void onReturnRenderBlockModelSubmits(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, boolean translucent, CallbackInfo ci) {
		DelegatingBlockMultiBufferSourceImpl blockMultiBufferSource = new DelegatingBlockMultiBufferSourceImpl(translucent);

		for (ExtendedBlockModelSubmit submit : ((SubmitNodeCollectionExtension) nodeCollection).fabric_getExtendedBlockModelSubmits()) {
			blockMultiBufferSource.renderTypeFunction = submit.renderTypeFunction();
			blockMultiBufferSource.multiBufferSource = bufferSource;
			FabricModelBlockRenderer.renderModel(
					submit.pose(), blockMultiBufferSource, blockMultiBufferSource, submit.model(), submit.r(), submit.g(),
					submit.b(), submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos(),
					submit.state());

			if (submit.outlineColor() != 0) {
				outlineBufferSource.setColor(submit.outlineColor());
				blockMultiBufferSource.multiBufferSource = outlineBufferSource;
				FabricModelBlockRenderer.renderModel(
						submit.pose(), blockMultiBufferSource, blockMultiBufferSource, submit.model(), submit.r(), submit.g(),
						submit.b(), submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos(),
						submit.state());
			}
		}
	}
}
