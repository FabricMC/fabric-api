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
	 *
	 * <p>NOTE: If you do not add to the vanilla Overworld tag, be sure to add to
	 * {@link net.minecraft.registry.tag.BiomeTags#STRONGHOLD_HAS_STRUCTURE} so
	 * some Strongholds do not go missing.)
	 */
	public static final TagKey<Biome> IS_OVERWORLD = register("is_overworld");

	/**
	 * Biomes that are above 0.8 temperature. (Excluding 0.8)
	 */
	public static final TagKey<Biome> IS_HOT = register("is_hot");
	public static final TagKey<Biome> IS_HOT_OVERWORLD = register("is_hot/overworld");
	public static final TagKey<Biome> IS_HOT_NETHER = register("is_hot/nether");
	public static final TagKey<Biome> IS_HOT_END = register("is_hot/end");

	/**
	 * Biomes that are between 0.5 and 0.8 temperature range. (Including 0.5 and 0.8)
	 */
	public static final TagKey<Biome> IS_TEMPERATE = register("is_temperate");
	public static final TagKey<Biome> IS_TEMPERATE_OVERWORLD = register("is_temperate/overworld");
	public static final TagKey<Biome> IS_TEMPERATE_NETHER = register("is_temperate/nether");
	public static final TagKey<Biome> IS_TEMPERATE_END = register("is_temperate/end");

	/**
	 * Biomes that are below 0.5 temperature. (Excluding 0.5)
	 */
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
	public static final TagKey<Biome> IS_VEGETATION_SPARSE_END = register("is_sparse_vegetation/end");

	/**
	 * If a biome has more vegetation than a regular Forest biome, then it is considered having dense vegetation.
	 * This is more subjective so simply do your best with classifying your biomes.
	 */
	public static final TagKey<Biome> IS_VEGETATION_DENSE = register("is_dense_vegetation");
	public static final TagKey<Biome> IS_VEGETATION_DENSE_OVERWORLD = register("is_dense_vegetation/overworld");
	public static final TagKey<Biome> IS_VEGETATION_DENSE_NETHER = register("is_dense_vegetation/nether");
	public static final TagKey<Biome> IS_VEGETATION_DENSE_END = register("is_dense_vegetation/end");

	/**
	 * Biomes that are primarily composed of a specific wood type.
	 * For example, normal Forest biomes are mostly Oak Trees with a few Birch Trees. This biome would be in `c:primary_wood_type/oak` tag due to Oak dominance.
	 * For biomes that are composed of multiple wood types but in equal proportions, put the biome into multiple wood type tags. Biomes with very few trees like Plains are skipped.
	 *
	 * <p></p>
	 * If a mod introduces a new wood type, create a new tag under the `c:primary_wood_type/` folder.
	 * Multiple mods introducing the same new wood type will both be sticking their biomes in same tag.
	 * For example, two mods providing Willow wood types would add their biomes to `c:primary_wood_type/willow` as it is the same kind of wood, despite different blocks.
	 */
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE = register("primary_wood_type");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_OAK = register("primary_wood_type/oak");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_BIRCH = register("primary_wood_type/birch");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_SPRUCE = register("primary_wood_type/spruce");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_JUNGLE = register("primary_wood_type/jungle");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_ACACIA = register("primary_wood_type/acacia");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_DARK_OAK = register("primary_wood_type/dark_oak");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_MANGROVE = register("primary_wood_type/mangrove");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_CHERRY = register("primary_wood_type/cherry");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_PALE_OAK = register("primary_wood_type/pale_oak");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_BAMBOO = register("primary_wood_type/bamboo");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_CRIMSON = register("primary_wood_type/crimson");
	public static final TagKey<Biome> PRIMARY_WOOD_TYPE_WARPED = register("primary_wood_type/warped");

	/**
	 * Biomes whose trees are a kind of Conifer-like tree.
	 * May not necessarily be a Spruce wood type.
	 */
	public static final TagKey<Biome> IS_CONIFEROUS_TREE = register("is_tree/coniferous");
	/**
	 * Biomes whose trees are a kind of Savanna-like tree.
	 * May not necessarily be a Savanna wood type.
	 */
	public static final TagKey<Biome> IS_SAVANNA_TREE = register("is_tree/savanna");
	/**
	 * Biomes whose trees are a kind of Jungle-like tree.
	 * May not necessarily be a Jungle wood type.
	 */
	public static final TagKey<Biome> IS_JUNGLE_TREE = register("is_tree/jungle");
	/**
	 * Biomes whose trees are a kind of Deciduous-like tree.
	 * May not necessarily be an Oak or Birch wood type.
	 */
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
	 * (This is for people who want to tag their biomes without getting side effects from {@link ConventionalBiomeTags#IS_FOREST})
	 */
	public static final TagKey<Biome> IS_FOREST = register("is_forest");
	/**
	 * For biomes that are a variant of Birch Forest. (has mostly birch trees)
	 */
	public static final TagKey<Biome> IS_BIRCH_FOREST = register("is_birch_forest");
	/**
	 * For biomes that are a variant of Dark Forest. (Has roofed trees that are reminiscent of Dark Forest's style)
	 * Pale Gardens is included in this tag because according to Mojang's blog post, they state it is a variation of the Dark Forest biome.
	 * <a href="https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-4#pale_garden:~:text=The%20Pale%20Garden%20is%20a%20biome%20variation%20of%20Dark%20Forest">...</a>.
	 */
	public static final TagKey<Biome> IS_DARK_FOREST = register("is_dark_forest");
	/**
	 * For biomes that are a variant of Flower Forest. (Is very dense in variety of flowers)
	 */
	public static final TagKey<Biome> IS_FLOWER_FOREST = register("is_flower_forest");
	/**
	 * Biomes that spawn as a taiga.
	 * (This is for people who want to tag their biomes without getting side effects from {@link ConventionalBiomeTags#IS_TAIGA})
	 */
	public static final TagKey<Biome> IS_TAIGA = register("is_taiga");
	/**
	 * For biomes that are an "old growth" variant of a regular biome.
	 * Usually this includes taller or different tree styles as if the biome is older.
	 */
	public static final TagKey<Biome> IS_OLD_GROWTH = register("is_old_growth");
	/**
	 * Biomes that spawn as a hills biome. (Previously was called Extreme Hills biome in past)
	 * (This is for people who want to tag their biomes without getting side effects from {@link ConventionalBiomeTags#IS_HILL})
	 */
	public static final TagKey<Biome> IS_HILL = register("is_hill");
	/**
	 * For biomes that are a "windswept" variant of a regular biome.
	 * Usually these biomes includes fewer trees than normal and more exposed stone on hilly terrain.
	 */
	public static final TagKey<Biome> IS_WINDSWEPT = register("is_windswept");
	/**
	 * Biomes that spawn as a jungle.
	 * (This is for people who want to tag their biomes without getting side effects from {@link ConventionalBiomeTags#IS_JUNGLE})
	 */
	public static final TagKey<Biome> IS_JUNGLE = register("is_jungle");
	/**
	 * Biomes that spawn as a savanna.
	 * (This is for people who want to tag their biomes without getting side effects from {@link ConventionalBiomeTags#IS_SAVANNA})
	 */
	public static final TagKey<Biome> IS_SAVANNA = register("is_savanna");
	/**
	 * For biomes that are considered a swamp such as Swamp or Mangrove Swamp.
	 */
	public static final TagKey<Biome> IS_SWAMP = register("is_swamp");
	/**
	 * For biomes that are considered a regular desert.
	 * Badlands have their own tag to better separate them from this tag.
	 */
	public static final TagKey<Biome> IS_DESERT = register("is_desert");
	/**
	 * Biomes that spawn as a badlands.
	 * (This is for people who want to tag their biomes without getting side effects from {@link ConventionalBiomeTags#IS_BADLANDS})
	 */
	public static final TagKey<Biome> IS_BADLANDS = register("is_badlands");
	/**
	 * Non-stony biomes that are dedicated to spawning on the shoreline of a body of water.
	 * (This is for people who want to tag their biomes without getting side effects from {@link ConventionalBiomeTags#IS_BEACH})
	 */
	public static final TagKey<Biome> IS_BEACH = register("is_beach");
	/**
	 * Stony biomes that are dedicated to spawning on the shoreline of a body of water.
	 */
	public static final TagKey<Biome> IS_STONY_SHORES = register("is_stony_shores");
	/**
	 * For biomes that spawn primarily mushrooms.
	 */
	public static final TagKey<Biome> IS_MUSHROOM = register("is_mushroom");

	/**
	 * Biomes that spawn as a river.
	 * (This is for people who want to tag their biomes without getting side effects from {@link ConventionalBiomeTags#IS_RIVER})
	 */
	public static final TagKey<Biome> IS_RIVER = register("is_river");
	/**
	 * Biomes that spawn as part of the world's oceans.
	 * (This is for people who want to tag their biomes without getting side effects from {@link ConventionalBiomeTags#IS_OCEAN})
	 */
	public static final TagKey<Biome> IS_OCEAN = register("is_ocean");
	/**
	 * Biomes that spawn as part of the world's oceans that have low depth.
	 * (This is for people who want to tag their biomes without getting side effects from {@link ConventionalBiomeTags#IS_DEEP_OCEAN})
	 */
	public static final TagKey<Biome> IS_DEEP_OCEAN = register("is_deep_ocean");
	/**
	 * Biomes that spawn as part of the world's oceans that have shallow depth.
	 */
	public static final TagKey<Biome> IS_SHALLOW_OCEAN = register("is_shallow_ocean");

	/**
	 * Biomes that spawn primarily underground. (Not necessarily always a cave)
	 */
	public static final TagKey<Biome> IS_UNDERGROUND = register("is_underground");
	/**
	 * Biomes dedicated to decorating caves such as Lush Caves or Dripstone Caves.
	 */
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
	 * Biomes whose theme revolves around magic. Like a forest full of fairies or plants of magical abilities.
	 */
	public static final TagKey<Biome> IS_MAGICAL = register("is_magical");
	/**
	 * Intended for biomes that spawns infrequently and can be difficult to find.
	 */
	public static final TagKey<Biome> IS_RARE = register("is_rare");
	/**
	 * Biomes that spawn as a flat-topped hill often.
	 */
	public static final TagKey<Biome> IS_PLATEAU = register("is_plateau");
	/**
	 * For biomes that are intended to be creepy or scary. For example, see Deep Dark biome or Dark Forest biome.
	 */
	public static final TagKey<Biome> IS_SPOOKY = register("is_spooky");
	/**
	 * Biomes with a large amount of flowers.
	 */
	public static final TagKey<Biome> IS_FLORAL = register("is_floral");
	/**
	 * Biomes that are able to spawn sand-based blocks on the surface.
	 */
	public static final TagKey<Biome> IS_SANDY = register("is_sandy");
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
