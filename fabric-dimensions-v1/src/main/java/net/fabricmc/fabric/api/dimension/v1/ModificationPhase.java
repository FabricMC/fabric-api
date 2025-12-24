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

package net.fabricmc.fabric.api.dimension.v1;

/**
 * To achieve a predictable order for dimension modifiers, and to aid with mod compatibility, modifiers need to declare
 * the phase in which they will be applied.
 *
 * <p>This will result in the following order:
 * <ol>
 *     <li>Additions to dimensions</li>
 *     <li>Removals from dimensions</li>
 *     <li>Replacements (removal + add) in dimensions</li>
 *     <li>Generic post-processing of dimensions</li>
 * </ol>
 */
public enum ModificationPhase {
	/**
	 * The appropriate phase for enriching dimensions by adding to them without relying on
	 * other information in the dimension, or removing other features.
	 *
	 * <p><b>Examples:</b> New ores, new vegetation, new structures
	 */
	ADDITIONS,

	/**
	 * The appropriate phase for modifiers that remove features or other aspects of dimensions (i.e. removal of spawns,
	 * removal of features, etc.).
	 *
	 * <p><b>Examples:</b> Remove iron ore from plains, remove ghasts
	 */
	REMOVALS,

	/**
	 * The appropriate phase for modifiers that replace existing features with modified features.
	 *
	 * <p><b>Examples:</b> Replace mineshafts with dimension-specific mineshafts
	 */
	REPLACEMENTS,

	/**
	 * The appropriate phase for modifiers that perform wide-reaching dimension postprocessing.
	 *
	 * <p><b>Examples:</b> Mods that allow modpack authors to customize world generation, changing dimension
	 * properties (i.e. category) that other mods rely on.
	 */
	POST_PROCESSING
}
