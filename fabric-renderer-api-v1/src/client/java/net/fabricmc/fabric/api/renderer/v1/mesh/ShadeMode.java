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

package net.fabricmc.fabric.api.renderer.v1.mesh;

/**
 * A hint to the renderer about how the quad is intended to be shaded, for example through ambient occlusion and
 * diffuse shading. The renderer is free to ignore this hint.
 */
public enum ShadeMode {
	/**
	 * Conveys the intent that shading should be generally consistent, lack edge cases, and produce visually pleasing
	 * results, even for quads that are not used by vanilla or are not possible to create through resource packs in
	 * vanilla.
	 */
	ENHANCED,

	/**
	 * Conveys the intent that shading should mimic vanilla results, potentially to preserve certain visuals produced
	 * by resource packs that modify models.
	 */
	VANILLA;
}
