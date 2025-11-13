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

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.impl.renderer.BatchingRenderCommandQueueExtension;
import net.fabricmc.fabric.impl.renderer.ExtendedBlockCommand;
import net.fabricmc.fabric.impl.renderer.ExtendedBlockStateModelCommand;

@Mixin(SubmitNodeCollection.class)
abstract class SubmitNodeCollectionMixin implements OrderedSubmitNodeCollector, BatchingRenderCommandQueueExtension {
	@Shadow
	@Final
	private SubmitNodeStorage submitNodeStorage;
	@Shadow
	private boolean wasUsed;

	@Unique
	private final List<ExtendedBlockCommand> extendedBlockCommands = new ArrayList<>();
	@Unique
	private final List<ExtendedBlockStateModelCommand> extendedBlockStateModelCommands = new ArrayList<>();

	@Override
	public void submitBlock(PoseStack matrices, BlockState state, int light, int overlay, int outlineColor, BlockAndTintGetter blockView, BlockPos pos) {
		wasUsed = true;
		extendedBlockCommands.add(new ExtendedBlockCommand(matrices.last().copy(), state, light, overlay, outlineColor, blockView, pos));
		Minecraft.getInstance().getModelManager().specialBlockModelRenderer().renderByBlock(state.getBlock(), ItemDisplayContext.NONE, matrices, submitNodeStorage, light, overlay, outlineColor);
	}

	@Override
	public void submitBlockStateModel(PoseStack matrices, Function<ChunkSectionLayer, RenderType> renderLayerFunction, BlockStateModel model, float r, float g, float b, int light, int overlay, int outlineColor, BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
		wasUsed = true;
		extendedBlockStateModelCommands.add(new ExtendedBlockStateModelCommand(matrices.last().copy(), renderLayerFunction, model, r, g, b, light, overlay, outlineColor, blockView, pos, state));
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
