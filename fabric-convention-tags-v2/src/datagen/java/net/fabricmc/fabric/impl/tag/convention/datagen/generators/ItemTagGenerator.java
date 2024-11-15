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

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;

public final class ItemTagGenerator extends FabricTagProvider.ItemTagProvider {
	public ItemTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture, FabricTagProvider.BlockTagProvider blockTags) {
		super(output, completableFuture, blockTags);
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup arg) {
		generateToolTags();
		generateBucketTags();
		generateOreAndRelatedTags();
		generateConsumableTags();
		generateFoodTags();
		generateDyeTags();
		generateDyedTags();
		generateCropTags();
		generateVillagerJobSites();
		generateOtherTags();
		copyItemTags();
		generateBackwardsCompatTags();
	}

	private void copyItemTags() {
		copy(ConventionalBlockTags.STONES, ConventionalItemTags.STONES);
		copy(ConventionalBlockTags.COBBLESTONES, ConventionalItemTags.COBBLESTONES);
		copy(ConventionalBlockTags.OBSIDIANS, ConventionalItemTags.OBSIDIANS);
		copy(ConventionalBlockTags.NORMAL_OBSIDIANS, ConventionalItemTags.NORMAL_OBSIDIANS);
		copy(ConventionalBlockTags.CRYING_OBSIDIANS, ConventionalItemTags.CRYING_OBSIDIANS);
		copy(ConventionalBlockTags.BARRELS, ConventionalItemTags.BARRELS);
		copy(ConventionalBlockTags.WOODEN_BARRELS, ConventionalItemTags.WOODEN_BARRELS);
		copy(ConventionalBlockTags.BOOKSHELVES, ConventionalItemTags.BOOKSHELVES);
		copy(ConventionalBlockTags.CHESTS, ConventionalItemTags.CHESTS);
		copy(ConventionalBlockTags.WOODEN_CHESTS, ConventionalItemTags.WOODEN_CHESTS);
		copy(ConventionalBlockTags.GLASS_BLOCKS, ConventionalItemTags.GLASS_BLOCKS);
		copy(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS, ConventionalItemTags.GLASS_BLOCKS_COLORLESS);
		copy(ConventionalBlockTags.GLASS_BLOCKS_TINTED, ConventionalItemTags.GLASS_BLOCKS_TINTED);
		copy(ConventionalBlockTags.GLASS_BLOCKS_CHEAP, ConventionalItemTags.GLASS_BLOCKS_CHEAP);
		copy(ConventionalBlockTags.GLASS_PANES, ConventionalItemTags.GLASS_PANES);
		copy(ConventionalBlockTags.GLASS_PANES_COLORLESS, ConventionalItemTags.GLASS_PANES_COLORLESS);
		getOrCreateTagBuilder(ConventionalItemTags.SHULKER_BOXES)
				.add(Items.SHULKER_BOX)
				.add(Items.WHITE_SHULKER_BOX)
				.add(Items.ORANGE_SHULKER_BOX)
				.add(Items.MAGENTA_SHULKER_BOX)
				.add(Items.LIGHT_BLUE_SHULKER_BOX)
				.add(Items.YELLOW_SHULKER_BOX)
				.add(Items.LIME_SHULKER_BOX)
				.add(Items.PINK_SHULKER_BOX)
				.add(Items.GRAY_SHULKER_BOX)
				.add(Items.LIGHT_GRAY_SHULKER_BOX)
				.add(Items.CYAN_SHULKER_BOX)
				.add(Items.PURPLE_SHULKER_BOX)
				.add(Items.BLUE_SHULKER_BOX)
				.add(Items.BROWN_SHULKER_BOX)
				.add(Items.GREEN_SHULKER_BOX)
				.add(Items.RED_SHULKER_BOX)
				.add(Items.BLACK_SHULKER_BOX);
		copy(ConventionalBlockTags.GLAZED_TERRACOTTAS, ConventionalItemTags.GLAZED_TERRACOTTAS);
		copy(ConventionalBlockTags.GLAZED_TERRACOTTA, ConventionalItemTags.GLAZED_TERRACOTTA);
		copy(ConventionalBlockTags.CONCRETES, ConventionalItemTags.CONCRETES);
		copy(ConventionalBlockTags.CONCRETE, ConventionalItemTags.CONCRETE);
		getOrCreateTagBuilder(ConventionalItemTags.CONCRETE_POWDERS)
				.add(Items.WHITE_CONCRETE_POWDER)
				.add(Items.ORANGE_CONCRETE_POWDER)
				.add(Items.MAGENTA_CONCRETE_POWDER)
				.add(Items.LIGHT_BLUE_CONCRETE_POWDER)
				.add(Items.YELLOW_CONCRETE_POWDER)
				.add(Items.LIME_CONCRETE_POWDER)
				.add(Items.PINK_CONCRETE_POWDER)
				.add(Items.GRAY_CONCRETE_POWDER)
				.add(Items.LIGHT_GRAY_CONCRETE_POWDER)
				.add(Items.CYAN_CONCRETE_POWDER)
				.add(Items.PURPLE_CONCRETE_POWDER)
				.add(Items.BLUE_CONCRETE_POWDER)
				.add(Items.BROWN_CONCRETE_POWDER)
				.add(Items.GREEN_CONCRETE_POWDER)
				.add(Items.RED_CONCRETE_POWDER)
				.add(Items.BLACK_CONCRETE_POWDER);
		getOrCreateTagBuilder(ConventionalItemTags.CONCRETE_POWDER)
				.add(Items.WHITE_CONCRETE_POWDER)
				.add(Items.ORANGE_CONCRETE_POWDER)
				.add(Items.MAGENTA_CONCRETE_POWDER)
				.add(Items.LIGHT_BLUE_CONCRETE_POWDER)
				.add(Items.YELLOW_CONCRETE_POWDER)
				.add(Items.LIME_CONCRETE_POWDER)
				.add(Items.PINK_CONCRETE_POWDER)
				.add(Items.GRAY_CONCRETE_POWDER)
				.add(Items.LIGHT_GRAY_CONCRETE_POWDER)
				.add(Items.CYAN_CONCRETE_POWDER)
				.add(Items.PURPLE_CONCRETE_POWDER)
				.add(Items.BLUE_CONCRETE_POWDER)
				.add(Items.BROWN_CONCRETE_POWDER)
				.add(Items.GREEN_CONCRETE_POWDER)
				.add(Items.RED_CONCRETE_POWDER)
				.add(Items.BLACK_CONCRETE_POWDER);

		copy(ConventionalBlockTags.BUDDING_BLOCKS, ConventionalItemTags.BUDDING_BLOCKS);
		copy(ConventionalBlockTags.BUDS, ConventionalItemTags.BUDS);
		copy(ConventionalBlockTags.CLUSTERS, ConventionalItemTags.CLUSTERS);

		copy(ConventionalBlockTags.SANDSTONE_BLOCKS, ConventionalItemTags.SANDSTONE_BLOCKS);
		copy(ConventionalBlockTags.SANDSTONE_SLABS, ConventionalItemTags.SANDSTONE_SLABS);
		copy(ConventionalBlockTags.SANDSTONE_STAIRS, ConventionalItemTags.SANDSTONE_STAIRS);
		copy(ConventionalBlockTags.RED_SANDSTONE_BLOCKS, ConventionalItemTags.RED_SANDSTONE_BLOCKS);
		copy(ConventionalBlockTags.RED_SANDSTONE_SLABS, ConventionalItemTags.RED_SANDSTONE_SLABS);
		copy(ConventionalBlockTags.RED_SANDSTONE_STAIRS, ConventionalItemTags.RED_SANDSTONE_STAIRS);
		copy(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS, ConventionalItemTags.UNCOLORED_SANDSTONE_BLOCKS);
		copy(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS, ConventionalItemTags.UNCOLORED_SANDSTONE_SLABS);
		copy(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS, ConventionalItemTags.UNCOLORED_SANDSTONE_STAIRS);

		copy(ConventionalBlockTags.STORAGE_BLOCKS, ConventionalItemTags.STORAGE_BLOCKS);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_BONE_MEAL, ConventionalItemTags.STORAGE_BLOCKS_BONE_MEAL);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_COAL, ConventionalItemTags.STORAGE_BLOCKS_COAL);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_COPPER, ConventionalItemTags.STORAGE_BLOCKS_COPPER);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_DIAMOND, ConventionalItemTags.STORAGE_BLOCKS_DIAMOND);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_DRIED_KELP, ConventionalItemTags.STORAGE_BLOCKS_DRIED_KELP);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_EMERALD, ConventionalItemTags.STORAGE_BLOCKS_EMERALD);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_GOLD, ConventionalItemTags.STORAGE_BLOCKS_GOLD);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_IRON, ConventionalItemTags.STORAGE_BLOCKS_IRON);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_LAPIS, ConventionalItemTags.STORAGE_BLOCKS_LAPIS);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_NETHERITE, ConventionalItemTags.STORAGE_BLOCKS_NETHERITE);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_RAW_COPPER, ConventionalItemTags.STORAGE_BLOCKS_RAW_COPPER);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_RAW_GOLD, ConventionalItemTags.STORAGE_BLOCKS_RAW_GOLD);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON, ConventionalItemTags.STORAGE_BLOCKS_RAW_IRON);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_REDSTONE, ConventionalItemTags.STORAGE_BLOCKS_REDSTONE);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_SLIME, ConventionalItemTags.STORAGE_BLOCKS_SLIME);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_WHEAT, ConventionalItemTags.STORAGE_BLOCKS_WHEAT);

		copy(ConventionalBlockTags.STRIPPED_LOGS, ConventionalItemTags.STRIPPED_LOGS);
		copy(ConventionalBlockTags.STRIPPED_WOODS, ConventionalItemTags.STRIPPED_WOODS);
	}

	private void generateDyeTags() {
		getOrCreateTagBuilder(ConventionalItemTags.DYES)
				.addOptionalTag(ConventionalItemTags.WHITE_DYES)
				.addOptionalTag(ConventionalItemTags.ORANGE_DYES)
				.addOptionalTag(ConventionalItemTags.MAGENTA_DYES)
				.addOptionalTag(ConventionalItemTags.LIGHT_BLUE_DYES)
				.addOptionalTag(ConventionalItemTags.YELLOW_DYES)
				.addOptionalTag(ConventionalItemTags.LIME_DYES)
				.addOptionalTag(ConventionalItemTags.PINK_DYES)
				.addOptionalTag(ConventionalItemTags.GRAY_DYES)
				.addOptionalTag(ConventionalItemTags.LIGHT_GRAY_DYES)
				.addOptionalTag(ConventionalItemTags.CYAN_DYES)
				.addOptionalTag(ConventionalItemTags.PURPLE_DYES)
				.addOptionalTag(ConventionalItemTags.BLUE_DYES)
				.addOptionalTag(ConventionalItemTags.BROWN_DYES)
				.addOptionalTag(ConventionalItemTags.GREEN_DYES)
				.addOptionalTag(ConventionalItemTags.RED_DYES)
				.addOptionalTag(ConventionalItemTags.BLACK_DYES);
		getOrCreateTagBuilder(ConventionalItemTags.BLACK_DYES)
				.add(Items.BLACK_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.BLUE_DYES)
				.add(Items.BLUE_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.BROWN_DYES)
				.add(Items.BROWN_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.GREEN_DYES)
				.add(Items.GREEN_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.RED_DYES)
				.add(Items.RED_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.WHITE_DYES)
				.add(Items.WHITE_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.YELLOW_DYES)
				.add(Items.YELLOW_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.LIGHT_BLUE_DYES)
				.add(Items.LIGHT_BLUE_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.LIGHT_GRAY_DYES)
				.add(Items.LIGHT_GRAY_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.LIME_DYES)
				.add(Items.LIME_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.MAGENTA_DYES)
				.add(Items.MAGENTA_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.ORANGE_DYES)
				.add(Items.ORANGE_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.PINK_DYES)
				.add(Items.PINK_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.CYAN_DYES)
				.add(Items.CYAN_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.GRAY_DYES)
				.add(Items.GRAY_DYE);
		getOrCreateTagBuilder(ConventionalItemTags.PURPLE_DYES)
				.add(Items.PURPLE_DYE);
	}

	private void generateConsumableTags() {
		getOrCreateTagBuilder(ConventionalItemTags.POTIONS)
				.add(Items.POTION)
				.add(Items.SPLASH_POTION)
				.add(Items.LINGERING_POTION);
	}

	private void generateFoodTags() {
		getOrCreateTagBuilder(ConventionalItemTags.FRUIT_FOODS)
				.add(Items.APPLE)
				.add(Items.GOLDEN_APPLE)
				.add(Items.ENCHANTED_GOLDEN_APPLE)
				.add(Items.CHORUS_FRUIT)
				.add(Items.MELON_SLICE)
				.addOptionalTag(ConventionalItemTags.FRUITS_FOODS);

		getOrCreateTagBuilder(ConventionalItemTags.VEGETABLE_FOODS)
				.add(Items.CARROT)
				.add(Items.GOLDEN_CARROT)
				.add(Items.POTATO)
				.add(Items.BEETROOT)
				.addOptionalTag(ConventionalItemTags.VEGETABLES_FOODS);

		getOrCreateTagBuilder(ConventionalItemTags.BERRY_FOODS)
				.add(Items.SWEET_BERRIES)
				.add(Items.GLOW_BERRIES)
				.addOptionalTag(ConventionalItemTags.BERRIES_FOODS);

		getOrCreateTagBuilder(ConventionalItemTags.BREAD_FOODS)
				.add(Items.BREAD)
				.addOptionalTag(ConventionalItemTags.BREADS_FOODS);

		getOrCreateTagBuilder(ConventionalItemTags.COOKIE_FOODS)
				.add(Items.COOKIE)
				.addOptionalTag(ConventionalItemTags.COOKIES_FOODS);

		getOrCreateTagBuilder(ConventionalItemTags.RAW_MEAT_FOODS)
				.add(Items.BEEF)
				.add(Items.PORKCHOP)
				.add(Items.CHICKEN)
				.add(Items.RABBIT)
				.add(Items.MUTTON)
				.addOptionalTag(ConventionalItemTags.RAW_MEATS_FOODS);

		getOrCreateTagBuilder(ConventionalItemTags.RAW_FISH_FOODS)
				.add(Items.COD)
				.add(Items.SALMON)
				.add(Items.TROPICAL_FISH)
				.add(Items.PUFFERFISH)
				.addOptionalTag(ConventionalItemTags.RAW_FISHES_FOODS);

		getOrCreateTagBuilder(ConventionalItemTags.COOKED_MEAT_FOODS)
				.add(Items.COOKED_BEEF)
				.add(Items.COOKED_PORKCHOP)
				.add(Items.COOKED_CHICKEN)
				.add(Items.COOKED_RABBIT)
				.add(Items.COOKED_MUTTON)
				.addOptionalTag(ConventionalItemTags.COOKED_MEATS_FOODS);

		getOrCreateTagBuilder(ConventionalItemTags.COOKED_FISH_FOODS)
				.add(Items.COOKED_COD)
				.add(Items.COOKED_SALMON)
				.addOptionalTag(ConventionalItemTags.COOKED_FISHES_FOODS);

		getOrCreateTagBuilder(ConventionalItemTags.SOUP_FOODS)
				.add(Items.BEETROOT_SOUP)
				.add(Items.MUSHROOM_STEW)
				.add(Items.RABBIT_STEW)
				.add(Items.SUSPICIOUS_STEW)
				.addOptionalTag(ConventionalItemTags.SOUPS_FOODS);

		getOrCreateTagBuilder(ConventionalItemTags.CANDY_FOODS)
				.addOptionalTag(ConventionalItemTags.CANDIES_FOODS);

		getOrCreateTagBuilder(ConventionalItemTags.PIE_FOODS)
				.add(Items.PUMPKIN_PIE);

		getOrCreateTagBuilder(ConventionalItemTags.GOLDEN_FOODS)
				.add(Items.GOLDEN_APPLE)
				.add(Items.ENCHANTED_GOLDEN_APPLE)
				.add(Items.GOLDEN_CARROT);

		getOrCreateTagBuilder(ConventionalItemTags.EDIBLE_WHEN_PLACED_FOODS)
				.add(Items.CAKE);

		getOrCreateTagBuilder(ConventionalItemTags.FOOD_POISONING_FOODS)
				.add(Items.POISONOUS_POTATO)
				.add(Items.PUFFERFISH)
				.add(Items.SPIDER_EYE)
				.add(Items.CHICKEN)
				.add(Items.ROTTEN_FLESH);

		getOrCreateTagBuilder(ConventionalItemTags.ANIMAL_FOODS)
				.addOptionalTag(ItemTags.ARMADILLO_FOOD)
				.addOptionalTag(ItemTags.AXOLOTL_FOOD)
				.addOptionalTag(ItemTags.BEE_FOOD)
				.addOptionalTag(ItemTags.CAMEL_FOOD)
				.addOptionalTag(ItemTags.CAT_FOOD)
				.addOptionalTag(ItemTags.CHICKEN_FOOD)
				.addOptionalTag(ItemTags.COW_FOOD)
				.addOptionalTag(ItemTags.FOX_FOOD)
				.addOptionalTag(ItemTags.FROG_FOOD)
				.addOptionalTag(ItemTags.GOAT_FOOD)
				.addOptionalTag(ItemTags.HOGLIN_FOOD)
				.addOptionalTag(ItemTags.HORSE_FOOD)
				.addOptionalTag(ItemTags.LLAMA_FOOD)
				.addOptionalTag(ItemTags.OCELOT_FOOD)
				.addOptionalTag(ItemTags.PANDA_FOOD)
				.addOptionalTag(ItemTags.PARROT_FOOD)
				.addOptionalTag(ItemTags.PIG_FOOD)
				.addOptionalTag(ItemTags.PIGLIN_FOOD)
				.addOptionalTag(ItemTags.RABBIT_FOOD)
				.addOptionalTag(ItemTags.SHEEP_FOOD)
				.addOptionalTag(ItemTags.SNIFFER_FOOD)
				.addOptionalTag(ItemTags.STRIDER_FOOD)
				.addOptionalTag(ItemTags.TURTLE_FOOD)
				.addOptionalTag(ItemTags.WOLF_FOOD);

		getOrCreateTagBuilder(ConventionalItemTags.FOODS)
				.add(Items.BAKED_POTATO)
				.add(Items.PUMPKIN_PIE)
				.add(Items.HONEY_BOTTLE)
				.add(Items.OMINOUS_BOTTLE)
				.add(Items.DRIED_KELP)
				.addOptionalTag(ConventionalItemTags.FRUIT_FOODS)
				.addOptionalTag(ConventionalItemTags.VEGETABLE_FOODS)
				.addOptionalTag(ConventionalItemTags.BERRY_FOODS)
				.addOptionalTag(ConventionalItemTags.BREAD_FOODS)
				.addOptionalTag(ConventionalItemTags.COOKIE_FOODS)
				.addOptionalTag(ConventionalItemTags.RAW_MEAT_FOODS)
				.addOptionalTag(ConventionalItemTags.RAW_FISH_FOODS)
				.addOptionalTag(ConventionalItemTags.COOKED_MEAT_FOODS)
				.addOptionalTag(ConventionalItemTags.COOKED_FISH_FOODS)
				.addOptionalTag(ConventionalItemTags.SOUP_FOODS)
				.addOptionalTag(ConventionalItemTags.CANDY_FOODS)
				.addOptionalTag(ConventionalItemTags.PIE_FOODS)
				.addOptionalTag(ConventionalItemTags.GOLDEN_FOODS)
				.addOptionalTag(ConventionalItemTags.EDIBLE_WHEN_PLACED_FOODS)
				.addOptionalTag(ConventionalItemTags.FOOD_POISONING_FOODS);

		// Deprecated tags below
		getOrCreateTagBuilder(ConventionalItemTags.FRUITS_FOODS)
				.add(Items.APPLE)
				.add(Items.GOLDEN_APPLE)
				.add(Items.ENCHANTED_GOLDEN_APPLE)
				.add(Items.MELON_SLICE);

		getOrCreateTagBuilder(ConventionalItemTags.VEGETABLES_FOODS)
				.add(Items.CARROT)
				.add(Items.GOLDEN_CARROT)
				.add(Items.POTATO)
				.add(Items.BEETROOT);

		getOrCreateTagBuilder(ConventionalItemTags.BERRIES_FOODS)
				.add(Items.SWEET_BERRIES)
				.add(Items.GLOW_BERRIES);

		getOrCreateTagBuilder(ConventionalItemTags.BREADS_FOODS)
				.add(Items.BREAD);

		getOrCreateTagBuilder(ConventionalItemTags.COOKIES_FOODS)
				.add(Items.COOKIE);

		getOrCreateTagBuilder(ConventionalItemTags.RAW_MEATS_FOODS)
				.add(Items.BEEF)
				.add(Items.PORKCHOP)
				.add(Items.CHICKEN)
				.add(Items.RABBIT)
				.add(Items.MUTTON);

		getOrCreateTagBuilder(ConventionalItemTags.RAW_FISHES_FOODS)
				.add(Items.COD)
				.add(Items.SALMON)
				.add(Items.TROPICAL_FISH)
				.add(Items.PUFFERFISH);

		getOrCreateTagBuilder(ConventionalItemTags.COOKED_MEATS_FOODS)
				.add(Items.COOKED_BEEF)
				.add(Items.COOKED_PORKCHOP)
				.add(Items.COOKED_CHICKEN)
				.add(Items.COOKED_RABBIT)
				.add(Items.COOKED_MUTTON);

		getOrCreateTagBuilder(ConventionalItemTags.COOKED_FISHES_FOODS)
				.add(Items.COOKED_COD)
				.add(Items.COOKED_SALMON);

		getOrCreateTagBuilder(ConventionalItemTags.SOUPS_FOODS)
				.add(Items.BEETROOT_SOUP)
				.add(Items.MUSHROOM_STEW)
				.add(Items.RABBIT_STEW)
				.add(Items.SUSPICIOUS_STEW);

		getOrCreateTagBuilder(ConventionalItemTags.CANDIES_FOODS);
	}

	private void generateBucketTags() {
		getOrCreateTagBuilder(ConventionalItemTags.EMPTY_BUCKETS)
				.add(Items.BUCKET);
		getOrCreateTagBuilder(ConventionalItemTags.LAVA_BUCKETS)
				.add(Items.LAVA_BUCKET);
		getOrCreateTagBuilder(ConventionalItemTags.ENTITY_WATER_BUCKETS)
				.add(Items.AXOLOTL_BUCKET)
				.add(Items.COD_BUCKET)
				.add(Items.PUFFERFISH_BUCKET)
				.add(Items.TADPOLE_BUCKET)
				.add(Items.TROPICAL_FISH_BUCKET)
				.add(Items.SALMON_BUCKET);
		getOrCreateTagBuilder(ConventionalItemTags.WATER_BUCKETS)
				.add(Items.WATER_BUCKET);
		getOrCreateTagBuilder(ConventionalItemTags.MILK_BUCKETS)
				.add(Items.MILK_BUCKET);
		getOrCreateTagBuilder(ConventionalItemTags.POWDER_SNOW_BUCKETS)
				.add(Items.POWDER_SNOW_BUCKET);
		getOrCreateTagBuilder(ConventionalItemTags.BUCKETS)
				.addOptionalTag(ConventionalItemTags.EMPTY_BUCKETS)
				.addOptionalTag(ConventionalItemTags.WATER_BUCKETS)
				.addOptionalTag(ConventionalItemTags.LAVA_BUCKETS)
				.addOptionalTag(ConventionalItemTags.MILK_BUCKETS)
				.addOptionalTag(ConventionalItemTags.POWDER_SNOW_BUCKETS)
				.addOptionalTag(ConventionalItemTags.ENTITY_WATER_BUCKETS);
	}

	private void generateOreAndRelatedTags() {
		// Categories
		getOrCreateTagBuilder(ConventionalItemTags.BRICKS)
				.addOptionalTag(ConventionalItemTags.NORMAL_BRICKS)
				.addOptionalTag(ConventionalItemTags.NETHER_BRICKS);
		getOrCreateTagBuilder(ConventionalItemTags.DUSTS)
				.addOptionalTag(ConventionalItemTags.GLOWSTONE_DUSTS)
				.addOptionalTag(ConventionalItemTags.REDSTONE_DUSTS);
		getOrCreateTagBuilder(ConventionalItemTags.GEMS)
				.addOptionalTag(ConventionalItemTags.AMETHYST_GEMS)
				.addOptionalTag(ConventionalItemTags.DIAMOND_GEMS)
				.addOptionalTag(ConventionalItemTags.EMERALD_GEMS)
				.addOptionalTag(ConventionalItemTags.LAPIS_GEMS)
				.addOptionalTag(ConventionalItemTags.PRISMARINE_GEMS)
				.addOptionalTag(ConventionalItemTags.QUARTZ_GEMS);
		getOrCreateTagBuilder(ConventionalItemTags.INGOTS)
				.addOptionalTag(ConventionalItemTags.COPPER_INGOTS)
				.addOptionalTag(ConventionalItemTags.IRON_INGOTS)
				.addOptionalTag(ConventionalItemTags.GOLD_INGOTS)
				.addOptionalTag(ConventionalItemTags.NETHERITE_INGOTS);
		getOrCreateTagBuilder(ConventionalItemTags.NUGGETS)
				.addOptionalTag(ConventionalItemTags.IRON_NUGGETS)
				.addOptionalTag(ConventionalItemTags.GOLD_NUGGETS);
		copy(ConventionalBlockTags.ORES, ConventionalItemTags.ORES);
		getOrCreateTagBuilder(ConventionalItemTags.ORES)
				.addOptionalTag(ConventionalItemTags.NETHERITE_SCRAP_ORES)
				.addOptionalTag(ConventionalItemTags.QUARTZ_ORES);
		getOrCreateTagBuilder(ConventionalItemTags.RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.COPPER_RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.GOLD_RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.IRON_RAW_MATERIALS);
		getOrCreateTagBuilder(ConventionalItemTags.RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.COPPER_RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.IRON_RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.GOLD_RAW_MATERIALS);

		getOrCreateTagBuilder(ConventionalItemTags.RAW_BLOCKS)
				.addOptionalTag(ConventionalItemTags.COPPER_RAW_BLOCKS)
				.addOptionalTag(ConventionalItemTags.GOLD_RAW_BLOCKS)
				.addOptionalTag(ConventionalItemTags.IRON_RAW_BLOCKS);

		// Vanilla instances
		getOrCreateTagBuilder(ConventionalItemTags.NORMAL_BRICKS)
				.add(Items.BRICK);
		getOrCreateTagBuilder(ConventionalItemTags.NETHER_BRICKS)
				.add(Items.NETHER_BRICK);

		getOrCreateTagBuilder(ConventionalItemTags.IRON_INGOTS)
				.add(Items.IRON_INGOT);
		getOrCreateTagBuilder(ConventionalItemTags.COPPER_INGOTS)
				.add(Items.COPPER_INGOT);
		getOrCreateTagBuilder(ConventionalItemTags.GOLD_INGOTS)
				.add(Items.GOLD_INGOT);
		getOrCreateTagBuilder(ConventionalItemTags.NETHERITE_INGOTS)
				.add(Items.NETHERITE_INGOT);

		getOrCreateTagBuilder(ConventionalItemTags.IRON_RAW_BLOCKS)
				.add(Items.RAW_IRON_BLOCK);
		getOrCreateTagBuilder(ConventionalItemTags.COPPER_RAW_BLOCKS)
				.add(Items.RAW_COPPER_BLOCK);
		getOrCreateTagBuilder(ConventionalItemTags.GOLD_RAW_BLOCKS)
				.add(Items.RAW_GOLD_BLOCK);

		getOrCreateTagBuilder(ConventionalItemTags.IRON_RAW_MATERIALS)
				.add(Items.RAW_IRON);
		getOrCreateTagBuilder(ConventionalItemTags.COPPER_RAW_MATERIALS)
				.add(Items.RAW_COPPER);
		getOrCreateTagBuilder(ConventionalItemTags.GOLD_RAW_MATERIALS)
				.add(Items.RAW_GOLD);

		getOrCreateTagBuilder(ConventionalItemTags.REDSTONE_DUSTS)
				.add(Items.REDSTONE);
		getOrCreateTagBuilder(ConventionalItemTags.GLOWSTONE_DUSTS)
				.add(Items.GLOWSTONE_DUST);
		getOrCreateTagBuilder(ConventionalItemTags.COAL)
				.addOptionalTag(ItemTags.COALS);

		getOrCreateTagBuilder(ConventionalItemTags.QUARTZ_ORES)
				.add(Items.NETHER_QUARTZ_ORE);
		getOrCreateTagBuilder(ConventionalItemTags.NETHERITE_SCRAP_ORES)
				.add(Items.ANCIENT_DEBRIS);

		getOrCreateTagBuilder(ConventionalItemTags.QUARTZ_GEMS)
				.add(Items.QUARTZ);
		getOrCreateTagBuilder(ConventionalItemTags.EMERALD_GEMS)
				.add(Items.EMERALD);
		getOrCreateTagBuilder(ConventionalItemTags.LAPIS_GEMS)
				.add(Items.LAPIS_LAZULI);
		getOrCreateTagBuilder(ConventionalItemTags.DIAMOND_GEMS)
				.add(Items.DIAMOND);
		getOrCreateTagBuilder(ConventionalItemTags.AMETHYST_GEMS)
				.add(Items.AMETHYST_SHARD);
		getOrCreateTagBuilder(ConventionalItemTags.PRISMARINE_GEMS)
				.add(Items.PRISMARINE_CRYSTALS);

		getOrCreateTagBuilder(ConventionalItemTags.IRON_NUGGETS)
				.add(Items.IRON_NUGGET);
		getOrCreateTagBuilder(ConventionalItemTags.GOLD_NUGGETS)
				.add(Items.GOLD_NUGGET);
	}

	private void generateToolTags() {
		getOrCreateTagBuilder(ConventionalItemTags.TOOLS)
				.addOptionalTag(ItemTags.AXES)
				.addOptionalTag(ItemTags.HOES)
				.addOptionalTag(ItemTags.PICKAXES)
				.addOptionalTag(ItemTags.SHOVELS)
				.addOptionalTag(ItemTags.SWORDS)
				.addOptionalTag(ConventionalItemTags.BOW_TOOLS)
				.addOptionalTag(ConventionalItemTags.BRUSH_TOOLS)
				.addOptionalTag(ConventionalItemTags.CROSSBOW_TOOLS)
				.addOptionalTag(ConventionalItemTags.FISHING_ROD_TOOLS)
				.addOptionalTag(ConventionalItemTags.IGNITER_TOOLS)
				.addOptionalTag(ConventionalItemTags.SHEAR_TOOLS)
				.addOptionalTag(ConventionalItemTags.SHIELD_TOOLS)
				.addOptionalTag(ConventionalItemTags.SPEAR_TOOLS)
				.addOptionalTag(ConventionalItemTags.MINING_TOOL_TOOLS)
				.addOptionalTag(ConventionalItemTags.MELEE_WEAPON_TOOLS)
				.addOptionalTag(ConventionalItemTags.RANGED_WEAPON_TOOLS);

		getOrCreateTagBuilder(ConventionalItemTags.BOW_TOOLS)
				.add(Items.BOW)
				.addOptionalTag(ConventionalItemTags.BOWS_TOOLS);
		getOrCreateTagBuilder(ConventionalItemTags.CROSSBOW_TOOLS)
				.add(Items.CROSSBOW)
				.addOptionalTag(ConventionalItemTags.CROSSBOWS_TOOLS);
		getOrCreateTagBuilder(ConventionalItemTags.SHEAR_TOOLS)
				.add(Items.SHEARS)
				.addOptionalTag(ConventionalItemTags.SHEARS_TOOLS);
		getOrCreateTagBuilder(ConventionalItemTags.SHIELD_TOOLS)
				.add(Items.SHIELD)
				.addOptionalTag(ConventionalItemTags.SHIELDS_TOOLS);
		getOrCreateTagBuilder(ConventionalItemTags.SPEAR_TOOLS)
				.add(Items.TRIDENT)
				.addOptionalTag(ConventionalItemTags.SPEARS_TOOLS);
		getOrCreateTagBuilder(ConventionalItemTags.FISHING_ROD_TOOLS)
				.add(Items.FISHING_ROD)
				.addOptionalTag(ConventionalItemTags.FISHING_RODS_TOOLS);
		getOrCreateTagBuilder(ConventionalItemTags.BRUSH_TOOLS)
				.add(Items.BRUSH)
				.addOptionalTag(ConventionalItemTags.BRUSHES_TOOLS);
		getOrCreateTagBuilder(ConventionalItemTags.IGNITER_TOOLS)
				.add(Items.FLINT_AND_STEEL);
		getOrCreateTagBuilder(ConventionalItemTags.MACE_TOOLS)
				.add(Items.MACE);

		getOrCreateTagBuilder(ConventionalItemTags.MINING_TOOL_TOOLS)
				.add(Items.WOODEN_PICKAXE)
				.add(Items.STONE_PICKAXE)
				.add(Items.GOLDEN_PICKAXE)
				.add(Items.IRON_PICKAXE)
				.add(Items.DIAMOND_PICKAXE)
				.add(Items.NETHERITE_PICKAXE)
				.addOptionalTag(ConventionalItemTags.MINING_TOOLS);

		getOrCreateTagBuilder(ConventionalItemTags.MELEE_WEAPON_TOOLS)
				.add(Items.MACE)
				.add(Items.TRIDENT)
				.add(Items.WOODEN_SWORD)
				.add(Items.STONE_SWORD)
				.add(Items.GOLDEN_SWORD)
				.add(Items.IRON_SWORD)
				.add(Items.DIAMOND_SWORD)
				.add(Items.NETHERITE_SWORD)
				.add(Items.WOODEN_AXE)
				.add(Items.STONE_AXE)
				.add(Items.GOLDEN_AXE)
				.add(Items.IRON_AXE)
				.add(Items.DIAMOND_AXE)
				.add(Items.NETHERITE_AXE)
				.addOptionalTag(ConventionalItemTags.MELEE_WEAPONS_TOOLS);

		getOrCreateTagBuilder(ConventionalItemTags.RANGED_WEAPON_TOOLS)
				.add(Items.BOW)
				.add(Items.CROSSBOW)
				.add(Items.TRIDENT)
				.addOptionalTag(ConventionalItemTags.RANGED_WEAPONS_TOOLS);

		getOrCreateTagBuilder(ConventionalItemTags.ARMORS)
				.addOptionalTag(ItemTags.HEAD_ARMOR)
				.addOptionalTag(ItemTags.CHEST_ARMOR)
				.addOptionalTag(ItemTags.LEG_ARMOR)
				.addOptionalTag(ItemTags.FOOT_ARMOR);

		getOrCreateTagBuilder(ConventionalItemTags.ENCHANTABLES)
				.addOptionalTag(ItemTags.ARMOR_ENCHANTABLE)
				.addOptionalTag(ItemTags.EQUIPPABLE_ENCHANTABLE)
				.addOptionalTag(ItemTags.WEAPON_ENCHANTABLE)
				.addOptionalTag(ItemTags.SWORD_ENCHANTABLE)
				.addOptionalTag(ItemTags.MINING_ENCHANTABLE)
				.addOptionalTag(ItemTags.MINING_LOOT_ENCHANTABLE)
				.addOptionalTag(ItemTags.FISHING_ENCHANTABLE)
				.addOptionalTag(ItemTags.TRIDENT_ENCHANTABLE)
				.addOptionalTag(ItemTags.BOW_ENCHANTABLE)
				.addOptionalTag(ItemTags.CROSSBOW_ENCHANTABLE)
				.addOptionalTag(ItemTags.MACE_ENCHANTABLE)
				.addOptionalTag(ItemTags.FIRE_ASPECT_ENCHANTABLE)
				.addOptionalTag(ItemTags.DURABILITY_ENCHANTABLE)
				.addOptionalTag(ItemTags.VANISHING_ENCHANTABLE);

		// Deprecated tags below

		getOrCreateTagBuilder(ConventionalItemTags.BOWS_TOOLS)
				.add(Items.BOW);
		getOrCreateTagBuilder(ConventionalItemTags.CROSSBOWS_TOOLS)
				.add(Items.CROSSBOW);
		getOrCreateTagBuilder(ConventionalItemTags.SHEARS_TOOLS)
				.add(Items.SHEARS);
		getOrCreateTagBuilder(ConventionalItemTags.SHIELDS_TOOLS)
				.add(Items.SHIELD);
		getOrCreateTagBuilder(ConventionalItemTags.SPEARS_TOOLS)
				.add(Items.TRIDENT);
		getOrCreateTagBuilder(ConventionalItemTags.FISHING_RODS_TOOLS)
				.add(Items.FISHING_ROD);
		getOrCreateTagBuilder(ConventionalItemTags.BRUSHES_TOOLS)
				.add(Items.BRUSH);

		getOrCreateTagBuilder(ConventionalItemTags.MINING_TOOLS)
				.add(Items.WOODEN_PICKAXE)
				.add(Items.STONE_PICKAXE)
				.add(Items.GOLDEN_PICKAXE)
				.add(Items.IRON_PICKAXE)
				.add(Items.DIAMOND_PICKAXE)
				.add(Items.NETHERITE_PICKAXE);

		getOrCreateTagBuilder(ConventionalItemTags.MELEE_WEAPONS_TOOLS)
				.add(Items.WOODEN_SWORD)
				.add(Items.STONE_SWORD)
				.add(Items.GOLDEN_SWORD)
				.add(Items.IRON_SWORD)
				.add(Items.DIAMOND_SWORD)
				.add(Items.NETHERITE_SWORD)
				.add(Items.WOODEN_AXE)
				.add(Items.STONE_AXE)
				.add(Items.GOLDEN_AXE)
				.add(Items.IRON_AXE)
				.add(Items.DIAMOND_AXE)
				.add(Items.NETHERITE_AXE);

		getOrCreateTagBuilder(ConventionalItemTags.RANGED_WEAPONS_TOOLS)
				.add(Items.BOW)
				.add(Items.CROSSBOW)
				.add(Items.TRIDENT);
	}

	private void generateVillagerJobSites() {
		BlockTagGenerator.VILLAGER_JOB_SITE_BLOCKS.stream()
				.map(ItemConvertible::asItem)
				.distinct() // cauldron blocks have the same item
				.forEach(getOrCreateTagBuilder(ConventionalItemTags.VILLAGER_JOB_SITES)::add);
	}

	private void generateCropTags() {
		getOrCreateTagBuilder(ConventionalItemTags.CROPS)
				.addOptionalTag(ConventionalItemTags.BEETROOT_CROPS)
				.addOptionalTag(ConventionalItemTags.CACTUS_CROPS)
				.addOptionalTag(ConventionalItemTags.CARROT_CROPS)
				.addOptionalTag(ConventionalItemTags.COCOA_BEAN_CROPS)
				.addOptionalTag(ConventionalItemTags.MELON_CROPS)
				.addOptionalTag(ConventionalItemTags.NETHER_WART_CROPS)
				.addOptionalTag(ConventionalItemTags.POTATO_CROPS)
				.addOptionalTag(ConventionalItemTags.PUMPKIN_CROPS)
				.addOptionalTag(ConventionalItemTags.SUGAR_CANE_CROPS)
				.addOptionalTag(ConventionalItemTags.WHEAT_CROPS);

		getOrCreateTagBuilder(ConventionalItemTags.BEETROOT_CROPS)
				.add(Items.BEETROOT);
		getOrCreateTagBuilder(ConventionalItemTags.CACTUS_CROPS)
				.add(Items.CACTUS);
		getOrCreateTagBuilder(ConventionalItemTags.CARROT_CROPS)
				.add(Items.CARROT);
		getOrCreateTagBuilder(ConventionalItemTags.COCOA_BEAN_CROPS)
				.add(Items.COCOA_BEANS);
		getOrCreateTagBuilder(ConventionalItemTags.MELON_CROPS)
				.add(Items.MELON);
		getOrCreateTagBuilder(ConventionalItemTags.NETHER_WART_CROPS)
				.add(Items.NETHER_WART);
		getOrCreateTagBuilder(ConventionalItemTags.POTATO_CROPS)
				.add(Items.POTATO);
		getOrCreateTagBuilder(ConventionalItemTags.PUMPKIN_CROPS)
				.add(Items.PUMPKIN);
		getOrCreateTagBuilder(ConventionalItemTags.SUGAR_CANE_CROPS)
				.add(Items.SUGAR_CANE);
		getOrCreateTagBuilder(ConventionalItemTags.WHEAT_CROPS)
				.add(Items.WHEAT);
	}

	private void generateOtherTags() {
		getOrCreateTagBuilder(ConventionalItemTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
				.add(Items.CRAFTING_TABLE);

		getOrCreateTagBuilder(ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES)
				.add(Items.FURNACE);

		getOrCreateTagBuilder(ConventionalItemTags.STRINGS)
				.add(Items.STRING);

		getOrCreateTagBuilder(ConventionalItemTags.LEATHERS)
				.add(Items.LEATHER);

		getOrCreateTagBuilder(ConventionalItemTags.MUSIC_DISCS)
				.add(Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR,
						Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD,
						Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_OTHERSIDE, Items.MUSIC_DISC_5, Items.MUSIC_DISC_PIGSTEP,
						Items.MUSIC_DISC_RELIC, Items.MUSIC_DISC_CREATOR, Items.MUSIC_DISC_CREATOR_MUSIC_BOX, Items.MUSIC_DISC_PRECIPICE);

		getOrCreateTagBuilder(ConventionalItemTags.WOODEN_RODS)
				.add(Items.STICK);

		getOrCreateTagBuilder(ConventionalItemTags.BLAZE_RODS)
				.add(Items.BLAZE_ROD);

		getOrCreateTagBuilder(ConventionalItemTags.BREEZE_RODS)
				.add(Items.BREEZE_ROD);

		getOrCreateTagBuilder(ConventionalItemTags.RODS)
				.addOptionalTag(ConventionalItemTags.WOODEN_RODS)
				.addOptionalTag(ConventionalItemTags.BLAZE_RODS)
				.addOptionalTag(ConventionalItemTags.BREEZE_RODS);

		getOrCreateTagBuilder(ConventionalItemTags.ROPES); // Generate tag so others can see it exists through JSON.

		getOrCreateTagBuilder(ConventionalItemTags.CHAINS)
				.add(Items.CHAIN);

		getOrCreateTagBuilder(ConventionalItemTags.ENDER_PEARLS)
				.add(Items.ENDER_PEARL);

		getOrCreateTagBuilder(ConventionalItemTags.SLIME_BALLS)
				.add(Items.SLIME_BALL);

		getOrCreateTagBuilder(ConventionalItemTags.FERTILIZERS)
				.add(Items.BONE_MEAL);

		getOrCreateTagBuilder(ConventionalItemTags.HIDDEN_FROM_RECIPE_VIEWERS); // Generate tag so others can see it exists through JSON.
	}

	private void generateDyedTags() {
		// Cannot pull entries from block tag because Wall Banners do not have an item form
		getOrCreateTagBuilder(ConventionalItemTags.BLACK_DYED)
				.add(Items.BLACK_BANNER).add(Items.BLACK_BED).add(Items.BLACK_CANDLE).add(Items.BLACK_CARPET)
				.add(Items.BLACK_CONCRETE).add(Items.BLACK_CONCRETE_POWDER).add(Items.BLACK_GLAZED_TERRACOTTA)
				.add(Items.BLACK_SHULKER_BOX).add(Items.BLACK_STAINED_GLASS).add(Items.BLACK_STAINED_GLASS_PANE)
				.add(Items.BLACK_TERRACOTTA).add(Items.BLACK_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.BLUE_DYED)
				.add(Items.BLUE_BANNER).add(Items.BLUE_BED).add(Items.BLUE_CANDLE).add(Items.BLUE_CARPET)
				.add(Items.BLUE_CONCRETE).add(Items.BLUE_CONCRETE_POWDER).add(Items.BLUE_GLAZED_TERRACOTTA)
				.add(Items.BLUE_SHULKER_BOX).add(Items.BLUE_STAINED_GLASS).add(Items.BLUE_STAINED_GLASS_PANE)
				.add(Items.BLUE_TERRACOTTA).add(Items.BLUE_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.BROWN_DYED)
				.add(Items.BROWN_BANNER).add(Items.BROWN_BED).add(Items.BROWN_CANDLE).add(Items.BROWN_CARPET)
				.add(Items.BROWN_CONCRETE).add(Items.BROWN_CONCRETE_POWDER).add(Items.BROWN_GLAZED_TERRACOTTA)
				.add(Items.BROWN_SHULKER_BOX).add(Items.BROWN_STAINED_GLASS).add(Items.BROWN_STAINED_GLASS_PANE)
				.add(Items.BROWN_TERRACOTTA).add(Items.BROWN_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.CYAN_DYED)
				.add(Items.CYAN_BANNER).add(Items.CYAN_BED).add(Items.CYAN_CANDLE).add(Items.CYAN_CARPET)
				.add(Items.CYAN_CONCRETE).add(Items.CYAN_CONCRETE_POWDER).add(Items.CYAN_GLAZED_TERRACOTTA)
				.add(Items.CYAN_SHULKER_BOX).add(Items.CYAN_STAINED_GLASS).add(Items.CYAN_STAINED_GLASS_PANE)
				.add(Items.CYAN_TERRACOTTA).add(Items.CYAN_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.GRAY_DYED)
				.add(Items.GRAY_BANNER).add(Items.GRAY_BED).add(Items.GRAY_CANDLE).add(Items.GRAY_CARPET)
				.add(Items.GRAY_CONCRETE).add(Items.GRAY_CONCRETE_POWDER).add(Items.GRAY_GLAZED_TERRACOTTA)
				.add(Items.GRAY_SHULKER_BOX).add(Items.GRAY_STAINED_GLASS).add(Items.GRAY_STAINED_GLASS_PANE)
				.add(Items.GRAY_TERRACOTTA).add(Items.GRAY_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.GREEN_DYED)
				.add(Items.GREEN_BANNER).add(Items.GREEN_BED).add(Items.GREEN_CANDLE).add(Items.GREEN_CARPET)
				.add(Items.GREEN_CONCRETE).add(Items.GREEN_CONCRETE_POWDER).add(Items.GREEN_GLAZED_TERRACOTTA)
				.add(Items.GREEN_SHULKER_BOX).add(Items.GREEN_STAINED_GLASS).add(Items.GREEN_STAINED_GLASS_PANE)
				.add(Items.GREEN_TERRACOTTA).add(Items.GREEN_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.LIGHT_BLUE_DYED)
				.add(Items.LIGHT_BLUE_BANNER).add(Items.LIGHT_BLUE_BED).add(Items.LIGHT_BLUE_CANDLE).add(Items.LIGHT_BLUE_CARPET)
				.add(Items.LIGHT_BLUE_CONCRETE).add(Items.LIGHT_BLUE_CONCRETE_POWDER).add(Items.LIGHT_BLUE_GLAZED_TERRACOTTA)
				.add(Items.LIGHT_BLUE_SHULKER_BOX).add(Items.LIGHT_BLUE_STAINED_GLASS).add(Items.LIGHT_BLUE_STAINED_GLASS_PANE)
				.add(Items.LIGHT_BLUE_TERRACOTTA).add(Items.LIGHT_BLUE_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.LIGHT_GRAY_DYED)
				.add(Items.LIGHT_GRAY_BANNER).add(Items.LIGHT_GRAY_BED).add(Items.LIGHT_GRAY_CANDLE).add(Items.LIGHT_GRAY_CARPET)
				.add(Items.LIGHT_GRAY_CONCRETE).add(Items.LIGHT_GRAY_CONCRETE_POWDER).add(Items.LIGHT_GRAY_GLAZED_TERRACOTTA)
				.add(Items.LIGHT_GRAY_SHULKER_BOX).add(Items.LIGHT_GRAY_STAINED_GLASS).add(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
				.add(Items.LIGHT_GRAY_TERRACOTTA).add(Items.LIGHT_GRAY_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.LIME_DYED)
				.add(Items.LIME_BANNER).add(Items.LIME_BED).add(Items.LIME_CANDLE).add(Items.LIME_CARPET)
				.add(Items.LIME_CONCRETE).add(Items.LIME_CONCRETE_POWDER).add(Items.LIME_GLAZED_TERRACOTTA)
				.add(Items.LIME_SHULKER_BOX).add(Items.LIME_STAINED_GLASS).add(Items.LIME_STAINED_GLASS_PANE)
				.add(Items.LIME_TERRACOTTA).add(Items.LIME_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.MAGENTA_DYED)
				.add(Items.MAGENTA_BANNER).add(Items.MAGENTA_BED).add(Items.MAGENTA_CANDLE).add(Items.MAGENTA_CARPET)
				.add(Items.MAGENTA_CONCRETE).add(Items.MAGENTA_CONCRETE_POWDER).add(Items.MAGENTA_GLAZED_TERRACOTTA)
				.add(Items.MAGENTA_SHULKER_BOX).add(Items.MAGENTA_STAINED_GLASS).add(Items.MAGENTA_STAINED_GLASS_PANE)
				.add(Items.MAGENTA_TERRACOTTA).add(Items.MAGENTA_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.ORANGE_DYED)
				.add(Items.ORANGE_BANNER).add(Items.ORANGE_BED).add(Items.ORANGE_CANDLE).add(Items.ORANGE_CARPET)
				.add(Items.ORANGE_CONCRETE).add(Items.ORANGE_CONCRETE_POWDER).add(Items.ORANGE_GLAZED_TERRACOTTA)
				.add(Items.ORANGE_SHULKER_BOX).add(Items.ORANGE_STAINED_GLASS).add(Items.ORANGE_STAINED_GLASS_PANE)
				.add(Items.ORANGE_TERRACOTTA).add(Items.ORANGE_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.PINK_DYED)
				.add(Items.PINK_BANNER).add(Items.PINK_BED).add(Items.PINK_CANDLE).add(Items.PINK_CARPET)
				.add(Items.PINK_CONCRETE).add(Items.PINK_CONCRETE_POWDER).add(Items.PINK_GLAZED_TERRACOTTA)
				.add(Items.PINK_SHULKER_BOX).add(Items.PINK_STAINED_GLASS).add(Items.PINK_STAINED_GLASS_PANE)
				.add(Items.PINK_TERRACOTTA).add(Items.PINK_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.PURPLE_DYED)
				.add(Items.PURPLE_BANNER).add(Items.PURPLE_BED).add(Items.PURPLE_CANDLE).add(Items.PURPLE_CARPET)
				.add(Items.PURPLE_CONCRETE).add(Items.PURPLE_CONCRETE_POWDER).add(Items.PURPLE_GLAZED_TERRACOTTA)
				.add(Items.PURPLE_SHULKER_BOX).add(Items.PURPLE_STAINED_GLASS).add(Items.PURPLE_STAINED_GLASS_PANE)
				.add(Items.PURPLE_TERRACOTTA).add(Items.PURPLE_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.RED_DYED)
				.add(Items.RED_BANNER).add(Items.RED_BED).add(Items.RED_CANDLE).add(Items.RED_CARPET)
				.add(Items.RED_CONCRETE).add(Items.RED_CONCRETE_POWDER).add(Items.RED_GLAZED_TERRACOTTA)
				.add(Items.RED_SHULKER_BOX).add(Items.RED_STAINED_GLASS).add(Items.RED_STAINED_GLASS_PANE)
				.add(Items.RED_TERRACOTTA).add(Items.RED_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.WHITE_DYED)
				.add(Items.WHITE_BANNER).add(Items.WHITE_BED).add(Items.WHITE_CANDLE).add(Items.WHITE_CARPET)
				.add(Items.WHITE_CONCRETE).add(Items.WHITE_CONCRETE_POWDER).add(Items.WHITE_GLAZED_TERRACOTTA)
				.add(Items.WHITE_SHULKER_BOX).add(Items.WHITE_STAINED_GLASS).add(Items.WHITE_STAINED_GLASS_PANE)
				.add(Items.WHITE_TERRACOTTA).add(Items.WHITE_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.YELLOW_DYED)
				.add(Items.YELLOW_BANNER).add(Items.YELLOW_BED).add(Items.YELLOW_CANDLE).add(Items.YELLOW_CARPET)
				.add(Items.YELLOW_CONCRETE).add(Items.YELLOW_CONCRETE_POWDER).add(Items.YELLOW_GLAZED_TERRACOTTA)
				.add(Items.YELLOW_SHULKER_BOX).add(Items.YELLOW_STAINED_GLASS).add(Items.YELLOW_STAINED_GLASS_PANE)
				.add(Items.YELLOW_TERRACOTTA).add(Items.YELLOW_WOOL);

		getOrCreateTagBuilder(ConventionalItemTags.DYED)
				.addTag(ConventionalItemTags.WHITE_DYED)
				.addTag(ConventionalItemTags.ORANGE_DYED)
				.addTag(ConventionalItemTags.MAGENTA_DYED)
				.addTag(ConventionalItemTags.LIGHT_BLUE_DYED)
				.addTag(ConventionalItemTags.YELLOW_DYED)
				.addTag(ConventionalItemTags.LIME_DYED)
				.addTag(ConventionalItemTags.PINK_DYED)
				.addTag(ConventionalItemTags.GRAY_DYED)
				.addTag(ConventionalItemTags.LIGHT_GRAY_DYED)
				.addTag(ConventionalItemTags.CYAN_DYED)
				.addTag(ConventionalItemTags.PURPLE_DYED)
				.addTag(ConventionalItemTags.BLUE_DYED)
				.addTag(ConventionalItemTags.BROWN_DYED)
				.addTag(ConventionalItemTags.GREEN_DYED)
				.addTag(ConventionalItemTags.RED_DYED)
				.addTag(ConventionalItemTags.BLACK_DYED);
	}

	private void generateBackwardsCompatTags() {
		// Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
		// TODO: Remove backwards compat tag entries in 1.22

		getOrCreateTagBuilder(ConventionalItemTags.WOODEN_BARRELS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "wooden_barrels"));
		getOrCreateTagBuilder(ConventionalItemTags.WOODEN_CHESTS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "wooden_chests"));
		getOrCreateTagBuilder(ConventionalItemTags.BLACK_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "black_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.BLUE_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "blue_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.BROWN_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "brown_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.GREEN_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "green_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.RED_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "red_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.WHITE_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "white_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.YELLOW_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "yellow_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.LIGHT_BLUE_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "light_blue_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.LIGHT_GRAY_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "light_gray_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.LIME_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "lime_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.MAGENTA_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "magenta_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.ORANGE_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "orange_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.PINK_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "pink_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.CYAN_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "cyan_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.GRAY_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "gray_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.PURPLE_DYES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "purple_dyes"));
		getOrCreateTagBuilder(ConventionalItemTags.IRON_RAW_MATERIALS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "raw_iron_ores"));
		getOrCreateTagBuilder(ConventionalItemTags.COPPER_RAW_MATERIALS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "raw_copper_ores"));
		getOrCreateTagBuilder(ConventionalItemTags.GOLD_RAW_MATERIALS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "raw_gold_ores"));
		getOrCreateTagBuilder(ConventionalItemTags.GLOWSTONE_DUSTS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "glowstone_dusts"));
		getOrCreateTagBuilder(ConventionalItemTags.REDSTONE_DUSTS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "redstone_dusts"));
		getOrCreateTagBuilder(ConventionalItemTags.DIAMOND_GEMS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "diamonds"));
		getOrCreateTagBuilder(ConventionalItemTags.LAPIS_GEMS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "lapis"));
		getOrCreateTagBuilder(ConventionalItemTags.EMERALD_GEMS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "emeralds"));
		getOrCreateTagBuilder(ConventionalItemTags.QUARTZ_GEMS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "quartz"));
		getOrCreateTagBuilder(ConventionalItemTags.SHEAR_TOOLS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "shears"));
		getOrCreateTagBuilder(ConventionalItemTags.SPEAR_TOOLS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "spears"));
		getOrCreateTagBuilder(ConventionalItemTags.BOW_TOOLS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "bows"));
		getOrCreateTagBuilder(ConventionalItemTags.SHIELD_TOOLS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "shields"));
		getOrCreateTagBuilder(ConventionalItemTags.STRINGS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "string"));
		getOrCreateTagBuilder(ConventionalItemTags.CONCRETE_POWDERS).addOptionalTag(ConventionalItemTags.CONCRETE_POWDER);
	}
}
