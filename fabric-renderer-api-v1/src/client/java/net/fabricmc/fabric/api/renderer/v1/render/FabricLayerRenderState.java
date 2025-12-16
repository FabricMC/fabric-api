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

package net.fabricmc.fabric.api.renderer.v1.render;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/**
 * Note: This interface is automatically implemented on {@link ItemStackRenderState.LayerRenderState} via Mixin and interface
 * injection.
 */
public interface FabricLayerRenderState {
	/**
	 * Retrieves the {@link QuadEmitter} used to append quads to this layer. Calling this method a second time
	 * invalidates any prior result. Geometry added to this emitter will not be visible in
	 * {@link ItemStackRenderState.LayerRenderState#prepareQuadList()} and will be rendered after any
	 * {@linkplain ItemStackRenderState.LayerRenderState#prepareQuadList()}  vanilla quads} when this layer is rendered. Vertex
	 * positions of geometry added to this emitter will automatically be output on
	 * {@link ItemStackRenderState#visitExtents(Consumer)} ({@link ItemStackRenderState.LayerRenderState#setExtents(Supplier)} must still
	 * be used to add positions of {@linkplain ItemStackRenderState.LayerRenderState#prepareQuadList() vanilla quads}). Adding quads
	 * that use animated sprites to this emitter will not automatically call {@link ItemStackRenderState#setAnimated()}. Any
	 * quads added to this emitter will be cleared on {@link ItemStackRenderState.LayerRenderState#clear()}.
	 *
	 * <p>Do not retain references outside the context of this layer.
	 */
	default QuadEmitter emitter() {
		return Renderer.get().getLayerRenderStateEmitter((ItemStackRenderState.LayerRenderState) this);
	}

	/**
	 * Sets the function that chooses the {@link RenderType} for quads added to this layer through {@link #emitter()}
	 * based on certain quad properties. This method has no effect on how
	 * {@linkplain ItemStackRenderState.LayerRenderState#prepareQuadList() vanilla quads} are rendered. If this function
	 * is not set, all non-vanilla quads in this layer will be rendered using this layer's
	 * {@linkplain ItemStackRenderState.LayerRenderState#setRenderType(RenderType) default render type}. If the
	 * function returns {@code null} for a certain combination of quad properties, then all non-vanilla quads with
	 * matching property values will use this layer's default render type. This layer's function will be unset on
	 * {@link ItemStackRenderState.LayerRenderState#clear()}.
	 *
	 * @see ItemRenderTypeGetter
	 */
	default void setRenderTypeGetter(ItemRenderTypeGetter renderTypeGetter) {
		Renderer.get().setLayerRenderTypeGetter((ItemStackRenderState.LayerRenderState) this, renderTypeGetter);
	}
}
