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

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DataGeneratorEntrypoint implements net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint {
	public static final ResourceKey<ConfiguredFeature<?, ?>> COMMON_DESERT_WELL = ResourceKey.create(
			Registries.CONFIGURED_FEATURE,
			Identifier.fromNamespaceAndPath(FabricBiomeTest.MOD_ID, "fab_desert_well")
	);
	public static final ResourceKey<PlacedFeature> PLACED_COMMON_DESERT_WELL = ResourceKey.create(
			Registries.PLACED_FEATURE,
			Identifier.fromNamespaceAndPath(FabricBiomeTest.MOD_ID, "fab_desert_well")
	);
	public static final ResourceKey<ConfiguredFeature<?, ?>> COMMON_ORE = ResourceKey.create(
			Registries.CONFIGURED_FEATURE,
			Identifier.fromNamespaceAndPath(FabricBiomeTest.MOD_ID, "common_ore")
	);
	public static final ResourceKey<PlacedFeature> PLACED_COMMON_ORE = ResourceKey.create(
			Registries.PLACED_FEATURE,
			Identifier.fromNamespaceAndPath(FabricBiomeTest.MOD_ID, "common_ore")
	);

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		FabricDataGenerator.Pack pack = dataGenerator.createPack();
		pack.addProvider(WorldgenProvider::new);
		pack.addProvider(TestBiomeTagProvider::new);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		registryBuilder.add(Registries.CONFIGURED_FEATURE, this::bootstrapConfiguredFeatures);
		registryBuilder.add(Registries.PLACED_FEATURE, this::bootstrapPlacedFeatures);
		registryBuilder.add(Registries.BIOME, TestBiomes::bootstrap);
	}

	private void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> registerable) {
		FeatureUtils.register(registerable, COMMON_DESERT_WELL, Feature.DESERT_WELL);

		OreConfiguration featureConfig = new OreConfiguration(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), Blocks.DIAMOND_BLOCK.defaultBlockState(), 5);
		FeatureUtils.register(registerable, COMMON_ORE, Feature.ORE, featureConfig);
	}

	private void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> registerable) {
		HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = registerable.lookup(Registries.CONFIGURED_FEATURE);
		Holder<ConfiguredFeature<?, ?>> commonDesertWell = configuredFeatures.getOrThrow(COMMON_DESERT_WELL);

		// The placement config is taken from the vanilla desert well, but no randomness
		PlacementUtils.register(registerable, PLACED_COMMON_DESERT_WELL, commonDesertWell,
				InSquarePlacement.spread(),
				PlacementUtils.HEIGHTMAP,
				BiomeFilter.biome()
		);

		PlacementUtils.register(registerable, PLACED_COMMON_ORE, configuredFeatures.getOrThrow(COMMON_ORE),
				CountPlacement.of(25),
				HeightRangePlacement.uniform(
					VerticalAnchor.BOTTOM,
					VerticalAnchor.TOP
				)
		);
	}
}
