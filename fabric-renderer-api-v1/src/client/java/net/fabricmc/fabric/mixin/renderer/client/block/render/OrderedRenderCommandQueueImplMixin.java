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

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

@Mixin(OrderedRenderCommandQueueImpl.class)
abstract class OrderedRenderCommandQueueImplMixin implements OrderedRenderCommandQueue {
	@Override
	public void submitBlock(MatrixStack matrices, BlockState state, int light, int overlay, int outlineColor, BlockRenderView blockView, BlockPos pos) {
		getBatchingQueue(0).submitBlock(matrices, state, light, overlay, outlineColor, blockView, pos);
	}

	@Override
	public void submitBlockStateModel(MatrixStack matrices, Function<BlockRenderLayer, RenderLayer> renderLayerFunction, BlockStateModel model, float r, float g, float b, int light, int overlay, int outlineColor, BlockRenderView blockView, BlockPos pos, BlockState state) {
		getBatchingQueue(0).submitBlockStateModel(matrices, renderLayerFunction, model, r, g, b, light, overlay, outlineColor, blockView, pos, state);
	}
}
