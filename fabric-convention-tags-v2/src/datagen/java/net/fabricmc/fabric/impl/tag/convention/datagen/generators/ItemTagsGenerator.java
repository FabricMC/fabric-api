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
import net.minecraft.data.tags.TagAppender;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;

public final class ItemTagsGenerator extends FabricTagsProvider.ItemTagsProvider {
	public ItemTagsGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, BlockTagsProvider blockTags) {
		super(output, registriesFuture, blockTags);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
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
		builder(ConventionalItemTags.SHULKER_BOXES)
				.add(BlockItemIds.SHULKER_BOX.item())
				.addAll(BlockItemIds.DYED_SHULKER_BOX.asList().stream().map(BlockItemId::item));
		copy(ConventionalBlockTags.GLAZED_TERRACOTTAS, ConventionalItemTags.GLAZED_TERRACOTTAS);
		copy(ConventionalBlockTags.CONCRETES, ConventionalItemTags.CONCRETES);
		builder(ConventionalItemTags.CONCRETE_POWDERS)
				.addAll(BlockItemIds.CONCRETE_POWDER.asList().stream().map(BlockItemId::item));

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

		copy(ConventionalBlockTags.OVERWORLD_NATURAL_LOGS, ConventionalItemTags.OVERWORLD_NATURAL_LOGS);
		copy(ConventionalBlockTags.NETHER_NATURAL_LOGS, ConventionalItemTags.NETHER_NATURAL_LOGS);
		copy(ConventionalBlockTags.NATURAL_LOGS, ConventionalItemTags.NATURAL_LOGS);
		copy(ConventionalBlockTags.NATURAL_WOODS, ConventionalItemTags.NATURAL_WOODS);
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
		builder(ConventionalItemTags.DYES)
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
		builder(ConventionalItemTags.BLACK_DYES)
				.add(ItemIds.DYE.black());
		builder(ConventionalItemTags.BLUE_DYES)
				.add(ItemIds.DYE.blue());
		builder(ConventionalItemTags.BROWN_DYES)
				.add(ItemIds.DYE.brown());
		builder(ConventionalItemTags.GREEN_DYES)
				.add(ItemIds.DYE.green());
		builder(ConventionalItemTags.RED_DYES)
				.add(ItemIds.DYE.red());
		builder(ConventionalItemTags.WHITE_DYES)
				.add(ItemIds.DYE.white());
		builder(ConventionalItemTags.YELLOW_DYES)
				.add(ItemIds.DYE.yellow());
		builder(ConventionalItemTags.LIGHT_BLUE_DYES)
				.add(ItemIds.DYE.lightBlue());
		builder(ConventionalItemTags.LIGHT_GRAY_DYES)
				.add(ItemIds.DYE.lightGray());
		builder(ConventionalItemTags.LIME_DYES)
				.add(ItemIds.DYE.lime());
		builder(ConventionalItemTags.MAGENTA_DYES)
				.add(ItemIds.DYE.magenta());
		builder(ConventionalItemTags.ORANGE_DYES)
				.add(ItemIds.DYE.orange());
		builder(ConventionalItemTags.PINK_DYES)
				.add(ItemIds.DYE.pink());
		builder(ConventionalItemTags.CYAN_DYES)
				.add(ItemIds.DYE.cyan());
		builder(ConventionalItemTags.GRAY_DYES)
				.add(ItemIds.DYE.gray());
		builder(ConventionalItemTags.PURPLE_DYES)
				.add(ItemIds.DYE.purple());
	}

	private void generateConsumableTags() {
		builder(ConventionalItemTags.BOTTLE_POTIONS)
				.add(ItemIds.POTION)
				.add(ItemIds.SPLASH_POTION)
				.add(ItemIds.LINGERING_POTION);
		builder(ConventionalItemTags.POTIONS)
				.addOptionalTag(ConventionalItemTags.BOTTLE_POTIONS);
	}

	private void generateFoodTags() {
		builder(ConventionalItemTags.FRUIT_FOODS)
				.add(ItemIds.APPLE)
				.add(ItemIds.GOLDEN_APPLE)
				.add(ItemIds.ENCHANTED_GOLDEN_APPLE)
				.add(ItemIds.CHORUS_FRUIT)
				.add(ItemIds.MELON_SLICE);

		builder(ConventionalItemTags.VEGETABLE_FOODS)
				.add(BlockItemIds.CARROT_CROP.item())
				.add(ItemIds.GOLDEN_CARROT)
				.add(BlockItemIds.POTATO_CROP.item())
				.add(ItemIds.BEETROOT);

		builder(ConventionalItemTags.BERRY_FOODS)
				.add(BlockItemIds.SWEET_BERRY_CROP.item())
				.add(BlockItemIds.GLOW_BERRY_CROP.item());

		builder(ConventionalItemTags.BREAD_FOODS)
				.add(ItemIds.BREAD);

		builder(ConventionalItemTags.COOKIE_FOODS)
				.add(ItemIds.COOKIE);

		builder(ConventionalItemTags.DOUGH_FOODS);

		builder(ConventionalItemTags.RAW_MEAT_FOODS)
				.add(ItemIds.BEEF)
				.add(ItemIds.PORKCHOP)
				.add(ItemIds.CHICKEN)
				.add(ItemIds.RABBIT)
				.add(ItemIds.MUTTON);

		builder(ConventionalItemTags.RAW_FISH_FOODS)
				.add(ItemIds.COD)
				.add(ItemIds.SALMON)
				.add(ItemIds.TROPICAL_FISH)
				.add(ItemIds.PUFFERFISH);

		builder(ConventionalItemTags.COOKED_MEAT_FOODS)
				.add(ItemIds.COOKED_BEEF)
				.add(ItemIds.COOKED_PORKCHOP)
				.add(ItemIds.COOKED_CHICKEN)
				.add(ItemIds.COOKED_RABBIT)
				.add(ItemIds.COOKED_MUTTON);

		builder(ConventionalItemTags.COOKED_FISH_FOODS)
				.add(ItemIds.COOKED_COD)
				.add(ItemIds.COOKED_SALMON);

		builder(ConventionalItemTags.SOUP_FOODS)
				.add(ItemIds.BEETROOT_SOUP)
				.add(ItemIds.MUSHROOM_STEW)
				.add(ItemIds.RABBIT_STEW)
				.add(ItemIds.SUSPICIOUS_STEW);

		builder(ConventionalItemTags.CANDY_FOODS);

		builder(ConventionalItemTags.PIE_FOODS)
				.add(ItemIds.PUMPKIN_PIE);

		builder(ConventionalItemTags.GOLDEN_FOODS)
				.add(ItemIds.GOLDEN_APPLE)
				.add(ItemIds.ENCHANTED_GOLDEN_APPLE)
				.add(ItemIds.GOLDEN_CARROT);

		builder(ConventionalItemTags.EDIBLE_WHEN_PLACED_FOODS)
				.add(BlockItemIds.CAKE.item());

		builder(ConventionalItemTags.FOOD_POISONING_FOODS)
				.add(ItemIds.POISONOUS_POTATO)
				.add(ItemIds.PUFFERFISH)
				.add(ItemIds.SPIDER_EYE)
				.add(ItemIds.CHICKEN)
				.add(ItemIds.ROTTEN_FLESH);

		builder(ConventionalItemTags.ANIMAL_FOODS)
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

		builder(ConventionalItemTags.FOODS)
				.add(ItemIds.BAKED_POTATO)
				.add(ItemIds.PUMPKIN_PIE)
				.add(ItemIds.HONEY_BOTTLE)
				.add(ItemIds.OMINOUS_BOTTLE)
				.add(ItemIds.DRIED_KELP)
				.addOptionalTag(ConventionalItemTags.FRUIT_FOODS)
				.addOptionalTag(ConventionalItemTags.VEGETABLE_FOODS)
				.addOptionalTag(ConventionalItemTags.BERRY_FOODS)
				.addOptionalTag(ConventionalItemTags.BREAD_FOODS)
				.addOptionalTag(ConventionalItemTags.COOKIE_FOODS)
				.addOptionalTag(ConventionalItemTags.DOUGH_FOODS)
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

		builder(ConventionalItemTags.DRINKS)
				.addOptionalTag(ConventionalItemTags.WATER_DRINKS)
				.addOptionalTag(ConventionalItemTags.WATERY_DRINKS)
				.addOptionalTag(ConventionalItemTags.MILK_DRINKS)
				.addOptionalTag(ConventionalItemTags.HONEY_DRINKS)
				.addOptionalTag(ConventionalItemTags.MAGIC_DRINKS)
				.addOptionalTag(ConventionalItemTags.OMINOUS_DRINKS)
				.addOptionalTag(ConventionalItemTags.JUICE_DRINKS);

		builder(ConventionalItemTags.WATER_DRINKS);

		builder(ConventionalItemTags.WATERY_DRINKS)
				.add(ItemIds.POTION)
				.addOptionalTag(ConventionalItemTags.WATER_DRINKS);

		builder(ConventionalItemTags.MILK_DRINKS)
				.add(ItemIds.MILK_BUCKET);

		builder(ConventionalItemTags.HONEY_DRINKS)
				.add(ItemIds.HONEY_BOTTLE);

		builder(ConventionalItemTags.MAGIC_DRINKS)
				.add(ItemIds.POTION)
				.addOptionalTag(ConventionalItemTags.OMINOUS_DRINKS);

		builder(ConventionalItemTags.OMINOUS_DRINKS)
				.add(ItemIds.OMINOUS_BOTTLE);

		builder(ConventionalItemTags.JUICE_DRINKS);

		builder(ConventionalItemTags.DRINK_CONTAINING_BUCKET)
				.add(ItemIds.MILK_BUCKET);

		builder(ConventionalItemTags.DRINK_CONTAINING_BOTTLE)
				.add(ItemIds.POTION)
				.add(ItemIds.HONEY_BOTTLE)
				.add(ItemIds.OMINOUS_BOTTLE);
	}

	private void generateBucketTags() {
		builder(ConventionalItemTags.EMPTY_BUCKETS)
				.add(ItemIds.BUCKET);
		builder(ConventionalItemTags.LAVA_BUCKETS)
				.add(ItemIds.LAVA_BUCKET);
		builder(ConventionalItemTags.ENTITY_WATER_BUCKETS)
				.add(ItemIds.AXOLOTL_BUCKET)
				.add(ItemIds.COD_BUCKET)
				.add(ItemIds.PUFFERFISH_BUCKET)
				.add(ItemIds.TADPOLE_BUCKET)
				.add(ItemIds.TROPICAL_FISH_BUCKET)
				.add(ItemIds.SALMON_BUCKET);
		builder(ConventionalItemTags.WATER_BUCKETS)
				.add(ItemIds.WATER_BUCKET);
		builder(ConventionalItemTags.MILK_BUCKETS)
				.add(ItemIds.MILK_BUCKET);
		builder(ConventionalItemTags.POWDER_SNOW_BUCKETS)
				.add(BlockItemIds.POWDER_SNOW.item());
		builder(ConventionalItemTags.BUCKETS)
				.addOptionalTag(ConventionalItemTags.EMPTY_BUCKETS)
				.addOptionalTag(ConventionalItemTags.WATER_BUCKETS)
				.addOptionalTag(ConventionalItemTags.LAVA_BUCKETS)
				.addOptionalTag(ConventionalItemTags.MILK_BUCKETS)
				.addOptionalTag(ConventionalItemTags.POWDER_SNOW_BUCKETS)
				.addOptionalTag(ConventionalItemTags.ENTITY_WATER_BUCKETS);
	}

	private void generateOreAndRelatedTags() {
		// Categories
		builder(ConventionalItemTags.BRICKS)
				.addOptionalTag(ConventionalItemTags.NORMAL_BRICKS)
				.addOptionalTag(ConventionalItemTags.NETHER_BRICKS)
				.addOptionalTag(ConventionalItemTags.RESIN_BRICKS);
		builder(ConventionalItemTags.DUSTS)
				.addOptionalTag(ConventionalItemTags.GLOWSTONE_DUSTS)
				.addOptionalTag(ConventionalItemTags.REDSTONE_DUSTS);
		builder(ConventionalItemTags.CLUMPS)
				.addOptionalTag(ConventionalItemTags.RESIN_CLUMPS);
		builder(ConventionalItemTags.GEMS)
				.addOptionalTag(ConventionalItemTags.AMETHYST_GEMS)
				.addOptionalTag(ConventionalItemTags.DIAMOND_GEMS)
				.addOptionalTag(ConventionalItemTags.EMERALD_GEMS)
				.addOptionalTag(ConventionalItemTags.LAPIS_GEMS)
				.addOptionalTag(ConventionalItemTags.PRISMARINE_GEMS)
				.addOptionalTag(ConventionalItemTags.QUARTZ_GEMS);
		builder(ConventionalItemTags.INGOTS)
				.addOptionalTag(ConventionalItemTags.COPPER_INGOTS)
				.addOptionalTag(ConventionalItemTags.IRON_INGOTS)
				.addOptionalTag(ConventionalItemTags.GOLD_INGOTS)
				.addOptionalTag(ConventionalItemTags.NETHERITE_INGOTS);
		builder(ConventionalItemTags.NUGGETS)
				.addOptionalTag(ConventionalItemTags.COPPER_NUGGETS)
				.addOptionalTag(ConventionalItemTags.IRON_NUGGETS)
				.addOptionalTag(ConventionalItemTags.GOLD_NUGGETS);
		copy(ConventionalBlockTags.ORES, ConventionalItemTags.ORES);
		builder(ConventionalItemTags.RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.COPPER_RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.GOLD_RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.IRON_RAW_MATERIALS);

		// Vanilla instances
		builder(ConventionalItemTags.NORMAL_BRICKS)
				.add(ItemIds.BRICK);
		builder(ConventionalItemTags.NETHER_BRICKS)
				.add(ItemIds.NETHER_BRICK);
		builder(ConventionalItemTags.RESIN_BRICKS)
				.add(ItemIds.RESIN_BRICK);

		builder(ConventionalItemTags.IRON_INGOTS)
				.add(ItemIds.IRON_INGOT);
		builder(ConventionalItemTags.COPPER_INGOTS)
				.add(ItemIds.COPPER_INGOT);
		builder(ConventionalItemTags.GOLD_INGOTS)
				.add(ItemIds.GOLD_INGOT);
		builder(ConventionalItemTags.NETHERITE_INGOTS)
				.add(ItemIds.NETHERITE_INGOT);

		builder(ConventionalItemTags.IRON_RAW_MATERIALS)
				.add(ItemIds.RAW_IRON);
		builder(ConventionalItemTags.COPPER_RAW_MATERIALS)
				.add(ItemIds.RAW_COPPER);
		builder(ConventionalItemTags.GOLD_RAW_MATERIALS)
				.add(ItemIds.RAW_GOLD);

		builder(ConventionalItemTags.REDSTONE_DUSTS)
				.add(BlockItemIds.REDSTONE_DUST.item());
		builder(ConventionalItemTags.GLOWSTONE_DUSTS)
				.add(ItemIds.GLOWSTONE_DUST);

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

		builder(ConventionalItemTags.RESIN_CLUMPS)
				.add(BlockItemIds.RESIN_CLUMP.item());

		builder(ConventionalItemTags.QUARTZ_GEMS)
				.add(ItemIds.QUARTZ);
		builder(ConventionalItemTags.EMERALD_GEMS)
				.add(ItemIds.EMERALD);
		builder(ConventionalItemTags.LAPIS_GEMS)
				.add(ItemIds.LAPIS_LAZULI);
		builder(ConventionalItemTags.DIAMOND_GEMS)
				.add(ItemIds.DIAMOND);
		builder(ConventionalItemTags.AMETHYST_GEMS)
				.add(ItemIds.AMETHYST_SHARD);
		builder(ConventionalItemTags.PRISMARINE_GEMS)
				.add(ItemIds.PRISMARINE_CRYSTALS);

		builder(ConventionalItemTags.COPPER_NUGGETS)
				.add(ItemIds.COPPER_NUGGET);
		builder(ConventionalItemTags.IRON_NUGGETS)
				.add(ItemIds.IRON_NUGGET);
		builder(ConventionalItemTags.GOLD_NUGGETS)
				.add(ItemIds.GOLD_NUGGET);

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
		builder(ConventionalItemTags.TOOLS)
				.addOptionalTag(ItemTags.AXES)
				.addOptionalTag(ItemTags.HOES)
				.addOptionalTag(ItemTags.PICKAXES)
				.addOptionalTag(ItemTags.SHOVELS)
				.addOptionalTag(ItemTags.SPEARS)
				.addOptionalTag(ItemTags.SWORDS)
				.addOptionalTag(ConventionalItemTags.BOW_TOOLS)
				.addOptionalTag(ConventionalItemTags.BRUSH_TOOLS)
				.addOptionalTag(ConventionalItemTags.CROSSBOW_TOOLS)
				.addOptionalTag(ConventionalItemTags.FISHING_ROD_TOOLS)
				.addOptionalTag(ConventionalItemTags.IGNITER_TOOLS)
				.addOptionalTag(ConventionalItemTags.SHEAR_TOOLS)
				.addOptionalTag(ConventionalItemTags.SHIELD_TOOLS)
				.addOptionalTag(ConventionalItemTags.TRIDENT_TOOLS)
				.addOptionalTag(ConventionalItemTags.MACE_TOOLS)
				.addOptionalTag(ConventionalItemTags.WRENCH_TOOLS)
				.addOptionalTag(ConventionalItemTags.MINING_TOOL_TOOLS)
				.addOptionalTag(ConventionalItemTags.MELEE_WEAPON_TOOLS)
				.addOptionalTag(ConventionalItemTags.RANGED_WEAPON_TOOLS);

		builder(ConventionalItemTags.BOW_TOOLS)
				.add(ItemIds.BOW);
		builder(ConventionalItemTags.CROSSBOW_TOOLS)
				.add(ItemIds.CROSSBOW);
		builder(ConventionalItemTags.SHEAR_TOOLS)
				.add(ItemIds.SHEARS);
		builder(ConventionalItemTags.SHIELD_TOOLS)
				.add(ItemIds.SHIELD);
		builder(ConventionalItemTags.TRIDENT_TOOLS)
				.add(ItemIds.TRIDENT);
		builder(ConventionalItemTags.FISHING_ROD_TOOLS)
				.add(ItemIds.FISHING_ROD);
		builder(ConventionalItemTags.BRUSH_TOOLS)
				.add(ItemIds.BRUSH);
		builder(ConventionalItemTags.IGNITER_TOOLS)
				.add(ItemIds.FLINT_AND_STEEL);
		builder(ConventionalItemTags.MACE_TOOLS)
				.add(ItemIds.MACE);
		builder(ConventionalItemTags.WRENCH_TOOLS);

		builder(ConventionalItemTags.MINING_TOOL_TOOLS)
				.add(ItemIds.WOODEN_PICKAXE)
				.add(ItemIds.STONE_PICKAXE)
				.add(ItemIds.COPPER_PICKAXE)
				.add(ItemIds.GOLDEN_PICKAXE)
				.add(ItemIds.IRON_PICKAXE)
				.add(ItemIds.DIAMOND_PICKAXE)
				.add(ItemIds.NETHERITE_PICKAXE);

		builder(ConventionalItemTags.MELEE_WEAPON_TOOLS)
				.add(ItemIds.MACE)
				.add(ItemIds.TRIDENT)
				.add(ItemIds.WOODEN_SWORD)
				.add(ItemIds.STONE_SWORD)
				.add(ItemIds.COPPER_SWORD)
				.add(ItemIds.GOLDEN_SWORD)
				.add(ItemIds.IRON_SWORD)
				.add(ItemIds.DIAMOND_SWORD)
				.add(ItemIds.NETHERITE_SWORD)
				.add(ItemIds.WOODEN_AXE)
				.add(ItemIds.STONE_AXE)
				.add(ItemIds.COPPER_AXE)
				.add(ItemIds.GOLDEN_AXE)
				.add(ItemIds.IRON_AXE)
				.add(ItemIds.DIAMOND_AXE)
				.add(ItemIds.NETHERITE_AXE)
				.add(ItemIds.WOODEN_SPEAR)
				.add(ItemIds.STONE_SPEAR)
				.add(ItemIds.COPPER_SPEAR)
				.add(ItemIds.IRON_SPEAR)
				.add(ItemIds.GOLDEN_SPEAR)
				.add(ItemIds.DIAMOND_SPEAR)
				.add(ItemIds.NETHERITE_SPEAR);

		builder(ConventionalItemTags.RANGED_WEAPON_TOOLS)
				.add(ItemIds.BOW)
				.add(ItemIds.CROSSBOW)
				.add(ItemIds.TRIDENT);

		builder(ConventionalItemTags.ARMORS)
				.addOptionalTag(ConventionalItemTags.HUMANOID_ARMORS)
				.addOptionalTag(ConventionalItemTags.HORSE_ARMORS)
				.addOptionalTag(ConventionalItemTags.NAUTILUS_ARMORS)
				.addOptionalTag(ConventionalItemTags.WOLF_ARMORS);

		builder(ConventionalItemTags.HORSE_ARMORS)
				.add(ItemIds.LEATHER_HORSE_ARMOR)
				.add(ItemIds.COPPER_HORSE_ARMOR)
				.add(ItemIds.IRON_HORSE_ARMOR)
				.add(ItemIds.GOLDEN_HORSE_ARMOR)
				.add(ItemIds.DIAMOND_HORSE_ARMOR)
				.add(ItemIds.NETHERITE_HORSE_ARMOR);

		builder(ConventionalItemTags.NAUTILUS_ARMORS)
				.add(ItemIds.COPPER_NAUTILUS_ARMOR)
				.add(ItemIds.IRON_NAUTILUS_ARMOR)
				.add(ItemIds.GOLDEN_NAUTILUS_ARMOR)
				.add(ItemIds.DIAMOND_NAUTILUS_ARMOR)
				.add(ItemIds.NETHERITE_NAUTILUS_ARMOR);

		builder(ConventionalItemTags.WOLF_ARMORS)
				.add(ItemIds.WOLF_ARMOR);

		builder(ConventionalItemTags.HUMANOID_ARMORS)
				.addOptionalTag(ItemTags.HEAD_ARMOR)
				.addOptionalTag(ItemTags.CHEST_ARMOR)
				.addOptionalTag(ItemTags.LEG_ARMOR)
				.addOptionalTag(ItemTags.FOOT_ARMOR);

		builder(ConventionalItemTags.ENCHANTABLES)
				.addOptionalTag(ItemTags.ARMOR_ENCHANTABLE)
				.addOptionalTag(ItemTags.EQUIPPABLE_ENCHANTABLE)
				.addOptionalTag(ItemTags.SHARP_WEAPON_ENCHANTABLE)
				.addOptionalTag(ItemTags.WEAPON_ENCHANTABLE)
				.addOptionalTag(ItemTags.SWEEPING_ENCHANTABLE)
				.addOptionalTag(ItemTags.MINING_ENCHANTABLE)
				.addOptionalTag(ItemTags.MINING_LOOT_ENCHANTABLE)
				.addOptionalTag(ItemTags.FISHING_ENCHANTABLE)
				.addOptionalTag(ItemTags.TRIDENT_ENCHANTABLE)
				.addOptionalTag(ItemTags.BOW_ENCHANTABLE)
				.addOptionalTag(ItemTags.CROSSBOW_ENCHANTABLE)
				.addOptionalTag(ItemTags.MACE_ENCHANTABLE)
				.addOptionalTag(ItemTags.FIRE_ASPECT_ENCHANTABLE)
				.addOptionalTag(ItemTags.DURABILITY_ENCHANTABLE)
				.addOptionalTag(ItemTags.VANISHING_ENCHANTABLE)
				.addOptionalTag(ItemTags.LUNGE_ENCHANTABLE)
				.addOptionalTag(ItemTags.MELEE_WEAPON_ENCHANTABLE);
	}

	private void generateVillagerJobSites() {
		builder(ConventionalItemTags.VILLAGER_JOB_SITES)
				.addAll(BlockTagsGenerator.VILLAGER_JOB_SITE_BLOCKS.stream().map(BlockItemId::item));
	}

	private void generateCropAndSeedsTags() {
		builder(ConventionalItemTags.CROPS)
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

		builder(ConventionalItemTags.BEETROOT_CROPS)
				.add(ItemIds.BEETROOT);
		builder(ConventionalItemTags.CACTUS_CROPS)
				.add(BlockItemIds.CACTUS.item());
		builder(ConventionalItemTags.CARROT_CROPS)
				.add(BlockItemIds.CARROT_CROP.item());
		builder(ConventionalItemTags.COCOA_BEAN_CROPS)
				.add(BlockItemIds.COCOA_CROP.item());
		builder(ConventionalItemTags.MELON_CROPS)
				.add(BlockItemIds.MELON.item());
		builder(ConventionalItemTags.NETHER_WART_CROPS)
				.add(BlockItemIds.NETHER_WART.item());
		builder(ConventionalItemTags.POTATO_CROPS)
				.add(BlockItemIds.POTATO_CROP.item());
		builder(ConventionalItemTags.PUMPKIN_CROPS)
				.add(BlockItemIds.PUMPKIN.item());
		builder(ConventionalItemTags.SUGAR_CANE_CROPS)
				.add(BlockItemIds.SUGAR_CANE.item());
		builder(ConventionalItemTags.WHEAT_CROPS)
				.add(ItemIds.WHEAT);

		builder(ConventionalItemTags.SEEDS)
				.addOptionalTag(ConventionalItemTags.BEETROOT_SEEDS)
				.addOptionalTag(ConventionalItemTags.MELON_SEEDS)
				.addOptionalTag(ConventionalItemTags.PUMPKIN_SEEDS)
				.addOptionalTag(ConventionalItemTags.TORCHFLOWER_SEEDS)
				.addOptionalTag(ConventionalItemTags.PITCHER_PLANT_SEEDS)
				.addOptionalTag(ConventionalItemTags.WHEAT_SEEDS);
		builder(ConventionalItemTags.BEETROOT_SEEDS)
				.add(BlockItemIds.BEETROOT_CROP.item());
		builder(ConventionalItemTags.MELON_SEEDS)
				.add(BlockItemIds.MELON_CROP.item());
		builder(ConventionalItemTags.PUMPKIN_SEEDS)
				.add(BlockItemIds.PUMPKIN_CROP.item());
		builder(ConventionalItemTags.TORCHFLOWER_SEEDS)
				.add(BlockItemIds.TORCHFLOWER_CROP.item());
		builder(ConventionalItemTags.PITCHER_PLANT_SEEDS)
				.add(BlockItemIds.PITCHER_CROP.item());
		builder(ConventionalItemTags.WHEAT_SEEDS)
				.add(BlockItemIds.WHEAT_CROP.item());
	}

	private void generateFlowerTags() {
		copy(ConventionalBlockTags.SMALL_FLOWERS, ConventionalItemTags.SMALL_FLOWERS);
		copy(ConventionalBlockTags.TALL_FLOWERS, ConventionalItemTags.TALL_FLOWERS);
		copy(ConventionalBlockTags.FLOWERS, ConventionalItemTags.FLOWERS);
	}

	private void generateOtherTags() {
		builder(ConventionalItemTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
				.add(BlockItemIds.CRAFTING_TABLE.item());

		builder(ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES)
				.add(BlockItemIds.FURNACE.item());

		builder(ConventionalItemTags.STRINGS)
				.add(BlockItemIds.TRIPWIRE.item());

		builder(ConventionalItemTags.LEATHERS)
				.add(ItemIds.LEATHER);

		builder(ConventionalItemTags.BONES)
				.add(ItemIds.BONE);

		builder(ConventionalItemTags.EGGS)
				.add(ItemIds.EGG, ItemIds.BROWN_EGG, ItemIds.BLUE_EGG);

		builder(ConventionalItemTags.FEATHERS)
				.add(ItemIds.FEATHER);

		builder(ConventionalItemTags.GUNPOWDERS)
				.add(ItemIds.GUNPOWDER);

		builder(ConventionalItemTags.MUSHROOMS)
				.add(BlockItemIds.RED_MUSHROOM.item())
				.add(BlockItemIds.BROWN_MUSHROOM.item());

		builder(ConventionalItemTags.NETHER_STARS)
				.add(ItemIds.NETHER_STAR);

		builder(ConventionalItemTags.MUSIC_DISCS)
				.add(ItemIds.MUSIC_DISC_13, ItemIds.MUSIC_DISC_CAT, ItemIds.MUSIC_DISC_BLOCKS, ItemIds.MUSIC_DISC_CHIRP, ItemIds.MUSIC_DISC_FAR,
					ItemIds.MUSIC_DISC_MALL, ItemIds.MUSIC_DISC_MELLOHI, ItemIds.MUSIC_DISC_STAL, ItemIds.MUSIC_DISC_STRAD, ItemIds.MUSIC_DISC_WARD,
					ItemIds.MUSIC_DISC_11, ItemIds.MUSIC_DISC_WAIT, ItemIds.MUSIC_DISC_OTHERSIDE, ItemIds.MUSIC_DISC_5, ItemIds.MUSIC_DISC_PIGSTEP,
					ItemIds.MUSIC_DISC_RELIC, ItemIds.MUSIC_DISC_CREATOR, ItemIds.MUSIC_DISC_CREATOR_MUSIC_BOX, ItemIds.MUSIC_DISC_PRECIPICE,
					ItemIds.MUSIC_DISC_TEARS, ItemIds.MUSIC_DISC_LAVA_CHICKEN);

		builder(ConventionalItemTags.WOODEN_RODS)
				.add(ItemIds.STICK);

		builder(ConventionalItemTags.BLAZE_RODS)
				.add(ItemIds.BLAZE_ROD);

		builder(ConventionalItemTags.BREEZE_RODS)
				.add(ItemIds.BREEZE_ROD);

		builder(ConventionalItemTags.RODS)
				.addOptionalTag(ConventionalItemTags.WOODEN_RODS)
				.addOptionalTag(ConventionalItemTags.BLAZE_RODS)
				.addOptionalTag(ConventionalItemTags.BREEZE_RODS);

		builder(ConventionalItemTags.ROPES); // Generate tag so others can see it exists through JSON.

		TagAppender<Item> chains = builder(ConventionalItemTags.CHAINS)
				.add(BlockItemIds.IRON_CHAIN.item());
		BlockItemIds.COPPER_CHAIN.asList().stream().map(BlockItemId::item).forEach(chains::add);

		builder(ConventionalItemTags.ENDER_PEARLS)
				.add(ItemIds.ENDER_PEARL);

		builder(ConventionalItemTags.SLIME_BALLS)
				.add(ItemIds.SLIME_BALL);

		builder(ConventionalItemTags.FERTILIZERS)
				.add(ItemIds.BONE_MEAL);

		builder(ConventionalItemTags.HIDDEN_FROM_RECIPE_VIEWERS); // Generate tag so others can see it exists through JSON.
	}

	private void generateDyedTags() {
		// Cannot pull entries from block tag because Wall Banners do not have an item form
		builder(ConventionalItemTags.BLACK_DYED)
				.add(BlockItemIds.BANNER.black().item()).add(BlockItemIds.BED.black().item()).add(BlockItemIds.DYED_CANDLE.black().item()).add(BlockItemIds.CARPET.black().item())
				.add(BlockItemIds.CONCRETE.black().item()).add(BlockItemIds.CONCRETE_POWDER.black().item()).add(BlockItemIds.GLAZED_TERRACOTTA.black().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.black().item()).add(BlockItemIds.STAINED_GLASS.black().item()).add(BlockItemIds.STAINED_GLASS_PANE.black().item())
				.add(BlockItemIds.DYED_TERRACOTTA.black().item()).add(BlockItemIds.WOOL.black().item()).add(ItemIds.DYED_BUNDLE.black()).add(ItemIds.HARNESS.black());

		builder(ConventionalItemTags.BLUE_DYED)
				.add(BlockItemIds.BANNER.blue().item()).add(BlockItemIds.BED.blue().item()).add(BlockItemIds.DYED_CANDLE.blue().item()).add(BlockItemIds.CARPET.blue().item())
				.add(BlockItemIds.CONCRETE.blue().item()).add(BlockItemIds.CONCRETE_POWDER.blue().item()).add(BlockItemIds.GLAZED_TERRACOTTA.blue().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.blue().item()).add(BlockItemIds.STAINED_GLASS.blue().item()).add(BlockItemIds.STAINED_GLASS_PANE.blue().item())
				.add(BlockItemIds.DYED_TERRACOTTA.blue().item()).add(BlockItemIds.WOOL.blue().item()).add(ItemIds.DYED_BUNDLE.blue()).add(ItemIds.HARNESS.blue());

		builder(ConventionalItemTags.BROWN_DYED)
				.add(BlockItemIds.BANNER.brown().item()).add(BlockItemIds.BED.brown().item()).add(BlockItemIds.DYED_CANDLE.brown().item()).add(BlockItemIds.CARPET.brown().item())
				.add(BlockItemIds.CONCRETE.brown().item()).add(BlockItemIds.CONCRETE_POWDER.brown().item()).add(BlockItemIds.GLAZED_TERRACOTTA.brown().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.brown().item()).add(BlockItemIds.STAINED_GLASS.brown().item()).add(BlockItemIds.STAINED_GLASS_PANE.brown().item())
				.add(BlockItemIds.DYED_TERRACOTTA.brown().item()).add(BlockItemIds.WOOL.brown().item()).add(ItemIds.DYED_BUNDLE.brown()).add(ItemIds.HARNESS.brown());

		builder(ConventionalItemTags.CYAN_DYED)
				.add(BlockItemIds.BANNER.cyan().item()).add(BlockItemIds.BED.cyan().item()).add(BlockItemIds.DYED_CANDLE.cyan().item()).add(BlockItemIds.CARPET.cyan().item())
				.add(BlockItemIds.CONCRETE.cyan().item()).add(BlockItemIds.CONCRETE_POWDER.cyan().item()).add(BlockItemIds.GLAZED_TERRACOTTA.cyan().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.cyan().item()).add(BlockItemIds.STAINED_GLASS.cyan().item()).add(BlockItemIds.STAINED_GLASS_PANE.cyan().item())
				.add(BlockItemIds.DYED_TERRACOTTA.cyan().item()).add(BlockItemIds.WOOL.cyan().item()).add(ItemIds.DYED_BUNDLE.cyan()).add(ItemIds.HARNESS.cyan());

		builder(ConventionalItemTags.GRAY_DYED)
				.add(BlockItemIds.BANNER.gray().item()).add(BlockItemIds.BED.gray().item()).add(BlockItemIds.DYED_CANDLE.gray().item()).add(BlockItemIds.CARPET.gray().item())
				.add(BlockItemIds.CONCRETE.gray().item()).add(BlockItemIds.CONCRETE_POWDER.gray().item()).add(BlockItemIds.GLAZED_TERRACOTTA.gray().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.gray().item()).add(BlockItemIds.STAINED_GLASS.gray().item()).add(BlockItemIds.STAINED_GLASS_PANE.gray().item())
				.add(BlockItemIds.DYED_TERRACOTTA.gray().item()).add(BlockItemIds.WOOL.gray().item()).add(ItemIds.DYED_BUNDLE.gray()).add(ItemIds.HARNESS.gray());

		builder(ConventionalItemTags.GREEN_DYED)
				.add(BlockItemIds.BANNER.green().item()).add(BlockItemIds.BED.green().item()).add(BlockItemIds.DYED_CANDLE.green().item()).add(BlockItemIds.CARPET.green().item())
				.add(BlockItemIds.CONCRETE.green().item()).add(BlockItemIds.CONCRETE_POWDER.green().item()).add(BlockItemIds.GLAZED_TERRACOTTA.green().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.green().item()).add(BlockItemIds.STAINED_GLASS.green().item()).add(BlockItemIds.STAINED_GLASS_PANE.green().item())
				.add(BlockItemIds.DYED_TERRACOTTA.green().item()).add(BlockItemIds.WOOL.green().item()).add(ItemIds.DYED_BUNDLE.green()).add(ItemIds.HARNESS.green());

		builder(ConventionalItemTags.LIGHT_BLUE_DYED)
				.add(BlockItemIds.BANNER.lightBlue().item()).add(BlockItemIds.BED.lightBlue().item()).add(BlockItemIds.DYED_CANDLE.lightBlue().item()).add(BlockItemIds.CARPET.lightBlue().item())
				.add(BlockItemIds.CONCRETE.lightBlue().item()).add(BlockItemIds.CONCRETE_POWDER.lightBlue().item()).add(BlockItemIds.GLAZED_TERRACOTTA.lightBlue().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.lightBlue().item()).add(BlockItemIds.STAINED_GLASS.lightBlue().item()).add(BlockItemIds.STAINED_GLASS_PANE.lightBlue().item())
				.add(BlockItemIds.DYED_TERRACOTTA.lightBlue().item()).add(BlockItemIds.WOOL.lightBlue().item()).add(ItemIds.DYED_BUNDLE.lightBlue()).add(ItemIds.HARNESS.lightBlue());

		builder(ConventionalItemTags.LIGHT_GRAY_DYED)
				.add(BlockItemIds.BANNER.lightGray().item()).add(BlockItemIds.BED.lightGray().item()).add(BlockItemIds.DYED_CANDLE.lightGray().item()).add(BlockItemIds.CARPET.lightGray().item())
				.add(BlockItemIds.CONCRETE.lightGray().item()).add(BlockItemIds.CONCRETE_POWDER.lightGray().item()).add(BlockItemIds.GLAZED_TERRACOTTA.lightGray().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.lightGray().item()).add(BlockItemIds.STAINED_GLASS.lightGray().item()).add(BlockItemIds.STAINED_GLASS_PANE.lightGray().item())
				.add(BlockItemIds.DYED_TERRACOTTA.lightGray().item()).add(BlockItemIds.WOOL.lightGray().item()).add(ItemIds.DYED_BUNDLE.lightGray()).add(ItemIds.HARNESS.lightGray());

		builder(ConventionalItemTags.LIME_DYED)
				.add(BlockItemIds.BANNER.lime().item()).add(BlockItemIds.BED.lime().item()).add(BlockItemIds.DYED_CANDLE.lime().item()).add(BlockItemIds.CARPET.lime().item())
				.add(BlockItemIds.CONCRETE.lime().item()).add(BlockItemIds.CONCRETE_POWDER.lime().item()).add(BlockItemIds.GLAZED_TERRACOTTA.lime().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.lime().item()).add(BlockItemIds.STAINED_GLASS.lime().item()).add(BlockItemIds.STAINED_GLASS_PANE.lime().item())
				.add(BlockItemIds.DYED_TERRACOTTA.lime().item()).add(BlockItemIds.WOOL.lime().item()).add(ItemIds.DYED_BUNDLE.lime()).add(ItemIds.HARNESS.lime());

		builder(ConventionalItemTags.MAGENTA_DYED)
				.add(BlockItemIds.BANNER.magenta().item()).add(BlockItemIds.BED.magenta().item()).add(BlockItemIds.DYED_CANDLE.magenta().item()).add(BlockItemIds.CARPET.magenta().item())
				.add(BlockItemIds.CONCRETE.magenta().item()).add(BlockItemIds.CONCRETE_POWDER.magenta().item()).add(BlockItemIds.GLAZED_TERRACOTTA.magenta().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.magenta().item()).add(BlockItemIds.STAINED_GLASS.magenta().item()).add(BlockItemIds.STAINED_GLASS_PANE.magenta().item())
				.add(BlockItemIds.DYED_TERRACOTTA.magenta().item()).add(BlockItemIds.WOOL.magenta().item()).add(ItemIds.DYED_BUNDLE.magenta()).add(ItemIds.HARNESS.magenta());

		builder(ConventionalItemTags.ORANGE_DYED)
				.add(BlockItemIds.BANNER.orange().item()).add(BlockItemIds.BED.orange().item()).add(BlockItemIds.DYED_CANDLE.orange().item()).add(BlockItemIds.CARPET.orange().item())
				.add(BlockItemIds.CONCRETE.orange().item()).add(BlockItemIds.CONCRETE_POWDER.orange().item()).add(BlockItemIds.GLAZED_TERRACOTTA.orange().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.orange().item()).add(BlockItemIds.STAINED_GLASS.orange().item()).add(BlockItemIds.STAINED_GLASS_PANE.orange().item())
				.add(BlockItemIds.DYED_TERRACOTTA.orange().item()).add(BlockItemIds.WOOL.orange().item()).add(ItemIds.DYED_BUNDLE.orange()).add(ItemIds.HARNESS.orange());

		builder(ConventionalItemTags.PINK_DYED)
				.add(BlockItemIds.BANNER.pink().item()).add(BlockItemIds.BED.pink().item()).add(BlockItemIds.DYED_CANDLE.pink().item()).add(BlockItemIds.CARPET.pink().item())
				.add(BlockItemIds.CONCRETE.pink().item()).add(BlockItemIds.CONCRETE_POWDER.pink().item()).add(BlockItemIds.GLAZED_TERRACOTTA.pink().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.pink().item()).add(BlockItemIds.STAINED_GLASS.pink().item()).add(BlockItemIds.STAINED_GLASS_PANE.pink().item())
				.add(BlockItemIds.DYED_TERRACOTTA.pink().item()).add(BlockItemIds.WOOL.pink().item()).add(ItemIds.DYED_BUNDLE.pink()).add(ItemIds.HARNESS.pink());

		builder(ConventionalItemTags.PURPLE_DYED)
				.add(BlockItemIds.BANNER.purple().item()).add(BlockItemIds.BED.purple().item()).add(BlockItemIds.DYED_CANDLE.purple().item()).add(BlockItemIds.CARPET.purple().item())
				.add(BlockItemIds.CONCRETE.purple().item()).add(BlockItemIds.CONCRETE_POWDER.purple().item()).add(BlockItemIds.GLAZED_TERRACOTTA.purple().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.purple().item()).add(BlockItemIds.STAINED_GLASS.purple().item()).add(BlockItemIds.STAINED_GLASS_PANE.purple().item())
				.add(BlockItemIds.DYED_TERRACOTTA.purple().item()).add(BlockItemIds.WOOL.purple().item()).add(ItemIds.DYED_BUNDLE.purple()).add(ItemIds.HARNESS.purple());

		builder(ConventionalItemTags.RED_DYED)
				.add(BlockItemIds.BANNER.red().item()).add(BlockItemIds.BED.red().item()).add(BlockItemIds.DYED_CANDLE.red().item()).add(BlockItemIds.CARPET.red().item())
				.add(BlockItemIds.CONCRETE.red().item()).add(BlockItemIds.CONCRETE_POWDER.red().item()).add(BlockItemIds.GLAZED_TERRACOTTA.red().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.red().item()).add(BlockItemIds.STAINED_GLASS.red().item()).add(BlockItemIds.STAINED_GLASS_PANE.red().item())
				.add(BlockItemIds.DYED_TERRACOTTA.red().item()).add(BlockItemIds.WOOL.red().item()).add(ItemIds.DYED_BUNDLE.red()).add(ItemIds.HARNESS.red());

		builder(ConventionalItemTags.WHITE_DYED)
				.add(BlockItemIds.BANNER.white().item()).add(BlockItemIds.BED.white().item()).add(BlockItemIds.DYED_CANDLE.white().item()).add(BlockItemIds.CARPET.white().item())
				.add(BlockItemIds.CONCRETE.white().item()).add(BlockItemIds.CONCRETE_POWDER.white().item()).add(BlockItemIds.GLAZED_TERRACOTTA.white().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.white().item()).add(BlockItemIds.STAINED_GLASS.white().item()).add(BlockItemIds.STAINED_GLASS_PANE.white().item())
				.add(BlockItemIds.DYED_TERRACOTTA.white().item()).add(BlockItemIds.WOOL.white().item()).add(ItemIds.DYED_BUNDLE.white()).add(ItemIds.HARNESS.white());

		builder(ConventionalItemTags.YELLOW_DYED)
				.add(BlockItemIds.BANNER.yellow().item()).add(BlockItemIds.BED.yellow().item()).add(BlockItemIds.DYED_CANDLE.yellow().item()).add(BlockItemIds.CARPET.yellow().item())
				.add(BlockItemIds.CONCRETE.yellow().item()).add(BlockItemIds.CONCRETE_POWDER.yellow().item()).add(BlockItemIds.GLAZED_TERRACOTTA.yellow().item())
				.add(BlockItemIds.DYED_SHULKER_BOX.yellow().item()).add(BlockItemIds.STAINED_GLASS.yellow().item()).add(BlockItemIds.STAINED_GLASS_PANE.yellow().item())
				.add(BlockItemIds.DYED_TERRACOTTA.yellow().item()).add(BlockItemIds.WOOL.yellow().item()).add(ItemIds.DYED_BUNDLE.yellow()).add(ItemIds.HARNESS.yellow());

		builder(ConventionalItemTags.DYED)
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

		aliasGroup("fences").add(BlockItemTags.FENCES.item(), ConventionalItemTags.FENCES);
		aliasGroup("fences/wooden").add(ItemTags.WOODEN_FENCES, ConventionalItemTags.WOODEN_FENCES);
		aliasGroup("fence_gates").add(ItemTags.FENCE_GATES, ConventionalItemTags.FENCE_GATES);

		aliasGroup("flowers/small").add(BlockItemTags.SMALL_FLOWERS.item(), ConventionalItemTags.SMALL_FLOWERS);
		aliasGroup("dyes").add(ItemTags.DYES, ConventionalItemTags.DYES);
	}
}
