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

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessLayerRenderState;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableMeshImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.SimpleBlockRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainLikeRenderContext;
import net.fabricmc.fabric.mixin.client.indigo.renderer.BlockRenderDispatcherAccessor;

/**
 * The Fabric default renderer implementation. Supports all features defined in the API.
 */
public class IndigoRenderer implements Renderer {
	public static final IndigoRenderer INSTANCE = new IndigoRenderer();

	private IndigoRenderer() { }

	@Override
	public MutableMesh mutableMesh() {
		return new MutableMeshImpl();
	}

	@Override
	public void render(ModelBlockRenderer modelRenderer, BlockAndTintGetter blockView, BlockStateModel model, BlockState state, BlockPos pos, PoseStack matrices, BlockVertexConsumerProvider vertexConsumers, boolean cull, long seed, int overlay) {
		TerrainLikeRenderContext.POOL.get().bufferModel(blockView, model, state, pos, matrices, vertexConsumers, cull, seed, overlay);
	}

	@Override
	public void render(PoseStack.Pose entry, BlockVertexConsumerProvider vertexConsumers, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
		SimpleBlockRenderContext.POOL.get().bufferModel(entry, vertexConsumers, model, red, green, blue, light, overlay, blockView, pos, state);
	}

	@Override
	public void renderBlockAsEntity(BlockRenderDispatcher renderManager, BlockState state, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BlockAndTintGetter blockView, BlockPos pos) {
		RenderShape blockRenderType = state.getRenderShape();

		if (blockRenderType != RenderShape.INVISIBLE) {
			BlockStateModel model = renderManager.getBlockModel(state);
			int tint = ((BlockRenderDispatcherAccessor) renderManager).getBlockColors().getColor(state, null, null, 0);
			float red = (tint >> 16 & 255) / 255.0F;
			float green = (tint >> 8 & 255) / 255.0F;
			float blue = (tint & 255) / 255.0F;
			FabricBlockModelRenderer.render(matrices.last(), RenderLayerHelper.entityDelegate(vertexConsumers), model, red, green, blue, light, overlay, blockView, pos, state);
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
