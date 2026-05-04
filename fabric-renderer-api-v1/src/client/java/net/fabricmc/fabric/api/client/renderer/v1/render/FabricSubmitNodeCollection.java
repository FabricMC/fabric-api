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
import net.minecraft.client.renderer.SubmitNodeStorage.BlockModelSubmit;
import net.minecraft.client.renderer.SubmitNodeStorage.ItemSubmit;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
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
	/**
	 * @return {@linkplain ExtendedBlockModelSubmit extended block model submits} in this
	 * {@link SubmitNodeCollection}.
	 */
	default List<ExtendedBlockModelSubmit> getExtendedBlockModelSubmits() {
		throw new UnsupportedOperationException("Implemented via Mixin.");
	}

	/**
	 * @return {@linkplain ExtendedItemSubmit extended item submits} in this
	 * {@link SubmitNodeCollection}.
	 */
	default List<ExtendedItemSubmit> getExtendedItemSubmits() {
		throw new UnsupportedOperationException("Implemented via Mixin.");
	}

	// CHECKSTYLE:OFF MatchXpath
	/**
	 * An alternative to {@link BlockModelSubmit} that accepts a {@link Mesh}.
	 */
	record ExtendedBlockModelSubmit(PoseStack.Pose pose, Function<ChunkSectionLayer, RenderType> renderTypeFunction, boolean translucent, List<BlockStateModelPart> modelParts, @Nullable Mesh mesh, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
	}

	/**
	 * An alternative to {@link ItemSubmit} that accepts a {@link MeshView}.
	 */
	record ExtendedItemSubmit(PoseStack.Pose pose, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, MeshView mesh, ItemStackRenderState.FoilType foilType) {
	}

	// CHECKSTYLE:ON MatchXpath
}
