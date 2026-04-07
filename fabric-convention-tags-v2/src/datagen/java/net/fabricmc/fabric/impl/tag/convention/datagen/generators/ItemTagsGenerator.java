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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

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
		valueLookupBuilder(ConventionalItemTags.SHULKER_BOXES)
				.add(Items.SHULKER_BOX)
				.addAll(Items.DYED_SHULKER_BOX.asList());
		copy(ConventionalBlockTags.GLAZED_TERRACOTTAS, ConventionalItemTags.GLAZED_TERRACOTTAS);
		copy(ConventionalBlockTags.CONCRETES, ConventionalItemTags.CONCRETES);
		valueLookupBuilder(ConventionalItemTags.CONCRETE_POWDERS)
				.addAll(Items.CONCRETE_POWDER.asList());

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
		valueLookupBuilder(ConventionalItemTags.DYES)
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
		valueLookupBuilder(ConventionalItemTags.BLACK_DYES)
				.add(Items.DYE.black());
		valueLookupBuilder(ConventionalItemTags.BLUE_DYES)
				.add(Items.DYE.blue());
		valueLookupBuilder(ConventionalItemTags.BROWN_DYES)
				.add(Items.DYE.brown());
		valueLookupBuilder(ConventionalItemTags.GREEN_DYES)
				.add(Items.DYE.green());
		valueLookupBuilder(ConventionalItemTags.RED_DYES)
				.add(Items.DYE.red());
		valueLookupBuilder(ConventionalItemTags.WHITE_DYES)
				.add(Items.DYE.white());
		valueLookupBuilder(ConventionalItemTags.YELLOW_DYES)
				.add(Items.DYE.yellow());
		valueLookupBuilder(ConventionalItemTags.LIGHT_BLUE_DYES)
				.add(Items.DYE.lightBlue());
		valueLookupBuilder(ConventionalItemTags.LIGHT_GRAY_DYES)
				.add(Items.DYE.lightGray());
		valueLookupBuilder(ConventionalItemTags.LIME_DYES)
				.add(Items.DYE.lime());
		valueLookupBuilder(ConventionalItemTags.MAGENTA_DYES)
				.add(Items.DYE.magenta());
		valueLookupBuilder(ConventionalItemTags.ORANGE_DYES)
				.add(Items.DYE.orange());
		valueLookupBuilder(ConventionalItemTags.PINK_DYES)
				.add(Items.DYE.pink());
		valueLookupBuilder(ConventionalItemTags.CYAN_DYES)
				.add(Items.DYE.cyan());
		valueLookupBuilder(ConventionalItemTags.GRAY_DYES)
				.add(Items.DYE.gray());
		valueLookupBuilder(ConventionalItemTags.PURPLE_DYES)
				.add(Items.DYE.purple());
	}

	private void generateConsumableTags() {
		valueLookupBuilder(ConventionalItemTags.BOTTLE_POTIONS)
				.add(Items.POTION)
				.add(Items.SPLASH_POTION)
				.add(Items.LINGERING_POTION);
		valueLookupBuilder(ConventionalItemTags.POTIONS)
				.addOptionalTag(ConventionalItemTags.BOTTLE_POTIONS);
	}

	private void generateFoodTags() {
		valueLookupBuilder(ConventionalItemTags.FRUIT_FOODS)
				.add(Items.APPLE)
				.add(Items.GOLDEN_APPLE)
				.add(Items.ENCHANTED_GOLDEN_APPLE)
				.add(Items.CHORUS_FRUIT)
				.add(Items.MELON_SLICE);

		valueLookupBuilder(ConventionalItemTags.VEGETABLE_FOODS)
				.add(Items.CARROT)
				.add(Items.GOLDEN_CARROT)
				.add(Items.POTATO)
				.add(Items.BEETROOT);

		valueLookupBuilder(ConventionalItemTags.BERRY_FOODS)
				.add(Items.SWEET_BERRIES)
				.add(Items.GLOW_BERRIES);

		valueLookupBuilder(ConventionalItemTags.BREAD_FOODS)
				.add(Items.BREAD);

		valueLookupBuilder(ConventionalItemTags.COOKIE_FOODS)
				.add(Items.COOKIE);

		valueLookupBuilder(ConventionalItemTags.DOUGH_FOODS);

		valueLookupBuilder(ConventionalItemTags.RAW_MEAT_FOODS)
				.add(Items.BEEF)
				.add(Items.PORKCHOP)
				.add(Items.CHICKEN)
				.add(Items.RABBIT)
				.add(Items.MUTTON);

		valueLookupBuilder(ConventionalItemTags.RAW_FISH_FOODS)
				.add(Items.COD)
				.add(Items.SALMON)
				.add(Items.TROPICAL_FISH)
				.add(Items.PUFFERFISH);

		valueLookupBuilder(ConventionalItemTags.COOKED_MEAT_FOODS)
				.add(Items.COOKED_BEEF)
				.add(Items.COOKED_PORKCHOP)
				.add(Items.COOKED_CHICKEN)
				.add(Items.COOKED_RABBIT)
				.add(Items.COOKED_MUTTON);

		valueLookupBuilder(ConventionalItemTags.COOKED_FISH_FOODS)
				.add(Items.COOKED_COD)
				.add(Items.COOKED_SALMON);

		valueLookupBuilder(ConventionalItemTags.SOUP_FOODS)
				.add(Items.BEETROOT_SOUP)
				.add(Items.MUSHROOM_STEW)
				.add(Items.RABBIT_STEW)
				.add(Items.SUSPICIOUS_STEW);

		valueLookupBuilder(ConventionalItemTags.CANDY_FOODS);

		valueLookupBuilder(ConventionalItemTags.PIE_FOODS)
				.add(Items.PUMPKIN_PIE);

		valueLookupBuilder(ConventionalItemTags.GOLDEN_FOODS)
				.add(Items.GOLDEN_APPLE)
				.add(Items.ENCHANTED_GOLDEN_APPLE)
				.add(Items.GOLDEN_CARROT);

		valueLookupBuilder(ConventionalItemTags.EDIBLE_WHEN_PLACED_FOODS)
				.add(Items.CAKE);

		valueLookupBuilder(ConventionalItemTags.FOOD_POISONING_FOODS)
				.add(Items.POISONOUS_POTATO)
				.add(Items.PUFFERFISH)
				.add(Items.SPIDER_EYE)
				.add(Items.CHICKEN)
				.add(Items.ROTTEN_FLESH);

		valueLookupBuilder(ConventionalItemTags.ANIMAL_FOODS)
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

		valueLookupBuilder(ConventionalItemTags.FOODS)
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

		valueLookupBuilder(ConventionalItemTags.DRINKS)
				.addOptionalTag(ConventionalItemTags.WATER_DRINKS)
				.addOptionalTag(ConventionalItemTags.WATERY_DRINKS)
				.addOptionalTag(ConventionalItemTags.MILK_DRINKS)
				.addOptionalTag(ConventionalItemTags.HONEY_DRINKS)
				.addOptionalTag(ConventionalItemTags.MAGIC_DRINKS)
				.addOptionalTag(ConventionalItemTags.OMINOUS_DRINKS)
				.addOptionalTag(ConventionalItemTags.JUICE_DRINKS);

		valueLookupBuilder(ConventionalItemTags.WATER_DRINKS);

		valueLookupBuilder(ConventionalItemTags.WATERY_DRINKS)
				.add(Items.POTION)
				.addOptionalTag(ConventionalItemTags.WATER_DRINKS);

		valueLookupBuilder(ConventionalItemTags.MILK_DRINKS)
				.add(Items.MILK_BUCKET);

		valueLookupBuilder(ConventionalItemTags.HONEY_DRINKS)
				.add(Items.HONEY_BOTTLE);

		valueLookupBuilder(ConventionalItemTags.MAGIC_DRINKS)
				.add(Items.POTION)
				.addOptionalTag(ConventionalItemTags.OMINOUS_DRINKS);

		valueLookupBuilder(ConventionalItemTags.OMINOUS_DRINKS)
				.add(Items.OMINOUS_BOTTLE);

		valueLookupBuilder(ConventionalItemTags.JUICE_DRINKS);

		valueLookupBuilder(ConventionalItemTags.DRINK_CONTAINING_BUCKET)
				.add(Items.MILK_BUCKET);

		valueLookupBuilder(ConventionalItemTags.DRINK_CONTAINING_BOTTLE)
				.add(Items.POTION)
				.add(Items.HONEY_BOTTLE)
				.add(Items.OMINOUS_BOTTLE);
	}

	private void generateBucketTags() {
		valueLookupBuilder(ConventionalItemTags.EMPTY_BUCKETS)
				.add(Items.BUCKET);
		valueLookupBuilder(ConventionalItemTags.LAVA_BUCKETS)
				.add(Items.LAVA_BUCKET);
		valueLookupBuilder(ConventionalItemTags.ENTITY_WATER_BUCKETS)
				.add(Items.AXOLOTL_BUCKET)
				.add(Items.COD_BUCKET)
				.add(Items.PUFFERFISH_BUCKET)
				.add(Items.TADPOLE_BUCKET)
				.add(Items.TROPICAL_FISH_BUCKET)
				.add(Items.SALMON_BUCKET);
		valueLookupBuilder(ConventionalItemTags.WATER_BUCKETS)
				.add(Items.WATER_BUCKET);
		valueLookupBuilder(ConventionalItemTags.MILK_BUCKETS)
				.add(Items.MILK_BUCKET);
		valueLookupBuilder(ConventionalItemTags.POWDER_SNOW_BUCKETS)
				.add(Items.POWDER_SNOW_BUCKET);
		valueLookupBuilder(ConventionalItemTags.BUCKETS)
				.addOptionalTag(ConventionalItemTags.EMPTY_BUCKETS)
				.addOptionalTag(ConventionalItemTags.WATER_BUCKETS)
				.addOptionalTag(ConventionalItemTags.LAVA_BUCKETS)
				.addOptionalTag(ConventionalItemTags.MILK_BUCKETS)
				.addOptionalTag(ConventionalItemTags.POWDER_SNOW_BUCKETS)
				.addOptionalTag(ConventionalItemTags.ENTITY_WATER_BUCKETS);
	}

	private void generateOreAndRelatedTags() {
		// Categories
		valueLookupBuilder(ConventionalItemTags.BRICKS)
				.addOptionalTag(ConventionalItemTags.NORMAL_BRICKS)
				.addOptionalTag(ConventionalItemTags.NETHER_BRICKS)
				.addOptionalTag(ConventionalItemTags.RESIN_BRICKS);
		valueLookupBuilder(ConventionalItemTags.DUSTS)
				.addOptionalTag(ConventionalItemTags.GLOWSTONE_DUSTS)
				.addOptionalTag(ConventionalItemTags.REDSTONE_DUSTS);
		valueLookupBuilder(ConventionalItemTags.CLUMPS)
				.addOptionalTag(ConventionalItemTags.RESIN_CLUMPS);
		valueLookupBuilder(ConventionalItemTags.GEMS)
				.addOptionalTag(ConventionalItemTags.AMETHYST_GEMS)
				.addOptionalTag(ConventionalItemTags.DIAMOND_GEMS)
				.addOptionalTag(ConventionalItemTags.EMERALD_GEMS)
				.addOptionalTag(ConventionalItemTags.LAPIS_GEMS)
				.addOptionalTag(ConventionalItemTags.PRISMARINE_GEMS)
				.addOptionalTag(ConventionalItemTags.QUARTZ_GEMS);
		valueLookupBuilder(ConventionalItemTags.INGOTS)
				.addOptionalTag(ConventionalItemTags.COPPER_INGOTS)
				.addOptionalTag(ConventionalItemTags.IRON_INGOTS)
				.addOptionalTag(ConventionalItemTags.GOLD_INGOTS)
				.addOptionalTag(ConventionalItemTags.NETHERITE_INGOTS);
		valueLookupBuilder(ConventionalItemTags.NUGGETS)
				.addOptionalTag(ConventionalItemTags.COPPER_NUGGETS)
				.addOptionalTag(ConventionalItemTags.IRON_NUGGETS)
				.addOptionalTag(ConventionalItemTags.GOLD_NUGGETS);
		copy(ConventionalBlockTags.ORES, ConventionalItemTags.ORES);
		valueLookupBuilder(ConventionalItemTags.RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.COPPER_RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.GOLD_RAW_MATERIALS)
				.addOptionalTag(ConventionalItemTags.IRON_RAW_MATERIALS);

		// Vanilla instances
		valueLookupBuilder(ConventionalItemTags.NORMAL_BRICKS)
				.add(Items.BRICK);
		valueLookupBuilder(ConventionalItemTags.NETHER_BRICKS)
				.add(Items.NETHER_BRICK);
		valueLookupBuilder(ConventionalItemTags.RESIN_BRICKS)
				.add(Items.RESIN_BRICK);

		valueLookupBuilder(ConventionalItemTags.IRON_INGOTS)
				.add(Items.IRON_INGOT);
		valueLookupBuilder(ConventionalItemTags.COPPER_INGOTS)
				.add(Items.COPPER_INGOT);
		valueLookupBuilder(ConventionalItemTags.GOLD_INGOTS)
				.add(Items.GOLD_INGOT);
		valueLookupBuilder(ConventionalItemTags.NETHERITE_INGOTS)
				.add(Items.NETHERITE_INGOT);

		valueLookupBuilder(ConventionalItemTags.IRON_RAW_MATERIALS)
				.add(Items.RAW_IRON);
		valueLookupBuilder(ConventionalItemTags.COPPER_RAW_MATERIALS)
				.add(Items.RAW_COPPER);
		valueLookupBuilder(ConventionalItemTags.GOLD_RAW_MATERIALS)
				.add(Items.RAW_GOLD);

		valueLookupBuilder(ConventionalItemTags.REDSTONE_DUSTS)
				.add(Items.REDSTONE);
		valueLookupBuilder(ConventionalItemTags.GLOWSTONE_DUSTS)
				.add(Items.GLOWSTONE_DUST);

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

		valueLookupBuilder(ConventionalItemTags.RESIN_CLUMPS)
				.add(Items.RESIN_CLUMP);

		valueLookupBuilder(ConventionalItemTags.QUARTZ_GEMS)
				.add(Items.QUARTZ);
		valueLookupBuilder(ConventionalItemTags.EMERALD_GEMS)
				.add(Items.EMERALD);
		valueLookupBuilder(ConventionalItemTags.LAPIS_GEMS)
				.add(Items.LAPIS_LAZULI);
		valueLookupBuilder(ConventionalItemTags.DIAMOND_GEMS)
				.add(Items.DIAMOND);
		valueLookupBuilder(ConventionalItemTags.AMETHYST_GEMS)
				.add(Items.AMETHYST_SHARD);
		valueLookupBuilder(ConventionalItemTags.PRISMARINE_GEMS)
				.add(Items.PRISMARINE_CRYSTALS);

		valueLookupBuilder(ConventionalItemTags.COPPER_NUGGETS)
				.add(Items.COPPER_NUGGET);
		valueLookupBuilder(ConventionalItemTags.IRON_NUGGETS)
				.add(Items.IRON_NUGGET);
		valueLookupBuilder(ConventionalItemTags.GOLD_NUGGETS)
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
		valueLookupBuilder(ConventionalItemTags.TOOLS)
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

		valueLookupBuilder(ConventionalItemTags.BOW_TOOLS)
				.add(Items.BOW);
		valueLookupBuilder(ConventionalItemTags.CROSSBOW_TOOLS)
				.add(Items.CROSSBOW);
		valueLookupBuilder(ConventionalItemTags.SHEAR_TOOLS)
				.add(Items.SHEARS);
		valueLookupBuilder(ConventionalItemTags.SHIELD_TOOLS)
				.add(Items.SHIELD);
		valueLookupBuilder(ConventionalItemTags.TRIDENT_TOOLS)
				.add(Items.TRIDENT);
		valueLookupBuilder(ConventionalItemTags.FISHING_ROD_TOOLS)
				.add(Items.FISHING_ROD);
		valueLookupBuilder(ConventionalItemTags.BRUSH_TOOLS)
				.add(Items.BRUSH);
		valueLookupBuilder(ConventionalItemTags.IGNITER_TOOLS)
				.add(Items.FLINT_AND_STEEL);
		valueLookupBuilder(ConventionalItemTags.MACE_TOOLS)
				.add(Items.MACE);
		valueLookupBuilder(ConventionalItemTags.WRENCH_TOOLS);

		valueLookupBuilder(ConventionalItemTags.MINING_TOOL_TOOLS)
				.add(Items.WOODEN_PICKAXE)
				.add(Items.STONE_PICKAXE)
				.add(Items.COPPER_PICKAXE)
				.add(Items.GOLDEN_PICKAXE)
				.add(Items.IRON_PICKAXE)
				.add(Items.DIAMOND_PICKAXE)
				.add(Items.NETHERITE_PICKAXE);

		valueLookupBuilder(ConventionalItemTags.MELEE_WEAPON_TOOLS)
				.add(Items.MACE)
				.add(Items.TRIDENT)
				.add(Items.WOODEN_SWORD)
				.add(Items.STONE_SWORD)
				.add(Items.COPPER_SWORD)
				.add(Items.GOLDEN_SWORD)
				.add(Items.IRON_SWORD)
				.add(Items.DIAMOND_SWORD)
				.add(Items.NETHERITE_SWORD)
				.add(Items.WOODEN_AXE)
				.add(Items.STONE_AXE)
				.add(Items.COPPER_AXE)
				.add(Items.GOLDEN_AXE)
				.add(Items.IRON_AXE)
				.add(Items.DIAMOND_AXE)
				.add(Items.NETHERITE_AXE)
				.add(Items.WOODEN_SPEAR)
				.add(Items.STONE_SPEAR)
				.add(Items.COPPER_SPEAR)
				.add(Items.IRON_SPEAR)
				.add(Items.GOLDEN_SPEAR)
				.add(Items.DIAMOND_SPEAR)
				.add(Items.NETHERITE_SPEAR);

		valueLookupBuilder(ConventionalItemTags.RANGED_WEAPON_TOOLS)
				.add(Items.BOW)
				.add(Items.CROSSBOW)
				.add(Items.TRIDENT);

		valueLookupBuilder(ConventionalItemTags.ARMORS)
				.addOptionalTag(ConventionalItemTags.HUMANOID_ARMORS)
				.addOptionalTag(ConventionalItemTags.HORSE_ARMORS)
				.addOptionalTag(ConventionalItemTags.NAUTILUS_ARMORS)
				.addOptionalTag(ConventionalItemTags.WOLF_ARMORS);

		valueLookupBuilder(ConventionalItemTags.HORSE_ARMORS)
				.add(Items.LEATHER_HORSE_ARMOR)
				.add(Items.COPPER_HORSE_ARMOR)
				.add(Items.IRON_HORSE_ARMOR)
				.add(Items.GOLDEN_HORSE_ARMOR)
				.add(Items.DIAMOND_HORSE_ARMOR)
				.add(Items.NETHERITE_HORSE_ARMOR);

		valueLookupBuilder(ConventionalItemTags.NAUTILUS_ARMORS)
				.add(Items.COPPER_NAUTILUS_ARMOR)
				.add(Items.IRON_NAUTILUS_ARMOR)
				.add(Items.GOLDEN_NAUTILUS_ARMOR)
				.add(Items.DIAMOND_NAUTILUS_ARMOR)
				.add(Items.NETHERITE_NAUTILUS_ARMOR);

		valueLookupBuilder(ConventionalItemTags.WOLF_ARMORS)
				.add(Items.WOLF_ARMOR);

		valueLookupBuilder(ConventionalItemTags.HUMANOID_ARMORS)
				.addOptionalTag(ItemTags.HEAD_ARMOR)
				.addOptionalTag(ItemTags.CHEST_ARMOR)
				.addOptionalTag(ItemTags.LEG_ARMOR)
				.addOptionalTag(ItemTags.FOOT_ARMOR);

		valueLookupBuilder(ConventionalItemTags.ENCHANTABLES)
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
		BlockTagsGenerator.VILLAGER_JOB_SITE_BLOCKS.stream()
				.map(ItemLike::asItem)
				.distinct() // cauldron blocks have the same item
				.forEach(valueLookupBuilder(ConventionalItemTags.VILLAGER_JOB_SITES)::add);
	}

	private void generateCropAndSeedsTags() {
		valueLookupBuilder(ConventionalItemTags.CROPS)
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

		valueLookupBuilder(ConventionalItemTags.BEETROOT_CROPS)
				.add(Items.BEETROOT);
		valueLookupBuilder(ConventionalItemTags.CACTUS_CROPS)
				.add(Items.CACTUS);
		valueLookupBuilder(ConventionalItemTags.CARROT_CROPS)
				.add(Items.CARROT);
		valueLookupBuilder(ConventionalItemTags.COCOA_BEAN_CROPS)
				.add(Items.COCOA_BEANS);
		valueLookupBuilder(ConventionalItemTags.MELON_CROPS)
				.add(Items.MELON);
		valueLookupBuilder(ConventionalItemTags.NETHER_WART_CROPS)
				.add(Items.NETHER_WART);
		valueLookupBuilder(ConventionalItemTags.POTATO_CROPS)
				.add(Items.POTATO);
		valueLookupBuilder(ConventionalItemTags.PUMPKIN_CROPS)
				.add(Items.PUMPKIN);
		valueLookupBuilder(ConventionalItemTags.SUGAR_CANE_CROPS)
				.add(Items.SUGAR_CANE);
		valueLookupBuilder(ConventionalItemTags.WHEAT_CROPS)
				.add(Items.WHEAT);

		valueLookupBuilder(ConventionalItemTags.SEEDS)
				.addOptionalTag(ConventionalItemTags.BEETROOT_SEEDS)
				.addOptionalTag(ConventionalItemTags.MELON_SEEDS)
				.addOptionalTag(ConventionalItemTags.PUMPKIN_SEEDS)
				.addOptionalTag(ConventionalItemTags.TORCHFLOWER_SEEDS)
				.addOptionalTag(ConventionalItemTags.PITCHER_PLANT_SEEDS)
				.addOptionalTag(ConventionalItemTags.WHEAT_SEEDS);
		valueLookupBuilder(ConventionalItemTags.BEETROOT_SEEDS)
				.add(Items.BEETROOT_SEEDS);
		valueLookupBuilder(ConventionalItemTags.MELON_SEEDS)
				.add(Items.MELON_SEEDS);
		valueLookupBuilder(ConventionalItemTags.PUMPKIN_SEEDS)
				.add(Items.PUMPKIN_SEEDS);
		valueLookupBuilder(ConventionalItemTags.TORCHFLOWER_SEEDS)
				.add(Items.TORCHFLOWER_SEEDS);
		valueLookupBuilder(ConventionalItemTags.PITCHER_PLANT_SEEDS)
				.add(Items.PITCHER_POD);
		valueLookupBuilder(ConventionalItemTags.WHEAT_SEEDS)
				.add(Items.WHEAT_SEEDS);
	}

	private void generateFlowerTags() {
		copy(ConventionalBlockTags.SMALL_FLOWERS, ConventionalItemTags.SMALL_FLOWERS);
		copy(ConventionalBlockTags.TALL_FLOWERS, ConventionalItemTags.TALL_FLOWERS);
		copy(ConventionalBlockTags.FLOWERS, ConventionalItemTags.FLOWERS);
	}

	private void generateOtherTags() {
		valueLookupBuilder(ConventionalItemTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
				.add(Items.CRAFTING_TABLE);

		valueLookupBuilder(ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES)
				.add(Items.FURNACE);

		valueLookupBuilder(ConventionalItemTags.STRINGS)
				.add(Items.STRING);

		valueLookupBuilder(ConventionalItemTags.LEATHERS)
				.add(Items.LEATHER);

		valueLookupBuilder(ConventionalItemTags.BONES)
				.add(Items.BONE);

		valueLookupBuilder(ConventionalItemTags.EGGS)
				.add(Items.EGG, Items.BROWN_EGG, Items.BLUE_EGG);

		valueLookupBuilder(ConventionalItemTags.FEATHERS)
				.add(Items.FEATHER);

		valueLookupBuilder(ConventionalItemTags.GUNPOWDERS)
				.add(Items.GUNPOWDER);

		valueLookupBuilder(ConventionalItemTags.MUSHROOMS)
				.add(Items.RED_MUSHROOM)
				.add(Items.BROWN_MUSHROOM);

		valueLookupBuilder(ConventionalItemTags.NETHER_STARS)
				.add(Items.NETHER_STAR);

		valueLookupBuilder(ConventionalItemTags.MUSIC_DISCS)
				.add(Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR,
					Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD,
					Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_OTHERSIDE, Items.MUSIC_DISC_5, Items.MUSIC_DISC_PIGSTEP,
					Items.MUSIC_DISC_RELIC, Items.MUSIC_DISC_CREATOR, Items.MUSIC_DISC_CREATOR_MUSIC_BOX, Items.MUSIC_DISC_PRECIPICE,
					Items.MUSIC_DISC_TEARS, Items.MUSIC_DISC_LAVA_CHICKEN);

		valueLookupBuilder(ConventionalItemTags.WOODEN_RODS)
				.add(Items.STICK);

		valueLookupBuilder(ConventionalItemTags.BLAZE_RODS)
				.add(Items.BLAZE_ROD);

		valueLookupBuilder(ConventionalItemTags.BREEZE_RODS)
				.add(Items.BREEZE_ROD);

		valueLookupBuilder(ConventionalItemTags.RODS)
				.addOptionalTag(ConventionalItemTags.WOODEN_RODS)
				.addOptionalTag(ConventionalItemTags.BLAZE_RODS)
				.addOptionalTag(ConventionalItemTags.BREEZE_RODS);

		valueLookupBuilder(ConventionalItemTags.ROPES); // Generate tag so others can see it exists through JSON.

		TagAppender<Item, Item> chains = valueLookupBuilder(ConventionalItemTags.CHAINS)
				.add(Items.IRON_CHAIN);
		Items.COPPER_CHAIN.forEach(chains::add);

		valueLookupBuilder(ConventionalItemTags.ENDER_PEARLS)
				.add(Items.ENDER_PEARL);

		valueLookupBuilder(ConventionalItemTags.SLIME_BALLS)
				.add(Items.SLIME_BALL);

		valueLookupBuilder(ConventionalItemTags.FERTILIZERS)
				.add(Items.BONE_MEAL);

		valueLookupBuilder(ConventionalItemTags.HIDDEN_FROM_RECIPE_VIEWERS); // Generate tag so others can see it exists through JSON.
	}

	private void generateDyedTags() {
		// Cannot pull entries from block tag because Wall Banners do not have an item form
		valueLookupBuilder(ConventionalItemTags.BLACK_DYED)
				.add(Items.BANNER.black()).add(Items.BED.black()).add(Items.DYED_CANDLE.black()).add(Items.CARPET.black())
				.add(Items.CONCRETE.black()).add(Items.CONCRETE_POWDER.black()).add(Items.GLAZED_TERRACOTTA.black())
				.add(Items.DYED_SHULKER_BOX.black()).add(Items.STAINED_GLASS.black()).add(Items.STAINED_GLASS_PANE.black())
				.add(Items.DYED_TERRACOTTA.black()).add(Items.WOOL.black()).add(Items.DYED_BUNDLE.black()).add(Items.HARNESS.black());

		valueLookupBuilder(ConventionalItemTags.BLUE_DYED)
				.add(Items.BANNER.blue()).add(Items.BED.blue()).add(Items.DYED_CANDLE.blue()).add(Items.CARPET.blue())
				.add(Items.CONCRETE.blue()).add(Items.CONCRETE_POWDER.blue()).add(Items.GLAZED_TERRACOTTA.blue())
				.add(Items.DYED_SHULKER_BOX.blue()).add(Items.STAINED_GLASS.blue()).add(Items.STAINED_GLASS_PANE.blue())
				.add(Items.DYED_TERRACOTTA.blue()).add(Items.WOOL.blue()).add(Items.DYED_BUNDLE.blue()).add(Items.HARNESS.blue());

		valueLookupBuilder(ConventionalItemTags.BROWN_DYED)
				.add(Items.BANNER.brown()).add(Items.BED.brown()).add(Items.DYED_CANDLE.brown()).add(Items.CARPET.brown())
				.add(Items.CONCRETE.brown()).add(Items.CONCRETE_POWDER.brown()).add(Items.GLAZED_TERRACOTTA.brown())
				.add(Items.DYED_SHULKER_BOX.brown()).add(Items.STAINED_GLASS.brown()).add(Items.STAINED_GLASS_PANE.brown())
				.add(Items.DYED_TERRACOTTA.brown()).add(Items.WOOL.brown()).add(Items.DYED_BUNDLE.brown()).add(Items.HARNESS.brown());

		valueLookupBuilder(ConventionalItemTags.CYAN_DYED)
				.add(Items.BANNER.cyan()).add(Items.BED.cyan()).add(Items.DYED_CANDLE.cyan()).add(Items.CARPET.cyan())
				.add(Items.CONCRETE.cyan()).add(Items.CONCRETE_POWDER.cyan()).add(Items.GLAZED_TERRACOTTA.cyan())
				.add(Items.DYED_SHULKER_BOX.cyan()).add(Items.STAINED_GLASS.cyan()).add(Items.STAINED_GLASS_PANE.cyan())
				.add(Items.DYED_TERRACOTTA.cyan()).add(Items.WOOL.cyan()).add(Items.DYED_BUNDLE.cyan()).add(Items.HARNESS.cyan());

		valueLookupBuilder(ConventionalItemTags.GRAY_DYED)
				.add(Items.BANNER.gray()).add(Items.BED.gray()).add(Items.DYED_CANDLE.gray()).add(Items.CARPET.gray())
				.add(Items.CONCRETE.gray()).add(Items.CONCRETE_POWDER.gray()).add(Items.GLAZED_TERRACOTTA.gray())
				.add(Items.DYED_SHULKER_BOX.gray()).add(Items.STAINED_GLASS.gray()).add(Items.STAINED_GLASS_PANE.gray())
				.add(Items.DYED_TERRACOTTA.gray()).add(Items.WOOL.gray()).add(Items.DYED_BUNDLE.gray()).add(Items.HARNESS.gray());

		valueLookupBuilder(ConventionalItemTags.GREEN_DYED)
				.add(Items.BANNER.green()).add(Items.BED.green()).add(Items.DYED_CANDLE.green()).add(Items.CARPET.green())
				.add(Items.CONCRETE.green()).add(Items.CONCRETE_POWDER.green()).add(Items.GLAZED_TERRACOTTA.green())
				.add(Items.DYED_SHULKER_BOX.green()).add(Items.STAINED_GLASS.green()).add(Items.STAINED_GLASS_PANE.green())
				.add(Items.DYED_TERRACOTTA.green()).add(Items.WOOL.green()).add(Items.DYED_BUNDLE.green()).add(Items.HARNESS.green());

		valueLookupBuilder(ConventionalItemTags.LIGHT_BLUE_DYED)
				.add(Items.BANNER.lightBlue()).add(Items.BED.lightBlue()).add(Items.DYED_CANDLE.lightBlue()).add(Items.CARPET.lightBlue())
				.add(Items.CONCRETE.lightBlue()).add(Items.CONCRETE_POWDER.lightBlue()).add(Items.GLAZED_TERRACOTTA.lightBlue())
				.add(Items.DYED_SHULKER_BOX.lightBlue()).add(Items.STAINED_GLASS.lightBlue()).add(Items.STAINED_GLASS_PANE.lightBlue())
				.add(Items.DYED_TERRACOTTA.lightBlue()).add(Items.WOOL.lightBlue()).add(Items.DYED_BUNDLE.lightBlue()).add(Items.HARNESS.lightBlue());

		valueLookupBuilder(ConventionalItemTags.LIGHT_GRAY_DYED)
				.add(Items.BANNER.lightGray()).add(Items.BED.lightGray()).add(Items.DYED_CANDLE.lightGray()).add(Items.CARPET.lightGray())
				.add(Items.CONCRETE.lightGray()).add(Items.CONCRETE_POWDER.lightGray()).add(Items.GLAZED_TERRACOTTA.lightGray())
				.add(Items.DYED_SHULKER_BOX.lightGray()).add(Items.STAINED_GLASS.lightGray()).add(Items.STAINED_GLASS_PANE.lightGray())
				.add(Items.DYED_TERRACOTTA.lightGray()).add(Items.WOOL.lightGray()).add(Items.DYED_BUNDLE.lightGray()).add(Items.HARNESS.lightGray());

		valueLookupBuilder(ConventionalItemTags.LIME_DYED)
				.add(Items.BANNER.lime()).add(Items.BED.lime()).add(Items.DYED_CANDLE.lime()).add(Items.CARPET.lime())
				.add(Items.CONCRETE.lime()).add(Items.CONCRETE_POWDER.lime()).add(Items.GLAZED_TERRACOTTA.lime())
				.add(Items.DYED_SHULKER_BOX.lime()).add(Items.STAINED_GLASS.lime()).add(Items.STAINED_GLASS_PANE.lime())
				.add(Items.DYED_TERRACOTTA.lime()).add(Items.WOOL.lime()).add(Items.DYED_BUNDLE.lime()).add(Items.HARNESS.lime());

		valueLookupBuilder(ConventionalItemTags.MAGENTA_DYED)
				.add(Items.BANNER.magenta()).add(Items.BED.magenta()).add(Items.DYED_CANDLE.magenta()).add(Items.CARPET.magenta())
				.add(Items.CONCRETE.magenta()).add(Items.CONCRETE_POWDER.magenta()).add(Items.GLAZED_TERRACOTTA.magenta())
				.add(Items.DYED_SHULKER_BOX.magenta()).add(Items.STAINED_GLASS.magenta()).add(Items.STAINED_GLASS_PANE.magenta())
				.add(Items.DYED_TERRACOTTA.magenta()).add(Items.WOOL.magenta()).add(Items.DYED_BUNDLE.magenta()).add(Items.HARNESS.magenta());

		valueLookupBuilder(ConventionalItemTags.ORANGE_DYED)
				.add(Items.BANNER.orange()).add(Items.BED.orange()).add(Items.DYED_CANDLE.orange()).add(Items.CARPET.orange())
				.add(Items.CONCRETE.orange()).add(Items.CONCRETE_POWDER.orange()).add(Items.GLAZED_TERRACOTTA.orange())
				.add(Items.DYED_SHULKER_BOX.orange()).add(Items.STAINED_GLASS.orange()).add(Items.STAINED_GLASS_PANE.orange())
				.add(Items.DYED_TERRACOTTA.orange()).add(Items.WOOL.orange()).add(Items.DYED_BUNDLE.orange()).add(Items.HARNESS.orange());

		valueLookupBuilder(ConventionalItemTags.PINK_DYED)
				.add(Items.BANNER.pink()).add(Items.BED.pink()).add(Items.DYED_CANDLE.pink()).add(Items.CARPET.pink())
				.add(Items.CONCRETE.pink()).add(Items.CONCRETE_POWDER.pink()).add(Items.GLAZED_TERRACOTTA.pink())
				.add(Items.DYED_SHULKER_BOX.pink()).add(Items.STAINED_GLASS.pink()).add(Items.STAINED_GLASS_PANE.pink())
				.add(Items.DYED_TERRACOTTA.pink()).add(Items.WOOL.pink()).add(Items.DYED_BUNDLE.pink()).add(Items.HARNESS.pink());

		valueLookupBuilder(ConventionalItemTags.PURPLE_DYED)
				.add(Items.BANNER.purple()).add(Items.BED.purple()).add(Items.DYED_CANDLE.purple()).add(Items.CARPET.purple())
				.add(Items.CONCRETE.purple()).add(Items.CONCRETE_POWDER.purple()).add(Items.GLAZED_TERRACOTTA.purple())
				.add(Items.DYED_SHULKER_BOX.purple()).add(Items.STAINED_GLASS.purple()).add(Items.STAINED_GLASS_PANE.purple())
				.add(Items.DYED_TERRACOTTA.purple()).add(Items.WOOL.purple()).add(Items.DYED_BUNDLE.purple()).add(Items.HARNESS.purple());

		valueLookupBuilder(ConventionalItemTags.RED_DYED)
				.add(Items.BANNER.red()).add(Items.BED.red()).add(Items.DYED_CANDLE.red()).add(Items.CARPET.red())
				.add(Items.CONCRETE.red()).add(Items.CONCRETE_POWDER.red()).add(Items.GLAZED_TERRACOTTA.red())
				.add(Items.DYED_SHULKER_BOX.red()).add(Items.STAINED_GLASS.red()).add(Items.STAINED_GLASS_PANE.red())
				.add(Items.DYED_TERRACOTTA.red()).add(Items.WOOL.red()).add(Items.DYED_BUNDLE.red()).add(Items.HARNESS.red());

		valueLookupBuilder(ConventionalItemTags.WHITE_DYED)
				.add(Items.BANNER.white()).add(Items.BED.white()).add(Items.DYED_CANDLE.white()).add(Items.CARPET.white())
				.add(Items.CONCRETE.white()).add(Items.CONCRETE_POWDER.white()).add(Items.GLAZED_TERRACOTTA.white())
				.add(Items.DYED_SHULKER_BOX.white()).add(Items.STAINED_GLASS.white()).add(Items.STAINED_GLASS_PANE.white())
				.add(Items.DYED_TERRACOTTA.white()).add(Items.WOOL.white()).add(Items.DYED_BUNDLE.white()).add(Items.HARNESS.white());

		valueLookupBuilder(ConventionalItemTags.YELLOW_DYED)
				.add(Items.BANNER.yellow()).add(Items.BED.yellow()).add(Items.DYED_CANDLE.yellow()).add(Items.CARPET.yellow())
				.add(Items.CONCRETE.yellow()).add(Items.CONCRETE_POWDER.yellow()).add(Items.GLAZED_TERRACOTTA.yellow())
				.add(Items.DYED_SHULKER_BOX.yellow()).add(Items.STAINED_GLASS.yellow()).add(Items.STAINED_GLASS_PANE.yellow())
				.add(Items.DYED_TERRACOTTA.yellow()).add(Items.WOOL.yellow()).add(Items.DYED_BUNDLE.yellow()).add(Items.HARNESS.yellow());

		valueLookupBuilder(ConventionalItemTags.DYED)
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
		aliasGroup("dyes").add(ItemTags.DYES, ConventionalItemTags.DYES);
	}
}
