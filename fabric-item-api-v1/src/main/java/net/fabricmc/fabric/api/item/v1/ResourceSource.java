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

package net.fabricmc.fabric.api.item.v1;

/**
 * Determines where a data driven resource has been loaded from.
 */
public enum ResourceSource {
	/**
	 * Something loaded from the vanilla data pack.
	 */
	VANILLA(true),
	/**
	 * Something loaded from mods' bundled resources.
	 *
	 * <p>This includes the additional built-in data packs registered by mods
	 * with Fabric Resource Loader.
	 */
	MOD(true),
	/**
	 * Something loaded from an external data pack.
	 */
	DATA_PACK(false);

	private final boolean builtIn;

	ResourceSource(boolean builtIn) {
		this.builtIn = builtIn;
	}

	/**
	 * Returns whether this source is built-in and bundled in the vanilla or mod resources.
	 *
	 * <p>{@link #VANILLA} and {@link #MOD} are built-in.
	 *
	 * @return {@code true} if built-in, {@code false} otherwise
	 */
	public boolean isBuiltIn() {
		return builtIn;
	}
}
