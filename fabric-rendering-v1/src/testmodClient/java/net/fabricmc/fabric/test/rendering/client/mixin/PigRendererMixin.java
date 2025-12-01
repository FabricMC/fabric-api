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

package net.fabricmc.fabric.test.rendering.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

/**
 * Tests {@link RenderStateDataKey} and {@link FabricRenderState}. Pigs will render the block they're standing on at their location.
 */
@Mixin(PigRenderer.class)
public class PigRendererMixin {
	@Unique
	private static final RenderStateDataKey<MovingBlockRenderState> MOVING_BLOCK = RenderStateDataKey.create(() -> "Moving block");

	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/pig/Pig;Lnet/minecraft/client/renderer/entity/state/PigRenderState;F)V", at = @At("TAIL"))
	private void updateRenderStateData(Pig entity, PigRenderState state, float tickProgress, CallbackInfo ci) {
		BlockState blockState = entity.getBlockStateOn();

		if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
			MovingBlockRenderState movingBlockRenderState = new MovingBlockRenderState();
			movingBlockRenderState.randomSeedPos = entity.getOnPos();
			movingBlockRenderState.blockPos = entity.blockPosition();
			movingBlockRenderState.blockState = entity.getBlockStateOn();
			movingBlockRenderState.biome = entity.level().getBiome(entity.blockPosition());
			movingBlockRenderState.level = entity.level();
			state.setData(MOVING_BLOCK, movingBlockRenderState);
		}
	}

	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/PigRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/MobRenderer;submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V"))
	private void renderUsingRenderStateData(PigRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
		MovingBlockRenderState movingBlockRenderState = state.getData(MOVING_BLOCK);

		if (movingBlockRenderState != null) {
			queue.submitMovingBlock(matrices, movingBlockRenderState);
		}
	}
}
