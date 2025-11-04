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

package net.fabricmc.fabric.impl.biome.modification;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;

public class BiomeSelectionContextImpl implements BiomeSelectionContext {
	private final RegistryAccess dynamicRegistries;
	private final ResourceKey<Biome> key;
	private final Biome biome;
	private final Holder<Biome> entry;

	public BiomeSelectionContextImpl(RegistryAccess dynamicRegistries, ResourceKey<Biome> key, Biome biome) {
		this.dynamicRegistries = dynamicRegistries;
		this.key = key;
		this.biome = biome;
		this.entry = dynamicRegistries.lookupOrThrow(Registries.BIOME).getOrThrow(this.key);
	}

	@Override
	public ResourceKey<Biome> getBiomeKey() {
		return key;
	}

	@Override
	public Biome getBiome() {
		return biome;
	}

	@Override
	public Holder<Biome> getBiomeRegistryEntry() {
		return entry;
	}

	@Override
	public Optional<ResourceKey<ConfiguredFeature<?, ?>>> getFeatureKey(ConfiguredFeature<?, ?> configuredFeature) {
		Registry<ConfiguredFeature<?, ?>> registry = dynamicRegistries.lookupOrThrow(Registries.CONFIGURED_FEATURE);
		return registry.getResourceKey(configuredFeature);
	}

	@Override
	public Optional<ResourceKey<PlacedFeature>> getPlacedFeatureKey(PlacedFeature placedFeature) {
		Registry<PlacedFeature> registry = dynamicRegistries.lookupOrThrow(Registries.PLACED_FEATURE);
		return registry.getResourceKey(placedFeature);
	}

	@Override
	public boolean validForStructure(ResourceKey<Structure> key) {
		Structure instance = dynamicRegistries.lookupOrThrow(Registries.STRUCTURE).getValue(key);

		if (instance == null) {
			return false;
		}

		return instance.biomes().contains(getBiomeRegistryEntry());
	}

	@Override
	public Optional<ResourceKey<Structure>> getStructureKey(Structure structure) {
		Registry<Structure> registry = dynamicRegistries.lookupOrThrow(Registries.STRUCTURE);
		return registry.getResourceKey(structure);
	}

	@Override
	public boolean canGenerateIn(ResourceKey<LevelStem> dimensionKey) {
		LevelStem dimension = dynamicRegistries.lookupOrThrow(Registries.LEVEL_STEM).getValue(dimensionKey);

		if (dimension == null) {
			return false;
		}

		return dimension.generator().getBiomeSource().possibleBiomes().stream().anyMatch(entry -> entry.value() == biome);
	}

	@Override
	public boolean hasTag(TagKey<Biome> tag) {
		Registry<Biome> biomeRegistry = dynamicRegistries.lookupOrThrow(Registries.BIOME);
		return biomeRegistry.getOrThrow(getBiomeKey()).is(tag);
	}
}
