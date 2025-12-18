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

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadAtlas;

@FunctionalInterface
public interface ItemRenderTypeGetter {
	/**
	 * Gets the {@link RenderType} for the given {@link QuadAtlas} and nullable {@link ChunkSectionLayer}. Quads with
	 * matching property values will be rendered using the returned render type.
	 *
	 * <p>A return value of {@code null} means that the current item layer's
	 * {@linkplain ItemStackRenderState.LayerRenderState#setRenderType(RenderType) default render type} will be used.
	 */
	@Nullable
	RenderType renderType(QuadAtlas quadAtlas, @Nullable ChunkSectionLayer sectionLayer);
}
