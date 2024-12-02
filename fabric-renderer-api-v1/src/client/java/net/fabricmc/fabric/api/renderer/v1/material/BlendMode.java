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

package net.fabricmc.fabric.api.renderer.v1.material;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;

/**
 * Controls how sprite pixels will be blended with the scene.
 */
public enum BlendMode {
	/**
	 * Emulate blending behavior of the {@link RenderLayer} associated with the block state through
	 * {@link RenderLayers}.
	 */
	DEFAULT(null),

	/**
	 * Fully opaque with depth test, no blending. Used for most normal blocks.
	 */
	SOLID(RenderLayer.getSolid()),

	/**
	 * Pixels with alpha &gt; 0.5 are rendered as if {@code SOLID}. Other pixels are not rendered.
	 * Texture mip-map enabled.  Used for leaves.
	 */
	CUTOUT_MIPPED(RenderLayer.getCutoutMipped()),

	/**
	 * Pixels with alpha &gt; 0.5 are rendered as if {@code SOLID}. Other pixels are not rendered.
	 * Texture mip-map disabled.  Used for iron bars, glass and other cutout sprites with hard edges.
	 */
	CUTOUT(RenderLayer.getCutout()),

	/**
	 * Pixels are blended with the background according to alpha color values. Some performance cost,
	 * use in moderation. Texture mip-map enabled.  Used for stained glass.
	 */
	TRANSLUCENT(RenderLayer.getTranslucent());

	@Nullable
	public final RenderLayer blockRenderLayer;

	BlendMode(@Nullable RenderLayer blockRenderLayer) {
		this.blockRenderLayer = blockRenderLayer;
	}

	public static BlendMode fromRenderLayer(@Nullable RenderLayer renderLayer) {
		if (renderLayer == RenderLayer.getSolid()) {
			return SOLID;
		} else if (renderLayer == RenderLayer.getCutoutMipped()) {
			return CUTOUT_MIPPED;
		} else if (renderLayer == RenderLayer.getCutout()) {
			return CUTOUT;
		} else if (renderLayer == RenderLayer.getTranslucent()) {
			return TRANSLUCENT;
		} else {
			return DEFAULT;
		}
	}
}
