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

package net.fabricmc.fabric.api.advancement.v1;

/**
 * Represents the origin of an advancement.
 */
public enum AdvancementSource {
	/**
	 * Represents a vanilla advancement.
	 */
	VANILLA(true),
	/**
	 * Represents an advancement created by a mod.
	 */
	MOD(true),
	/**
	 * Represents an advancement created by a datapack.
	 */
	DATA_PACK(false),
	/**
	 * Represents an advancement that has been modified with the advancement API.
	 */
	REPLACED(false);

	private final boolean builtin;

	AdvancementSource(boolean builtin) {
		this.builtin = builtin;
	}

	/**
	 * Returns whether this advancement source is builtin
	 * and bundled in the vanilla or mod resources.
	 *
	 * <p>{@link #VANILLA} and {@link #MOD} are builtin.
	 *
	 * @return {@code true} if builtin, {@code false} otherwise
	 */
	public boolean isBuiltin() {
		return builtin;
	}
}
