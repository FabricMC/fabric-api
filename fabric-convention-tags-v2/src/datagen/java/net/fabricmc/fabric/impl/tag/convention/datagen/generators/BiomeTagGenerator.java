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

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;

public final class BiomeTagGenerator extends FabricTagProvider<Biome> {
	public BiomeTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, Registries.BIOME, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider arg) {
		generateDimensionTags();
		generateCategoryTags();
		generateOtherBiomeTypes();
		generateClimateAndVegetationTags();
		generateTerrainDescriptorTags();
		generateBackwardsCompatTags();
	}

	private void generateDimensionTags() {
		builder(ConventionalBiomeTags.IS_NETHER)
				.addOptionalTag(BiomeTags.IS_NETHER);
		builder(ConventionalBiomeTags.IS_END)
				.addOptionalTag(BiomeTags.IS_END);
		builder(ConventionalBiomeTags.IS_OVERWORLD)
				.addOptionalTag(BiomeTags.IS_OVERWORLD);
	}

	private void generateCategoryTags() {
		builder(ConventionalBiomeTags.IS_TAIGA)
				.addOptionalTag(BiomeTags.IS_TAIGA);
		builder(ConventionalBiomeTags.IS_HILL)
				.addOptionalTag(BiomeTags.IS_HILL);
		builder(ConventionalBiomeTags.IS_WINDSWEPT)
				.add(Biomes.WINDSWEPT_HILLS)
				.add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
				.add(Biomes.WINDSWEPT_FOREST)
				.add(Biomes.WINDSWEPT_SAVANNA);
		builder(ConventionalBiomeTags.IS_JUNGLE)
				.addOptionalTag(BiomeTags.IS_JUNGLE);
		builder(ConventionalBiomeTags.IS_PLAINS)
				.add(Biomes.PLAINS)
				.add(Biomes.SUNFLOWER_PLAINS);
		builder(ConventionalBiomeTags.IS_SAVANNA)
				.addOptionalTag(BiomeTags.IS_SAVANNA);
		builder(ConventionalBiomeTags.IS_ICY)
				.add(Biomes.FROZEN_PEAKS)
				.add(Biomes.ICE_SPIKES);
		builder(ConventionalBiomeTags.IS_AQUATIC_ICY)
				.add(Biomes.FROZEN_RIVER)
				.add(Biomes.DEEP_FROZEN_OCEAN)
				.add(Biomes.FROZEN_OCEAN);
		builder(ConventionalBiomeTags.IS_SANDY)
				.add(Biomes.DESERT)
				.add(Biomes.BADLANDS)
				.add(Biomes.WOODED_BADLANDS)
				.add(Biomes.ERODED_BADLANDS)
				.add(Biomes.BEACH);
		builder(ConventionalBiomeTags.IS_SNOWY)
				.add(Biomes.SNOWY_BEACH)
				.add(Biomes.SNOWY_PLAINS)
				.add(Biomes.ICE_SPIKES)
				.add(Biomes.SNOWY_TAIGA)
				.add(Biomes.GROVE)
				.add(Biomes.SNOWY_SLOPES)
				.add(Biomes.JAGGED_PEAKS)
				.add(Biomes.FROZEN_PEAKS);
		builder(ConventionalBiomeTags.IS_BEACH)
				.addOptionalTag(BiomeTags.IS_BEACH);
		builder(ConventionalBiomeTags.IS_FOREST)
				.addOptionalTag(BiomeTags.IS_FOREST);
		builder(ConventionalBiomeTags.IS_BIRCH_FOREST)
				.add(Biomes.BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST);
		builder(ConventionalBiomeTags.IS_DARK_FOREST)
				.add(Biomes.DARK_FOREST)
				.add(Biomes.PALE_GARDEN);
		builder(ConventionalBiomeTags.IS_OCEAN)
				.addOptionalTag(BiomeTags.IS_OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_DEEP_OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_SHALLOW_OCEAN);
		builder(ConventionalBiomeTags.IS_DESERT)
				.add(Biomes.DESERT);
		builder(ConventionalBiomeTags.IS_RIVER)
				.addOptionalTag(BiomeTags.IS_RIVER);
		builder(ConventionalBiomeTags.IS_SWAMP)
				.add(Biomes.MANGROVE_SWAMP)
				.add(Biomes.SWAMP);
		builder(ConventionalBiomeTags.IS_MUSHROOM)
				.add(Biomes.MUSHROOM_FIELDS);
		builder(ConventionalBiomeTags.IS_UNDERGROUND)
				.addOptionalTag(ConventionalBiomeTags.IS_CAVE);
		builder(ConventionalBiomeTags.IS_MOUNTAIN)
				.addOptionalTag(BiomeTags.IS_MOUNTAIN)
				.addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)
				.addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE);
	}

	private void generateOtherBiomeTypes() {
		builder(ConventionalBiomeTags.IS_BADLANDS)
				.addOptionalTag(BiomeTags.IS_BADLANDS);
		builder(ConventionalBiomeTags.IS_CAVE)
				.add(Biomes.DEEP_DARK)
				.add(Biomes.DRIPSTONE_CAVES)
				.add(Biomes.LUSH_CAVES);
		builder(ConventionalBiomeTags.IS_VOID)
				.add(Biomes.THE_VOID);
		builder(ConventionalBiomeTags.IS_DEEP_OCEAN)
				.addOptionalTag(BiomeTags.IS_DEEP_OCEAN);
		builder(ConventionalBiomeTags.IS_SHALLOW_OCEAN)
				.add(Biomes.OCEAN)
				.add(Biomes.LUKEWARM_OCEAN)
				.add(Biomes.WARM_OCEAN)
				.add(Biomes.COLD_OCEAN)
				.add(Biomes.FROZEN_OCEAN);
		builder(ConventionalBiomeTags.NO_DEFAULT_MONSTERS)
				.add(Biomes.MUSHROOM_FIELDS)
				.add(Biomes.DEEP_DARK);
		builder(ConventionalBiomeTags.HIDDEN_FROM_LOCATOR_SELECTION); // Create tag file for visibility
	}

	private void generateClimateAndVegetationTags() {
		builder(ConventionalBiomeTags.IS_COLD_OVERWORLD)
				.add(Biomes.TAIGA)
				.add(Biomes.OLD_GROWTH_PINE_TAIGA)
				.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
				.add(Biomes.WINDSWEPT_HILLS)
				.add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
				.add(Biomes.WINDSWEPT_FOREST)
				.add(Biomes.SNOWY_PLAINS)
				.add(Biomes.ICE_SPIKES)
				.add(Biomes.GROVE)
				.add(Biomes.SNOWY_SLOPES)
				.add(Biomes.JAGGED_PEAKS)
				.add(Biomes.FROZEN_PEAKS)
				.add(Biomes.STONY_SHORE)
				.add(Biomes.SNOWY_BEACH)
				.add(Biomes.SNOWY_TAIGA)
				.add(Biomes.FROZEN_RIVER)
				.add(Biomes.COLD_OCEAN)
				.add(Biomes.FROZEN_OCEAN)
				.add(Biomes.DEEP_COLD_OCEAN)
				.add(Biomes.DEEP_FROZEN_OCEAN);
		builder(ConventionalBiomeTags.IS_COLD_END)
				.add(Biomes.THE_END)
				.add(Biomes.SMALL_END_ISLANDS)
				.add(Biomes.END_MIDLANDS)
				.add(Biomes.END_HIGHLANDS)
				.add(Biomes.END_BARRENS);
		builder(ConventionalBiomeTags.IS_COLD_NETHER);
		builder(ConventionalBiomeTags.IS_COLD)
				.addTag(ConventionalBiomeTags.IS_COLD_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_COLD_NETHER)
				.addTag(ConventionalBiomeTags.IS_COLD_END);

		builder(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD)
				.add(Biomes.PLAINS)
				.add(Biomes.SUNFLOWER_PLAINS)
				.add(Biomes.FOREST)
				.add(Biomes.FLOWER_FOREST)
				.add(Biomes.BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
				.add(Biomes.DARK_FOREST)
				.add(Biomes.PALE_GARDEN)
				.add(Biomes.CHERRY_GROVE)
				.add(Biomes.MEADOW)
				.add(Biomes.SWAMP)
				.add(Biomes.MANGROVE_SWAMP)
				.add(Biomes.BEACH)
				.add(Biomes.OCEAN)
				.add(Biomes.DEEP_OCEAN);
		builder(ConventionalBiomeTags.IS_TEMPERATE_NETHER);
		builder(ConventionalBiomeTags.IS_TEMPERATE_END);
		builder(ConventionalBiomeTags.IS_TEMPERATE)
				.addTag(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_TEMPERATE_NETHER)
				.addTag(ConventionalBiomeTags.IS_TEMPERATE_END);

		builder(ConventionalBiomeTags.IS_HOT_OVERWORLD)
				.add(Biomes.JUNGLE)
				.add(Biomes.BAMBOO_JUNGLE)
				.add(Biomes.SPARSE_JUNGLE)
				.add(Biomes.DESERT)
				.add(Biomes.BADLANDS)
				.add(Biomes.WOODED_BADLANDS)
				.add(Biomes.ERODED_BADLANDS)
				.add(Biomes.SAVANNA)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.WINDSWEPT_SAVANNA)
				.add(Biomes.STONY_PEAKS)
				.add(Biomes.MUSHROOM_FIELDS)
				.add(Biomes.WARM_OCEAN);
		builder(ConventionalBiomeTags.IS_HOT_NETHER)
				.add(Biomes.NETHER_WASTES)
				.add(Biomes.CRIMSON_FOREST)
				.add(Biomes.WARPED_FOREST)
				.add(Biomes.SOUL_SAND_VALLEY)
				.add(Biomes.BASALT_DELTAS);
		builder(ConventionalBiomeTags.IS_HOT_END);
		builder(ConventionalBiomeTags.IS_HOT)
				.addTag(ConventionalBiomeTags.IS_HOT_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_HOT_NETHER)
				.addTag(ConventionalBiomeTags.IS_HOT_END);

		builder(ConventionalBiomeTags.IS_WET_OVERWORLD)
				.add(Biomes.SWAMP)
				.add(Biomes.MANGROVE_SWAMP)
				.add(Biomes.JUNGLE)
				.add(Biomes.BAMBOO_JUNGLE)
				.add(Biomes.SPARSE_JUNGLE)
				.add(Biomes.BEACH)
				.add(Biomes.LUSH_CAVES)
				.add(Biomes.DRIPSTONE_CAVES);
		builder(ConventionalBiomeTags.IS_WET_NETHER);
		builder(ConventionalBiomeTags.IS_WET_END);
		builder(ConventionalBiomeTags.IS_WET)
				.addTag(ConventionalBiomeTags.IS_WET_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_WET_NETHER)
				.addTag(ConventionalBiomeTags.IS_WET_END);

		builder(ConventionalBiomeTags.IS_DRY_OVERWORLD)
				.add(Biomes.DESERT)
				.add(Biomes.BADLANDS)
				.add(Biomes.WOODED_BADLANDS)
				.add(Biomes.ERODED_BADLANDS)
				.add(Biomes.SAVANNA)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.WINDSWEPT_SAVANNA);
		builder(ConventionalBiomeTags.IS_DRY_NETHER)
				.add(Biomes.NETHER_WASTES)
				.add(Biomes.CRIMSON_FOREST)
				.add(Biomes.WARPED_FOREST)
				.add(Biomes.SOUL_SAND_VALLEY)
				.add(Biomes.BASALT_DELTAS);
		builder(ConventionalBiomeTags.IS_DRY_END)
				.add(Biomes.THE_END)
				.add(Biomes.SMALL_END_ISLANDS)
				.add(Biomes.END_MIDLANDS)
				.add(Biomes.END_HIGHLANDS)
				.add(Biomes.END_BARRENS);
		builder(ConventionalBiomeTags.IS_DRY)
				.addTag(ConventionalBiomeTags.IS_DRY_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_DRY_NETHER)
				.addTag(ConventionalBiomeTags.IS_DRY_END);

		builder(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD)
				.add(Biomes.DARK_FOREST)
				.add(Biomes.PALE_GARDEN)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
				.add(Biomes.JUNGLE)
				.add(Biomes.BAMBOO_JUNGLE)
				.add(Biomes.MANGROVE_SWAMP);
		builder(ConventionalBiomeTags.IS_VEGETATION_DENSE_NETHER);
		builder(ConventionalBiomeTags.IS_VEGETATION_DENSE_END);
		builder(ConventionalBiomeTags.IS_VEGETATION_DENSE)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_NETHER)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_END);

		builder(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD)
				.add(Biomes.WOODED_BADLANDS)
				.add(Biomes.SAVANNA)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.SPARSE_JUNGLE)
				.add(Biomes.WINDSWEPT_SAVANNA)
				.add(Biomes.WINDSWEPT_FOREST)
				.add(Biomes.WINDSWEPT_HILLS)
				.add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
				.add(Biomes.SNOWY_SLOPES)
				.add(Biomes.JAGGED_PEAKS)
				.add(Biomes.FROZEN_PEAKS);
		builder(ConventionalBiomeTags.IS_VEGETATION_SPARSE_NETHER);
		builder(ConventionalBiomeTags.IS_VEGETATION_SPARSE_END);
		builder(ConventionalBiomeTags.IS_VEGETATION_SPARSE)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_NETHER)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_END);

		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_OAK)
				.add(Biomes.FOREST)
				.add(Biomes.FLOWER_FOREST)
				.add(Biomes.SWAMP)
				.add(Biomes.WOODED_BADLANDS);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_BIRCH)
				.add(Biomes.BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_SPRUCE)
				.add(Biomes.TAIGA)
				.add(Biomes.SNOWY_TAIGA)
				.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
				.add(Biomes.OLD_GROWTH_PINE_TAIGA)
				.add(Biomes.GROVE);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_JUNGLE)
				.add(Biomes.JUNGLE)
				.add(Biomes.SPARSE_JUNGLE);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_ACACIA)
				.add(Biomes.SAVANNA)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.WINDSWEPT_SAVANNA);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_DARK_OAK)
				.add(Biomes.DARK_FOREST);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_MANGROVE)
				.add(Biomes.MANGROVE_SWAMP);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_CHERRY)
				.add(Biomes.CHERRY_GROVE);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_PALE_OAK)
				.add(Biomes.PALE_GARDEN);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_BAMBOO)
				.add(Biomes.BAMBOO_JUNGLE);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_CRIMSON)
				.add(Biomes.CRIMSON_FOREST);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_WARPED)
				.add(Biomes.WARPED_FOREST);
		builder(ConventionalBiomeTags.PRIMARY_WOOD_TYPE)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_OAK)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_BIRCH)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_SPRUCE)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_JUNGLE)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_ACACIA)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_DARK_OAK)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_MANGROVE)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_CHERRY)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_PALE_OAK)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_BAMBOO)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_CRIMSON)
				.addTag(ConventionalBiomeTags.PRIMARY_WOOD_TYPE_WARPED);

		builder(ConventionalBiomeTags.IS_CONIFEROUS_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_TAIGA)
				.add(Biomes.GROVE);
		builder(ConventionalBiomeTags.IS_DECIDUOUS_TREE)
				.add(Biomes.FOREST)
				.add(Biomes.FLOWER_FOREST)
				.add(Biomes.BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
				.add(Biomes.DARK_FOREST)
				.add(Biomes.PALE_GARDEN)
				.add(Biomes.WINDSWEPT_FOREST);
		builder(ConventionalBiomeTags.IS_JUNGLE_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_JUNGLE);
		builder(ConventionalBiomeTags.IS_SAVANNA_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_SAVANNA);

		builder(ConventionalBiomeTags.IS_LUSH)
				.add(Biomes.LUSH_CAVES);
		builder(ConventionalBiomeTags.IS_MAGICAL);
		builder(ConventionalBiomeTags.IS_RARE)
				.add(Biomes.SUNFLOWER_PLAINS)
				.add(Biomes.FLOWER_FOREST)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
				.add(Biomes.BAMBOO_JUNGLE)
				.add(Biomes.SPARSE_JUNGLE)
				.add(Biomes.ERODED_BADLANDS)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.WINDSWEPT_SAVANNA)
				.add(Biomes.ICE_SPIKES)
				.add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
				.add(Biomes.PALE_GARDEN)
				.add(Biomes.MUSHROOM_FIELDS)
				.add(Biomes.DEEP_DARK);
		builder(ConventionalBiomeTags.IS_PLATEAU)
				.add(Biomes.WOODED_BADLANDS)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.CHERRY_GROVE)
				.add(Biomes.MEADOW);
		builder(ConventionalBiomeTags.IS_SPOOKY)
				.add(Biomes.DARK_FOREST)
				.add(Biomes.PALE_GARDEN)
				.add(Biomes.DEEP_DARK);
		builder(ConventionalBiomeTags.IS_FLORAL)
				.add(Biomes.SUNFLOWER_PLAINS)
				.add(Biomes.MEADOW)
				.add(Biomes.CHERRY_GROVE)
				.addOptionalTag(ConventionalBiomeTags.IS_FLOWER_FOREST);
		builder(ConventionalBiomeTags.IS_FLOWER_FOREST)
				.add(Biomes.FLOWER_FOREST)
				.addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "flower_forests")));
		builder(ConventionalBiomeTags.IS_OLD_GROWTH)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_PINE_TAIGA)
				.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA);
	}

	private void generateTerrainDescriptorTags() {
		builder(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)
				.add(Biomes.FROZEN_PEAKS)
				.add(Biomes.JAGGED_PEAKS)
				.add(Biomes.STONY_PEAKS);
		builder(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE)
				.add(Biomes.SNOWY_SLOPES)
				.add(Biomes.MEADOW)
				.add(Biomes.GROVE)
				.add(Biomes.CHERRY_GROVE);
		builder(ConventionalBiomeTags.IS_AQUATIC)
				.addOptionalTag(ConventionalBiomeTags.IS_OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_RIVER);
		builder(ConventionalBiomeTags.IS_DEAD);
		builder(ConventionalBiomeTags.IS_WASTELAND);
		builder(ConventionalBiomeTags.IS_OUTER_END_ISLAND)
				.add(Biomes.END_HIGHLANDS)
				.add(Biomes.END_MIDLANDS)
				.add(Biomes.END_BARRENS);
		builder(ConventionalBiomeTags.IS_NETHER_FOREST)
				.add(Biomes.WARPED_FOREST)
				.add(Biomes.CRIMSON_FOREST);
		builder(ConventionalBiomeTags.IS_SNOWY_PLAINS)
				.add(Biomes.SNOWY_PLAINS);
		builder(ConventionalBiomeTags.IS_STONY_SHORES)
				.add(Biomes.STONY_SHORE);
	}

	private void generateBackwardsCompatTags() {
		// Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
		// TODO: Remove backwards compat tag entries in 1.22

		builder(ConventionalBiomeTags.IS_NETHER).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "in_nether")));
		builder(ConventionalBiomeTags.IS_END).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "in_the_end")));
		builder(ConventionalBiomeTags.IS_OVERWORLD).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "in_the_overworld")));
		builder(ConventionalBiomeTags.IS_CAVE).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "caves")));
		builder(ConventionalBiomeTags.IS_COLD_OVERWORLD).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "climate_cold")));
		builder(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "climate_temperate")));
		builder(ConventionalBiomeTags.IS_HOT_OVERWORLD).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "climate_hot")));
		builder(ConventionalBiomeTags.IS_WET_OVERWORLD).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "climate_wet")));
		builder(ConventionalBiomeTags.IS_DRY_OVERWORLD).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "climate_dry")));
		builder(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "vegetation_dense")));
		builder(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "vegetation_sparse")));
		builder(ConventionalBiomeTags.IS_CONIFEROUS_TREE).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "tree_coniferous")));
		builder(ConventionalBiomeTags.IS_DECIDUOUS_TREE).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "tree_deciduous")));
		builder(ConventionalBiomeTags.IS_JUNGLE_TREE).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "tree_jungle")));
		builder(ConventionalBiomeTags.IS_SAVANNA_TREE).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "tree_savanna")));
		builder(ConventionalBiomeTags.IS_MOUNTAIN_PEAK).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "mountain_peak")));
		builder(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "mountain_slope")));
		builder(ConventionalBiomeTags.IS_OUTER_END_ISLAND).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "end_islands")));
		builder(ConventionalBiomeTags.IS_NETHER_FOREST).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "nether_forests")));
		builder(ConventionalBiomeTags.IS_FLOWER_FOREST).addOptionalTag(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "flower_forests")));
	}
}
