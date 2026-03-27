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

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.LightCoordsUtil;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.mixin.client.indigo.renderer.ItemFeatureRendererAccessor;

/**
 * Used during item buffering to support geometry added through {@link FabricLayerRenderState#emitter()}.
 */
public class AltItemRenderer {
	private final MutableQuadViewImpl emitter = new MutableQuadViewImpl() {
		{
			data = new int[EncodingFormat.TOTAL_STRIDE];
			clear();
		}

		@Override
		protected void emitDirectly() {
			bufferQuad(this);
		}
	};

	private MultiBufferSource bufferSource;
	private OutlineBufferSource outlineBufferSource;
	private boolean translucent;

	private FabricSubmitNodeCollection.ExtendedItemSubmit submit;
	private PoseStack.@Nullable Pose foilDecalPose;

	public void prepare(MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, boolean translucent) {
		this.bufferSource = bufferSource;
		this.outlineBufferSource = outlineBufferSource;
		this.translucent = translucent;
	}

	public void clear() {
		bufferSource = null;
		outlineBufferSource = null;
	}

	public void renderItem(FabricSubmitNodeCollection.ExtendedItemSubmit submit) {
		this.submit = submit;

		if (submit.outlineColor() != 0) {
			outlineBufferSource.setColor(submit.outlineColor());
		}

		bufferQuads(submit.quads(), submit.mesh());

		foilDecalPose = null;
	}

	private void bufferQuads(List<BakedQuad> vanillaQuads, MeshView mesh) {
		QuadEmitter emitter = this.emitter;
		emitter.clear();

		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < vanillaQuads.size(); i++) {
			final BakedQuad q = vanillaQuads.get(i);
			emitter.fromBakedQuad(q);
			emitter.emit();
		}

		mesh.outputTo(emitter);
	}

	private void bufferQuad(MutableQuadViewImpl quad) {
		final RenderType renderType = quad.itemRenderType();

		if (renderType.hasBlending() != translucent) {
			return;
		}

		shadeQuad(quad, quad.emissive());
		tintQuad(quad);

		final FabricSubmitNodeCollection.ExtendedItemSubmit submit = this.submit;
		final ItemStackRenderState.FoilType foilType = quad.foilType() == null ? submit.foilType() : quad.foilType();

		if (foilType != ItemStackRenderState.FoilType.NONE) {
			final PoseStack.Pose foilDecalPose;

			if (foilType == ItemStackRenderState.FoilType.SPECIAL) {
				if (this.foilDecalPose == null) {
					this.foilDecalPose = ItemFeatureRendererAccessor.fabric_computeFoilDecalPose(submit.displayContext(), submit.pose());
				}

				foilDecalPose = this.foilDecalPose;
			} else {
				foilDecalPose = null;
			}

			final VertexConsumer foilBuffer = ItemFeatureRendererAccessor.fabric_getFoilBuffer(bufferSource, renderType, foilDecalPose);
			quad.buffer(submit.overlayCoords(), submit.pose(), foilBuffer);
		}

		if (submit.outlineColor() != 0) {
			quad.buffer(submit.overlayCoords(), submit.pose(), outlineBufferSource.getBuffer(renderType));
		}

		quad.buffer(submit.overlayCoords(), submit.pose(), bufferSource.getBuffer(renderType));
	}

	private void shadeQuad(MutableQuadViewImpl quad, boolean emissive) {
		if (emissive) {
			quad.lightmap(LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT);
		} else {
			quad.minLightmap(submit.lightCoords());
		}
	}

	private void tintQuad(MutableQuadViewImpl quad) {
		final int tintIndex = quad.tintIndex();

		if (tintIndex >= 0 && tintIndex < submit.tintLayers().length) {
			quad.multiplyColor(submit.tintLayers()[tintIndex]);
		}
	}
}
