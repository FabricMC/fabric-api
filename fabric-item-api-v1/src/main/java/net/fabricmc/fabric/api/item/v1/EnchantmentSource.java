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
 * Determines where an enchantment has been loaded from.
 *
 * @deprecated Use {@link ResourceSource} instead.
 */
@Deprecated
public enum EnchantmentSource {
	/**
	 * An enchantment loaded from the vanilla data pack.
	 */
	VANILLA(true),
	/**
	 * An enchantment loaded from mods' bundled resources.
	 *
	 * <p>This includes the additional builtin data packs registered by mods
	 * with Fabric Resource Loader.
	 */
	MOD(true),
	/**
	 * An enchantment loaded from an external data pack.
	 */
	DATA_PACK(false);

	private final boolean builtin;

	EnchantmentSource(boolean builtin) {
		this.builtin = builtin;
	}

	/**
	 * Returns whether this enchantment source is built-in and bundled in the vanilla or mod resources.
	 *
	 * <p>{@link #VANILLA} and {@link #MOD} are built-in.
	 *
	 * @return {@code true} if built-in, {@code false} otherwise
	 */
	public boolean isBuiltin() {
		return builtin;
	}

	/**
	 * Converts to the new {@link ResourceSource} which should be used instead of this class.
	 *
	 * @return The {@link ResourceSource} equivalent of this.
	 */
	public ResourceSource toResourceSource() {
		return switch (this) {
		case VANILLA -> ResourceSource.VANILLA;
		case MOD -> ResourceSource.MOD;
		case DATA_PACK -> ResourceSource.DATA_PACK;
		};
	}

	/**
	 * Converts from the new {@link ResourceSource} which should be used instead of this class.
	 *
	 * @param source The {@link ResourceSource} to convert from.
	 * @return The {@link EnchantmentSource} equivalent of the given source.
	 *
	 * @deprecated {@link ResourceSource} should be used instead of EnchantmentSource.
	 */
	@Deprecated
	public static EnchantmentSource fromResourceSource(ResourceSource source) {
		return switch (source) {
		case ResourceSource.VANILLA -> VANILLA;
		case ResourceSource.MOD -> MOD;
		case ResourceSource.DATA_PACK -> DATA_PACK;
		};
	}
}
