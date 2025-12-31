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

package net.fabricmc.fabric.api.loot.v3;

/// Describes where a loot table has been loaded from.
public enum LootTableSource {
	/// A loot table loaded from the default data pack.
	VANILLA(true),

	/// A loot table loaded from mods' bundled resources.
	///
	/// This includes the additional builtin data packs registered by mods
	/// with Fabric Resource Loader.
	MOD(true),

	/// A loot table loaded from an external data pack.
	DATA_PACK(false),

	/// A loot table created in [LootTableEvents#REPLACE].
	REPLACED(false);

	private final boolean builtin;

	LootTableSource(boolean builtin) {
		this.builtin = builtin;
	}

	/// Returns whether this loot table source is builtin
	/// and bundled in the vanilla or mod resources.
	///
	/// [#VANILLA] and [#MOD] are builtin.
	///
	/// @return `true` if builtin, `false` otherwise
	public boolean isBuiltin() {
		return builtin;
	}
}
