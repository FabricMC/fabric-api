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

import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/**
 * Note: This interface is automatically implemented on {@link OrderedSubmitNodeCollector} via Mixin and interface injection.
 */
public interface FabricRenderCommandQueue {
	/**
	 * Alternative for
	 * {@link OrderedSubmitNodeCollector#submitBlock(PoseStack, BlockState, int, int, int)} that additionally accepts the
	 * {@link BlockAndTintGetter} and {@link BlockPos} to pass to
	 * {@link BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)} when
	 * necessary. <b>Prefer using this method over the vanilla alternative to correctly render models that have geometry
	 * on multiple render layers and to provide the model with additional context.</b>
	 *
	 * <p>This method allows rendering a block model with minimal transformations to the model geometry. Also invokes
	 * the {@link SpecialModelRenderer}. Usually used by entity renderers.
	 *
	 * @param matrices The matrices.
	 * @param state The block state.
	 * @param light The minimum light value.
	 * @param overlay The overlay value.
	 * @param outlineColor The outline color.
	 * @param blockView The world in which to render the model. <b>Can be empty (i.e. {@link EmptyBlockAndTintGetter}).</b>
	 *                  <b>Must not be mutated after calling this method.</b>
	 * @param pos The position of the block in the world. <b>Should be {@link BlockPos#ZERO} if the world is empty.
	 *            </b> <b>Must not be mutated after calling this method.</b>
	 *
	 * @see FabricBlockRenderManager#renderBlockAsEntity(BlockState, PoseStack, MultiBufferSource, int, int, BlockAndTintGetter, BlockPos)
	 */
	default void submitBlock(PoseStack matrices, BlockState state, int light, int overlay, int outlineColor, BlockAndTintGetter blockView, BlockPos pos) {
		((OrderedSubmitNodeCollector) this).submitBlock(matrices, state, light, overlay, outlineColor);
	}

	/**
	 * Alternative for
	 * {@link OrderedSubmitNodeCollector#submitBlockModel(PoseStack, RenderType, BlockStateModel, float, float, float, int, int, int)}
	 * that accepts a {@code Function<BlockRenderLayer, RenderLayer>} instead of a {@link RenderType}. Also accepts the
	 * {@link BlockAndTintGetter}, {@link BlockPos}, and {@link BlockState} to pass to
	 * {@link BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)} when
	 * necessary. <b>Prefer using this method over the vanilla alternative to correctly render models that have geometry
	 * on multiple render layers and to provide the model with additional context.</b>
	 *
	 * <p>This method allows rendering a block model with minimal transformations to the model geometry. Usually used by
	 * entity renderers.
	 *
	 * @param matrices The matrices.
	 * @param renderLayerFunction The function to use to convert {@link ChunkSectionLayer}s to {@link RenderType}s.
	 *                            <b>Must not be mutated after calling this method.</b>
	 * @param model The model to render.
	 * @param r The red component of the tint color.
	 * @param g The green component of the tint color.
	 * @param b The blue component of the tint color.
	 * @param light The minimum light value.
	 * @param overlay The overlay value.
	 * @param outlineColor The outline color.
	 * @param blockView The world in which to render the model. <b>Can be empty (i.e. {@link EmptyBlockAndTintGetter}).</b>
	 *                  <b>Must not be mutated after calling this method.</b>
	 * @param pos The position of the block in the world. <b>Should be {@link BlockPos#ZERO} if the world is empty.
	 *            </b> <b>Must not be mutated after calling this method.</b>
	 * @param state The block state. <b>Should be {@code Blocks.AIR.getDefaultState()} if not applicable.</b>
	 *
	 * @see FabricBlockModelRenderer#render(PoseStack.Pose, BlockVertexConsumerProvider, BlockStateModel, float, float, float, int, int, BlockAndTintGetter, BlockPos, BlockState)
	 */
	default void submitBlockStateModel(PoseStack matrices, Function<ChunkSectionLayer, RenderType> renderLayerFunction, BlockStateModel model, float r, float g, float b, int light, int overlay, int outlineColor, BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
		((OrderedSubmitNodeCollector) this).submitBlockModel(matrices, renderLayerFunction.apply(ItemBlockRenderTypes.getChunkRenderType(state)), model, r, g, b, light, overlay, outlineColor);
	}
}
