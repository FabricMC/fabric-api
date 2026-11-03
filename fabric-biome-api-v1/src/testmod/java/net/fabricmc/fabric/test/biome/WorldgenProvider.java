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

package net.fabricmc.fabric.test.biome;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.fabricmc.fabric.api.biome.v1.data.BiomeModifier;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifiers;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

public class WorldgenProvider extends FabricDynamicRegistryProvider {
	public WorldgenProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		entries.addAll(registries.lookupOrThrow(Registries.BIOME));
		entries.addAll(registries.lookupOrThrow(Registries.PLACED_FEATURE));
		entries.addAll(registries.lookupOrThrow(Registries.FEATURE));
		entries.addAll(registries.lookupOrThrow(Registries.LEVEL_STEM));

		HolderSet<Biome> biomes = HolderSet.direct(registries.lookupOrThrow(Registries.BIOME).getOrThrow(TestBiomes.EXAMPLE_BIOME));
		HolderGetter<PlacedFeature> features = registries.lookupOrThrow(Registries.PLACED_FEATURE);
		HolderGetter<WorldCarver> carvers = registries.lookupOrThrow(Registries.CARVER);
		entries.add(modifierKey("add_features"), BiomeModifiers.addFeatures(biomes,
				HolderSet.direct(features.getOrThrow(DataGeneratorEntrypoint.PLACED_COMMON_DESERT_WELL)), GenerationStep.Decoration.SURFACE_STRUCTURES));
		entries.add(modifierKey("remove_features"), BiomeModifiers.removeFeatures(biomes,
				HolderSet.direct(features.getOrThrow(DataGeneratorEntrypoint.PLACED_COMMON_ORE)), List.of(GenerationStep.Decoration.UNDERGROUND_ORES)));
		entries.add(modifierKey("add_spawns"), BiomeModifiers.addSpawns(biomes,
				List.of(new Weighted<>(new MobSpawnSettings.SpawnerData(EntityTypes.COW, ConstantInt.of(2)), 10))));
		entries.add(modifierKey("remove_spawns"), BiomeModifiers.removeSpawns(biomes, HolderSet.direct(EntityTypes.ZOMBIE.builtInRegistryHolder())));
		entries.add(modifierKey("add_carvers"), BiomeModifiers.addCarvers(biomes, HolderSet.direct(carvers.getOrThrow(Carvers.CAVE))));
		entries.add(modifierKey("remove_carvers"), BiomeModifiers.removeCarvers(biomes, HolderSet.direct(carvers.getOrThrow(Carvers.CANYON))));
		entries.add(modifierKey("none"), BiomeModifiers.none());

		EnvironmentAttributeMap attributes = EnvironmentAttributeMap.builder().set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x123456)).build();
		entries.add(modifierKey("set_attributes"), BiomeModifiers.setAttributes(registries.lookupOrThrow(Registries.BIOME).getOrThrow(TestBiomeTags.MODIFIER_TARGETS), attributes));
		entries.add(modifierKey("disabled"), BiomeModifiers.addSpawns(biomes,
				List.of(new Weighted<>(new MobSpawnSettings.SpawnerData(EntityTypes.PIG, ConstantInt.of(1)), 10))), ResourceConditions.alwaysFalse());
	}

	private static ResourceKey<BiomeModifier> modifierKey(String path) {
		return ResourceKey.create(BiomeModifiers.REGISTRY_KEY, Identifier.fromNamespaceAndPath(FabricBiomeTest.MOD_ID, path));
	}

	@Override
	public String getName() {
		return "Fabric Biome Testmod";
	}
}
