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

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BakedQuadOutput;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Redirect(
			method = "renderBlockDestroyAnimation",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/block/BakedQuadOutput;)V"
			)
	)
	private void redirectRenderBlockBreakingTexture(
			BlockRenderDispatcher instance,
			BlockState state,
			BlockPos pos,
			BlockAndTintGetter level,
			PoseStack poseStack,
			BakedQuadOutput output,
			@Local(name = "buffer") VertexConsumer buffer
	) {
		instance.renderBreakingTexture(state, pos, level, poseStack, buffer);
	}
}
