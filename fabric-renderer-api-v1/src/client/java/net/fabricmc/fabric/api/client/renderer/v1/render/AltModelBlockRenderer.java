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

package net.fabricmc.fabric.api.client.renderer.v1.render;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

/**
 * An alternative to {@link ModelBlockRenderer} that tessellates block models into a
 * {@link QuadEmitter} instead of a {@link BlockQuadOutput}.
 */
public interface AltModelBlockRenderer {
	/**
	 * An alternative to {@link ModelBlockRenderer#tesselateBlock(BlockQuadOutput, float, float, float, BlockAndTintGetter, BlockPos, BlockState, BlockStateModel, long)}
	 * that tessellates a {@link BlockStateModel} into a {@link QuadEmitter} instead of a
	 * {@link BlockQuadOutput}.
	 *
	 * @param output the quad output
	 * @param x the x position offset
	 * @param y the y position offset
	 * @param z the z position offset
	 * @param level the level to tessellate in
	 * @param pos the model's in-level position
	 * @param blockState the model's block state
	 * @param model the model
	 * @param seed the model's random seed
	 */
	void tesselateBlock(QuadEmitter output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed);
}
