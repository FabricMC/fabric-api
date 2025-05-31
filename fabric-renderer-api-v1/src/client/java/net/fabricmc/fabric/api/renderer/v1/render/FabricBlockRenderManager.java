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

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.EmptyBlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/**
 * Note: This interface is automatically implemented on {@link BlockRenderManager} via Mixin and interface injection.
 */
public interface FabricBlockRenderManager {
	/**
	 * Alternative for
	 * {@link BlockRenderManager#renderBlockAsEntity(BlockState, MatrixStack, VertexConsumerProvider, int, int)} that
	 * additionally accepts the {@link BlockRenderView} and {@link BlockPos} to pass to
	 * {@link BlockStateModel#emitQuads(QuadEmitter, BlockRenderView, BlockPos, BlockState, Random, Predicate)} when
	 * necessary. <b>Prefer using this method over the vanilla alternative to correctly buffer models that have geometry
	 * on multiple render layers and to provide the model with additional context.</b>
	 *
	 * <p>This method allows buffering a block model with minimal transformations to the model geometry. Also invokes
	 * the {@link SpecialModelRenderer}. Usually used by entity renderers.
	 *
	 * @param state The block state.
	 * @param matrices The matrices.
	 * @param vertexConsumers The vertex consumers.
	 * @param light The minimum light value.
	 * @param overlay The overlay value.
	 * @param blockView The world in which to render the model. <b>Can be empty (i.e. {@link EmptyBlockRenderView}).</b>
	 * @param pos The position of the block in the world. <b>Should be {@link BlockPos#ORIGIN} if the world is empty.
	 *            </b>
	 */
	default void renderBlockAsEntity(BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BlockRenderView blockView, BlockPos pos) {
		Renderer.get().renderBlockAsEntity((BlockRenderManager) this, state, matrices, vertexConsumers, light, overlay, blockView, pos);
	}
}
