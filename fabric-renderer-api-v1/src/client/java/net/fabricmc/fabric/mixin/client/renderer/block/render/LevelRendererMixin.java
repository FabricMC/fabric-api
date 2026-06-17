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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.util.RandomSource;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
	@Inject(method = "submitBlockDestroyAnimation(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;createThreadLocalInstance()Lnet/minecraft/util/RandomSource;"))
	private void beforeCreateRandom(CallbackInfo ci, @Share("mutableMesh") LocalRef<MutableMesh> mutableMesh) {
		mutableMesh.set(Renderer.get().mutableMesh());
	}

	@Redirect(method = "submitBlockDestroyAnimation(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;collectParts(Lnet/minecraft/util/RandomSource;Ljava/util/List;)V"))
	private void cancelCollectParts(BlockStateModel model, RandomSource random, List<BlockStateModelPart> output) {
	}

	@Redirect(method = "submitBlockDestroyAnimation(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitBreakingBlockModel(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;I)V"))
	private void submitBreakingBlockModelProxy(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, List<BlockStateModelPart> parts, int progress, @Local(name = "random") RandomSource random, @Local(name = "state") BlockBreakingRenderState state, @Local(name = "model") BlockStateModel model, @Share("mutableMesh") LocalRef<MutableMesh> mutableMeshRef) {
		MutableMesh mutableMesh = mutableMeshRef.get();
		mutableMesh.clear();
		model.emitQuads(mutableMesh.emitter(), BlockAndTintGetter.EMPTY, state.blockPos(), state.blockState(), random, _ -> false);
		submitNodeCollector.submitBreakingBlockModel(poseStack, parts, mutableMesh.immutableCopy(), progress);
	}
}
