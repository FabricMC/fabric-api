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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.impl.renderer.BatchingRenderCommandQueueExtension;
import net.fabricmc.fabric.impl.renderer.ExtendedBlockCommand;
import net.fabricmc.fabric.impl.renderer.ExtendedBlockStateModelCommand;

@Mixin(BatchingRenderCommandQueue.class)
abstract class BatchingRenderCommandQueueMixin implements RenderCommandQueue, BatchingRenderCommandQueueExtension {
	@Shadow
	@Final
	private OrderedRenderCommandQueueImpl orderedQueueImpl;
	@Shadow
	private boolean hasCommands;

	@Unique
	private final List<ExtendedBlockCommand> extendedBlockCommands = new ArrayList<>();
	@Unique
	private final List<ExtendedBlockStateModelCommand> extendedBlockStateModelCommands = new ArrayList<>();

	@Override
	public void submitBlock(MatrixStack matrices, BlockState state, int light, int overlay, int outlineColor, BlockRenderView blockView, BlockPos pos) {
		hasCommands = true;
		extendedBlockCommands.add(new ExtendedBlockCommand(matrices.peek().copy(), state, light, overlay, outlineColor, blockView, pos));
		MinecraftClient.getInstance().getBakedModelManager().getBlockEntityModelsSupplier().get().render(state.getBlock(), ItemDisplayContext.NONE, matrices, orderedQueueImpl, light, overlay, outlineColor);
	}

	@Override
	public void submitBlockStateModel(MatrixStack matrices, Function<BlockRenderLayer, RenderLayer> renderLayerFunction, BlockStateModel model, float r, float g, float b, int light, int overlay, int outlineColor, BlockRenderView blockView, BlockPos pos, BlockState state) {
		hasCommands = true;
		extendedBlockStateModelCommands.add(new ExtendedBlockStateModelCommand(matrices.peek().copy(), renderLayerFunction, model, r, g, b, light, overlay, outlineColor, blockView, pos, state));
	}

	@Override
	public List<ExtendedBlockCommand> fabric_getExtendedBlockCommands() {
		return extendedBlockCommands;
	}

	@Override
	public List<ExtendedBlockStateModelCommand> fabric_getExtendedBlockStateModelCommands() {
		return extendedBlockStateModelCommands;
	}

	@Inject(method = "clear", at = @At("RETURN"))
	private void onReturnClear(CallbackInfo ci) {
		extendedBlockCommands.clear();
		extendedBlockStateModelCommands.clear();
	}
}
