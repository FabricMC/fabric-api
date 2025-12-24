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

package net.fabricmc.fabric.impl.dimension.modification;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Utility class for accessing the worldgen data that vanilla uses to generate its vanilla datapack.
 */
public final class BuiltInRegistryKeys {
	private static final HolderLookup.Provider vanillaRegistries = VanillaRegistries.createLookup();

	private BuiltInRegistryKeys() {
	}

	public static boolean isBuiltinDimension(ResourceKey<DimensionType> key) {
		return dimensionRegistryWrapper().get(key).isPresent();
	}

	public static HolderGetter<DimensionType> dimensionRegistryWrapper() {
		return vanillaRegistries.lookupOrThrow(Registries.DIMENSION_TYPE);
	}
}
