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

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BakedQuadOutput;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.render.ChunkSectionLayerHelper;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricModelBlockRenderer;
import net.fabricmc.fabric.impl.client.indigo.Indigo;

@Mixin(BlockRenderDispatcher.class)
abstract class BlockRenderDispatcherMixin {
	@Shadow
	@Final
	private ModelBlockRenderer modelRenderer;

	@Definition(
			id = "getBlockModel",
			method = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/block/model/BlockStateModel;"
	)
	@Expression("? = ?.getBlockModel(?)")
	@Inject(
			method = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/block/BakedQuadOutput;)V",
			at = @At(
					value = "MIXINEXTRAS:EXPRESSION",
					shift = At.Shift.AFTER
			),
			cancellable = true
	)
	private void afterGetModel(
			BlockState blockState,
			BlockPos blockPos,
			BlockAndTintGetter level,
			PoseStack poseStack,
			BakedQuadOutput output,
			CallbackInfo ci,
			@Local(name = "model") BlockStateModel model
	) {
		modelRenderer.tesselateBlock(level, model, blockState, blockPos,
				poseStack, ChunkSectionLayerHelper.entityDelegate(Indigo.LEVEL_RENDERER_BUFFER_SOURCE.get()),
				true, blockState.getSeed(blockPos), OverlayTexture.NO_OVERLAY);
		ci.cancel();
	}

	@Redirect(method = "renderSingleBlock(Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/BakedQuadOutput;Lnet/minecraft/client/renderer/block/model/BlockStateModel;III)V"))
	private void renderProxy(
			PoseStack.Pose pose,
			BakedQuadOutput output,
			BlockStateModel model,
			int tint,
			int light,
			int overlay,
			@Local(name = "bufferSource") MultiBufferSource bufferSource
	) {
		FabricModelBlockRenderer.renderModel(pose,
				ChunkSectionLayerHelper.entityDelegate(bufferSource), model,
				tint, light, overlay, EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO, Blocks.AIR.defaultBlockState());
	}
}
