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

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.MatrixUtil;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.GlintMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MeshViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.mixin.client.indigo.renderer.ItemRendererAccessor;

/**
 * Used during item buffering to support geometry added through {@link FabricLayerRenderState#emitter()}.
 */
public class ItemRenderContext extends AbstractRenderContext {
	private static final int GLINT_COUNT = ItemRenderState.Glint.values().length;

	public static final ThreadLocal<ItemRenderContext> POOL = ThreadLocal.withInitial(ItemRenderContext::new);

	private ItemDisplayContext displayContext;
	private VertexConsumerProvider vertexConsumers;
	private int light;
	private int[] tints;

	private RenderLayer defaultLayer;
	private ItemRenderState.Glint defaultGlint;

	private MatrixStack.Entry specialGlintEntry;
	private final VertexConsumer[] vertexConsumerCache = new VertexConsumer[3 * GLINT_COUNT];

	public void renderItem(ItemDisplayContext displayContext, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, int overlay, int[] tints, List<BakedQuad> vanillaQuads, MeshViewImpl mesh, RenderLayer layer, ItemRenderState.Glint glint) {
		this.displayContext = displayContext;
		matrices = matrixStack.peek();
		this.vertexConsumers = vertexConsumers;
		this.light = light;
		this.overlay = overlay;
		this.tints = tints;

		defaultLayer = layer;
		defaultGlint = glint;

		bufferQuads(vanillaQuads, mesh);

		matrices = null;
		this.vertexConsumers = null;
		this.tints = null;

		defaultLayer = null;

		specialGlintEntry = null;
		Arrays.fill(vertexConsumerCache, null);
	}

	private void bufferQuads(List<BakedQuad> vanillaQuads, MeshViewImpl mesh) {
		QuadEmitter emitter = getEmitter();

		final int vanillaQuadCount = vanillaQuads.size();

		for (int i = 0; i < vanillaQuadCount; i++) {
			final BakedQuad q = vanillaQuads.get(i);
			emitter.fromVanilla(q, IndigoRenderer.STANDARD_MATERIAL, null);
			emitter.emit();
		}

		mesh.outputTo(emitter);
	}

	@Override
	protected void bufferQuad(MutableQuadViewImpl quad) {
		final RenderMaterial mat = quad.material();
		final VertexConsumer vertexConsumer = getVertexConsumer(mat.blendMode(), mat.glintMode());

		tintQuad(quad);
		shadeQuad(quad, mat.emissive());
		bufferQuad(quad, vertexConsumer);
	}

	private void tintQuad(MutableQuadViewImpl quad) {
		int tintIndex = quad.tintIndex();

		if (tintIndex >= 0 && tintIndex < tints.length) {
			final int tint = tints[tintIndex];

			for (int i = 0; i < 4; i++) {
				quad.color(i, net.minecraft.util.math.ColorHelper.mix(quad.color(i), tint));
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

	private VertexConsumer getVertexConsumer(BlendMode blendMode, GlintMode glintMode) {
		RenderLayer layer;
		ItemRenderState.Glint glint;

		if (blendMode == BlendMode.DEFAULT) {
			layer = defaultLayer;
		} else {
			layer = blendMode == BlendMode.TRANSLUCENT ? TexturedRenderLayers.getItemEntityTranslucentCull() : TexturedRenderLayers.getEntityCutout();
		}

		if (glintMode == GlintMode.DEFAULT) {
			glint = defaultGlint;
		} else {
			glint = glintMode.glint;
		}

		int cacheIndex;

		if (layer == TexturedRenderLayers.getItemEntityTranslucentCull()) {
			cacheIndex = 0;
		} else if (layer == TexturedRenderLayers.getEntityCutout()) {
			cacheIndex = GLINT_COUNT;
		} else {
			cacheIndex = 2 * GLINT_COUNT;
		}

		cacheIndex += glint.ordinal();
		VertexConsumer vertexConsumer = vertexConsumerCache[cacheIndex];

		if (vertexConsumer == null) {
			vertexConsumer = createVertexConsumer(layer, glint);
			vertexConsumerCache[cacheIndex] = vertexConsumer;
		}

		return vertexConsumer;
	}

	private VertexConsumer createVertexConsumer(RenderLayer layer, ItemRenderState.Glint glint) {
		if (glint == ItemRenderState.Glint.SPECIAL) {
			if (specialGlintEntry == null) {
				specialGlintEntry = matrices.copy();

				if (displayContext == ItemDisplayContext.GUI) {
					MatrixUtil.scale(specialGlintEntry.getPositionMatrix(), 0.5F);
				} else if (displayContext.isFirstPerson()) {
					MatrixUtil.scale(specialGlintEntry.getPositionMatrix(), 0.75F);
				}
			}

			return ItemRendererAccessor.fabric_getDynamicDisplayGlintConsumer(vertexConsumers, layer, specialGlintEntry);
		}

		return ItemRenderer.getItemGlintConsumer(vertexConsumers, layer, true, glint != ItemRenderState.Glint.NONE);
	}
}
