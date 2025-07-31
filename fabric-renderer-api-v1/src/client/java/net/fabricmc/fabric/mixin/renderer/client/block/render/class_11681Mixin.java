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

import java.util.Iterator;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.class_11681;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.command.EntityRenderCommandQueueImpl;
import net.minecraft.client.render.entity.state.FallingBlockEntityRenderState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;

import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;

// TODO 1.21.9
@Mixin(class_11681.class)
abstract class class_11681Mixin {
	@Shadow
	@Final
	private MatrixStack field_61826;

	// Support multi-render layer models (FallingBlockCommand).
	@Inject(method = "method_72998", at = @At(value = "INVOKE", target = "java/util/Iterator.hasNext()Z", remap = false, ordinal = 0))
	private void beforeRenderFallingBlocks(EntityRenderCommandQueueImpl commandQueueImpl, VertexConsumerProvider.Immediate vertexConsumers, BlockRenderManager blockRenderManager, CallbackInfo ci, @Local Iterator<EntityRenderCommandQueueImpl.FallingBlockCommand> iterator) {
		while (iterator.hasNext()) {
			EntityRenderCommandQueueImpl.FallingBlockCommand fallingBlockCommand = iterator.next();
			FallingBlockEntityRenderState renderState = fallingBlockCommand.renderState();
			BlockState blockState = renderState.blockState;
			BlockStateModel model = blockRenderManager.getModel(blockState);
			long seed = blockState.getRenderingSeed(renderState.fallingBlockPos);
			field_61826.push();
			field_61826.multiplyPositionMatrix(fallingBlockCommand.pose());
			blockRenderManager.getModelRenderer().render(renderState, model, blockState, renderState.currentPos, field_61826, RenderLayerHelper.movingDelegate(vertexConsumers), false, seed, OverlayTexture.DEFAULT_UV);
			field_61826.pop();
		}
	}
}
