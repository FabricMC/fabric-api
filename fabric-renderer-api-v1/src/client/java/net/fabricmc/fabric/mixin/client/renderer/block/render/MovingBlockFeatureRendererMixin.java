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

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.MovingBlockFeatureRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.ChunkSectionLayerHelper;

@Mixin(MovingBlockFeatureRenderer.class)
abstract class MovingBlockFeatureRendererMixin {
	@Inject(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.<init>(ZZLnet/minecraft/client/color/block/BlockColors;)V"))
	private void beforeInitBlockRenderer(SubmitNodeCollection nodeCollection, FeatureFrameContext context, boolean translucent, CallbackInfo ci, @Local(name = "poseStack") PoseStack poseStack, @Share("altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share("altQuadOutput") LocalRef<QuadEmitter> altQuadOutput) {
		altBlockRenderer.set(Renderer.get().altModelBlockRenderer(context.options().ambientOcclusion, false, Minecraft.getInstance().getBlockColors()));
		altQuadOutput.set(Renderer.get().quadEmitter(quad -> {
			quad.buffer(0, poseStack.last(), context.bufferSource().getBuffer(ChunkSectionLayerHelper.getMovingBlockRenderType(quad.chunkLayer())));
		}));
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;hasMaterialFlag(I)Z"))
	private boolean hasMaterialFlagProxy(BlockStateModel model, @BakedQuad.MaterialFlags int flag, @Local(name = "movingBlockRenderState") MovingBlockRenderState movingBlockRenderState, @Local(name = "blockState") BlockState blockState) {
		RandomSource random = RandomSource.createThreadLocalInstance(0L);
		long blockSeed = blockState.getSeed(movingBlockRenderState.randomSeedPos);
		random.setSeed(blockSeed);
		return model.hasMaterialFlag(movingBlockRenderState, movingBlockRenderState.blockPos, blockState, random, flag);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V"))
	private void tesselateBlockProxy(ModelBlockRenderer blockRenderer, BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed, @Share("altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share("altQuadOutput") LocalRef<QuadEmitter> altQuadOutput) {
		altBlockRenderer.get().tesselateBlock(altQuadOutput.get(), x, y, z, level, pos, blockState, model, seed);
	}
}
