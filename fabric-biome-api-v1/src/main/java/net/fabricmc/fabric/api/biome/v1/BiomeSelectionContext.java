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

import java.util.List;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Context given to a biome selector for deciding whether it applies to a biome or not.
 */
public interface BiomeSelectionContext {
	ResourceKey<Biome> getBiomeKey();

	/**
	 * Returns the biome with modifications by biome modifiers of higher priority already applied.
	 */
	Biome getBiome();

	Holder<Biome> getBiomeRegistryEntry();

	/**
	 * Returns true if this biome contains a placed feature referencing a configured feature with the given key.
	 */
	default boolean hasFeature(ResourceKey<ConfiguredFeature<?, ?>> key) {
		List<HolderSet<PlacedFeature>> featureSteps = getBiome().getGenerationSettings().features();

		for (HolderSet<PlacedFeature> featureSuppliers : featureSteps) {
			for (Holder<PlacedFeature> featureSupplier : featureSuppliers) {
				if (featureSupplier.value().getFeatures().anyMatch(cf -> getFeatureKey(cf).orElse(null) == key)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Returns true if this biome contains a placed feature with the given key.
	 */
	default boolean hasPlacedFeature(ResourceKey<PlacedFeature> key) {
		List<HolderSet<PlacedFeature>> featureSteps = getBiome().getGenerationSettings().features();

		for (HolderSet<PlacedFeature> featureSuppliers : featureSteps) {
			for (Holder<PlacedFeature> featureSupplier : featureSuppliers) {
				if (getPlacedFeatureKey(featureSupplier.value()).orElse(null) == key) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Tries to retrieve the registry key for the given configured feature, which should be from this biomes
	 * current feature list. May be empty if the configured feature is not registered, or does not come
	 * from this biomes feature list.
	 */
	Optional<ResourceKey<ConfiguredFeature<?, ?>>> getFeatureKey(ConfiguredFeature<?, ?> configuredFeature);

	/**
	 * Tries to retrieve the registry key for the given placed feature, which should be from this biomes
	 * current feature list. May be empty if the placed feature is not registered, or does not come
	 * from this biomes feature list.
	 */
	Optional<ResourceKey<PlacedFeature>> getPlacedFeatureKey(PlacedFeature placedFeature);

	/**
	 * Returns true if the configured structure with the given key can start in this biome in any chunk generator
	 * used by the current world-save.
	 */
	boolean validForStructure(ResourceKey<Structure> key);

	/**
	 * Tries to retrieve the registry key for the given configured feature, which should be from this biomes
	 * current structure list. May be empty if the configured feature is not registered, or does not come
	 * from this biomes feature list.
	 */
	Optional<ResourceKey<Structure>> getStructureKey(Structure structureFeature);

	/**
	 * Tries to determine whether this biome generates in a specific dimension, based on the {@link net.minecraft.world.level.levelgen.WorldOptions}
	 * used by the current world-save.
	 *
	 * <p>If no dimension options exist for the given dimension key, <code>false</code> is returned.
	 */
	boolean canGenerateIn(ResourceKey<LevelStem> dimensionKey);

	/**
	 * {@return true if this biome is in the given {@link TagKey }}.
	 */
	boolean hasTag(TagKey<Biome> tag);
}
