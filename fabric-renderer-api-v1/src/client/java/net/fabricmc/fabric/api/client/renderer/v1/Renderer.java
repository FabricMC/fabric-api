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

package net.fabricmc.fabric.api.client.renderer.v1;

import java.util.List;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.BlockMultiBufferSource;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricBlockRenderDispatcher;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricModelBlockRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.ItemRenderTypeGetter;
import net.fabricmc.fabric.impl.client.renderer.RendererManager;

/**
 * Interface for rendering plug-ins that provide enhanced capabilities
 * for model lighting, buffering and rendering. Such plug-ins implement the
 * enhanced model rendering interfaces specified by the Fabric API.
 *
 * <p>Renderers must ensure that terrain buffering supports {@link BlockStateModel#emitQuads}, which happens in
 * {@link SectionCompiler} in vanilla; this code is not patched automatically. Renderers must also ensure that the
 * following vanilla methods support {@link BlockStateModel#emitQuads}; these methods are not patched automatically.
 *
 * <ul><li>{@link ModelBlockRenderer#renderModel(PoseStack.Pose, VertexConsumer, BlockStateModel, float, float, float, int, int)}
 *
 * <li>{@link BlockRenderDispatcher#renderBreakingTexture(BlockState, BlockPos, BlockAndTintGetter, PoseStack, VertexConsumer)}
 *
 * <li>{@link BlockRenderDispatcher#renderSingleBlock(BlockState, PoseStack, MultiBufferSource, int, int)}</ul>
 *
 * <p>All other places in vanilla code that invoke {@link BlockStateModel#collectParts(RandomSource, List)},
 * {@link BlockStateModel#collectParts(RandomSource)}, or
 * {@link ModelBlockRenderer#renderModel(PoseStack.Pose, VertexConsumer, BlockStateModel, float, float, float, int, int)}
 * are, where appropriate, patched automatically to invoke the corresponding method above or the corresponding method in
 * {@link FabricModelBlockRenderer} or {@link FabricBlockRenderDispatcher}.
 */
public interface Renderer {
	/**
	 * Access to the current {@link Renderer} for creating and retrieving mesh builders
	 * and materials.
	 *
	 * <p><b>Warning:</b> do not call this method before {@link ModInitializer} has been invoked. Doing
	 * so will likely crash. If you need to determine which renderer is chosen, use
	 * {@link RendererProvider#getModId()}.
	 */
	static Renderer get() {
		return RendererManager.getRenderer();
	}

	/**
	 * Obtain a new {@link MutableMesh} instance to build optimized meshes and create baked models
	 * with enhanced features.
	 *
	 * <p>Renderer does not retain a reference to returned instances, so they should be re-used
	 * when possible to avoid memory allocation overhead.
	 */
	MutableMesh mutableMesh();

	/**
	 * @see FabricModelBlockRenderer#tesselateBlock(BlockAndTintGetter, BlockStateModel, BlockState, BlockPos, PoseStack, BlockMultiBufferSource, Predicate, boolean, long, int)
	 */
	@ApiStatus.OverrideOnly
	void tesselateBlock(ModelBlockRenderer blockRenderer, BlockAndTintGetter level, BlockStateModel model, BlockState state, BlockPos pos, PoseStack poseStack, BlockMultiBufferSource bufferSource, @Nullable Predicate<ChunkSectionLayer> layerFilter, boolean cull, long seed, int overlay);

	/**
	 * @see FabricModelBlockRenderer#renderModel(PoseStack.Pose, BlockMultiBufferSource, Predicate, BlockStateModel, float, float, float, int, int, BlockAndTintGetter, BlockPos, BlockState)
	 */
	@ApiStatus.OverrideOnly
	void renderModel(PoseStack.Pose pose, BlockMultiBufferSource bufferSource, @Nullable Predicate<ChunkSectionLayer> layerFilter, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockAndTintGetter level, BlockPos pos, BlockState state);

	/**
	 * @see FabricBlockRenderDispatcher#renderSingleBlock(BlockState, PoseStack, MultiBufferSource, Predicate, int, int, BlockAndTintGetter, BlockPos)
	 */
	@ApiStatus.OverrideOnly
	void renderSingleBlock(BlockRenderDispatcher renderDispatcher, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, @Nullable Predicate<ChunkSectionLayer> layerFilter, int light, int overlay, BlockAndTintGetter level, BlockPos pos);

	/**
	 * @see FabricLayerRenderState#emitter()
	 */
	@ApiStatus.OverrideOnly
	QuadEmitter getLayerRenderStateEmitter(ItemStackRenderState.LayerRenderState layer);

	/**
	 * @see FabricLayerRenderState#setRenderTypeGetter(ItemRenderTypeGetter)
	 */
	@ApiStatus.OverrideOnly
	void setLayerRenderTypeGetter(ItemStackRenderState.LayerRenderState layer, ItemRenderTypeGetter renderTypeGetter);
}
