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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.MatrixUtil;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;

import net.fabricmc.fabric.api.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.mixin.client.indigo.renderer.ItemRendererAccessor;

/**
 * Used during item buffering to support geometry added through {@link FabricLayerRenderState#emitter()}.
 */
public class ItemRenderContext extends AbstractRenderContext {
	private static final int GLINT_COUNT = ItemStackRenderState.FoilType.values().length;

	private ItemDisplayContext displayContext;
	private MultiBufferSource vertexConsumers;
	private int light;
	private int[] tints;

	private RenderType defaultLayer;
	@Nullable
	private ItemRenderTypeGetter renderTypeGetter;
	private ItemStackRenderState.FoilType defaultGlint;
	private boolean ignoreQuadGlint;

	private PoseStack.Pose specialGlintEntry;
	private final VertexConsumer[] vertexConsumerCache = new VertexConsumer[3 * GLINT_COUNT];

	public void renderItem(
			ItemDisplayContext displayContext,
			PoseStack matrixStack,
			MultiBufferSource vertexConsumers,
			int light,
			int overlay,
			int[] tints,
			List<BakedQuad> vanillaQuads,
			MeshView mesh,
			RenderType layer,
			@Nullable ItemRenderTypeGetter renderTypeGetter,
			ItemStackRenderState.FoilType glint,
			boolean ignoreQuadGlint
	) {
		this.displayContext = displayContext;
		matrices = matrixStack.last();
		this.vertexConsumers = vertexConsumers;
		this.light = light;
		this.overlay = overlay;
		this.tints = tints;

		defaultLayer = layer;
		this.renderTypeGetter = renderTypeGetter;
		defaultGlint = glint;
		this.ignoreQuadGlint = ignoreQuadGlint;

		bufferQuads(vanillaQuads, mesh);

		matrices = null;
		this.vertexConsumers = null;
		this.tints = null;

		defaultLayer = null;
		this.renderTypeGetter = null;

		specialGlintEntry = null;
		Arrays.fill(vertexConsumerCache, null);
	}

	private void bufferQuads(List<BakedQuad> vanillaQuads, MeshView mesh) {
		QuadEmitter emitter = getEmitter();

		final int vanillaQuadCount = vanillaQuads.size();

		for (int i = 0; i < vanillaQuadCount; i++) {
			final BakedQuad q = vanillaQuads.get(i);
			emitter.fromBakedQuad(q);
			emitter.emit();
		}

		mesh.outputTo(emitter);
	}

	@Override
	protected void bufferQuad(MutableQuadViewImpl quad) {
		final VertexConsumer vertexConsumer = getVertexConsumer(quad.atlas(), quad.renderLayer(), quad.glint());

		tintQuad(quad);
		shadeQuad(quad, quad.emissive());
		bufferQuad(quad, vertexConsumer);
	}

	private void tintQuad(MutableQuadViewImpl quad) {
		int tintIndex = quad.tintIndex();

		if (tintIndex >= 0 && tintIndex < tints.length) {
			final int tint = tints[tintIndex];

			for (int i = 0; i < 4; i++) {
				quad.color(i, ARGB.multiply(quad.color(i), tint));
			}
		}
	}

	private void shadeQuad(MutableQuadViewImpl quad, boolean emissive) {
		if (emissive) {
			for (int i = 0; i < 4; i++) {
				quad.lightmap(i, LightTexture.FULL_BRIGHT);
			}
		} else {
			final int light = this.light;

			for (int i = 0; i < 4; i++) {
				quad.lightmap(i, ColorHelper.maxLight(quad.lightmap(i), light));
			}
		}
	}

	private VertexConsumer getVertexConsumer(QuadAtlas quadAtlas, @Nullable ChunkSectionLayer quadRenderLayer, ItemStackRenderState.@Nullable FoilType quadGlint) {
		RenderType layer;
		ItemStackRenderState.FoilType glint;

		if (renderTypeGetter != null) {
			layer = renderTypeGetter.renderType(quadAtlas, quadRenderLayer);

			if (layer == null) {
				layer = defaultLayer;
			}
		} else {
			layer = defaultLayer;
		}

		if (ignoreQuadGlint || quadGlint == null) {
			glint = defaultGlint;
		} else {
			glint = quadGlint;
		}

		int cacheIndex;

		if (layer == Sheets.translucentItemSheet()) {
			cacheIndex = 0;
		} else if (layer == Sheets.cutoutBlockSheet()) {
			cacheIndex = GLINT_COUNT;
		} else if (layer == Sheets.translucentBlockItemSheet()) {
			cacheIndex = 2 * GLINT_COUNT;
		} else {
			return createVertexConsumer(layer, glint);
		}

		cacheIndex += glint.ordinal();
		VertexConsumer vertexConsumer = vertexConsumerCache[cacheIndex];

		if (vertexConsumer == null) {
			vertexConsumer = createVertexConsumer(layer, glint);
			vertexConsumerCache[cacheIndex] = vertexConsumer;
		}

		return vertexConsumer;
	}

	private VertexConsumer createVertexConsumer(RenderType layer, ItemStackRenderState.FoilType glint) {
		if (glint == ItemStackRenderState.FoilType.SPECIAL) {
			if (specialGlintEntry == null) {
				specialGlintEntry = matrices.copy();

				if (displayContext == ItemDisplayContext.GUI) {
					MatrixUtil.mulComponentWise(specialGlintEntry.pose(), 0.5F);
				} else if (displayContext.firstPerson()) {
					MatrixUtil.mulComponentWise(specialGlintEntry.pose(), 0.75F);
				}
			}

			return ItemRendererAccessor.fabric_getDynamicDisplayGlintConsumer(vertexConsumers, layer, specialGlintEntry);
		}

		return ItemRenderer.getFoilBuffer(vertexConsumers, layer, true, glint != ItemStackRenderState.FoilType.NONE);
	}
}
