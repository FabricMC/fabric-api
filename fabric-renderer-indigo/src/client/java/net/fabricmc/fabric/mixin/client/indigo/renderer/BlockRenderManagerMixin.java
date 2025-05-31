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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.EmptyBlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;

@Mixin(BlockRenderManager.class)
abstract class BlockRenderManagerMixin {
	@Shadow
	@Final
	private BlockModelRenderer blockModelRenderer;

	@Inject(method = "renderDamage(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V", at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/client/render/block/BlockModels.getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BlockStateModel;", shift = At.Shift.AFTER), cancellable = true)
	private void afterGetModel(BlockState blockState, BlockPos blockPos, BlockRenderView world, MatrixStack matrixStack, VertexConsumer vertexConsumer, CallbackInfo ci, @Local BlockStateModel model) {
		blockModelRenderer.render(world, model, blockState, blockPos, matrixStack, layer -> vertexConsumer, true, blockState.getRenderingSeed(blockPos), OverlayTexture.DEFAULT_UV);
		ci.cancel();
	}

	@Redirect(method = "renderBlockAsEntity(Lnet/minecraft/block/BlockState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At(value = "INVOKE", target = "net/minecraft/client/render/block/BlockModelRenderer.render(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/model/BlockStateModel;FFFII)V"))
	private void renderProxy(MatrixStack.Entry entry, VertexConsumer vertexConsumer, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light1, int overlay1) {
		FabricBlockModelRenderer.render(entry, RenderLayerHelper.entityDelegate(vertexConsumers), model, red, green, blue, light, overlay, EmptyBlockRenderView.INSTANCE, BlockPos.ORIGIN, state);
	}
}
