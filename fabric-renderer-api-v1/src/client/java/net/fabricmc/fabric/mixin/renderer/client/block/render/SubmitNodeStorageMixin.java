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

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(SubmitNodeStorage.class)
abstract class SubmitNodeStorageMixin implements SubmitNodeCollector {
	@Override
	public void submitBlock(PoseStack matrices, BlockState state, int light, int overlay, int outlineColor, BlockAndTintGetter blockView, BlockPos pos) {
		order(0).submitBlock(matrices, state, light, overlay, outlineColor, blockView, pos);
	}

	@Override
	public void submitBlockStateModel(PoseStack matrices, Function<ChunkSectionLayer, RenderType> renderLayerFunction, BlockStateModel model, float r, float g, float b, int light, int overlay, int outlineColor, BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
		order(0).submitBlockStateModel(matrices, renderLayerFunction, model, r, g, b, light, overlay, outlineColor, blockView, pos, state);
	}
}
