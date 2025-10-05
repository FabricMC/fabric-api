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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.ItemCommandRenderer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;

import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessBatchingRenderCommandQueue;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.ItemRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.MeshItemCommand;

@Mixin(ItemCommandRenderer.class)
abstract class ItemCommandRendererMixin {
	@Shadow
	@Final
	private MatrixStack matrices;

	@Unique
	private final ItemRenderContext itemRenderContext = new ItemRenderContext();

	@Inject(method = "render", at = @At("RETURN"))
	private void onReturnRender(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers, OutlineVertexConsumerProvider outlineVertexConsumers, CallbackInfo ci) {
		for (MeshItemCommand itemCommand : ((AccessBatchingRenderCommandQueue) queue).fabric_getMeshItemCommands()) {
			matrices.push();
			matrices.peek().copy(itemCommand.positionMatrix());

			itemRenderContext.renderItem(itemCommand.displayContext(), matrices, vertexConsumers, itemCommand.lightCoords(), itemCommand.overlayCoords(), itemCommand.tintLayers(), itemCommand.quads(), itemCommand.mesh(), itemCommand.renderLayer(), itemCommand.glintType(), false);

			if (itemCommand.outlineColor() != 0) {
				outlineVertexConsumers.setColor(itemCommand.outlineColor());
				itemRenderContext.renderItem(itemCommand.displayContext(), matrices, outlineVertexConsumers, itemCommand.lightCoords(), itemCommand.overlayCoords(), itemCommand.tintLayers(), itemCommand.quads(), itemCommand.mesh(), itemCommand.renderLayer(), ItemRenderState.Glint.NONE, true);
			}

			matrices.pop();
		}
	}
}
