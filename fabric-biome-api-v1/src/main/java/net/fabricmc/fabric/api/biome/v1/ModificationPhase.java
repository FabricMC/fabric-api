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

package net.fabricmc.fabric.api.biome.v1;

/// To achieve a predictable order for biome modifiers, and to aid with mod compatibility, modifiers need to declare
/// the phase in which they will be applied.
///
/// This will result in the following order:
///
/// 1. Additions to biomes
/// 2. Removals from biomes
/// 3. Replacements (removal + add) in biomes
/// 4. Generic post-processing of biomes
public enum ModificationPhase {
	/// The appropriate phase for enriching biomes by adding to them without relying on
	/// other information in the biome, or removing other features.
	///
	/// **Examples:** New ores, new vegetation, new structures
	ADDITIONS,

	/// The appropriate phase for modifiers that remove features or other aspects of biomes (i.e. removal of spawns,
	/// removal of features, etc.).
	///
	/// **Examples:** Remove iron ore from plains, remove ghasts
	REMOVALS,

	/// The appropriate phase for modifiers that replace existing features with modified features.
	///
	/// **Examples:** Replace mineshafts with biome-specific mineshafts
	REPLACEMENTS,

	/// The appropriate phase for modifiers that perform wide-reaching biome postprocessing.
	///
	/// **Examples:** Mods that allow modpack authors to customize world generation, changing biome
	/// properties (i.e. category) that other mods rely on.
	POST_PROCESSING
}
