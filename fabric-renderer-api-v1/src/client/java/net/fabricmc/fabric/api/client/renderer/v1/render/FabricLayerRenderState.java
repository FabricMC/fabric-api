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

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.renderer.item.ItemStackRenderState;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

/**
 * Note: This interface is automatically implemented on {@link ItemStackRenderState.LayerRenderState} via Mixin and interface
 * injection.
 */
public interface FabricLayerRenderState {
	/**
	 * Retrieves the {@link QuadEmitter} used to append quads to this layer. Calling this method a second time
	 * invalidates any prior result. Geometry added to this emitter will not be visible in
	 * {@link ItemStackRenderState.LayerRenderState#prepareQuadList()} and will be rendered after any
	 * {@linkplain ItemStackRenderState.LayerRenderState#prepareQuadList() vanilla quads} when this layer is rendered. Vertex
	 * positions of geometry added to this emitter will automatically be output on
	 * {@link ItemStackRenderState#visitExtents(Consumer)} ({@link ItemStackRenderState.LayerRenderState#setExtents(Supplier)} must still
	 * be used to add positions of {@linkplain ItemStackRenderState.LayerRenderState#prepareQuadList() vanilla quads}). Adding quads
	 * that use animated sprites or guaranteed glint to this emitter will not automatically call {@link ItemStackRenderState#setAnimated()}. Any
	 * quads added to this emitter will be cleared on {@link ItemStackRenderState.LayerRenderState#clear()}.
	 *
	 * <p>Do not retain references outside the context of this layer.
	 */
	default QuadEmitter emitter() {
		throw new AssertionError("Implemented in mixin.");
	}
}
