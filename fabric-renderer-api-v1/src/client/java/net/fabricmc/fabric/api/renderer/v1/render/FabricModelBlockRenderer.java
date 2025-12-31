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

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/// Note: This interface is automatically implemented on [ModelBlockRenderer] via Mixin and interface injection.
public interface FabricModelBlockRenderer {
	/// Alternative for
	/// [ModelBlockRenderer#tesselateBlock(BlockAndTintGetter, List, BlockState, BlockPos, PoseStack, VertexConsumer, boolean, int)]
	/// and
	/// [BlockRenderDispatcher#renderBatched(BlockState, BlockPos, BlockAndTintGetter, PoseStack, VertexConsumer, boolean, List)]
	/// that accepts a [BlockStateModel] instead of a `List<BlockModelPart>` and a
	/// [BlockMultiBufferSource] instead of a [VertexConsumer]. Also accepts the random seed. **Prefer
	/// using this method over the vanilla alternative to correctly retrieve geometry from models that implement
	/// [BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)] and to
	/// correctly buffer models that have geometry on multiple
	/// {@linkplain net.minecraft.client.renderer.chunk.ChunkSectionLayer chunk layers}.**
	///
	/// This method allows buffering a block model in a terrain-like context, which usually includes stages like
	/// culling, dynamic tinting, shading, and flat/smooth lighting.
	///
	/// @param level The level in which to render the model. **Should not be empty (i.e. not
	///                  [EmptyBlockAndTintGetter]).**
	/// @param model The model to render.
	/// @param state The block state.
	/// @param pos The position of the block in the level.
	/// @param poseStack The pose stack.
	/// @param bufferSource The buffer source.
	/// @param cull Whether to try to cull faces hidden by other blocks.
	/// @param seed The random seed. Usually retrieved by the caller from [BlockState#getSeed(BlockPos)].
	/// @param overlay The overlay value to pass to output [VertexConsumer]s.
	default void render(BlockAndTintGetter level, BlockStateModel model, BlockState state, BlockPos pos, PoseStack poseStack, BlockMultiBufferSource bufferSource, boolean cull, long seed, int overlay) {
		Renderer.get().render((ModelBlockRenderer) this,
				level, model, state, pos, poseStack,
				bufferSource, cull, seed, overlay);
	}

	/// Alternative for
	/// [ModelBlockRenderer#renderModel(PoseStack.Pose, VertexConsumer, BlockStateModel, float, float, float, int, int)]
	/// that accepts a [BlockMultiBufferSource] instead of a [VertexConsumer]. Also accepts the
	/// [BlockAndTintGetter], [BlockPos], and [BlockState] to pass to
	/// [BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)] when
	/// necessary. **Prefer using this method over the vanilla alternative to correctly buffer models that have geometry
	/// on multiple chunk layers and to provide the model with additional context.**
	///
	/// This method allows buffering a block model with minimal transformations to the model geometry. Usually used by
	/// entity renderers.
	///
	/// @param pose The pose.
	/// @param bufferSource The buffer source.
	/// @param model The model to render.
	/// @param red The red component of the tint color.
	/// @param green The green component of the tint color.
	/// @param blue The blue component of the tint color.
	/// @param light The minimum light value.
	/// @param overlay The overlay value.
	/// @param level The level in which to render the model. **Can be empty (i.e. [EmptyBlockAndTintGetter]).**
	/// @param pos The position of the block in the level. **Should be [BlockPos#ZERO] if the level is empty.
	///            **
	/// @param state The block state. **Should be `Blocks.AIR.getDefaultState()` if not applicable.**
	///
	/// @see FabricOrderedSubmitNodeCollector#submitBlockStateModel(PoseStack, Function, BlockStateModel, float, float, float, int, int, int, BlockAndTintGetter, BlockPos, BlockState)
	static void render(PoseStack.Pose pose, BlockMultiBufferSource bufferSource, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockAndTintGetter level, BlockPos pos, BlockState state) {
		Renderer.get().render(
				pose,
				bufferSource, model, red, green, blue, light, overlay,
				level, pos, state);
	}
}
