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

package net.fabricmc.fabric.mixin.renderer.client.block.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ItemFrameRenderer.class)
abstract class ItemFrameRendererMixin {
	// Provide the BlockState as context.
	@Redirect(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitBlockModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/block/model/BlockStateModel;FFFIII)V"))
	private void renderProxy(SubmitNodeCollector commandQueue, PoseStack matrices, RenderType renderLayer, BlockStateModel model, float r, float g, float b, int light, int overlay, int outlineColor, @Local BlockState blockState) {
		// The vertex consumer is for a special layer that renders solid, but vanilla has no equivalent
		// cutout/translucent layers that we can use here without risking compatibility.
		commandQueue.submitBlockStateModel(matrices, blockLayer -> renderLayer, model, r, g, b, light, overlay, outlineColor, EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO, blockState);
	}
}
