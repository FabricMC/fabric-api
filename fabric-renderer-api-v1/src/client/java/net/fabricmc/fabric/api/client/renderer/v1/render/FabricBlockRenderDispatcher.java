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

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

// TODO 26.1: update docs
/**
 * Note: This interface is automatically implemented on {@link BlockRenderDispatcher} via Mixin and interface injection.
 */
public interface FabricBlockRenderDispatcher {
	/**
	 * Alternative for
	 * {@link BlockRenderDispatcher#renderBreakingTexture(BlockState, BlockPos, BlockAndTintGetter, PoseStack, BakedQuadOutput)}
	 * that accepts a {@link VertexConsumer} instead of a {@link BakedQuadOutput}. <b>Use this method instead of the
	 * vanilla alternative to correctly provide the model with a {@link VertexConsumer}.</b>
	 *
	 * @param state The block state.
	 * @param pos The block position.
	 * @param level The level in which to render the breaking texture. <b>Can be empty (i.e. {@link BlockAndTintGetter#EMPTY}).</b>
	 * @param poseStack The pose stack.
	 * @param vertexConsumer The vertex consumer. <b>Consider using {@link SheetedDecalTextureGenerator} in
	 * conjunction with one of {@link ModelBakery#DESTROY_TYPES} where the index is the breaking progress.</b>
	 */
	default void renderBreakingTexture(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack, VertexConsumer vertexConsumer) {
		Renderer.get().renderBreakingTexture(state, pos, level, poseStack, vertexConsumer);
	}

	/**
	 * Alternative for
	 * {@link BlockRenderDispatcher#renderSingleBlock(BlockState, PoseStack, MultiBufferSource, int, int)} that
	 * additionally accepts the {@link BlockAndTintGetter} and {@link BlockPos} to pass to
	 * {@link BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)}
	 * when necessary. <b>Use this method instead of the vanilla alternative to provide the model with additional
	 * context.</b>
	 *
	 * <p>This method allows buffering a block model with minimal transformations to the model geometry. Usually used by
	 * entity renderers.
	 *
	 * @param state The block state.
	 * @param poseStack The pose stack.
	 * @param bufferSource The buffer source.
	 * @param layerFilter Specifies the chunk layers for which geometry should be buffered ({@code true}) or discarded
	 *                    ({@code false}).
	 * @param light The minimum light value.
	 * @param overlay The overlay value.
	 * @param level The level in which to render the model. <b>Can be empty (i.e. {@link BlockAndTintGetter#EMPTY}).</b>
	 * @param pos The position of the block in the level. <b>Should be {@link BlockPos#ZERO} if the level is empty.
	 *            </b>
	 *
	 * @see FabricOrderedSubmitNodeCollector#submitBlock(PoseStack, BlockState, int, int, int, BlockAndTintGetter, BlockPos)
	 */
	default void renderSingleBlock(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, Predicate<ChunkSectionLayer> layerFilter, int light, int overlay, BlockAndTintGetter level, BlockPos pos) {
		Renderer.get().renderSingleBlock(state, poseStack, bufferSource, layerFilter, light, overlay, level, pos);
	}

	/**
	 * Alternative for
	 * {@link BlockRenderDispatcher#renderSingleBlock(BlockState, PoseStack, MultiBufferSource, int, int)} that
	 * additionally accepts the {@link BlockAndTintGetter} and {@link BlockPos} to pass to
	 * {@link BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)}
	 * when necessary. <b>Use this method instead of the vanilla alternative to provide the model with additional
	 * context.</b>
	 *
	 * <p>This method allows buffering a block model with minimal transformations to the model geometry. Usually used by
	 * entity renderers.
	 *
	 * @param state The block state.
	 * @param poseStack The pose stack.
	 * @param bufferSource The buffer source.
	 * @param light The minimum light value.
	 * @param overlay The overlay value.
	 * @param level The level in which to render the model. <b>Can be empty (i.e. {@link EmptyBlockAndTintGetter}).</b>
	 * @param pos The position of the block in the level. <b>Should be {@link BlockPos#ZERO} if the level is empty.
	 *            </b>
	 *
	 * @see FabricOrderedSubmitNodeCollector#submitBlock(PoseStack, BlockState, int, int, int, BlockAndTintGetter, BlockPos)
	 */
	default void renderSingleBlock(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay, BlockAndTintGetter level, BlockPos pos) {
		Renderer.get().renderSingleBlock(state, poseStack, bufferSource, null, light, overlay, level, pos);
	}
}
