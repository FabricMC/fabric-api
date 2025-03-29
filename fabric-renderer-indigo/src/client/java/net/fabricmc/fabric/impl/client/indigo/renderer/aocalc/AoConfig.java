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

package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

/**
 * Defines the configuration modes for the AoCalculator.
 * This determines the appearance of smooth lighting.
 */
public enum AoConfig {
	/**
	 * Quads will be lit with a slightly modified copy of the vanilla ambient
	 * occlusion calculator. Quads with triangles, non-square or slopes will
	 * not look good in this model.  This model also requires a fixed vertex
	 * winding order for all quads.
	 */
	VANILLA,

	/**
	 * Quads are lit with enhanced lighting logic.  Enhanced lighting will be
	 * similar to vanilla lighting for face-aligned quads, and will be different
	 * (generally better) for triangles, non-square and sloped quads.  Axis-
	 * aligned quads not on the block face will have interpolated brightness based
	 * on depth instead of the all-or-nothing brightness of vanilla.
	 *
	 * <p>Non-vanilla quads can have vertices in any (counter-clockwise) order.
	 */
	ENHANCED,

	/**
	 * Enhanced lighting is configured to mimic vanilla lighting. Results will be
	 * identical to vanilla except that non-square quads, triangles, etc. will
	 * not be sensitive to vertex order.  However shading will not be interpolated
	 * as it is with enhanced. These quads do not occur in vanilla models.
	 * Not recommended for models with complex geometry, but may be faster than
	 * the vanilla calculator when vanilla lighting is desired.
	 */
	EMULATE,

	/**
	 * Quads from vanilla models are lit using {@link #EMULATE} mode and all
	 * other quads are lit using {@link #ENHANCED} mode.  This mode ensures
	 * all vanilla models retain their normal appearance while providing
	 * better lighting for models with more complex geometry.  However,
	 * inconsistencies may be visible when vanilla and non-vanilla models are
	 * near each other.
	 */
	HYBRID;
}
