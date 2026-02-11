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

package net.fabricmc.fabric.impl.client.indigo.renderer;

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.BlockMultiBufferSource;
import net.fabricmc.fabric.api.client.renderer.v1.render.ChunkSectionLayerHelper;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricModelBlockRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.ItemRenderTypeGetter;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessLayerRenderState;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableMeshImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.SimpleBlockRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainLikeRenderContext;
import net.fabricmc.fabric.mixin.client.indigo.renderer.BlockRenderDispatcherAccessor;

/**
 * The Fabric default renderer implementation. Supports all features defined in the API.
 */
public class IndigoRenderer implements Renderer {
	private static IndigoRenderer instance;

	private IndigoRenderer() {
	}

	static IndigoRenderer getOrCreateInstance() {
		if (instance == null) {
			instance = new IndigoRenderer();
		}

		return instance;
	}

	@Override
	public MutableMesh mutableMesh() {
		return new MutableMeshImpl();
	}

	@Override
	public void tesselateBlock(ModelBlockRenderer blockRenderer, BlockAndTintGetter level, BlockStateModel model, BlockState state, BlockPos pos, PoseStack poseStack, BlockMultiBufferSource bufferSource, @Nullable Predicate<ChunkSectionLayer> layerFilter, boolean cull, long seed, int overlay) {
		TerrainLikeRenderContext.POOL.get().bufferModel(
				level, model, state, pos, poseStack, bufferSource, layerFilter, cull, seed, overlay);
	}

	@Override
	public void renderModel(PoseStack.Pose pose, BlockMultiBufferSource bufferSource, @Nullable Predicate<ChunkSectionLayer> layerFilter, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockAndTintGetter level, BlockPos pos, BlockState state) {
		SimpleBlockRenderContext.POOL.get().bufferModel(
				pose, bufferSource, layerFilter, model, red, green, blue, light, overlay, level, pos, state);
	}

	@Override
	public void renderSingleBlock(BlockRenderDispatcher renderDispatcher, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, @Nullable Predicate<ChunkSectionLayer> layerFilter, int light, int overlay, BlockAndTintGetter level, BlockPos pos) {
		RenderShape renderShape = state.getRenderShape();

		if (renderShape != RenderShape.INVISIBLE) {
			BlockStateModel model = renderDispatcher.getBlockModel(state);
			int tint = ((BlockRenderDispatcherAccessor) renderDispatcher).getBlockColors().getColor(state, null, null, 0);
			float red = (tint >> 16 & 255) / 255.0F;
			float green = (tint >> 8 & 255) / 255.0F;
			float blue = (tint & 255) / 255.0F;
			FabricModelBlockRenderer.renderModel(
					poseStack.last(), ChunkSectionLayerHelper.entityDelegate(
							bufferSource), layerFilter, model, red, green, blue, light, overlay,
					level, pos, state);
		}
	}

	@Override
	public QuadEmitter getLayerRenderStateEmitter(ItemStackRenderState.LayerRenderState layer) {
		return ((AccessLayerRenderState) layer).fabric_getMutableMesh().emitter();
	}

	@Override
	public void setLayerRenderTypeGetter(
			ItemStackRenderState.LayerRenderState layer,
			ItemRenderTypeGetter renderTypeGetter
	) {
		((AccessLayerRenderState) layer).fabric_setRenderTypeGetter(renderTypeGetter);
	}
}
