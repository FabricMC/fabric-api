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

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.BlockFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.ChunkSectionLayerHelper;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;
import net.fabricmc.fabric.impl.client.renderer.BlockModelBufferCache;
import net.fabricmc.fabric.impl.client.renderer.QuadConsumers;

@Mixin(BlockFeatureRenderer.class)
abstract class BlockFeatureRendererMixin {
	@Shadow
	@Final
	private static Direction[] DIRECTIONS;
	@Shadow
	@Final
	private QuadInstance quadInstance;
	@Shadow
	@Final
	private RandomSource random;

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

	@Inject(method = "renderMovingBlockSubmits", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.<init>(ZZLnet/minecraft/client/color/block/BlockColors;)V"))
	private void beforeInitBlockRenderer(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, BlockStateModelSet blockStateModelSet, OptionsRenderState optionsState, boolean translucent, CallbackInfo ci, @Local(name = "poseStack") PoseStack poseStack, @Share("altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share("altQuadOutput") LocalRef<QuadEmitter> altQuadOutput) {
		altBlockRenderer.set(Renderer.get().altModelBlockRenderer(optionsState.ambientOcclusion, false, Minecraft.getInstance().getBlockColors()));
		altQuadOutput.set(Renderer.get().quadEmitter(quad -> {
			RenderType renderType = ChunkSectionLayerHelper.getMovingBlockRenderType(quad.chunkLayer());
			VertexConsumer buffer = bufferSource.getBuffer(renderType);
			quad.buffer(OverlayTexture.NO_OVERLAY, poseStack.last(), buffer);
		}));
	}

	@Redirect(method = "renderMovingBlockSubmits", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;hasMaterialFlag(I)Z"))
	private boolean hasMaterialFlagProxy(BlockStateModel model, @BakedQuad.MaterialFlags int flag, @Local(name = "movingBlockRenderState") MovingBlockRenderState movingBlockRenderState, @Local(name = "blockState") BlockState blockState) {
		long blockSeed = blockState.getSeed(movingBlockRenderState.randomSeedPos);
		random.setSeed(blockSeed);
		return model.hasMaterialFlag(movingBlockRenderState, movingBlockRenderState.blockPos, blockState, random, flag);
	}

	@Redirect(method = "renderMovingBlockSubmits", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V"))
	private void tesselateBlockProxy(ModelBlockRenderer blockRenderer, BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed, @Share("altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share("altQuadOutput") LocalRef<QuadEmitter> altQuadOutput) {
		altBlockRenderer.get().tesselateBlock(altQuadOutput.get(), x, y, z, level, pos, blockState, model, seed);
	}

	@Inject(method = "renderBlockModelSubmits", at = @At("RETURN"))
	private void onReturnRenderBlockModelSubmits(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, boolean translucent, CallbackInfo ci) {
		BlockModelBufferCache bufferCache = new BlockModelBufferCache(bufferSource, outlineBufferSource);
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

	@Overwrite
	private void renderBreakingBlockModelSubmits(final SubmitNodeCollection nodeCollection, final MultiBufferSource.BufferSource bufferSource) {
		QuadConsumers.BreakingBlockModel quadConsumer = new QuadConsumers.BreakingBlockModel();
		QuadEmitter output = Renderer.get().quadEmitter(quadConsumer);

		for (SubmitNodeStorage.BreakingBlockModelSubmit submit : nodeCollection.getBreakingBlockModelSubmits()) {
			VertexConsumer buffer = new SheetedDecalTextureGenerator(bufferSource.getBuffer(ModelBakery.DESTROY_TYPES.get(submit.progress())), submit.pose(), 1.0F);
			quadConsumer.pose = submit.pose();
			quadConsumer.buffer = buffer;
			output.clear();
			random.setSeed(submit.seed());
			// TODO 26.1: somehow pass the level, pos, and state here when available? maybe via extended submit type?
			submit.model().emitQuads(output, BlockAndTintGetter.EMPTY, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), random, _ -> false);
		}
	}
}
