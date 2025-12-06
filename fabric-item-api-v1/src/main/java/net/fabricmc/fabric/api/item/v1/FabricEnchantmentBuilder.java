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

import org.jspecify.annotations.Nullable;

import net.minecraft.core.HolderLookup.Provider;

/**
 * An interface implemented by {@link Enchantment.Builder} to provide access to
 * registries.
 */
public interface FabricEnchantmentBuilder {
	/**
	 * Returns the registry lookup provider, if available.
	 *
	 * @return the registry lookup provider, or {@code null} if not available
	 */
	@Nullable
	Provider getRegistries();
}
