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

package net.fabricmc.fabric.api.renderer.v1.render;

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/**
 * Note: This interface is automatically implemented on {@link BlockRenderDispatcher} via Mixin and interface injection.
 */
public interface FabricBlockRenderManager {
	/**
	 * Alternative for
	 * {@link BlockRenderDispatcher#renderSingleBlock(BlockState, PoseStack, MultiBufferSource, int, int)} that
	 * additionally accepts the {@link BlockAndTintGetter} and {@link BlockPos} to pass to
	 * {@link BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)} when
	 * necessary. <b>Prefer using this method over the vanilla alternative to correctly buffer models that have geometry
	 * on multiple render layers and to provide the model with additional context.</b>
	 *
	 * <p>This method allows buffering a block model with minimal transformations to the model geometry. Usually used by
	 * entity renderers.
	 *
	 * @param state The block state.
	 * @param matrices The matrices.
	 * @param vertexConsumers The vertex consumers.
	 * @param light The minimum light value.
	 * @param overlay The overlay value.
	 * @param blockView The world in which to render the model. <b>Can be empty (i.e. {@link EmptyBlockAndTintGetter}).</b>
	 * @param pos The position of the block in the world. <b>Should be {@link BlockPos#ZERO} if the world is empty.
	 *            </b>
	 *
	 * @see FabricRenderCommandQueue#submitBlock(PoseStack, BlockState, int, int, int, BlockAndTintGetter, BlockPos)
	 */
	default void renderBlockAsEntity(BlockState state, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BlockAndTintGetter blockView, BlockPos pos) {
		Renderer.get().renderBlockAsEntity((BlockRenderDispatcher) this, state, matrices, vertexConsumers, light, overlay, blockView, pos);
	}
}
