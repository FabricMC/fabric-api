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

package net.fabricmc.fabric.api.tag.convention.v2;

import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;

/**
 * See {@link net.minecraft.registry.tag.BiomeTags} for vanilla tags.
 * Note that addition to some vanilla tags implies having certain functionality,
 * and as such certain biome tags exist to mirror vanilla tags, and should be preferred
 * over vanilla unless its behavior is desired.
 */
public final class ConventionalBiomeTags {
	private ConventionalBiomeTags() {
	}

	/**
	 * For biomes that should not spawn monsters over time the normal way.
	 * In other words, their Spawners and Spawn Cost entries have the monster category empty.
	 * Example: Mushroom Biomes not having Zombies, Creepers, Skeleton, nor any other normal monsters.
	 */
	public static final TagKey<Biome> NO_DEFAULT_MONSTERS = register("no_default_monsters");
	/**
	 * Biomes that should not be locatable/selectable by modded biome-locating items or abilities.
	 */
	public static final TagKey<Biome> HIDDEN_FROM_LOCATOR_SELECTION = register("hidden_from_locator_selection");

	public static final TagKey<Biome> IS_VOID = register("is_void");

	/**
	 * Biomes that spawn in the Overworld.
	 * (This is for people who want to tag their biomes as Overworld without getting
	 * side effects from {@link net.minecraft.registry.tag.BiomeTags#IS_OVERWORLD}.
	 * <p></p>
	 * NOTE: If you do not add to the vanilla Overworld tag, be sure to add to
	 * {@link net.minecraft.registry.tag.BiomeTags#STRONGHOLD_HAS_STRUCTURE} so
	 * some Strongholds do not go missing.)
	 */
	public static final TagKey<Biome> IS_OVERWORLD = register("is_overworld");

	public static final TagKey<Biome> IS_HOT = register("is_hot");
	public static final TagKey<Biome> IS_HOT_OVERWORLD = register("is_hot/overworld");
	public static final TagKey<Biome> IS_HOT_NETHER = register("is_hot/nether");
	public static final TagKey<Biome> IS_HOT_END = register("is_hot/end");

	public static final TagKey<Biome> IS_TEMPERATE = register("is_temperate");
	public static final TagKey<Biome> IS_TEMPERATE_OVERWORLD = register("is_temperate/overworld");
	public static final TagKey<Biome> IS_TEMPERATE_NETHER = register("is_temperate/nether");
	public static final TagKey<Biome> IS_TEMPERATE_END = register("is_temperate/end");

	public static final TagKey<Biome> IS_COLD = register("is_cold");
	public static final TagKey<Biome> IS_COLD_OVERWORLD = register("is_cold/overworld");
	public static final TagKey<Biome> IS_COLD_NETHER = register("is_cold/nether");
	public static final TagKey<Biome> IS_COLD_END = register("is_cold/end");

	public static final TagKey<Biome> IS_WET = register("is_wet");
	public static final TagKey<Biome> IS_WET_OVERWORLD = register("is_wet/overworld");
	public static final TagKey<Biome> IS_WET_NETHER = register("is_wet/nether");
	public static final TagKey<Biome> IS_WET_END = register("is_wet/end");

	public static final TagKey<Biome> IS_DRY = register("is_dry");
	public static final TagKey<Biome> IS_DRY_OVERWORLD = register("is_dry/overworld");
	public static final TagKey<Biome> IS_DRY_NETHER = register("is_dry/nether");
	public static final TagKey<Biome> IS_DRY_END = register("is_dry/end");

	/**
	 * If a biome has trees but spawn infrequently like a Savanna or Sparse Jungle, then the biome is considered having sparse vegetation. It does NOT mean no trees.
	 */
	public static final TagKey<Biome> IS_VEGETATION_SPARSE = register("is_sparse_vegetation");
	public static final TagKey<Biome> IS_VEGETATION_SPARSE_OVERWORLD = register("is_sparse_vegetation/overworld");
	public static final TagKey<Biome> IS_VEGETATION_SPARSE_NETHER = register("is_sparse_vegetation/nether");
	public static final TagKey<Biome> IS_VEGETATION_SPARSE_END  = register("is_sparse_vegetation/end");

	/**
	 * If a biome has more vegetation than a regular Forest biome, then it is considered having dense vegetation.
	 * This is more subjective so simply do your best with classifying your biomes.
	 */
	public static final TagKey<Biome> IS_VEGETATION_DENSE = register("is_dense_vegetation");
	public static final TagKey<Biome> IS_VEGETATION_DENSE_OVERWORLD = register("is_dense_vegetation/overworld");
	public static final TagKey<Biome> IS_VEGETATION_DENSE_NETHER = register("is_dense_vegetation/nether");
	public static final TagKey<Biome> IS_VEGETATION_DENSE_END  = register("is_dense_vegetation/end");

	public static final TagKey<Biome> IS_CONIFEROUS_TREE = register("is_tree/coniferous");
	public static final TagKey<Biome> IS_SAVANNA_TREE = register("is_tree/savanna");
	public static final TagKey<Biome> IS_JUNGLE_TREE = register("is_tree/jungle");
	public static final TagKey<Biome> IS_DECIDUOUS_TREE = register("is_tree/deciduous");

	public static final TagKey<Biome> IS_MOUNTAIN = register("is_mountain");
	public static final TagKey<Biome> IS_MOUNTAIN_PEAK = register("is_mountain/peak");
	public static final TagKey<Biome> IS_MOUNTAIN_SLOPE = register("is_mountain/slope");

	/**
	 * For temperate or warmer plains-like biomes.
	 * For snowy plains-like biomes, see {@link ConventionalBiomeTags#IS_SNOWY_PLAINS}.
	 */
	public static final TagKey<Biome> IS_PLAINS = register("is_plains");
	/**
	 * For snowy plains-like biomes.
	 * For warmer plains-like biomes, see {@link ConventionalBiomeTags#IS_PLAINS}.
	 */
	public static final TagKey<Biome> IS_SNOWY_PLAINS = register("is_snowy_plains");
	/**
	 * Biomes densely populated with deciduous trees.
	 */
	public static final TagKey<Biome> IS_FOREST = register("is_forest");
	public static final TagKey<Biome> IS_BIRCH_FOREST = register("is_birch_forest");
	public static final TagKey<Biome> IS_DARK_FOREST = register("is_dark_forest");
	public static final TagKey<Biome> IS_FLOWER_FOREST = register("is_flower_forest");
	public static final TagKey<Biome> IS_TAIGA = register("is_taiga");
	public static final TagKey<Biome> IS_OLD_GROWTH = register("is_old_growth");
	/**
	 * Biomes that spawn as a hills biome. (Previously was called Extreme Hills biome in past)
	 */
	public static final TagKey<Biome> IS_HILL = register("is_hill");
	public static final TagKey<Biome> IS_WINDSWEPT = register("is_windswept");
	public static final TagKey<Biome> IS_JUNGLE = register("is_jungle");
	public static final TagKey<Biome> IS_SAVANNA = register("is_savanna");
	public static final TagKey<Biome> IS_SWAMP = register("is_swamp");
	public static final TagKey<Biome> IS_DESERT = register("is_desert");
	public static final TagKey<Biome> IS_BADLANDS = register("is_badlands");
	/**
	 * Biomes that are dedicated to spawning on the shoreline of a body of water.
	 */
	public static final TagKey<Biome> IS_BEACH = register("is_beach");
	public static final TagKey<Biome> IS_STONY_SHORES = register("is_stony_shores");
	public static final TagKey<Biome> IS_MUSHROOM = register("is_mushroom");

	public static final TagKey<Biome> IS_RIVER = register("is_river");
	public static final TagKey<Biome> IS_OCEAN = register("is_ocean");
	public static final TagKey<Biome> IS_DEEP_OCEAN = register("is_deep_ocean");
	public static final TagKey<Biome> IS_SHALLOW_OCEAN = register("is_shallow_ocean");

	public static final TagKey<Biome> IS_UNDERGROUND = register("is_underground");
	public static final TagKey<Biome> IS_CAVE = register("is_cave");

	/**
	 * Biomes that lack any natural life or vegetation.
	 * (Example, land destroyed and sterilized by nuclear weapons)
	 */
	public static final TagKey<Biome> IS_WASTELAND = register("is_wasteland");
	/**
	 * Biomes whose flora primarily consists of dead or decaying vegetation.
	 */
	public static final TagKey<Biome> IS_DEAD = register("is_dead");
	/**
	 * Biomes whose flora primarily consists of vibrant thick vegetation and pools of water. Think of Lush Caves as an example.
	 */
	public static final TagKey<Biome> IS_LUSH = register("is_lush");
	/**
	 * Biomes with a large amount of flowers.
	 */
	public static final TagKey<Biome> IS_FLORAL = register("is_floral");
	/**
	 * For biomes that contains lots of naturally spawned snow.
	 * For biomes where lot of ice is present, see {@link ConventionalBiomeTags#IS_ICY}.
	 * Biome with lots of both snow and ice may be in both tags.
	 */
	public static final TagKey<Biome> IS_SNOWY = register("is_snowy");
	/**
	 * For land biomes where ice naturally spawns.
	 * For biomes where snow alone spawns, see {@link ConventionalBiomeTags#IS_SNOWY}.
	 */
	public static final TagKey<Biome> IS_ICY = register("is_icy");
	/**
	 * Biomes consisting primarily of water.
	 */
	public static final TagKey<Biome> IS_AQUATIC = register("is_aquatic");
	/**
	 * For water biomes where ice naturally spawns.
	 * For biomes where snow alone spawns, see {@link ConventionalBiomeTags#IS_SNOWY}.
	 */
	public static final TagKey<Biome> IS_AQUATIC_ICY = register("is_aquatic_icy");

	/**
	 * Biomes that spawn in the Nether.
	 * (This is for people who want to tag their biomes as Nether without getting
	 * side effects from {@link net.minecraft.registry.tag.BiomeTags#IS_NETHER})
	 */
	public static final TagKey<Biome> IS_NETHER = register("is_nether");
	public static final TagKey<Biome> IS_NETHER_FOREST = register("is_nether_forest");

	/**
	 * Biomes that spawn in the End.
	 * (This is for people who want to tag their biomes as End without getting
	 * side effects from {@link net.minecraft.registry.tag.BiomeTags#IS_END})
	 */
	public static final TagKey<Biome> IS_END = register("is_end");
	/**
	 * Biomes that spawn as part of the large islands outside the center island in The End dimension.
	 */
	public static final TagKey<Biome> IS_OUTER_END_ISLAND = register("is_outer_end_island");

	private static TagKey<Biome> register(String tagId) {
		return TagRegistration.BIOME_TAG.registerC(tagId);
	}
}
