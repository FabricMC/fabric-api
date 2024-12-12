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

import net.fabricmc.fabric.api.util.TriState;

/**
 * Getter methods for {@link RenderMaterial} (immutable) and {@link MaterialFinder} (mutable).
 *
 * <p>Values returned by the getters may not necessarily be identical to those requested in the {@link MaterialFinder}.
 * The renderer may choose different values that are sufficiently representative for its own processing.
 */
public interface MaterialView {
	/**
	 * @see MaterialFinder#blendMode(BlendMode)
	 */
	BlendMode blendMode();

	/**
	 * @see MaterialFinder#emissive(boolean)
	 */
	boolean emissive();

	/**
	 * @see MaterialFinder#disableDiffuse(boolean)
	 */
	boolean disableDiffuse();

	/**
	 * @see MaterialFinder#ambientOcclusion(TriState)
	 */
	TriState ambientOcclusion();

	/**
	 * @see MaterialFinder#glintMode(GlintMode)
	 */
	GlintMode glintMode();

	/**
	 * @see MaterialFinder#shadeMode(ShadeMode)
	 */
	ShadeMode shadeMode();
}
