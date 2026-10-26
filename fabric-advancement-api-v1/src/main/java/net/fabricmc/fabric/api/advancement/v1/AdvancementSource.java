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
	 * An advancement loaded from the vanilla data of the game.
	 */
	VANILLA(true),
	/**
	 * An advancement loaded from the bundled resources of a mod.
	 */
	MOD(true),
	/**
	 * An advancement loaded from a data pack that is not bundled with the game or a mod,
	 * such as one installed by the user in the world's {@code datapacks} directory.
	 */
	DATA_PACK(false),
	/**
	 * An advancement that has been replaced by {@link AdvancementEvents#REPLACE}.
	 *
	 * <p>This source hides the source of the advancement that was replaced. It is only seen by
	 * {@link AdvancementEvents#MODIFY} and by the {@code REPLACE} listeners registered after the
	 * one that replaced the advancement.
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
