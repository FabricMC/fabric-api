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

package net.fabricmc.fabric.api.client.renderer.v1.render;

import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.BlockModelFeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;

/**
 * Note: This interface is automatically implemented on {@link SubmitNodeCollection} via Mixin and interface injection.
 */
public interface FabricSubmitNodeCollection {
	// CHECKSTYLE:OFF MatchXpath
	/**
	 * An alternative to {@link BlockModelFeatureRenderer.Submit} that accepts a {@link Mesh}.
	 */
	record ExtendedBlockModelSubmit(PoseStack.Pose pose, Function<ChunkSectionLayer, @Nullable RenderType> renderTypeFunction, List<BlockStateModelPart> modelParts, @Nullable Mesh mesh, int[] tintLayers, int lightCoords, int overlayCoords, int tintColor) implements TranslucentSubmit {
		public static final FeatureRendererType<FabricSubmitNodeCollection.ExtendedBlockModelSubmit> TYPE = FeatureRendererType.create("Extended Block Model");

		@Override
		public float distanceToCameraSq() {
			return TranslucentSubmit.computeDistanceToCameraSq(this.pose.pose(), 0.5F, 0.5F, 0.5F);
		}

		@Override
		public FeatureRendererType<? extends TranslucentSubmit> featureType() {
			return ExtendedBlockModelSubmit.TYPE;
		}
	}

	/**
	 * An alternative to {@link ItemFeatureRenderer.Submit} that accepts a {@link MeshView}.
	 */
	record ExtendedItemSubmit(PoseStack.Pose pose, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, MeshView mesh, ItemStackRenderState.FoilType foilType) implements TranslucentSubmit {
		public static final FeatureRendererType<FabricSubmitNodeCollection.ExtendedItemSubmit> TYPE = FeatureRendererType.create("Extended Item");

		@Override
		public float distanceToCameraSq() {
			return TranslucentSubmit.computeDistanceToCameraSq(this.pose.pose(), 0.5F, 0.5F, 0.5F);
		}

		@Override
		public FeatureRendererType<? extends TranslucentSubmit> featureType() {
			return ExtendedItemSubmit.TYPE;
		}

		public boolean hasTranslucency() {
			for (BakedQuad quad : this.quads()) {
				if (quad.materialInfo().itemRenderType().hasBlending()) {
					return true;
				}
			}

			return false;
		}
	}

	// CHECKSTYLE:ON MatchXpath
}
