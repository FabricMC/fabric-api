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

import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.MovingBlockFeatureRenderer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.ChunkSectionLayerHelper;
import net.fabricmc.fabric.impl.client.renderer.MovingBlockQuadConsumer;

@Mixin(MovingBlockFeatureRenderer.class)
abstract class MovingBlockFeatureRendererMixin extends RenderTypeFeatureRenderer<MovingBlockFeatureRenderer.Submit> {
	@Shadow
	@Final
	private PoseStack poseStack;

	@Inject(method = "buildGroup", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.<init>(ZZLnet/minecraft/client/color/block/BlockColors;)V"))
	private void beforeInitBlockRenderer(FeatureFrameContext context, List<MovingBlockFeatureRenderer.Submit> submits, CallbackInfo ci, @Share("altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share("altQuadOutput") LocalRef<QuadEmitter> altQuadOutput, @Share("quadConsumer") LocalRef<MovingBlockQuadConsumer> quadConsumerRef) {
		altBlockRenderer.set(Renderer.get().altModelBlockRenderer(context.options().ambientOcclusion, false, context.blockColors()));
		MovingBlockQuadConsumer quadConsumer = new MovingBlockQuadConsumer() {
			@Override
			public void accept(MutableQuadView quad) {
				RenderType renderType = ChunkSectionLayerHelper.getMovingBlockRenderType(quad.chunkLayer());
				VertexConsumer buffer;

				if (outlineColor != 0 && renderType.outline().isPresent()) {
					quad.color(outlineColor, outlineColor, outlineColor, outlineColor);
					buffer = getVertexBuilder(renderType.outline().get());
				} else {
					buffer = getVertexBuilder(renderType);
				}

				quad.buffer(OverlayTexture.NO_OVERLAY, poseStack.last(), buffer);
			}
		};
		altQuadOutput.set(Renderer.get().quadEmitter(quadConsumer));
		quadConsumerRef.set(quadConsumer);
	}

	@Redirect(method = "buildGroup", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V"))
	private void tesselateBlockProxy(ModelBlockRenderer blockRenderer, BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed, @Local(name = "submit") MovingBlockFeatureRenderer.Submit submit, @Share("altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share("altQuadOutput") LocalRef<QuadEmitter> altQuadOutput, @Share("quadConsumer") LocalRef<MovingBlockQuadConsumer> quadConsumer) {
		quadConsumer.get().outlineColor(submit.outlineColor());
		altBlockRenderer.get().tesselateBlock(altQuadOutput.get(), x, y, z, level, pos, blockState, model, seed);
	}
}
