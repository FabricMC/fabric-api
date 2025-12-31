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

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/// Note: This interface is automatically implemented on [OrderedSubmitNodeCollector] via Mixin and interface injection.
public interface FabricOrderedSubmitNodeCollector {
	/// Alternative for
	/// [OrderedSubmitNodeCollector#submitBlock(PoseStack, BlockState, int, int, int)] that additionally accepts the
	/// [BlockAndTintGetter] and [BlockPos] to pass to
	/// [BlockStateModel#emitQuads(net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, net.minecraft.util.RandomSource, java.util.function.Predicate)] when
	/// necessary. **Prefer using this method over the vanilla alternative to correctly render models that have geometry
	/// on multiple {@linkplain ChunkSectionLayer chunk layers} and to provide the model with additional context.**
	///
	/// This method allows rendering a block model with minimal transformations to the model geometry. Also invokes
	/// the [net.minecraft.client.renderer.special.SpecialModelRenderer]. Usually used by entity renderers.
	///
	/// @param poseStack The pose stack.
	/// @param state The block state.
	/// @param light The minimum light value.
	/// @param overlay The overlay value.
	/// @param outlineColor The outline color.
	/// @param level The level in which to render the model. **Can be empty (i.e. [net.minecraft.world.level.EmptyBlockAndTintGetter]).**
	///                  **Must not be mutated after calling this method.**
	/// @param pos The position of the block in the level. **Should be [BlockPos#ZERO] if the level is empty.
	///            ** **Must not be mutated after calling this method.**
	///
	/// @see FabricBlockRenderDispatcher#renderBlockAsEntity(BlockState, PoseStack, net.minecraft.client.renderer.MultiBufferSource, int, int, BlockAndTintGetter, BlockPos)
	default void submitBlock(PoseStack poseStack, BlockState state, int light, int overlay, int outlineColor, BlockAndTintGetter level, BlockPos pos) {
		((OrderedSubmitNodeCollector) this).submitBlock(poseStack, state, light, overlay, outlineColor);
	}

	/// Alternative for
	/// [OrderedSubmitNodeCollector#submitBlockModel(PoseStack, RenderType, BlockStateModel, float, float, float, int, int, int)]
	/// that accepts a `Function<ChunkSectionLayer, RenderType>` instead of a [RenderType]. Also accepts the
	/// [BlockAndTintGetter], [BlockPos], and [BlockState] to pass to
	/// [BlockStateModel#emitQuads(net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, net.minecraft.util.RandomSource, java.util.function.Predicate)] when
	/// necessary. **Prefer using this method over the vanilla alternative to correctly render models that have geometry
	/// on multiple {@linkplain ChunkSectionLayer chunk layers} and to provide the model with additional context.**
	///
	/// This method allows rendering a block model with minimal transformations to the model geometry. Usually used by
	/// entity renderers.
	///
	/// @param poseStack The pose stack.
	/// @param renderTypeFunction The function to use to convert [ChunkSectionLayer]s to [RenderType]s.
	///                            **Must not be mutated after calling this method.**
	/// @param model The model to render.
	/// @param r The red component of the tint color.
	/// @param g The green component of the tint color.
	/// @param b The blue component of the tint color.
	/// @param light The minimum light value.
	/// @param overlay The overlay value.
	/// @param outlineColor The outline color.
	/// @param level The level in which to render the model. **Can be empty (i.e. [net.minecraft.world.level.EmptyBlockAndTintGetter]).**
	///                  **Must not be mutated after calling this method.**
	/// @param pos The position of the block in the level. **Should be [BlockPos#ZERO] if the level is empty.
	///            ** **Must not be mutated after calling this method.**
	/// @param state The block state. **Should be `Blocks.AIR.getDefaultState()` if not applicable.**
	///
	/// @see FabricModelBlockRenderer#render(PoseStack.Pose, BlockMultiBufferSource, BlockStateModel, float, float, float, int, int, BlockAndTintGetter, BlockPos, BlockState)
	default void submitBlockStateModel(PoseStack poseStack, Function<ChunkSectionLayer, RenderType> renderTypeFunction, BlockStateModel model, float r, float g, float b, int light, int overlay, int outlineColor, BlockAndTintGetter level, BlockPos pos, BlockState state) {
		((OrderedSubmitNodeCollector) this).submitBlockModel(poseStack, renderTypeFunction.apply(ItemBlockRenderTypes.getChunkRenderType(state)), model, r, g, b, light, overlay, outlineColor);
	}
}
