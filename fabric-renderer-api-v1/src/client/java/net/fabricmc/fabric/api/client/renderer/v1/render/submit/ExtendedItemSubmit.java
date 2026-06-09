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

package net.fabricmc.fabric.api.client.renderer.v1.render.submit;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;

/**
 * An alternative to {@link ItemFeatureRenderer.Submit} that accepts a {@link MeshView}.
 */
//CHECKSTYLE.OFF: MatchXpath
public record ExtendedItemSubmit(PoseStack.Pose pose, ItemDisplayContext displayContext,
								int lightCoords, int overlayCoords, int outlineColor,
								int[] tintLayers, List<BakedQuad> quads, MeshView mesh,
								ItemStackRenderState.FoilType foilType) implements TranslucentSubmit {
	//CHECKSTYLE.ON: MatchXpath
	public static final FeatureRendererType<ExtendedItemSubmit> TYPE = FeatureRendererType.create("Extended Item");

	public boolean hasTranslucency() {
		for (BakedQuad quad : quads()) {
			if (quad.materialInfo().itemRenderType().hasBlending()) {
				return true;
			}
		}

		var quadInspector = new Consumer<QuadView>() {
			private boolean translucent = false;

			@Override
			public void accept(QuadView quad) {
				if (quad.itemRenderType().hasBlending()) {
					translucent = true;
				}
			}
		};
		mesh.forEach(quadInspector);

		return quadInspector.translucent;
	}

	@Override
	public float distanceToCameraSq() {
		return TranslucentSubmit.computeDistanceToCameraSq(pose.pose());
	}

	@Override
	public FeatureRendererType<? extends TranslucentSubmit> featureType() {
		return TYPE;
	}
}
