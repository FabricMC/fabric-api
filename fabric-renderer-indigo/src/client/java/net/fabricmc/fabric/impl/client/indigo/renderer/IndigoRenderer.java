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

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessLayerRenderState;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableMeshImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.SimpleBlockRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainLikeRenderContext;
import net.fabricmc.fabric.mixin.client.indigo.renderer.BlockRenderManagerAccessor;

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
	public void render(BlockModelRenderer modelRenderer, BlockRenderView blockView, BlockStateModel model, BlockState state, BlockPos pos, MatrixStack matrices, BlockVertexConsumerProvider vertexConsumers, boolean cull, long seed, int overlay) {
		TerrainLikeRenderContext.POOL.get().bufferModel(blockView, model, state, pos, matrices, vertexConsumers, cull, seed, overlay);
	}

	@Override
	public void render(MatrixStack.Entry entry, BlockVertexConsumerProvider vertexConsumers, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockRenderView blockView, BlockPos pos, BlockState state) {
		SimpleBlockRenderContext.POOL.get().bufferModel(entry, vertexConsumers, model, red, green, blue, light, overlay, blockView, pos, state);
	}

	@Override
	public void renderBlockAsEntity(BlockRenderManager renderManager, BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BlockRenderView blockView, BlockPos pos) {
		BlockRenderType blockRenderType = state.getRenderType();

		if (blockRenderType != BlockRenderType.INVISIBLE) {
			BlockStateModel model = renderManager.getModel(state);
			int tint = ((BlockRenderManagerAccessor) renderManager).getBlockColors().getColor(state, null, null, 0);
			float red = (tint >> 16 & 255) / 255.0F;
			float green = (tint >> 8 & 255) / 255.0F;
			float blue = (tint & 255) / 255.0F;
			FabricBlockModelRenderer.render(matrices.peek(), RenderLayerHelper.entityDelegate(vertexConsumers), model, red, green, blue, light, overlay, blockView, pos, state);
			((BlockRenderManagerAccessor) renderManager).getBlockEntityModelsGetter().get().render(state.getBlock(), ItemDisplayContext.NONE, matrices, vertexConsumers, light, overlay);
		}
	}

	@Override
	public QuadEmitter getLayerRenderStateEmitter(ItemRenderState.LayerRenderState layer) {
		return ((AccessLayerRenderState) layer).fabric_getMutableMesh().emitter();
	}
}
