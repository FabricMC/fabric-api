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

import net.minecraft.client.render.item.ItemRenderState;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/**
 * Note: This interface is automatically implemented on {@link ItemRenderState.LayerRenderState} via Mixin and interface
 * injection.
 */
public interface FabricLayerRenderState {
	/**
	 * Retrieves the {@link QuadEmitter} used to append quads to this layer. Calling this method a second time
	 * invalidates any prior result. Geometry added to this emitter will not be visible in
	 * {@link ItemRenderState.LayerRenderState#getQuads()} and will be rendered after any
	 * {@linkplain ItemRenderState.LayerRenderState#getQuads() vanilla quads} when this layer is rendered. Vertex
	 * positions of geometry added to this emitter will automatically be output on
	 * {@link ItemRenderState#load(Consumer)} ({@link ItemRenderState.LayerRenderState#setVertices(Supplier)} must still
	 * be used to add positions of {@linkplain ItemRenderState.LayerRenderState#getQuads() vanilla quads}). Adding quads
	 * that use animated sprites to this emitter will not automatically call {@link ItemRenderState#markAnimated()}. Any
	 * quads added to this emitter will be cleared on {@link ItemRenderState.LayerRenderState#clear()}.
	 *
	 * <p>Do not retain references outside the context of this layer.
	 */
	default QuadEmitter emitter() {
		return Renderer.get().getLayerRenderStateEmitter((ItemRenderState.LayerRenderState) this);
	}
}
