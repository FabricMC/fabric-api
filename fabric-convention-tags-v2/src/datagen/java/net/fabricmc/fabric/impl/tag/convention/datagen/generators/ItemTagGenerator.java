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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
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
		generateCropAndSeedsTags();
		generateVillagerJobSites();
		generateFlowerTags();
		generateOtherTags();
		copyItemTags();
		generateTagAlias();
		generateBackwardsCompatTags();
	}

	private void copyItemTags() {
		copy(ConventionalBlockTags.STONES, ConventionalItemTags.STONES);
		copy(ConventionalBlockTags.COBBLESTONES, ConventionalItemTags.COBBLESTONES);
		copy(ConventionalBlockTags.NORMAL_COBBLESTONES, ConventionalItemTags.NORMAL_COBBLESTONES);
		copy(ConventionalBlockTags.MOSSY_COBBLESTONES, ConventionalItemTags.MOSSY_COBBLESTONES);
		copy(ConventionalBlockTags.INFESTED_COBBLESTONES, ConventionalItemTags.INFESTED_COBBLESTONES);
		copy(ConventionalBlockTags.DEEPSLATE_COBBLESTONES, ConventionalItemTags.DEEPSLATE_COBBLESTONES);
		copy(ConventionalBlockTags.NETHERRACKS, ConventionalItemTags.NETHERRACKS);
		copy(ConventionalBlockTags.END_STONES, ConventionalItemTags.END_STONES);
		copy(ConventionalBlockTags.GRAVELS, ConventionalItemTags.GRAVELS);
		copy(ConventionalBlockTags.OBSIDIANS, ConventionalItemTags.OBSIDIANS);
		copy(ConventionalBlockTags.NORMAL_OBSIDIANS, ConventionalItemTags.NORMAL_OBSIDIANS);
		copy(ConventionalBlockTags.CRYING_OBSIDIANS, ConventionalItemTags.CRYING_OBSIDIANS);
		copy(ConventionalBlockTags.BARRELS, ConventionalItemTags.BARRELS);
		copy(ConventionalBlockTags.WOODEN_BARRELS, ConventionalItemTags.WOODEN_BARRELS);
		copy(ConventionalBlockTags.BOOKSHELVES, ConventionalItemTags.BOOKSHELVES);
		copy(ConventionalBlockTags.CHESTS, ConventionalItemTags.CHESTS);
		copy(ConventionalBlockTags.WOODEN_CHESTS, ConventionalItemTags.WOODEN_CHESTS);
		copy(ConventionalBlockTags.TRAPPED_CHESTS, ConventionalItemTags.TRAPPED_CHESTS);
		copy(ConventionalBlockTags.ENDER_CHESTS, ConventionalItemTags.ENDER_CHESTS);
		copy(ConventionalBlockTags.GLASS_BLOCKS, ConventionalItemTags.GLASS_BLOCKS);
		copy(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS, ConventionalItemTags.GLASS_BLOCKS_COLORLESS);
		copy(ConventionalBlockTags.GLASS_BLOCKS_TINTED, ConventionalItemTags.GLASS_BLOCKS_TINTED);
		copy(ConventionalBlockTags.GLASS_BLOCKS_CHEAP, ConventionalItemTags.GLASS_BLOCKS_CHEAP);
		copy(ConventionalBlockTags.GLASS_PANES, ConventionalItemTags.GLASS_PANES);
		copy(ConventionalBlockTags.GLASS_PANES_COLORLESS, ConventionalItemTags.GLASS_PANES_COLORLESS);
		tag(ConventionalItemTags.SHULKER_BOXES)
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
		tag(ConventionalItemTags.CONCRETE_POWDERS)
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
		tag(ConventionalItemTags.CONCRETE_POWDER)
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

		copy(ConventionalBlockTags.COLORLESS_SANDS, ConventionalItemTags.COLORLESS_SANDS);
		copy(ConventionalBlockTags.RED_SANDS, ConventionalItemTags.RED_SANDS);
		copy(ConventionalBlockTags.SANDS, ConventionalItemTags.SANDS);

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
		copy(ConventionalBlockTags.STORAGE_BLOCKS_RESIN, ConventionalItemTags.STORAGE_BLOCKS_RESIN);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_SLIME, ConventionalItemTags.STORAGE_BLOCKS_SLIME);
		copy(ConventionalBlockTags.STORAGE_BLOCKS_WHEAT, ConventionalItemTags.STORAGE_BLOCKS_WHEAT);

		copy(ConventionalBlockTags.STRIPPED_LOGS, ConventionalItemTags.STRIPPED_LOGS);
		copy(ConventionalBlockTags.STRIPPED_WOODS, ConventionalItemTags.STRIPPED_WOODS);
		copy(ConventionalBlockTags.FENCES, ConventionalItemTags.FENCES);
		copy(ConventionalBlockTags.WOODEN_FENCES, ConventionalItemTags.WOODEN_FENCES);
		copy(ConventionalBlockTags.NETHER_BRICK_FENCES, ConventionalItemTags.NETHER_BRICK_FENCES);
		copy(ConventionalBlockTags.FENCE_GATES, ConventionalItemTags.FENCE_GATES);
		copy(ConventionalBlockTags.WOODEN_FENCE_GATES, ConventionalItemTags.WOODEN_FENCE_GATES);

		copy(ConventionalBlockTags.PUMPKINS, ConventionalItemTags.PUMPKINS);
		copy(ConventionalBlockTags.NORMAL_PUMPKINS, ConventionalItemTags.NORMAL_PUMPKINS);
		copy(ConventionalBlockTags.CARVED_PUMPKINS, ConventionalItemTags.CARVED_PUMPKINS);
		copy(ConventionalBlockTags.JACK_O_LANTERNS_PUMPKINS, ConventionalItemTags.JACK_O_LANTERNS_PUMPKINS);
	}

	private void generateDyeTags() {
		tag(ConventionalItemTags.DYES)
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
		tag(ConventionalItemTags.BLACK_DYES)
				.add(Items.BLACK_DYE);
		tag(ConventionalItemTags.BLUE_DYES)
				.add(Items.BLUE_DYE);
		tag(ConventionalItemTags.BROWN_DYES)
				.add(Items.BROWN_DYE);
		tag(ConventionalItemTags.GREEN_DYES)
				.add(Items.GREEN_DYE);
		tag(ConventionalItemTags.RED_DYES)
				.add(Items.RED_DYE);
		tag(ConventionalItemTags.WHITE_DYES)
				.add(Items.WHITE_DYE);
		tag(ConventionalItemTags.YELLOW_DYES)
				.add(Items.YELLOW_DYE);
		tag(ConventionalItemTags.LIGHT_BLUE_DYES)
				.add(Items.LIGHT_BLUE_DYE);
		tag(ConventionalItemTags.LIGHT_GRAY_DYES)
				.add(Items.LIGHT_GRAY_DYE);
		tag(ConventionalItemTags.LIME_DYES)
				.add(Items.LIME_DYE);
		tag(ConventionalItemTags.MAGENTA_DYES)
				.add(Items.MAGENTA_DYE);
		tag(ConventionalItemTags.ORANGE_DYES)
				.add(Items.ORANGE_DYE);
		tag(ConventionalItemTags.PINK_DYES)
				.add(Items.PINK_DYE);
		tag(ConventionalItemTags.CYAN_DYES)
				.add(Items.CYAN_DYE);
		tag(ConventionalItemTags.GRAY_DYES)
				.add(Items.GRAY_DYE);
		tag(ConventionalItemTags.PURPLE_DYES)
				.add(Items.PURPLE_DYE);
	}

	private void generateConsumableTags() {
		tag(ConventionalItemTags.BOTTLE_POTIONS)
				.add(Items.POTION)
				.add(Items.SPLASH_POTION)
				.add(Items.LINGERING_POTION);
		tag(ConventionalItemTags.POTIONS)
				.addOptionalTag(ConventionalItemTags.BOTTLE_POTIONS);
	}

	private void generateFoodTags() {
		tag(ConventionalItemTags.FRUIT_FOODS)
				.add(Items.APPLE)
				.add(Items.GOLDEN_APPLE)
				.add(Items.ENCHANTED_GOLDEN_APPLE)
				.add(Items.CHORUS_FRUIT)
				.add(Items.MELON_SLICE)
				.addOptionalTag(ConventionalItemTags.FRUITS_FOODS);

		tag(ConventionalItemTags.VEGETABLE_FOODS)
				.add(Items.CARROT)
				.add(Items.GOLDEN_CARROT)
				.add(Items.POTATO)
				.add(Items.BEETROOT)
				.addOptionalTag(ConventionalItemTags.VEGETABLES_FOODS);

		tag(ConventionalItemTags.BERRY_FOODS)
				.add(Items.SWEET_BERRIES)
				.add(Items.GLOW_BERRIES)
				.addOptionalTag(ConventionalItemTags.BERRIES_FOODS);

		tag(ConventionalItemTags.BREAD_FOODS)
				.add(Items.BREAD)
				.addOptionalTag(ConventionalItemTags.BREADS_FOODS);

		tag(ConventionalItemTags.COOKIE_FOODS)
				.add(Items.COOKIE)
				.addOptionalTag(ConventionalItemTags.COOKIES_FOODS);

		tag(ConventionalItemTags.RAW_MEAT_FOODS)
				.add(Items.BEEF)
				.add(Items.PORKCHOP)
				.add(Items.CHICKEN)
				.add(Items.RABBIT)
				.add(Items.MUTTON)
				.addOptionalTag(ConventionalItemTags.RAW_MEATS_FOODS);

		tag(ConventionalItemTags.RAW_FISH_FOODS)
				.add(Items.COD)
				.add(Items.SALMON)
				.add(Items.TROPICAL_FISH)
				.add(Items.PUFFERFISH)
				.addOptionalTag(ConventionalItemTags.RAW_FISHES_FOODS);

		tag(ConventionalItemTags.COOKED_MEAT_FOODS)
				.add(Items.COOKED_BEEF)
				.add(Items.COOKED_PORKCHOP)
				.add(Items.COOKED_CHICKEN)
				.add(Items.COOKED_RABBIT)
				.add(Items.COOKED_MUTTON)
				.addOptionalTag(ConventionalItemTags.COOKED_MEATS_FOODS);

		tag(ConventionalItemTags.COOKED_FISH_FOODS)
				.add(Items.COOKED_COD)
				.add(Items.COOKED_SALMON)
				.addOptionalTag(ConventionalItemTags.COOKED_FISHES_FOODS);

		tag(ConventionalItemTags.SOUP_FOODS)
				.add(Items.BEETROOT_SOUP)
				.add(Items.MUSHROOM_STEW)
				.add(Items.RABBIT_STEW)
				.add(Items.SUSPICIOUS_STEW)
				.addOptionalTag(ConventionalItemTags.SOUPS_FOODS);

		tag(ConventionalItemTags.CANDY_FOODS)
				.addOptionalTag(ConventionalItemTags.CANDIES_FOODS);

		tag(ConventionalItemTags.PIE_FOODS)
				.add(Items.PUMPKIN_PIE);

		tag(ConventionalItemTags.GOLDEN_FOODS)
				.add(Items.GOLDEN_APPLE)
				.add(Items.ENCHANTED_GOLDEN_APPLE)
				.add(Items.GOLDEN_CARROT);

		tag(ConventionalItemTags.EDIBLE_WHEN_PLACED_FOODS)
				.add(Items.CAKE);

		tag(ConventionalItemTags.FOOD_POISONING_FOODS)
				.add(Items.POISONOUS_POTATO)
				.add(Items.PUFFERFISH)
				.add(Items.SPIDER_EYE)
				.add(Items.CHICKEN)
				.add(Items.ROTTEN_FLESH);

		tag(ConventionalItemTags.ANIMAL_FOODS)
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

		tag(ConventionalItemTags.FOODS)
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

		tag(ConventionalItemTags.DRINKS)
				.addOptionalTag(ConventionalItemTags.WATER_DRINKS)
				.addOptionalTag(ConventionalItemTags.WATERY_DRINKS)
				.addOptionalTag(ConventionalItemTags.MILK_DRINKS)
				.addOptionalTag(ConventionalItemTags.HONEY_DRINKS)
				.addOptionalTag(ConventionalItemTags.MAGIC_DRINKS)
				.addOptionalTag(ConventionalItemTags.OMINOUS_DRINKS)
				.addOptionalTag(ConventionalItemTags.JUICE_DRINKS);

		tag(ConventionalItemTags.WATER_DRINKS);

		tag(ConventionalItemTags.WATERY_DRINKS)
				.add(Items.POTION)
				.addOptionalTag(ConventionalItemTags.WATER_DRINKS);

		tag(ConventionalItemTags.MILK_DRINKS)
				.add(Items.MILK_BUCKET);

		tag(ConventionalItemTags.HONEY_DRINKS)
				.add(Items.HONEY_BOTTLE);

		tag(ConventionalItemTags.MAGIC_DRINKS)
				.add(Items.POTION)
				.addOptionalTag(ConventionalItemTags.OMINOUS_DRINKS);

		tag(ConventionalItemTags.OMINOUS_DRINKS)
				.add(Items.OMINOUS_BOTTLE);

		tag(ConventionalItemTags.JUICE_DRINKS);

		tag(ConventionalItemTags.DRINK_CONTAINING_BUCKET)
				.add(Items.MILK_BUCKET);

		tag(ConventionalItemTags.DRINK_CONTAINING_BOTTLE)
				.add(Items.POTION)
				.add(Items.HONEY_BOTTLE)
				.add(Items.OMINOUS_BOTTLE);

		// Deprecated tags below
		tag(ConventionalItemTags.FRUITS_FOODS)
				.add(Items.APPLE)
				.add(Items.GOLDEN_APPLE)
				.add(Items.ENCHANTED_GOLDEN_APPLE)
				.add(Items.MELON_SLICE);

		tag(ConventionalItemTags.VEGETABLES_FOODS)
				.add(Items.CARROT)
				.add(Items.GOLDEN_CARROT)
				.add(Items.POTATO)
				.add(Items.BEETROOT);

		tag(ConventionalItemTags.BERRIES_FOODS)
				.add(Items.SWEET_BERRIES)
				.add(Items.GLOW_BERRIES);

		tag(ConventionalItemTags.BREADS_FOODS)
				.add(Items.BREAD);

		tag(ConventionalItemTags.COOKIES_FOODS)
				.add(Items.COOKIE);

		tag(ConventionalItemTags.RAW_MEATS_FOODS)
				.add(Items.BEEF)
				.add(Items.PORKCHOP)
				.add(Items.CHICKEN)
				.add(Items.RABBIT)
				.add(Items.MUTTON);

		tag(ConventionalItemTags.RAW_FISHES_FOODS)
				.add(Items.COD)
				.add(Items.SALMON)
				.add(Items.TROPICAL_FISH)
				.add(Items.PUFFERFISH);

		tag(ConventionalItemTags.COOKED_MEATS_FOODS)
				.add(Items.COOKED_BEEF)
				.add(Items.COOKED_PORKCHOP)
				.add(Items.COOKED_CHICKEN)
				.add(Items.COOKED_RABBIT)
				.add(Items.COOKED_MUTTON);

		tag(ConventionalItemTags.COOKED_FISHES_FOODS)
				.add(Items.COOKED_COD)
				.add(Items.COOKED_SALMON);

		tag(ConventionalItemTags.SOUPS_FOODS)
				.add(Items.BEETROOT_SOUP)
				.add(Items.MUSHROOM_STEW)
				.add(Items.RABBIT_STEW)
				.add(Items.SUSPICIOUS_STEW);

		tag(ConventionalItemTags.CANDIES_FOODS);
	}

	private void generateBucketTags() {
		tag(ConventionalItemTags.EMPTY_BUCKETS)
				.add(Items.BUCKET);
		tag(ConventionalItemTags.LAVA_BUCKETS)
				.add(Items.LAVA_BUCKET);
		tag(ConventionalItemTags.ENTITY_WATER_BUCKETS)
				.add(Items.AXOLOTL_BUCKET)
				.add(Items.COD_BUCKET)
				.add(Items.PUFFERFISH_BUCKET)
				.add(Items.TADPOLE_BUCKET)
				.add(Items.TROPICAL_FISH_BUCKET)
				.add(Items.SALMON_BUCKET);
		tag(ConventionalItemTags.WATER_BUCKETS)
				.add(Items.WATER_BUCKET);
		tag(ConventionalItemTags.MILK_BUCKETS)
				.add(Items.MILK_BUCKET);
		tag(ConventionalItemTags.POWDER_SNOW_BUCKETS)
				.add(Items.POWDER_SNOW_BUCKET);
		tag(ConventionalItemTags.BUCKETS)
				.addOptionalTag(ConventionalItemTags.EMPTY_BUCKETS)
				.addOptionalTag(ConventionalItemTags.WATER_BUCKETS)
				.addOptionalTag(ConventionalItemTags.LAVA_BUCKETS)
				.addOptionalTag(ConventionalItemTags.MILK_BUCKETS)
				.addOptionalTag(ConventionalItemTags.POWDER_SNOW_BUCKETS)
				.addOptionalTag(ConventionalItemTags.ENTITY_WATER_BUCKETS);
	}

	private void generateOreAndRelatedTags() {
		// Categories
		tag(ConventionalItemTags.BRICKS)
				.addOptionalTag(ConventionalItemTags.NORMAL_BRICKS)
				.addOptionalTag(ConventionalItemTags.NETHER_BRICKS)
				.addOptionalTag(ConventionalItemTags.RESIN_BRICKS);
		tag(ConventionalItemTags.DUSTS)
				.addOptionalTag(ConventionalItemTags.GLOWSTONE_DUSTS)
				.addOptionalTag(ConventionalItemTags.REDSTONE_DUSTS);
		tag(ConventionalItemTags.CLUMPS)
				.addOptionalTag(ConventionalItemTags.RESIN_CLUMPS);
		tag(ConventionalItemTags.GEMS)
				.addOptionalTag(ConventionalItemTags.AMETHYST_GEMS)
				.addOptionalTag(ConventionalItemTags.DIAMOND_GEMS)
				.addOptionalTag(ConventionalItemTags.EMERALD_GEMS)
				.addOptionalTag(ConventionalItemTags.LAPIS_GEMS)
				.addOptionalTag(ConventionalItemTags.PRISMARINE_GEMS)
				.addOptionalTag(ConventionalItemTags.QUARTZ_GEMS);
		tag(ConventionalItemTags.INGOTS)
				.addOptionalTag(ConventionalItemTags.COPPER_INGOTS)
				.addOptionalTag(ConventionalItemTags.IRON_INGOTS)
				.addOptionalTag(ConventionalItemTags.GOLD_INGOTS)
				.addOptionalTag(ConventionalItemTags.NETHERITE_INGOTS);
		tag(ConventionalItemTags.NUGGETS)
				.addOptionalTag(ConventionalItemTags.IRON_NUGGETS)
				.addOptionalTag(ConventionalItemTags.GOLD_NUGGETS);
		copy(ConventionalBlockTags.ORES, ConventionalItemTags.ORES);
		tag(ConventionalItemTags.RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.COPPER_RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.GOLD_RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.IRON_RAW_MATERIALS);

		tag(ConventionalItemTags.RAW_BLOCKS)
				.addOptionalTag(ConventionalItemTags.COPPER_RAW_BLOCKS)
				.addOptionalTag(ConventionalItemTags.GOLD_RAW_BLOCKS)
				.addOptionalTag(ConventionalItemTags.IRON_RAW_BLOCKS);

		// Vanilla instances
		tag(ConventionalItemTags.NORMAL_BRICKS)
				.add(Items.BRICK);
		tag(ConventionalItemTags.NETHER_BRICKS)
				.add(Items.NETHER_BRICK);
		tag(ConventionalItemTags.RESIN_BRICKS)
				.add(Items.RESIN_BRICK);

		tag(ConventionalItemTags.IRON_INGOTS)
				.add(Items.IRON_INGOT);
		tag(ConventionalItemTags.COPPER_INGOTS)
				.add(Items.COPPER_INGOT);
		tag(ConventionalItemTags.GOLD_INGOTS)
				.add(Items.GOLD_INGOT);
		tag(ConventionalItemTags.NETHERITE_INGOTS)
				.add(Items.NETHERITE_INGOT);

		tag(ConventionalItemTags.IRON_RAW_BLOCKS)
				.add(Items.RAW_IRON_BLOCK);
		tag(ConventionalItemTags.COPPER_RAW_BLOCKS)
				.add(Items.RAW_COPPER_BLOCK);
		tag(ConventionalItemTags.GOLD_RAW_BLOCKS)
				.add(Items.RAW_GOLD_BLOCK);

		tag(ConventionalItemTags.IRON_RAW_MATERIALS)
				.add(Items.RAW_IRON);
		tag(ConventionalItemTags.COPPER_RAW_MATERIALS)
				.add(Items.RAW_COPPER);
		tag(ConventionalItemTags.GOLD_RAW_MATERIALS)
				.add(Items.RAW_GOLD);

		tag(ConventionalItemTags.REDSTONE_DUSTS)
				.add(Items.REDSTONE);
		tag(ConventionalItemTags.GLOWSTONE_DUSTS)
				.add(Items.GLOWSTONE_DUST);
		tag(ConventionalItemTags.COAL)
				.addOptionalTag(ItemTags.COALS);

		copy(ConventionalBlockTags.COAL_ORES, ConventionalItemTags.COAL_ORES);
		copy(ConventionalBlockTags.COPPER_ORES, ConventionalItemTags.COPPER_ORES);
		copy(ConventionalBlockTags.DIAMOND_ORES, ConventionalItemTags.DIAMOND_ORES);
		copy(ConventionalBlockTags.EMERALD_ORES, ConventionalItemTags.EMERALD_ORES);
		copy(ConventionalBlockTags.GOLD_ORES, ConventionalItemTags.GOLD_ORES);
		copy(ConventionalBlockTags.IRON_ORES, ConventionalItemTags.IRON_ORES);
		copy(ConventionalBlockTags.LAPIS_ORES, ConventionalItemTags.LAPIS_ORES);
		copy(ConventionalBlockTags.NETHERITE_SCRAP_ORES, ConventionalItemTags.NETHERITE_SCRAP_ORES);
		copy(ConventionalBlockTags.REDSTONE_ORES, ConventionalItemTags.REDSTONE_ORES);
		copy(ConventionalBlockTags.QUARTZ_ORES, ConventionalItemTags.QUARTZ_ORES);

		tag(ConventionalItemTags.RESIN_CLUMPS)
				.add(Items.RESIN_CLUMP);

		tag(ConventionalItemTags.QUARTZ_GEMS)
				.add(Items.QUARTZ);
		tag(ConventionalItemTags.EMERALD_GEMS)
				.add(Items.EMERALD);
		tag(ConventionalItemTags.LAPIS_GEMS)
				.add(Items.LAPIS_LAZULI);
		tag(ConventionalItemTags.DIAMOND_GEMS)
				.add(Items.DIAMOND);
		tag(ConventionalItemTags.AMETHYST_GEMS)
				.add(Items.AMETHYST_SHARD);
		tag(ConventionalItemTags.PRISMARINE_GEMS)
				.add(Items.PRISMARINE_CRYSTALS);

		tag(ConventionalItemTags.IRON_NUGGETS)
				.add(Items.IRON_NUGGET);
		tag(ConventionalItemTags.GOLD_NUGGETS)
				.add(Items.GOLD_NUGGET);

		copy(ConventionalBlockTags.ORE_BEARING_GROUND_DEEPSLATE, ConventionalItemTags.ORE_BEARING_GROUND_DEEPSLATE);
		copy(ConventionalBlockTags.ORE_BEARING_GROUND_NETHERRACK, ConventionalItemTags.ORE_BEARING_GROUND_NETHERRACK);
		copy(ConventionalBlockTags.ORE_BEARING_GROUND_STONE, ConventionalItemTags.ORE_BEARING_GROUND_STONE);
		copy(ConventionalBlockTags.ORE_RATES_DENSE, ConventionalItemTags.ORE_RATES_DENSE);
		copy(ConventionalBlockTags.ORE_RATES_SINGULAR, ConventionalItemTags.ORE_RATES_SINGULAR);
		copy(ConventionalBlockTags.ORE_RATES_SPARSE, ConventionalItemTags.ORE_RATES_SPARSE);
		copy(ConventionalBlockTags.ORES_IN_GROUND_DEEPSLATE, ConventionalItemTags.ORES_IN_GROUND_DEEPSLATE);
		copy(ConventionalBlockTags.ORES_IN_GROUND_NETHERRACK, ConventionalItemTags.ORES_IN_GROUND_NETHERRACK);
		copy(ConventionalBlockTags.ORES_IN_GROUND_STONE, ConventionalItemTags.ORES_IN_GROUND_STONE);
	}

	private void generateToolTags() {
		tag(ConventionalItemTags.TOOLS)
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
				.addOptionalTag(ConventionalItemTags.MACE_TOOLS)
				.addOptionalTag(ConventionalItemTags.WRENCH_TOOLS)
				.addOptionalTag(ConventionalItemTags.MINING_TOOL_TOOLS)
				.addOptionalTag(ConventionalItemTags.MELEE_WEAPON_TOOLS)
				.addOptionalTag(ConventionalItemTags.RANGED_WEAPON_TOOLS);

		tag(ConventionalItemTags.BOW_TOOLS)
				.add(Items.BOW)
				.addOptionalTag(ConventionalItemTags.BOWS_TOOLS);
		tag(ConventionalItemTags.CROSSBOW_TOOLS)
				.add(Items.CROSSBOW)
				.addOptionalTag(ConventionalItemTags.CROSSBOWS_TOOLS);
		tag(ConventionalItemTags.SHEAR_TOOLS)
				.add(Items.SHEARS)
				.addOptionalTag(ConventionalItemTags.SHEARS_TOOLS);
		tag(ConventionalItemTags.SHIELD_TOOLS)
				.add(Items.SHIELD)
				.addOptionalTag(ConventionalItemTags.SHIELDS_TOOLS);
		tag(ConventionalItemTags.SPEAR_TOOLS)
				.add(Items.TRIDENT)
				.addOptionalTag(ConventionalItemTags.SPEARS_TOOLS);
		tag(ConventionalItemTags.FISHING_ROD_TOOLS)
				.add(Items.FISHING_ROD)
				.addOptionalTag(ConventionalItemTags.FISHING_RODS_TOOLS);
		tag(ConventionalItemTags.BRUSH_TOOLS)
				.add(Items.BRUSH)
				.addOptionalTag(ConventionalItemTags.BRUSHES_TOOLS);
		tag(ConventionalItemTags.IGNITER_TOOLS)
				.add(Items.FLINT_AND_STEEL);
		tag(ConventionalItemTags.MACE_TOOLS)
				.add(Items.MACE);
		tag(ConventionalItemTags.WRENCH_TOOLS);

		tag(ConventionalItemTags.MINING_TOOL_TOOLS)
				.add(Items.WOODEN_PICKAXE)
				.add(Items.STONE_PICKAXE)
				.add(Items.GOLDEN_PICKAXE)
				.add(Items.IRON_PICKAXE)
				.add(Items.DIAMOND_PICKAXE)
				.add(Items.NETHERITE_PICKAXE)
				.addOptionalTag(ConventionalItemTags.MINING_TOOLS);

		tag(ConventionalItemTags.MELEE_WEAPON_TOOLS)
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

		tag(ConventionalItemTags.RANGED_WEAPON_TOOLS)
				.add(Items.BOW)
				.add(Items.CROSSBOW)
				.add(Items.TRIDENT)
				.addOptionalTag(ConventionalItemTags.RANGED_WEAPONS_TOOLS);

		tag(ConventionalItemTags.ARMORS)
				.addOptionalTag(ItemTags.HEAD_ARMOR)
				.addOptionalTag(ItemTags.CHEST_ARMOR)
				.addOptionalTag(ItemTags.LEG_ARMOR)
				.addOptionalTag(ItemTags.FOOT_ARMOR);

		tag(ConventionalItemTags.ENCHANTABLES)
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

		tag(ConventionalItemTags.BOWS_TOOLS)
				.add(Items.BOW);
		tag(ConventionalItemTags.CROSSBOWS_TOOLS)
				.add(Items.CROSSBOW);
		tag(ConventionalItemTags.SHEARS_TOOLS)
				.add(Items.SHEARS);
		tag(ConventionalItemTags.SHIELDS_TOOLS)
				.add(Items.SHIELD);
		tag(ConventionalItemTags.SPEARS_TOOLS)
				.add(Items.TRIDENT);
		tag(ConventionalItemTags.FISHING_RODS_TOOLS)
				.add(Items.FISHING_ROD);
		tag(ConventionalItemTags.BRUSHES_TOOLS)
				.add(Items.BRUSH);

		tag(ConventionalItemTags.MINING_TOOLS)
				.add(Items.WOODEN_PICKAXE)
				.add(Items.STONE_PICKAXE)
				.add(Items.GOLDEN_PICKAXE)
				.add(Items.IRON_PICKAXE)
				.add(Items.DIAMOND_PICKAXE)
				.add(Items.NETHERITE_PICKAXE);

		tag(ConventionalItemTags.MELEE_WEAPONS_TOOLS)
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

		tag(ConventionalItemTags.RANGED_WEAPONS_TOOLS)
				.add(Items.BOW)
				.add(Items.CROSSBOW)
				.add(Items.TRIDENT);
	}

	private void generateVillagerJobSites() {
		BlockTagGenerator.VILLAGER_JOB_SITE_BLOCKS.stream()
				.map(ItemConvertible::asItem)
				.distinct() // cauldron blocks have the same item
				.forEach(tag(ConventionalItemTags.VILLAGER_JOB_SITES)::add);
	}

	private void generateCropAndSeedsTags() {
		tag(ConventionalItemTags.CROPS)
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

		tag(ConventionalItemTags.BEETROOT_CROPS)
				.add(Items.BEETROOT);
		tag(ConventionalItemTags.CACTUS_CROPS)
				.add(Items.CACTUS);
		tag(ConventionalItemTags.CARROT_CROPS)
				.add(Items.CARROT);
		tag(ConventionalItemTags.COCOA_BEAN_CROPS)
				.add(Items.COCOA_BEANS);
		tag(ConventionalItemTags.MELON_CROPS)
				.add(Items.MELON);
		tag(ConventionalItemTags.NETHER_WART_CROPS)
				.add(Items.NETHER_WART);
		tag(ConventionalItemTags.POTATO_CROPS)
				.add(Items.POTATO);
		tag(ConventionalItemTags.PUMPKIN_CROPS)
				.add(Items.PUMPKIN);
		tag(ConventionalItemTags.SUGAR_CANE_CROPS)
				.add(Items.SUGAR_CANE);
		tag(ConventionalItemTags.WHEAT_CROPS)
				.add(Items.WHEAT);

		tag(ConventionalItemTags.SEEDS)
				.addOptionalTag(ConventionalItemTags.BEETROOT_SEEDS)
				.addOptionalTag(ConventionalItemTags.MELON_SEEDS)
				.addOptionalTag(ConventionalItemTags.PUMPKIN_SEEDS)
				.addOptionalTag(ConventionalItemTags.TORCHFLOWER_SEEDS)
				.addOptionalTag(ConventionalItemTags.PITCHER_PLANT_SEEDS)
				.addOptionalTag(ConventionalItemTags.WHEAT_SEEDS);
		tag(ConventionalItemTags.BEETROOT_SEEDS)
				.add(Items.BEETROOT_SEEDS);
		tag(ConventionalItemTags.MELON_SEEDS)
				.add(Items.MELON_SEEDS);
		tag(ConventionalItemTags.PUMPKIN_SEEDS)
				.add(Items.PUMPKIN_SEEDS);
		tag(ConventionalItemTags.TORCHFLOWER_SEEDS)
				.add(Items.TORCHFLOWER_SEEDS);
		tag(ConventionalItemTags.PITCHER_PLANT_SEEDS)
				.add(Items.PITCHER_POD);
		tag(ConventionalItemTags.WHEAT_SEEDS)
				.add(Items.WHEAT_SEEDS);
	}

	private void generateFlowerTags() {
		copy(ConventionalBlockTags.SMALL_FLOWERS, ConventionalItemTags.SMALL_FLOWERS);
		copy(ConventionalBlockTags.TALL_FLOWERS, ConventionalItemTags.TALL_FLOWERS);
		copy(ConventionalBlockTags.FLOWERS, ConventionalItemTags.FLOWERS);
	}

	private void generateOtherTags() {
		tag(ConventionalItemTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
				.add(Items.CRAFTING_TABLE);

		tag(ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES)
				.add(Items.FURNACE);

		tag(ConventionalItemTags.STRINGS)
				.add(Items.STRING);

		tag(ConventionalItemTags.LEATHERS)
				.add(Items.LEATHER);

		tag(ConventionalItemTags.BONES)
				.add(Items.BONE);

		tag(ConventionalItemTags.EGGS)
				.add(Items.EGG, Items.BROWN_EGG, Items.BLUE_EGG);

		tag(ConventionalItemTags.FEATHERS)
				.add(Items.FEATHER);

		tag(ConventionalItemTags.GUNPOWDERS)
				.add(Items.GUNPOWDER);

		tag(ConventionalItemTags.MUSHROOMS)
				.add(Items.RED_MUSHROOM)
				.add(Items.BROWN_MUSHROOM);

		tag(ConventionalItemTags.NETHER_STARS)
				.add(Items.NETHER_STAR);

		tag(ConventionalItemTags.MUSIC_DISCS)
				.add(Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR,
					Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD,
					Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_OTHERSIDE, Items.MUSIC_DISC_5, Items.MUSIC_DISC_PIGSTEP,
					Items.MUSIC_DISC_RELIC, Items.MUSIC_DISC_CREATOR, Items.MUSIC_DISC_CREATOR_MUSIC_BOX, Items.MUSIC_DISC_PRECIPICE);

		tag(ConventionalItemTags.WOODEN_RODS)
				.add(Items.STICK);

		tag(ConventionalItemTags.BLAZE_RODS)
				.add(Items.BLAZE_ROD);

		tag(ConventionalItemTags.BREEZE_RODS)
				.add(Items.BREEZE_ROD);

		tag(ConventionalItemTags.RODS)
				.addOptionalTag(ConventionalItemTags.WOODEN_RODS)
				.addOptionalTag(ConventionalItemTags.BLAZE_RODS)
				.addOptionalTag(ConventionalItemTags.BREEZE_RODS);

		tag(ConventionalItemTags.ROPES); // Generate tag so others can see it exists through JSON.

		tag(ConventionalItemTags.CHAINS)
				.add(Items.CHAIN);

		tag(ConventionalItemTags.ENDER_PEARLS)
				.add(Items.ENDER_PEARL);

		tag(ConventionalItemTags.SLIME_BALLS)
				.add(Items.SLIME_BALL);

		tag(ConventionalItemTags.FERTILIZERS)
				.add(Items.BONE_MEAL);

		tag(ConventionalItemTags.HIDDEN_FROM_RECIPE_VIEWERS); // Generate tag so others can see it exists through JSON.
	}

	private void generateDyedTags() {
		// Cannot pull entries from block tag because Wall Banners do not have an item form
		tag(ConventionalItemTags.BLACK_DYED)
				.add(Items.BLACK_BANNER).add(Items.BLACK_BED).add(Items.BLACK_CANDLE).add(Items.BLACK_CARPET)
				.add(Items.BLACK_CONCRETE).add(Items.BLACK_CONCRETE_POWDER).add(Items.BLACK_GLAZED_TERRACOTTA)
				.add(Items.BLACK_SHULKER_BOX).add(Items.BLACK_STAINED_GLASS).add(Items.BLACK_STAINED_GLASS_PANE)
				.add(Items.BLACK_TERRACOTTA).add(Items.BLACK_WOOL).add(Items.BLACK_BUNDLE);

		tag(ConventionalItemTags.BLUE_DYED)
				.add(Items.BLUE_BANNER).add(Items.BLUE_BED).add(Items.BLUE_CANDLE).add(Items.BLUE_CARPET)
				.add(Items.BLUE_CONCRETE).add(Items.BLUE_CONCRETE_POWDER).add(Items.BLUE_GLAZED_TERRACOTTA)
				.add(Items.BLUE_SHULKER_BOX).add(Items.BLUE_STAINED_GLASS).add(Items.BLUE_STAINED_GLASS_PANE)
				.add(Items.BLUE_TERRACOTTA).add(Items.BLUE_WOOL).add(Items.BLUE_BUNDLE);

		tag(ConventionalItemTags.BROWN_DYED)
				.add(Items.BROWN_BANNER).add(Items.BROWN_BED).add(Items.BROWN_CANDLE).add(Items.BROWN_CARPET)
				.add(Items.BROWN_CONCRETE).add(Items.BROWN_CONCRETE_POWDER).add(Items.BROWN_GLAZED_TERRACOTTA)
				.add(Items.BROWN_SHULKER_BOX).add(Items.BROWN_STAINED_GLASS).add(Items.BROWN_STAINED_GLASS_PANE)
				.add(Items.BROWN_TERRACOTTA).add(Items.BROWN_WOOL).add(Items.BROWN_BUNDLE);

		tag(ConventionalItemTags.CYAN_DYED)
				.add(Items.CYAN_BANNER).add(Items.CYAN_BED).add(Items.CYAN_CANDLE).add(Items.CYAN_CARPET)
				.add(Items.CYAN_CONCRETE).add(Items.CYAN_CONCRETE_POWDER).add(Items.CYAN_GLAZED_TERRACOTTA)
				.add(Items.CYAN_SHULKER_BOX).add(Items.CYAN_STAINED_GLASS).add(Items.CYAN_STAINED_GLASS_PANE)
				.add(Items.CYAN_TERRACOTTA).add(Items.CYAN_WOOL).add(Items.CYAN_BUNDLE);

		tag(ConventionalItemTags.GRAY_DYED)
				.add(Items.GRAY_BANNER).add(Items.GRAY_BED).add(Items.GRAY_CANDLE).add(Items.GRAY_CARPET)
				.add(Items.GRAY_CONCRETE).add(Items.GRAY_CONCRETE_POWDER).add(Items.GRAY_GLAZED_TERRACOTTA)
				.add(Items.GRAY_SHULKER_BOX).add(Items.GRAY_STAINED_GLASS).add(Items.GRAY_STAINED_GLASS_PANE)
				.add(Items.GRAY_TERRACOTTA).add(Items.GRAY_WOOL).add(Items.GRAY_BUNDLE);

		tag(ConventionalItemTags.GREEN_DYED)
				.add(Items.GREEN_BANNER).add(Items.GREEN_BED).add(Items.GREEN_CANDLE).add(Items.GREEN_CARPET)
				.add(Items.GREEN_CONCRETE).add(Items.GREEN_CONCRETE_POWDER).add(Items.GREEN_GLAZED_TERRACOTTA)
				.add(Items.GREEN_SHULKER_BOX).add(Items.GREEN_STAINED_GLASS).add(Items.GREEN_STAINED_GLASS_PANE)
				.add(Items.GREEN_TERRACOTTA).add(Items.GREEN_WOOL).add(Items.GREEN_BUNDLE);

		tag(ConventionalItemTags.LIGHT_BLUE_DYED)
				.add(Items.LIGHT_BLUE_BANNER).add(Items.LIGHT_BLUE_BED).add(Items.LIGHT_BLUE_CANDLE).add(Items.LIGHT_BLUE_CARPET)
				.add(Items.LIGHT_BLUE_CONCRETE).add(Items.LIGHT_BLUE_CONCRETE_POWDER).add(Items.LIGHT_BLUE_GLAZED_TERRACOTTA)
				.add(Items.LIGHT_BLUE_SHULKER_BOX).add(Items.LIGHT_BLUE_STAINED_GLASS).add(Items.LIGHT_BLUE_STAINED_GLASS_PANE)
				.add(Items.LIGHT_BLUE_TERRACOTTA).add(Items.LIGHT_BLUE_WOOL).add(Items.LIGHT_BLUE_BUNDLE);

		tag(ConventionalItemTags.LIGHT_GRAY_DYED)
				.add(Items.LIGHT_GRAY_BANNER).add(Items.LIGHT_GRAY_BED).add(Items.LIGHT_GRAY_CANDLE).add(Items.LIGHT_GRAY_CARPET)
				.add(Items.LIGHT_GRAY_CONCRETE).add(Items.LIGHT_GRAY_CONCRETE_POWDER).add(Items.LIGHT_GRAY_GLAZED_TERRACOTTA)
				.add(Items.LIGHT_GRAY_SHULKER_BOX).add(Items.LIGHT_GRAY_STAINED_GLASS).add(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
				.add(Items.LIGHT_GRAY_TERRACOTTA).add(Items.LIGHT_GRAY_WOOL).add(Items.LIGHT_GRAY_BUNDLE);

		tag(ConventionalItemTags.LIME_DYED)
				.add(Items.LIME_BANNER).add(Items.LIME_BED).add(Items.LIME_CANDLE).add(Items.LIME_CARPET)
				.add(Items.LIME_CONCRETE).add(Items.LIME_CONCRETE_POWDER).add(Items.LIME_GLAZED_TERRACOTTA)
				.add(Items.LIME_SHULKER_BOX).add(Items.LIME_STAINED_GLASS).add(Items.LIME_STAINED_GLASS_PANE)
				.add(Items.LIME_TERRACOTTA).add(Items.LIME_WOOL).add(Items.LIME_BUNDLE);

		tag(ConventionalItemTags.MAGENTA_DYED)
				.add(Items.MAGENTA_BANNER).add(Items.MAGENTA_BED).add(Items.MAGENTA_CANDLE).add(Items.MAGENTA_CARPET)
				.add(Items.MAGENTA_CONCRETE).add(Items.MAGENTA_CONCRETE_POWDER).add(Items.MAGENTA_GLAZED_TERRACOTTA)
				.add(Items.MAGENTA_SHULKER_BOX).add(Items.MAGENTA_STAINED_GLASS).add(Items.MAGENTA_STAINED_GLASS_PANE)
				.add(Items.MAGENTA_TERRACOTTA).add(Items.MAGENTA_WOOL).add(Items.MAGENTA_BUNDLE);

		tag(ConventionalItemTags.ORANGE_DYED)
				.add(Items.ORANGE_BANNER).add(Items.ORANGE_BED).add(Items.ORANGE_CANDLE).add(Items.ORANGE_CARPET)
				.add(Items.ORANGE_CONCRETE).add(Items.ORANGE_CONCRETE_POWDER).add(Items.ORANGE_GLAZED_TERRACOTTA)
				.add(Items.ORANGE_SHULKER_BOX).add(Items.ORANGE_STAINED_GLASS).add(Items.ORANGE_STAINED_GLASS_PANE)
				.add(Items.ORANGE_TERRACOTTA).add(Items.ORANGE_WOOL).add(Items.ORANGE_BUNDLE);

		tag(ConventionalItemTags.PINK_DYED)
				.add(Items.PINK_BANNER).add(Items.PINK_BED).add(Items.PINK_CANDLE).add(Items.PINK_CARPET)
				.add(Items.PINK_CONCRETE).add(Items.PINK_CONCRETE_POWDER).add(Items.PINK_GLAZED_TERRACOTTA)
				.add(Items.PINK_SHULKER_BOX).add(Items.PINK_STAINED_GLASS).add(Items.PINK_STAINED_GLASS_PANE)
				.add(Items.PINK_TERRACOTTA).add(Items.PINK_WOOL).add(Items.PINK_BUNDLE);

		tag(ConventionalItemTags.PURPLE_DYED)
				.add(Items.PURPLE_BANNER).add(Items.PURPLE_BED).add(Items.PURPLE_CANDLE).add(Items.PURPLE_CARPET)
				.add(Items.PURPLE_CONCRETE).add(Items.PURPLE_CONCRETE_POWDER).add(Items.PURPLE_GLAZED_TERRACOTTA)
				.add(Items.PURPLE_SHULKER_BOX).add(Items.PURPLE_STAINED_GLASS).add(Items.PURPLE_STAINED_GLASS_PANE)
				.add(Items.PURPLE_TERRACOTTA).add(Items.PURPLE_WOOL).add(Items.PURPLE_BUNDLE);

		tag(ConventionalItemTags.RED_DYED)
				.add(Items.RED_BANNER).add(Items.RED_BED).add(Items.RED_CANDLE).add(Items.RED_CARPET)
				.add(Items.RED_CONCRETE).add(Items.RED_CONCRETE_POWDER).add(Items.RED_GLAZED_TERRACOTTA)
				.add(Items.RED_SHULKER_BOX).add(Items.RED_STAINED_GLASS).add(Items.RED_STAINED_GLASS_PANE)
				.add(Items.RED_TERRACOTTA).add(Items.RED_WOOL).add(Items.RED_BUNDLE);

		tag(ConventionalItemTags.WHITE_DYED)
				.add(Items.WHITE_BANNER).add(Items.WHITE_BED).add(Items.WHITE_CANDLE).add(Items.WHITE_CARPET)
				.add(Items.WHITE_CONCRETE).add(Items.WHITE_CONCRETE_POWDER).add(Items.WHITE_GLAZED_TERRACOTTA)
				.add(Items.WHITE_SHULKER_BOX).add(Items.WHITE_STAINED_GLASS).add(Items.WHITE_STAINED_GLASS_PANE)
				.add(Items.WHITE_TERRACOTTA).add(Items.WHITE_WOOL).add(Items.WHITE_BUNDLE);

		tag(ConventionalItemTags.YELLOW_DYED)
				.add(Items.YELLOW_BANNER).add(Items.YELLOW_BED).add(Items.YELLOW_CANDLE).add(Items.YELLOW_CARPET)
				.add(Items.YELLOW_CONCRETE).add(Items.YELLOW_CONCRETE_POWDER).add(Items.YELLOW_GLAZED_TERRACOTTA)
				.add(Items.YELLOW_SHULKER_BOX).add(Items.YELLOW_STAINED_GLASS).add(Items.YELLOW_STAINED_GLASS_PANE)
				.add(Items.YELLOW_TERRACOTTA).add(Items.YELLOW_WOOL).add(Items.YELLOW_BUNDLE);

		tag(ConventionalItemTags.DYED)
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

	private void generateTagAlias() {
		aliasGroup("ores/coal").add(ItemTags.COAL_ORES, ConventionalItemTags.COAL_ORES);
		aliasGroup("ores/copper").add(ItemTags.COPPER_ORES, ConventionalItemTags.COPPER_ORES);
		aliasGroup("ores/diamond").add(ItemTags.DIAMOND_ORES, ConventionalItemTags.DIAMOND_ORES);
		aliasGroup("ores/emerald").add(ItemTags.EMERALD_ORES, ConventionalItemTags.EMERALD_ORES);
		aliasGroup("ores/gold").add(ItemTags.GOLD_ORES, ConventionalItemTags.GOLD_ORES);
		aliasGroup("ores/iron").add(ItemTags.IRON_ORES, ConventionalItemTags.IRON_ORES);
		aliasGroup("ores/lapis").add(ItemTags.LAPIS_ORES, ConventionalItemTags.LAPIS_ORES);
		aliasGroup("ores/redstone").add(ItemTags.REDSTONE_ORES, ConventionalItemTags.REDSTONE_ORES);

		aliasGroup("fences").add(ItemTags.FENCES, ConventionalItemTags.FENCES);
		aliasGroup("fences/wooden").add(ItemTags.WOODEN_FENCES, ConventionalItemTags.WOODEN_FENCES);
		aliasGroup("fence_gates").add(ItemTags.FENCE_GATES, ConventionalItemTags.FENCE_GATES);

		aliasGroup("flowers/small").add(ItemTags.SMALL_FLOWERS, ConventionalItemTags.SMALL_FLOWERS);
	}

	private void generateBackwardsCompatTags() {
		// Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
		// TODO: Remove backwards compat tag entries in 1.22

		tag(ConventionalItemTags.WOODEN_BARRELS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "wooden_barrels")));
		tag(ConventionalItemTags.WOODEN_CHESTS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "wooden_chests")));
		tag(ConventionalItemTags.BLACK_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "black_dyes")));
		tag(ConventionalItemTags.BLUE_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "blue_dyes")));
		tag(ConventionalItemTags.BROWN_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "brown_dyes")));
		tag(ConventionalItemTags.GREEN_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "green_dyes")));
		tag(ConventionalItemTags.RED_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "red_dyes")));
		tag(ConventionalItemTags.WHITE_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "white_dyes")));
		tag(ConventionalItemTags.YELLOW_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "yellow_dyes")));
		tag(ConventionalItemTags.LIGHT_BLUE_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "light_blue_dyes")));
		tag(ConventionalItemTags.LIGHT_GRAY_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "light_gray_dyes")));
		tag(ConventionalItemTags.LIME_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "lime_dyes")));
		tag(ConventionalItemTags.MAGENTA_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "magenta_dyes")));
		tag(ConventionalItemTags.ORANGE_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "orange_dyes")));
		tag(ConventionalItemTags.PINK_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "pink_dyes")));
		tag(ConventionalItemTags.CYAN_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "cyan_dyes")));
		tag(ConventionalItemTags.GRAY_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "gray_dyes")));
		tag(ConventionalItemTags.PURPLE_DYES).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "purple_dyes")));
		tag(ConventionalItemTags.IRON_RAW_MATERIALS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "raw_iron_ores")));
		tag(ConventionalItemTags.COPPER_RAW_MATERIALS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "raw_copper_ores")));
		tag(ConventionalItemTags.GOLD_RAW_MATERIALS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "raw_gold_ores")));
		tag(ConventionalItemTags.GLOWSTONE_DUSTS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "glowstone_dusts")));
		tag(ConventionalItemTags.REDSTONE_DUSTS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "redstone_dusts")));
		tag(ConventionalItemTags.DIAMOND_GEMS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "diamonds")));
		tag(ConventionalItemTags.LAPIS_GEMS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "lapis")));
		tag(ConventionalItemTags.EMERALD_GEMS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "emeralds")));
		tag(ConventionalItemTags.QUARTZ_GEMS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "quartz")));
		tag(ConventionalItemTags.SHEAR_TOOLS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "shears")));
		tag(ConventionalItemTags.SPEAR_TOOLS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "spears")));
		tag(ConventionalItemTags.BOW_TOOLS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "bows")));
		tag(ConventionalItemTags.SHIELD_TOOLS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "shields")));
		tag(ConventionalItemTags.STRINGS).addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of(TagUtil.C_TAG_NAMESPACE, "string")));
		tag(ConventionalItemTags.CONCRETE_POWDERS).addOptionalTag(ConventionalItemTags.CONCRETE_POWDER);
	}
}
