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

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
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
	private static final int FOIL_TYPE_COUNT = ItemStackRenderState.FoilType.values().length;

	private ItemDisplayContext displayContext;
	private MultiBufferSource bufferSource;
	private int light;
	private int[] tints;

	private RenderType defaultRenderType;
	@Nullable
	private ItemRenderTypeGetter renderTypeGetter;
	private ItemStackRenderState.FoilType defaultFoilType;
	private boolean ignoreQuadFoilType;

	private PoseStack.Pose specialFoilPose;
	private final VertexConsumer[] vertexConsumerCache = new VertexConsumer[3 * FOIL_TYPE_COUNT];

	public void renderItem(
			ItemDisplayContext displayContext,
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int light,
			int overlay,
			int[] tints,
			List<BakedQuad> vanillaQuads,
			MeshView mesh,
			RenderType renderType,
			@Nullable ItemRenderTypeGetter renderTypeGetter,
			ItemStackRenderState.FoilType foilType,
			boolean ignoreQuadFoilType
	) {
		this.displayContext = displayContext;
		pose = poseStack.last();
		this.bufferSource = bufferSource;
		this.light = light;
		this.overlay = overlay;
		this.tints = tints;

		defaultRenderType = renderType;
		this.renderTypeGetter = renderTypeGetter;
		defaultFoilType = foilType;
		this.ignoreQuadFoilType = ignoreQuadFoilType;

		bufferQuads(vanillaQuads, mesh);

		pose = null;
		this.bufferSource = null;
		this.tints = null;

		defaultRenderType = null;
		this.renderTypeGetter = null;

		specialFoilPose = null;
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
		final VertexConsumer vertexConsumer = getVertexConsumer(quad.atlas(), quad.chunkLayer(), quad.foilType());

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
				quad.lightmap(i, LightCoordsUtil.FULL_BRIGHT);
			}
		} else {
			final int light = this.light;

			for (int i = 0; i < 4; i++) {
				quad.lightmap(i, ColorHelper.maxLight(quad.lightmap(i), light));
			}
		}
	}

	private VertexConsumer getVertexConsumer(QuadAtlas quadAtlas, @Nullable ChunkSectionLayer quadLayer, ItemStackRenderState.@Nullable FoilType quadFoilType) {
		RenderType renderType;
		ItemStackRenderState.FoilType foilType;

		if (renderTypeGetter != null) {
			renderType = renderTypeGetter.renderType(quadAtlas, quadLayer);

			if (renderType == null) {
				renderType = defaultRenderType;
			}
		} else {
			renderType = defaultRenderType;
		}

		if (ignoreQuadFoilType || quadFoilType == null) {
			foilType = defaultFoilType;
		} else {
			foilType = quadFoilType;
		}

		int cacheIndex;

		if (renderType == Sheets.translucentItemSheet()) {
			cacheIndex = 0;
		} else if (renderType == Sheets.cutoutBlockSheet()) {
			cacheIndex = FOIL_TYPE_COUNT;
		} else if (renderType == Sheets.translucentBlockItemSheet()) {
			cacheIndex = 2 * FOIL_TYPE_COUNT;
		} else {
			return createVertexConsumer(renderType, foilType);
		}

		cacheIndex += foilType.ordinal();
		VertexConsumer vertexConsumer = vertexConsumerCache[cacheIndex];

		if (vertexConsumer == null) {
			vertexConsumer = createVertexConsumer(renderType, foilType);
			vertexConsumerCache[cacheIndex] = vertexConsumer;
		}

		return vertexConsumer;
	}

	private VertexConsumer createVertexConsumer(RenderType renderType, ItemStackRenderState.FoilType foilType) {
		if (foilType == ItemStackRenderState.FoilType.SPECIAL) {
			if (specialFoilPose == null) {
				specialFoilPose = pose.copy();

				if (displayContext == ItemDisplayContext.GUI) {
					MatrixUtil.mulComponentWise(specialFoilPose.pose(), 0.5F);
				} else if (displayContext.firstPerson()) {
					MatrixUtil.mulComponentWise(specialFoilPose.pose(), 0.75F);
				}
			}

			return ItemRendererAccessor.fabric_getSpecialFoilBuffer(
					bufferSource,
					renderType,
					specialFoilPose
			);
		}

		return ItemRenderer.getFoilBuffer(
				bufferSource,
				renderType, true, foilType != ItemStackRenderState.FoilType.NONE);
	}
}
