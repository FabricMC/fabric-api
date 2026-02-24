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

package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.render.BlockMultiBufferSource;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;

public class SimpleBlockRenderContext extends AbstractRenderContext {
	public static final ThreadLocal<SimpleBlockRenderContext> POOL = ThreadLocal.withInitial(SimpleBlockRenderContext::new);

	private final RandomSource random = RandomSource.createNewThreadLocalInstance();

	private BlockMultiBufferSource bufferSource;
	@Nullable
	private Predicate<ChunkSectionLayer> layerFilter;
	private ChunkSectionLayer defaultChunkLayer;
	private float red;
	private float green;
	private float blue;
	private int light;

	@Nullable
	private ChunkSectionLayer lastChunkLayer;
	@Nullable
	private VertexConsumer lastVertexConsumer;

	@Override
	protected void bufferQuad(MutableQuadViewImpl quad) {
		final ChunkSectionLayer quadLayer = quad.chunkLayer();
		final ChunkSectionLayer layer = quadLayer == null ? defaultChunkLayer : quadLayer;

		if (layerFilter != null && !layerFilter.test(layer)) {
			return;
		}

		final VertexConsumer vertexConsumer;

		if (layer == lastChunkLayer) {
			vertexConsumer = lastVertexConsumer;
		} else {
			lastVertexConsumer = vertexConsumer = bufferSource.getBuffer(layer);
			lastChunkLayer = layer;
		}

		tintQuad(quad);
		shadeQuad(quad, quad.emissive());
		bufferQuad(quad, vertexConsumer);
	}

	private void tintQuad(MutableQuadViewImpl quad) {
		if (quad.tintIndex() != -1) {
			final float red = this.red;
			final float green = this.green;
			final float blue = this.blue;

			for (int i = 0; i < 4; i++) {
				quad.color(i, ARGB.scaleRGB(quad.color(i), red, green, blue));
			}
		}
	}

	private void shadeQuad(MutableQuadViewImpl quad, boolean emissive) {
		if (emissive) {
			for (int i = 0; i < 4; i++) {
				quad.lightmap(i, LightCoordsUtil.FULL_BRIGHT);
			}
		} else {
			final int light = this.light;

			for (int i = 0; i < 4; i++) {
				quad.lightmap(i, ColorHelper.maxLight(quad.lightmap(i), light));
			}
		}
	}

	public void bufferModel(PoseStack.Pose pose, BlockMultiBufferSource bufferSource, @Nullable Predicate<ChunkSectionLayer> layerFilter, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockAndTintGetter level, BlockPos pos, BlockState state) {
		this.pose = pose;
		this.overlay = overlay;

		this.bufferSource = bufferSource;
		this.layerFilter = layerFilter;
		this.defaultChunkLayer = ItemBlockRenderTypes.getChunkRenderType(state);
		this.red = Mth.clamp(red, 0, 1);
		this.green = Mth.clamp(green, 0, 1);
		this.blue = Mth.clamp(blue, 0, 1);
		this.light = light;

		random.setSeed(42L);

		model.emitQuads(getEmitter(), level, pos, state, random, _ -> false);

		this.pose = null;
		this.bufferSource = null;
		this.layerFilter = null;
		lastChunkLayer = null;
		lastVertexConsumer = null;
	}
}
