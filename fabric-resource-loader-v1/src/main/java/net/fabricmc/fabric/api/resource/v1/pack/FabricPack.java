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

package net.fabricmc.fabric.api.resource.v1.pack;

import net.minecraft.server.packs.repository.Pack;

/**
 * Fabric extensions for {@link Pack}.
 * Automatically implemented on {@link Pack} via a mixin.
 *
 * <p>Packs can be hidden from the user-facing pack selection screens ({@code PackSelectionScreen})
 * and from the {@code /datapack} command. Fabric hides the internal packs it creates for bundled mod
 * resources automatically; mods that register their own packs can hide them with
 * {@link #setHidden(boolean)}.
 */
public interface FabricPack {
	/**
	 * Returns whether this pack is hidden from the user.
	 *
	 * <p>This is {@code true} for Fabric's internal packs and for any pack hidden with
	 * {@link #setHidden(boolean)}.
	 *
	 * @return whether this pack is hidden
	 */
	default boolean isHidden() {
		return false;
	}

	/**
	 * Sets whether this pack is hidden from the user.
	 *
	 * <p>A hidden pack is not listed in the pack selection screens or by the {@code /datapack}
	 * command, and cannot be enabled or disabled by the user. Hidden state is not persisted by
	 * Fabric; it lives on the {@link Pack} instance, so it must be applied again whenever the pack is
	 * recreated (for example in {@code RepositorySource#loadPacks}).
	 *
	 * <p>Passing {@code false} only clears the explicit hidden flag. A pack whose activation is
	 * managed internally by Fabric stays hidden regardless.
	 *
	 * @param hidden whether this pack should be hidden
	 * @return the effective hidden state after this call; {@code true} means the pack is still
	 *         hidden, so a {@code true} return after passing {@code false} indicates that Fabric
	 *         manages this pack's visibility and it cannot be revealed
	 */
	default boolean setHidden(boolean hidden) {
		return isHidden();
	}
}
