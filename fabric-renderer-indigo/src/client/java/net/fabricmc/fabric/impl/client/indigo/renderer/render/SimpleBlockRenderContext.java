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

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;

public class SimpleBlockRenderContext extends AbstractRenderContext {
	public static final ThreadLocal<SimpleBlockRenderContext> POOL = ThreadLocal.withInitial(SimpleBlockRenderContext::new);

	private final Random random = Random.createLocal();

	private BlockVertexConsumerProvider vertexConsumers;
	private BlockRenderLayer defaultRenderLayer;
	private float red;
	private float green;
	private float blue;
	private int light;

	@Nullable
	private BlockRenderLayer lastRenderLayer;
	@Nullable
	private VertexConsumer lastVertexConsumer;

	@Override
	protected void bufferQuad(MutableQuadViewImpl quad) {
		final BlockRenderLayer quadRenderLayer = quad.renderLayer();
		final BlockRenderLayer renderLayer = quadRenderLayer == null ? defaultRenderLayer : quadRenderLayer;
		final VertexConsumer vertexConsumer;

		if (renderLayer == lastRenderLayer) {
			vertexConsumer = lastVertexConsumer;
		} else {
			lastVertexConsumer = vertexConsumer = vertexConsumers.getBuffer(renderLayer);
			lastRenderLayer = renderLayer;
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
				quad.color(i, net.minecraft.util.math.ColorHelper.scaleRgb(quad.color(i), red, green, blue));
			}
		}
	}

	private void shadeQuad(MutableQuadViewImpl quad, boolean emissive) {
		if (emissive) {
			for (int i = 0; i < 4; i++) {
				quad.lightmap(i, LightmapTextureManager.MAX_LIGHT_COORDINATE);
			}
		} else {
			final int light = this.light;

			for (int i = 0; i < 4; i++) {
				quad.lightmap(i, ColorHelper.maxLight(quad.lightmap(i), light));
			}
		}
	}

	public void bufferModel(MatrixStack.Entry entry, BlockVertexConsumerProvider vertexConsumers, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockRenderView blockView, BlockPos pos, BlockState state) {
		matrices = entry;
		this.overlay = overlay;

		this.vertexConsumers = vertexConsumers;
		this.defaultRenderLayer = RenderLayers.getBlockLayer(state);
		this.red = MathHelper.clamp(red, 0, 1);
		this.green = MathHelper.clamp(green, 0, 1);
		this.blue = MathHelper.clamp(blue, 0, 1);
		this.light = light;

		random.setSeed(42L);

		model.emitQuads(getEmitter(), blockView, pos, state, random, cullFace -> false);

		matrices = null;
		this.vertexConsumers = null;
		lastRenderLayer = null;
		lastVertexConsumer = null;
	}
}
