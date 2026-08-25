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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.BlockItemTagAppender;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ColorCollection;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockItemTags;
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
		generateDyeableTags();
		generateCropAndSeedsTags();
		generateVillagerJobSites();
		generateFlowerTags();
		generateOtherTags();
		copyItemTags();
		generateTagAlias();
	}

	private void copyItemTags() {
		copy(ConventionalBlockItemTags.STONES);
		copy(ConventionalBlockItemTags.COBBLESTONES);
		copy(ConventionalBlockItemTags.NORMAL_COBBLESTONES);
		copy(ConventionalBlockItemTags.MOSSY_COBBLESTONES);
		copy(ConventionalBlockItemTags.INFESTED_COBBLESTONES);
		copy(ConventionalBlockItemTags.DEEPSLATE_COBBLESTONES);
		copy(ConventionalBlockItemTags.NETHERRACKS);
		copy(ConventionalBlockItemTags.END_STONES);
		copy(ConventionalBlockItemTags.GRAVELS);
		copy(ConventionalBlockItemTags.OBSIDIANS);
		copy(ConventionalBlockItemTags.NORMAL_OBSIDIANS);
		copy(ConventionalBlockItemTags.CRYING_OBSIDIANS);
		copy(ConventionalBlockItemTags.FROGLIGHTS);
		copy(ConventionalBlockItemTags.BARRELS);
		copy(ConventionalBlockItemTags.WOODEN_BARRELS);
		copy(ConventionalBlockItemTags.BOOKSHELVES);
		copy(ConventionalBlockItemTags.CHESTS);
		copy(ConventionalBlockItemTags.WOODEN_CHESTS);
		copy(ConventionalBlockItemTags.TRAPPED_CHESTS);
		copy(ConventionalBlockItemTags.ENDER_CHESTS);
		copy(ConventionalBlockItemTags.GLASS_BLOCKS);
		copy(ConventionalBlockItemTags.GLASS_BLOCKS_COLORLESS);
		copy(ConventionalBlockItemTags.GLASS_BLOCKS_TINTED);
		copy(ConventionalBlockItemTags.GLASS_BLOCKS_CHEAP);
		copy(ConventionalBlockItemTags.GLASS_PANES);
		copy(ConventionalBlockItemTags.GLASS_PANES_COLORLESS);
		builder(ConventionalItemTags.SHULKER_BOXES)
				.add(BlockItemIds.SHULKER_BOX)
				.addAll(BlockItemIds.DYED_SHULKER_BOX.asList().stream().map(BlockItemId::item));
		copy(ConventionalBlockItemTags.GLAZED_TERRACOTTAS);
		copy(ConventionalBlockItemTags.CONCRETES);
		builder(ConventionalItemTags.CONCRETE_POWDERS)
				.addAll(BlockItemIds.CONCRETE_POWDER.asList().stream().map(BlockItemId::item));

		copy(ConventionalBlockItemTags.BUDDING_BLOCKS);
		copy(ConventionalBlockItemTags.BUDS);
		copy(ConventionalBlockItemTags.CLUSTERS);

		copy(ConventionalBlockItemTags.COLORLESS_SANDS);
		copy(ConventionalBlockItemTags.RED_SANDS);
		copy(ConventionalBlockItemTags.SANDS);

		copy(ConventionalBlockItemTags.SANDSTONE_BLOCKS);
		copy(ConventionalBlockItemTags.SANDSTONE_SLABS);
		copy(ConventionalBlockItemTags.SANDSTONE_STAIRS);
		copy(ConventionalBlockItemTags.RED_SANDSTONE_BLOCKS);
		copy(ConventionalBlockItemTags.RED_SANDSTONE_SLABS);
		copy(ConventionalBlockItemTags.RED_SANDSTONE_STAIRS);
		copy(ConventionalBlockItemTags.UNCOLORED_SANDSTONE_BLOCKS);
		copy(ConventionalBlockItemTags.UNCOLORED_SANDSTONE_SLABS);
		copy(ConventionalBlockItemTags.UNCOLORED_SANDSTONE_STAIRS);

		copy(ConventionalBlockItemTags.STORAGE_BLOCKS);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_BONE_MEAL);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_COAL);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_COPPER);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_DIAMOND);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_DRIED_KELP);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_EMERALD);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_GOLD);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_IRON);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_LAPIS);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_NETHERITE);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_RAW_COPPER);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_RAW_GOLD);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_RAW_IRON);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_REDSTONE);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_RESIN);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_SLIME);
		copy(ConventionalBlockItemTags.STORAGE_BLOCKS_WHEAT);

		copy(ConventionalBlockItemTags.OVERWORLD_NATURAL_LOGS);
		copy(ConventionalBlockItemTags.NETHER_NATURAL_LOGS);
		copy(ConventionalBlockItemTags.NATURAL_LOGS);
		copy(ConventionalBlockItemTags.NATURAL_WOODS);
		copy(ConventionalBlockItemTags.STRIPPED_LOGS);
		copy(ConventionalBlockItemTags.STRIPPED_WOODS);
		copy(ConventionalBlockItemTags.FENCES);
		copy(ConventionalBlockItemTags.WOODEN_FENCES);
		copy(ConventionalBlockItemTags.NETHER_BRICK_FENCES);
		copy(ConventionalBlockItemTags.FENCE_GATES);
		copy(ConventionalBlockItemTags.WOODEN_FENCE_GATES);

		copy(ConventionalBlockItemTags.BARS);
		copy(ConventionalBlockItemTags.IRON_BARS);
		copy(ConventionalBlockItemTags.COPPER_BARS);

		copy(ConventionalBlockItemTags.PUMPKINS);
		copy(ConventionalBlockItemTags.NORMAL_PUMPKINS);
		copy(ConventionalBlockItemTags.CARVED_PUMPKINS);
		copy(ConventionalBlockItemTags.JACK_O_LANTERNS_PUMPKINS);
	}

	private void generateDyeTags() {
		ColorCollection<BlockItemTagAppender<Item>> builders = ConventionalItemTags.COLOR_DYES.map(this::builder);

		ColorCollection.zipApply(builders, ItemIds.DYE, BlockItemTagAppender::add);

		builder(ConventionalItemTags.DYES)
				.addTag(ConventionalItemTags.WHITE_DYES)
				.addTag(ConventionalItemTags.ORANGE_DYES)
				.addTag(ConventionalItemTags.MAGENTA_DYES)
				.addTag(ConventionalItemTags.LIGHT_BLUE_DYES)
				.addTag(ConventionalItemTags.YELLOW_DYES)
				.addTag(ConventionalItemTags.LIME_DYES)
				.addTag(ConventionalItemTags.PINK_DYES)
				.addTag(ConventionalItemTags.GRAY_DYES)
				.addTag(ConventionalItemTags.LIGHT_GRAY_DYES)
				.addTag(ConventionalItemTags.CYAN_DYES)
				.addTag(ConventionalItemTags.PURPLE_DYES)
				.addTag(ConventionalItemTags.BLUE_DYES)
				.addTag(ConventionalItemTags.BROWN_DYES)
				.addTag(ConventionalItemTags.GREEN_DYES)
				.addTag(ConventionalItemTags.RED_DYES)
				.addTag(ConventionalItemTags.BLACK_DYES);
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
				.add(BlockItemIds.CARROT_CROP)
				.add(ItemIds.GOLDEN_CARROT)
				.add(BlockItemIds.POTATO_CROP)
				.add(ItemIds.BEETROOT);

		builder(ConventionalItemTags.BERRY_FOODS)
				.add(BlockItemIds.SWEET_BERRY_CROP)
				.add(BlockItemIds.GLOW_BERRY_CROP);

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
				.add(BlockItemIds.CAKE);

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
		builder(ConventionalItemTags.ENTITY_DRY_BUCKETS)
				.add(ItemIds.SULFUR_CUBE_BUCKET);
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
				.add(BlockItemIds.POWDER_SNOW);
		builder(ConventionalItemTags.BUCKETS)
				.addOptionalTag(ConventionalItemTags.EMPTY_BUCKETS)
				.addOptionalTag(ConventionalItemTags.WATER_BUCKETS)
				.addOptionalTag(ConventionalItemTags.LAVA_BUCKETS)
				.addOptionalTag(ConventionalItemTags.MILK_BUCKETS)
				.addOptionalTag(ConventionalItemTags.POWDER_SNOW_BUCKETS)
				.addOptionalTag(ConventionalItemTags.ENTITY_WATER_BUCKETS)
				.addOptionalTag(ConventionalItemTags.ENTITY_DRY_BUCKETS);
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
				.add(BlockItemIds.REDSTONE_DUST);
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
				.add(BlockItemIds.RESIN_CLUMP);

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
				.add(BlockItemIds.CACTUS);
		builder(ConventionalItemTags.CARROT_CROPS)
				.add(BlockItemIds.CARROT_CROP);
		builder(ConventionalItemTags.COCOA_BEAN_CROPS)
				.add(BlockItemIds.COCOA_CROP);
		builder(ConventionalItemTags.MELON_CROPS)
				.add(BlockItemIds.MELON);
		builder(ConventionalItemTags.NETHER_WART_CROPS)
				.add(BlockItemIds.NETHER_WART);
		builder(ConventionalItemTags.POTATO_CROPS)
				.add(BlockItemIds.POTATO_CROP);
		builder(ConventionalItemTags.PUMPKIN_CROPS)
				.add(BlockItemIds.PUMPKIN);
		builder(ConventionalItemTags.SUGAR_CANE_CROPS)
				.add(BlockItemIds.SUGAR_CANE);
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
				.add(BlockItemIds.BEETROOT_CROP);
		builder(ConventionalItemTags.MELON_SEEDS)
				.add(BlockItemIds.MELON_CROP);
		builder(ConventionalItemTags.PUMPKIN_SEEDS)
				.add(BlockItemIds.PUMPKIN_CROP);
		builder(ConventionalItemTags.TORCHFLOWER_SEEDS)
				.add(BlockItemIds.TORCHFLOWER_CROP);
		builder(ConventionalItemTags.PITCHER_PLANT_SEEDS)
				.add(BlockItemIds.PITCHER_CROP);
		builder(ConventionalItemTags.WHEAT_SEEDS)
				.add(BlockItemIds.WHEAT_CROP);
	}

	private void generateFlowerTags() {
		copy(ConventionalBlockItemTags.SMALL_FLOWERS);
		copy(ConventionalBlockItemTags.TALL_FLOWERS);
		copy(ConventionalBlockItemTags.FLOWERS);
	}

	private void generateOtherTags() {
		builder(ConventionalItemTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
				.add(BlockItemIds.CRAFTING_TABLE);

		builder(ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES)
				.add(BlockItemIds.FURNACE);

		builder(ConventionalItemTags.STRINGS)
				.add(BlockItemIds.TRIPWIRE);

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
				.addOptionalTag(ItemTags.MUSHROOMS);

		builder(ConventionalItemTags.NETHER_STARS)
				.add(ItemIds.NETHER_STAR);

		builder(ConventionalItemTags.MUSIC_DISCS)
				.add(ItemIds.MUSIC_DISC_13, ItemIds.MUSIC_DISC_CAT, ItemIds.MUSIC_DISC_BLOCKS, ItemIds.MUSIC_DISC_CHIRP, ItemIds.MUSIC_DISC_FAR,
					ItemIds.MUSIC_DISC_MALL, ItemIds.MUSIC_DISC_MELLOHI, ItemIds.MUSIC_DISC_STAL, ItemIds.MUSIC_DISC_STRAD, ItemIds.MUSIC_DISC_WARD,
					ItemIds.MUSIC_DISC_11, ItemIds.MUSIC_DISC_WAIT, ItemIds.MUSIC_DISC_OTHERSIDE, ItemIds.MUSIC_DISC_5, ItemIds.MUSIC_DISC_PIGSTEP,
					ItemIds.MUSIC_DISC_RELIC, ItemIds.MUSIC_DISC_CREATOR, ItemIds.MUSIC_DISC_CREATOR_MUSIC_BOX, ItemIds.MUSIC_DISC_PRECIPICE,
					ItemIds.MUSIC_DISC_TEARS, ItemIds.MUSIC_DISC_LAVA_CHICKEN, ItemIds.MUSIC_DISC_BOUNCE);

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
				.add(BlockItemIds.IRON_CHAIN);
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
		ColorCollection<BlockItemTagAppender<Item>> builders = ConventionalItemTags.COLOR_DYED.map(this::builder);

		for (ColorCollection<BlockItemId> colorCollection : List.of(
				BlockItemIds.BANNER, BlockItemIds.BED, BlockItemIds.DYED_CANDLE, BlockItemIds.CARPET,
				BlockItemIds.CONCRETE, BlockItemIds.CONCRETE_POWDER, BlockItemIds.GLAZED_TERRACOTTA,
				BlockItemIds.DYED_SHULKER_BOX, BlockItemIds.STAINED_GLASS, BlockItemIds.STAINED_GLASS_PANE,
				BlockItemIds.DYED_TERRACOTTA, BlockItemIds.WOOL, BlockItemIds.WOOL_SLAB, BlockItemIds.WOOL_STAIRS)) {
			ColorCollection.zipApply(builders, colorCollection, BlockItemTagAppender::add);
		}

		for (ColorCollection<ResourceKey<Item>> colorCollection : List.of(
				ItemIds.DYED_BUNDLE, ItemIds.CUSHION, ItemIds.HARNESS)) {
			ColorCollection.zipApply(builders, colorCollection, BlockItemTagAppender::add);
		}

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

	private void generateDyeableTags() {
		builder(ConventionalItemTags.UNDYED_SIMPLE_DYEABLE)
				.add(ItemIds.BUNDLE)
				.add(BlockItemIds.CANDLE)
				.add(BlockItemIds.GLASS)
				.add(BlockItemIds.GLASS_PANE)
				.add(BlockItemIds.SHULKER_BOX)
				.add(BlockItemIds.TERRACOTTA);

		builder(ConventionalItemTags.REDYEABLE_SIMPLE_DYEABLE)
				.addOptionalTag(BlockItemTags.BEDS.item())
				.addAll(ItemIds.DYED_BUNDLE)
				.addOptionalTag(ItemTags.CUSHIONS)
				.addOptionalTag(ItemTags.HARNESSES)
				.addOptionalTag(BlockItemTags.WOOL.item())
				.addOptionalTag(BlockItemTags.WOOL_CARPETS.item())
				.addOptionalTag(BlockItemTags.WOOL_SLABS.item())
				.addOptionalTag(BlockItemTags.WOOL_STAIRS.item())
				.addAll(BlockItemIds.DYED_SHULKER_BOX.map(BlockItemId::item));

		builder(ConventionalItemTags.SIMPLE_DYEABLE)
				.addTag(ConventionalItemTags.UNDYED_SIMPLE_DYEABLE)
				.addTag(ConventionalItemTags.REDYEABLE_SIMPLE_DYEABLE);

		builder(ConventionalItemTags.DYNAMIC_DYEABLE)
				.add(ItemIds.LEATHER_HELMET)
				.add(ItemIds.LEATHER_CHESTPLATE)
				.add(ItemIds.LEATHER_LEGGINGS)
				.add(ItemIds.LEATHER_BOOTS)
				.add(ItemIds.LEATHER_HORSE_ARMOR)
				.add(ItemIds.WOLF_ARMOR)
				.add(ItemIds.FIREWORK_STAR);

		builder(ConventionalItemTags.DYEABLE)
				.addTag(ConventionalItemTags.SIMPLE_DYEABLE)
				.addTag(ConventionalItemTags.DYNAMIC_DYEABLE);
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

		aliasGroup("bars").add(BlockItemTags.BARS.item(), ConventionalItemTags.BARS);

		aliasGroup("flowers/small").add(BlockItemTags.SMALL_FLOWERS.item(), ConventionalItemTags.SMALL_FLOWERS);
		aliasGroup("dyes").add(ItemTags.DYES, ConventionalItemTags.DYES);
	}
}
