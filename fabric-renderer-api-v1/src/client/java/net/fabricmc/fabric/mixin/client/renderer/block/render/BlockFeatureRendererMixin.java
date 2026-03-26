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

import java.util.function.Consumer;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
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
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.feature.BlockFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.ChunkSectionLayerHelper;
import net.fabricmc.fabric.api.client.renderer.v1.render.ExtraLightCoordsUtil;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;

// TODO FRAPI 26.1: how much of this, if any, should be moved to Indigo?
@Mixin(BlockFeatureRenderer.class)
abstract class BlockFeatureRendererMixin {
	@Shadow
	@Final
	private RandomSource random;

	@Inject(method = "renderMovingBlockSubmits", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.<init>(ZZLnet/minecraft/client/color/block/BlockColors;)V"))
	private void beforeInitBlockRenderer(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, BlockStateModelSet blockStateModelSet, OptionsRenderState optionsState, boolean translucent, CallbackInfo ci, @Local PoseStack poseStack, @Share("altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share("altQuadOutput") LocalRef<QuadEmitter> altQuadOutput) {
		altBlockRenderer.set(Renderer.get().altModelBlockRenderer(optionsState.ambientOcclusion, false, Minecraft.getInstance().getBlockColors()));
		altQuadOutput.set(Renderer.get().quadEmitter(quad -> {
			RenderType renderType = ChunkSectionLayerHelper.getMovingBlockRenderType(quad.chunkLayer());
			VertexConsumer buffer = bufferSource.getBuffer(renderType);
			quad.buffer(OverlayTexture.NO_OVERLAY, poseStack.last(), buffer);
		}));
	}

	@Redirect(method = "renderMovingBlockSubmits", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V"))
	private void tesselateBlockProxy(ModelBlockRenderer blockRenderer, BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed, @Share("altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share("altQuadOutput") LocalRef<QuadEmitter> altQuadOutput) {
		altBlockRenderer.get().tesselateBlock(altQuadOutput.get(), x, y, z, level, pos, blockState, model, seed);
	}

	@Inject(method = "renderBlockModelSubmits", at = @At("RETURN"))
	private void onReturnRenderBlockModelSubmits(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, boolean translucent, CallbackInfo ci) {
		var quadConsumer = new Consumer<MutableQuadView>() {
			int[] tintLayers;
			int lightCoords;
			int overlayCoords;
			PoseStack.Pose pose;
			VertexConsumer buffer;
			@Nullable
			VertexConsumer outlineBuffer;

			@Override
			public void accept(MutableQuadView quad) {
				if (quad.emissive()) {
					quad.lightmap(LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT);
				} else {
					for (int i = 0; i < 4; i++) {
						quad.lightmap(i, ExtraLightCoordsUtil.smoothMax(quad.lightmap(i), lightCoords));
					}
				}

				int tintIndex = quad.tintIndex();

				if (tintIndex != -1 && tintIndex < tintLayers.length) {
					quad.multiplyColor(tintLayers[tintIndex]);
				}

				quad.buffer(overlayCoords, pose, buffer);
				if (outlineBuffer != null) {
					quad.buffer(overlayCoords, pose, outlineBuffer);
				}
			}
		};

		QuadEmitter output = Renderer.get().quadEmitter(quadConsumer);

		for (FabricSubmitNodeCollection.ExtendedBlockModelSubmit submit : nodeCollection.getExtendedBlockModelSubmits()) {
			if (submit.renderType().hasBlending() == translucent) {
				VertexConsumer buffer = bufferSource.getBuffer(submit.renderType());
				VertexConsumer outlineBuffer;

				if (submit.outlineColor() != 0) {
					outlineBufferSource.setColor(submit.outlineColor());
					outlineBuffer = outlineBufferSource.getBuffer(submit.renderType());
				} else {
					outlineBuffer = null;
				}

				// FIXME 26.1: mixin doesn't allow this
//				quadConsumer.tintLayers = submit.tintLayers();
//				quadConsumer.lightCoords = submit.lightCoords();
//				quadConsumer.overlayCoords = submit.overlayCoords();
//				quadConsumer.pose = submit.pose();
//				quadConsumer.buffer = buffer;
//				quadConsumer.outlineBuffer = outlineBuffer;

				// FIXME FRAPI 26.1: submit.modelParts() is ignored. respect it (render before mesh) or remove it from the submit.
				submit.mesh().outputTo(output);
			}
		}
	}

	// TODO FRAPI 26.1: don't use an overwrite if possible
	@Overwrite
	private void renderBreakingBlockModelSubmits(final SubmitNodeCollection nodeCollection, final MultiBufferSource.BufferSource bufferSource) {
		var quadConsumer = new Consumer<MutableQuadView>() {
			PoseStack.Pose pose;
			VertexConsumer buffer;

			@Override
			public void accept(MutableQuadView quad) {
				quad.lightmap(LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT);
				quad.buffer(OverlayTexture.NO_OVERLAY, pose, buffer);
			}
		};

		QuadEmitter output = Renderer.get().quadEmitter(quadConsumer);

		for (SubmitNodeStorage.BreakingBlockModelSubmit submit : nodeCollection.getBreakingBlockModelSubmits()) {
			VertexConsumer buffer = new SheetedDecalTextureGenerator(bufferSource.getBuffer(ModelBakery.DESTROY_TYPES.get(submit.progress())), submit.pose(), 1.0F);
			// FIXME 26.1: mixin doesn't allow this
//			quadConsumer.pose = submit.pose();
//			quadConsumer.buffer = buffer;
			output.clear();
			random.setSeed(submit.seed());
			// TODO FRAPI 26.1: somehow pass the level, pos, and state here when available? maybe via extended submit type?
			submit.model().emitQuads(output, BlockAndTintGetter.EMPTY, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), random, _ -> false);
		}
	}
}
