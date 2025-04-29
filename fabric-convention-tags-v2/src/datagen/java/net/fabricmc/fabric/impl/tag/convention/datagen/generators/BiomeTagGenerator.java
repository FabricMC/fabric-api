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

package net.fabricmc.fabric.impl.tag.convention.datagen.generators;

import java.util.concurrent.CompletableFuture;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;

public final class BiomeTagGenerator extends FabricTagProvider<Biome> {
	public BiomeTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
		super(output, RegistryKeys.BIOME, completableFuture);
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup arg) {
		generateDimensionTags();
		generateCategoryTags();
		generateOtherBiomeTypes();
		generateClimateAndVegetationTags();
		generateTerrainDescriptorTags();
		generateBackwardsCompatTags();
	}

	private void generateDimensionTags() {
		keyTag(ConventionalBiomeTags.IS_NETHER)
				.addOptionalTag(BiomeTags.IS_NETHER);
		keyTag(ConventionalBiomeTags.IS_END)
				.addOptionalTag(BiomeTags.IS_END);
		keyTag(ConventionalBiomeTags.IS_OVERWORLD)
				.addOptionalTag(BiomeTags.IS_OVERWORLD);
	}

	private void generateCategoryTags() {
		keyTag(ConventionalBiomeTags.IS_TAIGA)
				.addOptionalTag(BiomeTags.IS_TAIGA);
		keyTag(ConventionalBiomeTags.IS_HILL)
				.addOptionalTag(BiomeTags.IS_HILL);
		keyTag(ConventionalBiomeTags.IS_WINDSWEPT)
				.add(BiomeKeys.WINDSWEPT_HILLS)
				.add(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS)
				.add(BiomeKeys.WINDSWEPT_FOREST)
				.add(BiomeKeys.WINDSWEPT_SAVANNA);
		keyTag(ConventionalBiomeTags.IS_JUNGLE)
				.addOptionalTag(BiomeTags.IS_JUNGLE);
		keyTag(ConventionalBiomeTags.IS_PLAINS)
				.add(BiomeKeys.PLAINS)
				.add(BiomeKeys.SUNFLOWER_PLAINS);
		keyTag(ConventionalBiomeTags.IS_SAVANNA)
				.addOptionalTag(BiomeTags.IS_SAVANNA);
		keyTag(ConventionalBiomeTags.IS_ICY)
				.add(BiomeKeys.FROZEN_PEAKS)
				.add(BiomeKeys.ICE_SPIKES);
		keyTag(ConventionalBiomeTags.IS_AQUATIC_ICY)
				.add(BiomeKeys.FROZEN_RIVER)
				.add(BiomeKeys.DEEP_FROZEN_OCEAN)
				.add(BiomeKeys.FROZEN_OCEAN);
		keyTag(ConventionalBiomeTags.IS_SANDY)
				.add(BiomeKeys.DESERT)
				.add(BiomeKeys.BADLANDS)
				.add(BiomeKeys.WOODED_BADLANDS)
				.add(BiomeKeys.ERODED_BADLANDS)
				.add(BiomeKeys.BEACH);
		keyTag(ConventionalBiomeTags.IS_SNOWY)
				.add(BiomeKeys.SNOWY_BEACH)
				.add(BiomeKeys.SNOWY_PLAINS)
				.add(BiomeKeys.ICE_SPIKES)
				.add(BiomeKeys.SNOWY_TAIGA)
				.add(BiomeKeys.GROVE)
				.add(BiomeKeys.SNOWY_SLOPES)
				.add(BiomeKeys.JAGGED_PEAKS)
				.add(BiomeKeys.FROZEN_PEAKS);
		keyTag(ConventionalBiomeTags.IS_BEACH)
				.addOptionalTag(BiomeTags.IS_BEACH);
		keyTag(ConventionalBiomeTags.IS_FOREST)
				.addOptionalTag(BiomeTags.IS_FOREST);
		keyTag(ConventionalBiomeTags.IS_BIRCH_FOREST)
				.add(BiomeKeys.BIRCH_FOREST)
				.add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST);
		keyTag(ConventionalBiomeTags.IS_DARK_FOREST)
				.add(BiomeKeys.DARK_FOREST)
				.add(BiomeKeys.PALE_GARDEN);
		keyTag(ConventionalBiomeTags.IS_OCEAN)
				.addOptionalTag(BiomeTags.IS_OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_DEEP_OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_SHALLOW_OCEAN);
		keyTag(ConventionalBiomeTags.IS_DESERT)
				.add(BiomeKeys.DESERT);
		keyTag(ConventionalBiomeTags.IS_RIVER)
				.addOptionalTag(BiomeTags.IS_RIVER);
		keyTag(ConventionalBiomeTags.IS_SWAMP)
				.add(BiomeKeys.MANGROVE_SWAMP)
				.add(BiomeKeys.SWAMP);
		keyTag(ConventionalBiomeTags.IS_MUSHROOM)
				.add(BiomeKeys.MUSHROOM_FIELDS);
		keyTag(ConventionalBiomeTags.IS_UNDERGROUND)
				.addOptionalTag(ConventionalBiomeTags.IS_CAVE);
		keyTag(ConventionalBiomeTags.IS_MOUNTAIN)
				.addOptionalTag(BiomeTags.IS_MOUNTAIN)
				.addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)
				.addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE);
	}

	private void generateOtherBiomeTypes() {
		keyTag(ConventionalBiomeTags.IS_BADLANDS)
				.addOptionalTag(BiomeTags.IS_BADLANDS);
		keyTag(ConventionalBiomeTags.IS_CAVE)
				.add(BiomeKeys.DEEP_DARK)
				.add(BiomeKeys.DRIPSTONE_CAVES)
				.add(BiomeKeys.LUSH_CAVES);
		keyTag(ConventionalBiomeTags.IS_VOID)
				.add(BiomeKeys.THE_VOID);
		keyTag(ConventionalBiomeTags.IS_DEEP_OCEAN)
				.addOptionalTag(BiomeTags.IS_DEEP_OCEAN);
		keyTag(ConventionalBiomeTags.IS_SHALLOW_OCEAN)
				.add(BiomeKeys.OCEAN)
				.add(BiomeKeys.LUKEWARM_OCEAN)
				.add(BiomeKeys.WARM_OCEAN)
				.add(BiomeKeys.COLD_OCEAN)
				.add(BiomeKeys.FROZEN_OCEAN);
		keyTag(ConventionalBiomeTags.NO_DEFAULT_MONSTERS)
				.add(BiomeKeys.MUSHROOM_FIELDS)
				.add(BiomeKeys.DEEP_DARK);
		keyTag(ConventionalBiomeTags.HIDDEN_FROM_LOCATOR_SELECTION); // Create tag file for visibility
	}

	private void generateClimateAndVegetationTags() {
		keyTag(ConventionalBiomeTags.IS_COLD_OVERWORLD)
				.add(BiomeKeys.TAIGA)
				.add(BiomeKeys.OLD_GROWTH_PINE_TAIGA)
				.add(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA)
				.add(BiomeKeys.WINDSWEPT_HILLS)
				.add(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS)
				.add(BiomeKeys.WINDSWEPT_FOREST)
				.add(BiomeKeys.SNOWY_PLAINS)
				.add(BiomeKeys.ICE_SPIKES)
				.add(BiomeKeys.GROVE)
				.add(BiomeKeys.SNOWY_SLOPES)
				.add(BiomeKeys.JAGGED_PEAKS)
				.add(BiomeKeys.FROZEN_PEAKS)
				.add(BiomeKeys.STONY_SHORE)
				.add(BiomeKeys.SNOWY_BEACH)
				.add(BiomeKeys.SNOWY_TAIGA)
				.add(BiomeKeys.FROZEN_RIVER)
				.add(BiomeKeys.COLD_OCEAN)
				.add(BiomeKeys.FROZEN_OCEAN)
				.add(BiomeKeys.DEEP_COLD_OCEAN)
				.add(BiomeKeys.DEEP_FROZEN_OCEAN);
		keyTag(ConventionalBiomeTags.IS_COLD_END)
				.add(BiomeKeys.THE_END)
				.add(BiomeKeys.SMALL_END_ISLANDS)
				.add(BiomeKeys.END_MIDLANDS)
				.add(BiomeKeys.END_HIGHLANDS)
				.add(BiomeKeys.END_BARRENS);
		keyTag(ConventionalBiomeTags.IS_COLD_NETHER);
		keyTag(ConventionalBiomeTags.IS_COLD)
				.addTag(ConventionalBiomeTags.IS_COLD_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_COLD_NETHER)
				.addTag(ConventionalBiomeTags.IS_COLD_END);

		keyTag(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD)
				.add(BiomeKeys.PLAINS)
				.add(BiomeKeys.SUNFLOWER_PLAINS)
				.add(BiomeKeys.FOREST)
				.add(BiomeKeys.FLOWER_FOREST)
				.add(BiomeKeys.BIRCH_FOREST)
				.add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST)
				.add(BiomeKeys.DARK_FOREST)
				.add(BiomeKeys.PALE_GARDEN)
				.add(BiomeKeys.CHERRY_GROVE)
				.add(BiomeKeys.MEADOW)
				.add(BiomeKeys.SWAMP)
				.add(BiomeKeys.MANGROVE_SWAMP)
				.add(BiomeKeys.BEACH)
				.add(BiomeKeys.OCEAN)
				.add(BiomeKeys.DEEP_OCEAN);
		keyTag(ConventionalBiomeTags.IS_TEMPERATE_NETHER);
		keyTag(ConventionalBiomeTags.IS_TEMPERATE_END);
		keyTag(ConventionalBiomeTags.IS_TEMPERATE)
				.addTag(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_TEMPERATE_NETHER)
				.addTag(ConventionalBiomeTags.IS_TEMPERATE_END);

		keyTag(ConventionalBiomeTags.IS_HOT_OVERWORLD)
				.add(BiomeKeys.JUNGLE)
				.add(BiomeKeys.BAMBOO_JUNGLE)
				.add(BiomeKeys.SPARSE_JUNGLE)
				.add(BiomeKeys.DESERT)
				.add(BiomeKeys.BADLANDS)
				.add(BiomeKeys.WOODED_BADLANDS)
				.add(BiomeKeys.ERODED_BADLANDS)
				.add(BiomeKeys.SAVANNA)
				.add(BiomeKeys.SAVANNA_PLATEAU)
				.add(BiomeKeys.WINDSWEPT_SAVANNA)
				.add(BiomeKeys.STONY_PEAKS)
				.add(BiomeKeys.MUSHROOM_FIELDS)
				.add(BiomeKeys.WARM_OCEAN);
		keyTag(ConventionalBiomeTags.IS_HOT_NETHER)
				.add(BiomeKeys.NETHER_WASTES)
				.add(BiomeKeys.CRIMSON_FOREST)
				.add(BiomeKeys.WARPED_FOREST)
				.add(BiomeKeys.SOUL_SAND_VALLEY)
				.add(BiomeKeys.BASALT_DELTAS);
		keyTag(ConventionalBiomeTags.IS_HOT_END);
		keyTag(ConventionalBiomeTags.IS_HOT)
				.addTag(ConventionalBiomeTags.IS_HOT_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_HOT_NETHER)
				.addTag(ConventionalBiomeTags.IS_HOT_END);

		keyTag(ConventionalBiomeTags.IS_WET_OVERWORLD)
				.add(BiomeKeys.SWAMP)
				.add(BiomeKeys.MANGROVE_SWAMP)
				.add(BiomeKeys.JUNGLE)
				.add(BiomeKeys.BAMBOO_JUNGLE)
				.add(BiomeKeys.SPARSE_JUNGLE)
				.add(BiomeKeys.BEACH)
				.add(BiomeKeys.LUSH_CAVES)
				.add(BiomeKeys.DRIPSTONE_CAVES);
		keyTag(ConventionalBiomeTags.IS_WET_NETHER);
		keyTag(ConventionalBiomeTags.IS_WET_END);
		keyTag(ConventionalBiomeTags.IS_WET)
				.addTag(ConventionalBiomeTags.IS_WET_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_WET_NETHER)
				.addTag(ConventionalBiomeTags.IS_WET_END);

		keyTag(ConventionalBiomeTags.IS_DRY_OVERWORLD)
				.add(BiomeKeys.DESERT)
				.add(BiomeKeys.BADLANDS)
				.add(BiomeKeys.WOODED_BADLANDS)
				.add(BiomeKeys.ERODED_BADLANDS)
				.add(BiomeKeys.SAVANNA)
				.add(BiomeKeys.SAVANNA_PLATEAU)
				.add(BiomeKeys.WINDSWEPT_SAVANNA);
		keyTag(ConventionalBiomeTags.IS_DRY_NETHER)
				.add(BiomeKeys.NETHER_WASTES)
				.add(BiomeKeys.CRIMSON_FOREST)
				.add(BiomeKeys.WARPED_FOREST)
				.add(BiomeKeys.SOUL_SAND_VALLEY)
				.add(BiomeKeys.BASALT_DELTAS);
		keyTag(ConventionalBiomeTags.IS_DRY_END)
				.add(BiomeKeys.THE_END)
				.add(BiomeKeys.SMALL_END_ISLANDS)
				.add(BiomeKeys.END_MIDLANDS)
				.add(BiomeKeys.END_HIGHLANDS)
				.add(BiomeKeys.END_BARRENS);
		keyTag(ConventionalBiomeTags.IS_DRY)
				.addTag(ConventionalBiomeTags.IS_DRY_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_DRY_NETHER)
				.addTag(ConventionalBiomeTags.IS_DRY_END);

		keyTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD)
				.add(BiomeKeys.DARK_FOREST)
				.add(BiomeKeys.PALE_GARDEN)
				.add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST)
				.add(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA)
				.add(BiomeKeys.JUNGLE)
				.add(BiomeKeys.BAMBOO_JUNGLE)
				.add(BiomeKeys.MANGROVE_SWAMP);
		keyTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_NETHER);
		keyTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_END);
		keyTag(ConventionalBiomeTags.IS_VEGETATION_DENSE)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_NETHER)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_END);

		keyTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD)
				.add(BiomeKeys.WOODED_BADLANDS)
				.add(BiomeKeys.SAVANNA)
				.add(BiomeKeys.SAVANNA_PLATEAU)
				.add(BiomeKeys.SPARSE_JUNGLE)
				.add(BiomeKeys.WINDSWEPT_SAVANNA)
				.add(BiomeKeys.WINDSWEPT_FOREST)
				.add(BiomeKeys.WINDSWEPT_HILLS)
				.add(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS)
				.add(BiomeKeys.SNOWY_SLOPES)
				.add(BiomeKeys.JAGGED_PEAKS)
				.add(BiomeKeys.FROZEN_PEAKS);
		keyTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_NETHER);
		keyTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_END);
		keyTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_NETHER)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_END);

		keyTag(ConventionalBiomeTags.IS_CONIFEROUS_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_TAIGA)
				.add(BiomeKeys.GROVE);
		keyTag(ConventionalBiomeTags.IS_DECIDUOUS_TREE)
				.add(BiomeKeys.FOREST)
				.add(BiomeKeys.FLOWER_FOREST)
				.add(BiomeKeys.BIRCH_FOREST)
				.add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST)
				.add(BiomeKeys.DARK_FOREST)
				.add(BiomeKeys.PALE_GARDEN)
				.add(BiomeKeys.WINDSWEPT_FOREST);
		keyTag(ConventionalBiomeTags.IS_JUNGLE_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_JUNGLE);
		keyTag(ConventionalBiomeTags.IS_SAVANNA_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_SAVANNA);

		keyTag(ConventionalBiomeTags.IS_LUSH)
				.add(BiomeKeys.LUSH_CAVES);
		keyTag(ConventionalBiomeTags.IS_MAGICAL);
		keyTag(ConventionalBiomeTags.IS_RARE)
				.add(BiomeKeys.SUNFLOWER_PLAINS)
				.add(BiomeKeys.FLOWER_FOREST)
				.add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST)
				.add(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA)
				.add(BiomeKeys.BAMBOO_JUNGLE)
				.add(BiomeKeys.SPARSE_JUNGLE)
				.add(BiomeKeys.ERODED_BADLANDS)
				.add(BiomeKeys.SAVANNA_PLATEAU)
				.add(BiomeKeys.WINDSWEPT_SAVANNA)
				.add(BiomeKeys.ICE_SPIKES)
				.add(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS)
				.add(BiomeKeys.PALE_GARDEN)
				.add(BiomeKeys.MUSHROOM_FIELDS)
				.add(BiomeKeys.DEEP_DARK);
		keyTag(ConventionalBiomeTags.IS_PLATEAU)
				.add(BiomeKeys.WOODED_BADLANDS)
				.add(BiomeKeys.SAVANNA_PLATEAU)
				.add(BiomeKeys.CHERRY_GROVE)
				.add(BiomeKeys.MEADOW);
		keyTag(ConventionalBiomeTags.IS_SPOOKY)
				.add(BiomeKeys.DARK_FOREST)
				.add(BiomeKeys.PALE_GARDEN)
				.add(BiomeKeys.DEEP_DARK);
		keyTag(ConventionalBiomeTags.IS_FLORAL)
				.add(BiomeKeys.SUNFLOWER_PLAINS)
				.add(BiomeKeys.MEADOW)
				.add(BiomeKeys.CHERRY_GROVE)
				.addOptionalTag(ConventionalBiomeTags.IS_FLOWER_FOREST);
		keyTag(ConventionalBiomeTags.IS_FLOWER_FOREST)
				.add(BiomeKeys.FLOWER_FOREST)
				.addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "flower_forests")));
		keyTag(ConventionalBiomeTags.IS_OLD_GROWTH)
				.add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST)
				.add(BiomeKeys.OLD_GROWTH_PINE_TAIGA)
				.add(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA);
	}

	private void generateTerrainDescriptorTags() {
		keyTag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)
				.add(BiomeKeys.FROZEN_PEAKS)
				.add(BiomeKeys.JAGGED_PEAKS)
				.add(BiomeKeys.STONY_PEAKS);
		keyTag(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE)
				.add(BiomeKeys.SNOWY_SLOPES)
				.add(BiomeKeys.MEADOW)
				.add(BiomeKeys.GROVE)
				.add(BiomeKeys.CHERRY_GROVE);
		keyTag(ConventionalBiomeTags.IS_AQUATIC)
				.addOptionalTag(ConventionalBiomeTags.IS_OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_RIVER);
		keyTag(ConventionalBiomeTags.IS_DEAD);
		keyTag(ConventionalBiomeTags.IS_WASTELAND);
		keyTag(ConventionalBiomeTags.IS_OUTER_END_ISLAND)
				.add(BiomeKeys.END_HIGHLANDS)
				.add(BiomeKeys.END_MIDLANDS)
				.add(BiomeKeys.END_BARRENS);
		keyTag(ConventionalBiomeTags.IS_NETHER_FOREST)
				.add(BiomeKeys.WARPED_FOREST)
				.add(BiomeKeys.CRIMSON_FOREST);
		keyTag(ConventionalBiomeTags.IS_SNOWY_PLAINS)
				.add(BiomeKeys.SNOWY_PLAINS);
		keyTag(ConventionalBiomeTags.IS_STONY_SHORES)
				.add(BiomeKeys.STONY_SHORE);
	}

	private void generateBackwardsCompatTags() {
		// Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
		// TODO: Remove backwards compat tag entries in 1.22

		keyTag(ConventionalBiomeTags.IS_NETHER).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "in_nether")));
		keyTag(ConventionalBiomeTags.IS_END).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "in_the_end")));
		keyTag(ConventionalBiomeTags.IS_OVERWORLD).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "in_the_overworld")));
		keyTag(ConventionalBiomeTags.IS_CAVE).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "caves")));
		keyTag(ConventionalBiomeTags.IS_COLD_OVERWORLD).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "climate_cold")));
		keyTag(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "climate_temperate")));
		keyTag(ConventionalBiomeTags.IS_HOT_OVERWORLD).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "climate_hot")));
		keyTag(ConventionalBiomeTags.IS_WET_OVERWORLD).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "climate_wet")));
		keyTag(ConventionalBiomeTags.IS_DRY_OVERWORLD).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "climate_dry")));
		keyTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "vegetation_dense")));
		keyTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "vegetation_sparse")));
		keyTag(ConventionalBiomeTags.IS_CONIFEROUS_TREE).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "tree_coniferous")));
		keyTag(ConventionalBiomeTags.IS_DECIDUOUS_TREE).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "tree_deciduous")));
		keyTag(ConventionalBiomeTags.IS_JUNGLE_TREE).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "tree_jungle")));
		keyTag(ConventionalBiomeTags.IS_SAVANNA_TREE).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "tree_savanna")));
		keyTag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "mountain_peak")));
		keyTag(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "mountain_slope")));
		keyTag(ConventionalBiomeTags.IS_OUTER_END_ISLAND).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "end_islands")));
		keyTag(ConventionalBiomeTags.IS_NETHER_FOREST).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "nether_forests")));
		keyTag(ConventionalBiomeTags.IS_FLOWER_FOREST).addOptionalTag(TagKey.of(RegistryKeys.BIOME, Identifier.of(TagUtil.C_TAG_NAMESPACE, "flower_forests")));
	}
}
