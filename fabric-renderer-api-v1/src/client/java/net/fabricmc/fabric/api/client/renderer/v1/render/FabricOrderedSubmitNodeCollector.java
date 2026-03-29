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

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;

/**
 * Note: This interface is automatically implemented on {@link OrderedSubmitNodeCollector} via Mixin and interface injection.
 */
public interface FabricOrderedSubmitNodeCollector {
	// TODO FRAPI 26.1
	//  reintroduce Function<ChunkSectionLayer, RenderType> renderTypeFunction? probably yes, but
	//  needs thought about how to determine whether a submit is translucent or not
	/**
	 * Alternative to {@link OrderedSubmitNodeCollector#submitBlockModel(PoseStack, RenderType, List, int[], int, int, int)} that also accepts a {@link Mesh}.
	 *
	 * @param poseStack the pose stack
	 * @param renderType the render type
	 * @param parts the vanilla {@linkplain BlockStateModelPart parts}
	 * @param mesh the mesh
	 * @param tintLayers the array of tint layers
	 * @param lightCoords the light coordinates
	 * @param overlayCoords the overlay coordinates
	 * @param outlineColor the block outline color
	 */
	default void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> parts, Mesh mesh, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
		((OrderedSubmitNodeCollector) this).submitBlockModel(poseStack, renderType, parts, tintLayers, lightCoords, overlayCoords, outlineColor);
	}

	/**
	 * Alternative to {@link OrderedSubmitNodeCollector#submitItem(PoseStack, ItemDisplayContext, int, int, int, int[], List, ItemStackRenderState.FoilType)} that also accepts a {@link MeshView}.
	 *
	 * @param poseStack the pose stack
	 * @param displayContext the item display context
	 * @param lightCoords the light coordinates
	 * @param overlayCoords the overlay coordinates
	 * @param outlineColor the block outline color
	 * @param tintLayers the array of tint layers
	 * @param quads the list of vanilla quads
	 * @param mesh the mesh
	 * @param foilType the foil type
	 */
	default void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, MeshView mesh, ItemStackRenderState.FoilType foilType) {
		((OrderedSubmitNodeCollector) this).submitItem(poseStack, displayContext, lightCoords, overlayCoords, outlineColor, tintLayers, quads, foilType);
	}
}
