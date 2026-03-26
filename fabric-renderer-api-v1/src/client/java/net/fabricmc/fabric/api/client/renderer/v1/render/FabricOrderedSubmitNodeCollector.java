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

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;

// TODO 26.1: update docs
/**
 * Note: This interface is automatically implemented on {@link OrderedSubmitNodeCollector} via Mixin and interface injection.
 */
public interface FabricOrderedSubmitNodeCollector {
//	/**
//	 * Alternative for
//	 * {@link OrderedSubmitNodeCollector#submitBlockModel(PoseStack, RenderType, List, int[], int, int, int)}
//	 * that accepts a {@code Function<ChunkSectionLayer, RenderType>} instead of a {@link RenderType}. Also accepts the
//	 * {@link BlockAndTintGetter}, {@link BlockPos}, and {@link BlockState} to pass to
//	 * {@link BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)} when
//	 * necessary. <b>Prefer using this method over the vanilla alternative to correctly render models that have geometry
//	 * on multiple {@linkplain ChunkSectionLayer chunk layers} and to provide the model with additional context.</b>
//	 *
//	 * <p>This method allows rendering a block model with minimal transformations to the model geometry. Usually used by
//	 * entity renderers.
//	 *
//	 * @param poseStack The pose stack.
//	 * @param renderTypeFunction The function to use to convert {@link ChunkSectionLayer}s to {@link RenderType}s.
//	 *                            <b>Must not be mutated after calling this method.</b>
//	 * @param model The model to render.
//	 * @param tintLayers The tint color layers.
//	 * @param lightCoords The minimum light value.
//	 * @param overlayCoords The overlay value.
//	 * @param outlineColor The outline color.
//	 * @param level The level in which to render the model. <b>Can be empty (i.e. {@link BlockAndTintGetter#EMPTY}).</b>
//	 *                  <b>Must not be mutated after calling this method.</b>
//	 * @param pos The position of the block in the level. <b>Should be {@link BlockPos#ZERO} if the level is empty.
//	 *            </b> <b>Must not be mutated after calling this method.</b>
//	 * @param state The block state. <b>Should be {@code Blocks.AIR.getDefaultState()} if not applicable.</b>
//	 *
//	 * @see FabricBlockFeatureRenderer#putModelQuads(PoseStack.Pose, BlockMultiBufferSource, BlockStateModel, int[], int, int, BlockAndTintGetter, BlockPos, BlockState)
//	 */
//	default void submitBlockModel(PoseStack poseStack, Function<ChunkSectionLayer, RenderType> renderTypeFunction, BlockStateModel model, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor, BlockAndTintGetter level, BlockPos pos, BlockState state) {
//		this.submitBlockModel(poseStack, renderTypeFunction, FrapiRenderTypes.getBlockModelRenderType(model), RandomSource.create(state.getSeed(pos)), model, tintLayers, lightCoords, overlayCoords, outlineColor, level, pos, state);
//	}
//
//	/**
//	 * Alternative for
//	 * {@link OrderedSubmitNodeCollector#submitBlockModel(PoseStack, RenderType, List, int[], int, int, int)}
//	 * that accepts a {@code Function<ChunkSectionLayer, RenderType>} instead of a {@link RenderType}. Also accepts the
//	 * {@link BlockAndTintGetter}, {@link BlockPos}, and {@link BlockState} to pass to
//	 * {@link BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)} when
//	 * necessary. <b>Prefer using this method over the vanilla alternative to correctly render models that have geometry
//	 * on multiple {@linkplain ChunkSectionLayer chunk layers} and to provide the model with additional context.</b>
//	 *
//	 * <p>This method allows rendering a block model with minimal transformations to the model geometry. Usually used by
//	 * entity renderers.
//	 *
//	 * @param poseStack The pose stack.
//	 * @param renderTypeFunction The function to use to convert {@link ChunkSectionLayer}s to {@link RenderType}s.
//	 *                            <b>Must not be mutated after calling this method.</b>
//	 * @param renderType The {@link RenderType} to render with.
//	 * @param random The source of randomness.
//	 * @param mesh The mesh to render.
//	 * @param tintLayers The tint color layers.
//	 * @param lightCoords The minimum light value.
//	 * @param overlayCoords The overlay value.
//	 * @param outlineColor The outline color.
//	 * @param level The level in which to render the model. <b>Can be empty (i.e. {@link BlockAndTintGetter#EMPTY}).</b>
//	 *                  <b>Must not be mutated after calling this method.</b>
//	 * @param pos The position of the block in the level. <b>Should be {@link BlockPos#ZERO} if the level is empty.
//	 *            </b> <b>Must not be mutated after calling this method.</b>
//	 * @param state The block state. <b>Should be {@code Blocks.AIR.getDefaultState()} if not applicable.</b>
//	 *
//	 * @see FabricBlockFeatureRenderer#putModelQuads(PoseStack.Pose, BlockMultiBufferSource, BlockStateModel, int[], int, int, BlockAndTintGetter, BlockPos, BlockState)
//	 */
//	default void submitBlockModel(PoseStack poseStack, Function<ChunkSectionLayer, RenderType> renderTypeFunction, RenderType renderType, RandomSource random, MeshView mesh, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor, BlockAndTintGetter level, BlockPos pos, BlockState state) {
//		throw new IllegalStateException("Implemented via Mixin.");
//	}
//
//	/**
//	 * Alternative for
//	 * {@link OrderedSubmitNodeCollector#submitBlockModel(PoseStack, RenderType, List, int[], int, int, int)}
//	 * that accepts a {@code Function<ChunkSectionLayer, RenderType>} instead of a {@link RenderType}. Also accepts the
//	 * {@link BlockAndTintGetter}, {@link BlockPos}, and {@link BlockState} to pass to
//	 * {@link BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)} when
//	 * necessary. <b>Prefer using this method over the vanilla alternative to correctly render models that have geometry
//	 * on multiple {@linkplain ChunkSectionLayer chunk layers} and to provide the model with additional context.</b>
//	 *
//	 * <p>This method allows rendering a block model with minimal transformations to the model geometry. Usually used by
//	 * entity renderers.
//	 *
//	 * @param poseStack The pose stack.
//	 * @param renderTypeFunction The function to use to convert {@link ChunkSectionLayer}s to {@link RenderType}s.
//	 *                            <b>Must not be mutated after calling this method.</b>
//	 * @param renderType The {@link RenderType} to render with.
//	 * @param random The source of randomness.
//	 * @param model The model to render.
//	 * @param tintLayers The tint color layers.
//	 * @param lightCoords The minimum light value.
//	 * @param overlayCoords The overlay value.
//	 * @param outlineColor The outline color.
//	 * @param level The level in which to render the model. <b>Can be empty (i.e. {@link BlockAndTintGetter#EMPTY}).</b>
//	 *                  <b>Must not be mutated after calling this method.</b>
//	 * @param pos The position of the block in the level. <b>Should be {@link BlockPos#ZERO} if the level is empty.
//	 *            </b> <b>Must not be mutated after calling this method.</b>
//	 * @param state The block state. <b>Should be {@code Blocks.AIR.getDefaultState()} if not applicable.</b>
//	 *
//	 * @see FabricBlockFeatureRenderer#putModelQuads(PoseStack.Pose, BlockMultiBufferSource, BlockStateModel, int[], int, int, BlockAndTintGetter, BlockPos, BlockState)
//	 */
//	default void submitBlockModel(PoseStack poseStack, Function<ChunkSectionLayer, RenderType> renderTypeFunction, RenderType renderType, RandomSource random, BlockStateModel model, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor, BlockAndTintGetter level, BlockPos pos, BlockState state) {
//		((OrderedSubmitNodeCollector) this).submitBlockModel(poseStack, renderType, model.collectParts(random), tintLayers, lightCoords, overlayCoords, outlineColor);
//	}



	// TODO FRAPI 26.1
	// reintroduce Function<ChunkSectionLayer, RenderType> renderTypeFunction?
	// should this be MeshView instead of Mesh?
	default void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> parts, Mesh mesh, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
		((OrderedSubmitNodeCollector) this).submitBlockModel(poseStack, renderType, parts, tintLayers, lightCoords, overlayCoords, outlineColor);
	}

	default void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, MeshView mesh, ItemStackRenderState.FoilType foilType) {
		((OrderedSubmitNodeCollector) this).submitItem(poseStack, displayContext, lightCoords, overlayCoords, outlineColor, tintLayers, quads, foilType);
	}
}
