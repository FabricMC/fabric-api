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
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.block.ModelBlockRenderer;

import net.minecraft.client.renderer.texture.OverlayTexture;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.BlockFeatureRenderer;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.render.BlockMultiBufferSource;
import net.fabricmc.fabric.api.client.renderer.v1.render.ChunkSectionLayerHelper;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricBlockFeatureRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricModelBlockRenderer;
import net.fabricmc.fabric.impl.client.renderer.DelegatingBlockMultiBufferSourceImpl;
import net.fabricmc.fabric.impl.client.renderer.ExtendedBlockModelSubmit;
import net.fabricmc.fabric.impl.client.renderer.ExtendedBlockSubmit;
import net.fabricmc.fabric.impl.client.renderer.SubmitNodeCollectionExtension;

@Mixin(BlockFeatureRenderer.class)
abstract class BlockFeatureRendererMixin {
	@Unique
	@Final
	private PoseStack old$poseStack;

	// Support multi-chunk layer models (MovingBlockSubmit).
	@Overwrite
	private void renderMovingBlockSubmits(
			final SubmitNodeCollection nodeCollection,
			final MultiBufferSource.BufferSource bufferSource,
			final BlockStateModelSet blockStateModelSet,
			final OptionsRenderState optionsState,
			final boolean translucent
	) {
		PoseStack poseStack = new PoseStack();
		BlockMultiBufferSource blockBufferSource = ChunkSectionLayerHelper.movingDelegate(bufferSource);
		Predicate<ChunkSectionLayer> layerFilter = translucent ? layer -> layer == ChunkSectionLayer.TRANSLUCENT : layer -> layer != ChunkSectionLayer.TRANSLUCENT;
		ModelBlockRenderer blockRenderer = new ModelBlockRenderer(
				optionsState.ambientOcclusion,
				false,
				Minecraft.getInstance().getBlockColors()
		);

		for (SubmitNodeStorage.MovingBlockSubmit submit : nodeCollection.getMovingBlockSubmits()) {
			MovingBlockRenderState renderState = submit.movingBlockRenderState();
			BlockState blockState = renderState.blockState;
			BlockStateModel model = blockStateModelSet.get(blockState);

			if (model.hasMaterialFlag(1) == translucent) {
				long seed = blockState.getSeed(renderState.randomSeedPos);
				poseStack.setIdentity();
				poseStack.mulPose(submit.pose());
				blockRenderer.tesselateBlock(renderState, model, blockState, renderState.blockPos, poseStack, blockBufferSource, layerFilter, false, seed, OverlayTexture.NO_OVERLAY);
			}
		}
	}

	// Support multi-chunk layer models (BlockSubmit) and ExtendedBlockSubmit.
	@Overwrite
	private void old$renderBlockSubmits(final SubmitNodeCollection nodeCollection, final MultiBufferSource.BufferSource bufferSource, final BlockRenderDispatcher blockRenderDispatcher, final OutlineBufferSource outlineBufferSource, final boolean translucent) {
		Predicate<ChunkSectionLayer> layerFilter = translucent ? layer -> layer == ChunkSectionLayer.TRANSLUCENT : layer -> layer != ChunkSectionLayer.TRANSLUCENT;

		for (SubmitNodeStorage.BlockSubmit submit : nodeCollection.getBlockSubmits()) {
			old$poseStack.pushPose();
			old$poseStack.last().set(submit.pose());
			blockRenderDispatcher.renderSingleBlock(submit.state(), old$poseStack, bufferSource, layerFilter, submit.lightCoords(), submit.overlayCoords(), EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO);

			if (submit.outlineColor() != 0) {
				outlineBufferSource.setColor(submit.outlineColor());
				blockRenderDispatcher.renderSingleBlock(submit.state(), old$poseStack, outlineBufferSource, layerFilter, submit.lightCoords(), submit.overlayCoords(), EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO);
			}

			old$poseStack.popPose();
		}

		for (ExtendedBlockSubmit submit : ((SubmitNodeCollectionExtension) nodeCollection).fabric_getExtendedBlockSubmits()) {
			old$poseStack.pushPose();
			old$poseStack.last().set(submit.pose());
			blockRenderDispatcher.renderSingleBlock(
					submit.state(), old$poseStack,
					bufferSource, layerFilter, submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos());

			if (submit.outlineColor() != 0) {
				outlineBufferSource.setColor(submit.outlineColor());
				blockRenderDispatcher.renderSingleBlock(
						submit.state(), old$poseStack,
						outlineBufferSource, layerFilter, submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos());
			}

			old$poseStack.popPose();
		}
	}

	// Support FRAPI models in BlockModelSubmit and support ExtendedBlockModelSubmit.
	@Overwrite
	private void renderBlockModelSubmits(
			final SubmitNodeCollection nodeCollection,
			final MultiBufferSource.BufferSource bufferSource,
			final OutlineBufferSource outlineBufferSource,
			final boolean translucent
	) {
		for (SubmitNodeStorage.BlockModelSubmit submit : nodeCollection.getBlockModelSubmits()) {
			if (submit.renderType().hasBlending() == translucent) {
				VertexConsumer buffer = bufferSource.getBuffer(submit.renderType());
				FabricBlockFeatureRenderer.putModelQuads(
						submit.pose(), _ -> buffer, submit.modelParts()
				);

				VertexConsumer outlineBuffer;

				if (submit.outlineColor() != 0) {
					outlineBufferSource.setColor(submit.outlineColor());
					outlineBuffer = outlineBufferSource.getBuffer(submit.renderType());
				} else {
					outlineBuffer = null;
				}
			}
		}
	}

	// Support FRAPI models in BlockModelSubmit and support ExtendedBlockModelSubmit.
	@Overwrite
	private void old$renderBlockModelSubmits(final SubmitNodeCollection nodeCollection, final MultiBufferSource.BufferSource bufferSource, final OutlineBufferSource outlineBufferSource, final boolean translucent) {
		for (SubmitNodeStorage.BlockModelSubmit submit : nodeCollection.getBlockModelSubmits()) {
			if (submit.renderType().hasBlending() == translucent) {
				VertexConsumer buffer = bufferSource.getBuffer(submit.renderType());
				FabricModelBlockRenderer.renderModel(
						submit.pose(), _ -> buffer, submit.model(), submit.tintColor(), submit.lightCoords(), submit.overlayCoords(), BlockAndTintGetter.EMPTY, BlockPos.ZERO,
						Blocks.AIR.defaultBlockState());

				if (submit.outlineColor() != 0) {
					outlineBufferSource.setColor(submit.outlineColor());
					VertexConsumer outlineBuffer = outlineBufferSource.getBuffer(submit.renderType());
					FabricModelBlockRenderer.renderModel(
							submit.pose(), _ -> outlineBuffer, submit.model(), submit.tintColor(), submit.lightCoords(), submit.overlayCoords(), BlockAndTintGetter.EMPTY, BlockPos.ZERO,
							Blocks.AIR.defaultBlockState());
				}
			}
		}

		DelegatingBlockMultiBufferSourceImpl blockMultiBufferSource = new DelegatingBlockMultiBufferSourceImpl(translucent);

		for (ExtendedBlockModelSubmit submit : ((SubmitNodeCollectionExtension) nodeCollection).fabric_getExtendedBlockModelSubmits()) {
			blockMultiBufferSource.renderTypeFunction = submit.renderTypeFunction();
			blockMultiBufferSource.multiBufferSource = bufferSource;
			FabricModelBlockRenderer.renderModel(
					submit.pose(), blockMultiBufferSource, blockMultiBufferSource, submit.model(), submit.tintColor(), submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos(),
					submit.state());

			if (submit.outlineColor() != 0) {
				outlineBufferSource.setColor(submit.outlineColor());
				blockMultiBufferSource.multiBufferSource = outlineBufferSource;
				FabricModelBlockRenderer.renderModel(
						submit.pose(), blockMultiBufferSource, blockMultiBufferSource, submit.model(), submit.tintColor(), submit.lightCoords(), submit.overlayCoords(), submit.level(), submit.pos(),
						submit.state());
			}
		}
	}
}
