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

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;

public final class BlockTagGenerator extends FabricTagProvider.BlockTagProvider {
	static List<Block> VILLAGER_JOB_SITE_BLOCKS = List.of(
			Blocks.BARREL,
			Blocks.BLAST_FURNACE,
			Blocks.BREWING_STAND,
			Blocks.CARTOGRAPHY_TABLE,
			Blocks.CAULDRON,
			Blocks.LAVA_CAULDRON,
			Blocks.WATER_CAULDRON,
			Blocks.POWDER_SNOW_CAULDRON,
			Blocks.COMPOSTER,
			Blocks.FLETCHING_TABLE,
			Blocks.GRINDSTONE,
			Blocks.LECTERN,
			Blocks.LOOM,
			Blocks.SMITHING_TABLE,
			Blocks.SMOKER,
			Blocks.STONECUTTER
	);

	public BlockTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup registries) {
		getOrCreateTagBuilder(ConventionalBlockTags.STONES)
				.add(Blocks.STONE)
				.add(Blocks.ANDESITE)
				.add(Blocks.DIORITE)
				.add(Blocks.GRANITE)
				.add(Blocks.TUFF)
				.add(Blocks.DEEPSLATE);
		getOrCreateTagBuilder(ConventionalBlockTags.COBBLESTONES)
				.add(Blocks.COBBLESTONE)
				.add(Blocks.MOSSY_COBBLESTONE)
				.add(Blocks.COBBLED_DEEPSLATE)
				.add(Blocks.INFESTED_COBBLESTONE);
		getOrCreateTagBuilder(ConventionalBlockTags.NORMAL_OBSIDIANS)
				.add(Blocks.OBSIDIAN);
		getOrCreateTagBuilder(ConventionalBlockTags.CRYING_OBSIDIANS)
				.add(Blocks.CRYING_OBSIDIAN);
		getOrCreateTagBuilder(ConventionalBlockTags.OBSIDIANS)
				.addOptionalTag(ConventionalBlockTags.NORMAL_OBSIDIANS)
				.addOptionalTag(ConventionalBlockTags.CRYING_OBSIDIANS);

		getOrCreateTagBuilder(ConventionalBlockTags.QUARTZ_ORES)
				.add(Blocks.NETHER_QUARTZ_ORE);
		getOrCreateTagBuilder(ConventionalBlockTags.NETHERITE_SCRAP_ORES)
				.add(Blocks.ANCIENT_DEBRIS);
		getOrCreateTagBuilder(ConventionalBlockTags.ORES)
				.addOptionalTag(BlockTags.COAL_ORES)
				.addOptionalTag(BlockTags.COPPER_ORES)
				.addOptionalTag(BlockTags.DIAMOND_ORES)
				.addOptionalTag(BlockTags.EMERALD_ORES)
				.addOptionalTag(BlockTags.GOLD_ORES)
				.addOptionalTag(BlockTags.IRON_ORES)
				.addOptionalTag(BlockTags.LAPIS_ORES)
				.addOptionalTag(ConventionalBlockTags.NETHERITE_SCRAP_ORES)
				.addOptionalTag(BlockTags.REDSTONE_ORES)
				.addOptionalTag(ConventionalBlockTags.QUARTZ_ORES);

		getOrCreateTagBuilder(ConventionalBlockTags.WOODEN_CHESTS)
				.add(Blocks.CHEST)
				.add(Blocks.TRAPPED_CHEST);
		getOrCreateTagBuilder(ConventionalBlockTags.CHESTS)
				.addTag(ConventionalBlockTags.WOODEN_CHESTS)
				.add(Blocks.ENDER_CHEST);
		getOrCreateTagBuilder(ConventionalBlockTags.BOOKSHELVES)
				.add(Blocks.BOOKSHELF);
		generateGlassTags();
		generateGlazeTerracottaTags();
		generateConcreteTags();
		getOrCreateTagBuilder(ConventionalBlockTags.WOODEN_BARRELS)
				.add(Blocks.BARREL);
		getOrCreateTagBuilder(ConventionalBlockTags.BARRELS)
				.addTag(ConventionalBlockTags.WOODEN_BARRELS);

		generateBuddingTags();

		generateSandstoneTags();

		generateDyedTags();

		generateStorageTags();

		generateLogTags();

		generateHeadTags();

		generateMiscTags();

		generateBackwardsCompatTags();
	}

	private void generateMiscTags() {
		getOrCreateTagBuilder(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
				.add(Blocks.CRAFTING_TABLE);
		getOrCreateTagBuilder(ConventionalBlockTags.PLAYER_WORKSTATIONS_FURNACES)
				.add(Blocks.FURNACE);

		VILLAGER_JOB_SITE_BLOCKS.forEach(getOrCreateTagBuilder(ConventionalBlockTags.VILLAGER_JOB_SITES)::add);

		getOrCreateTagBuilder(ConventionalBlockTags.RELOCATION_NOT_SUPPORTED); // Generate tag so others can see it exists through JSON.

		getOrCreateTagBuilder(ConventionalBlockTags.ROPES); // Generate tag so others can see it exists through JSON.

		getOrCreateTagBuilder(ConventionalBlockTags.CHAINS)
				.add(Blocks.CHAIN);

		getOrCreateTagBuilder(ConventionalBlockTags.HIDDEN_FROM_RECIPE_VIEWERS); // Generate tag so others can see it exists through JSON.
	}

	private void generateSandstoneTags() {
		getOrCreateTagBuilder(ConventionalBlockTags.SANDSTONE_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_BLOCKS);
		getOrCreateTagBuilder(ConventionalBlockTags.SANDSTONE_SLABS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_SLABS);
		getOrCreateTagBuilder(ConventionalBlockTags.SANDSTONE_STAIRS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_STAIRS);

		getOrCreateTagBuilder(ConventionalBlockTags.RED_SANDSTONE_BLOCKS)
				.add(Blocks.RED_SANDSTONE)
				.add(Blocks.CUT_RED_SANDSTONE)
				.add(Blocks.SMOOTH_RED_SANDSTONE)
				.add(Blocks.CHISELED_RED_SANDSTONE);
		getOrCreateTagBuilder(ConventionalBlockTags.RED_SANDSTONE_SLABS)
				.add(Blocks.RED_SANDSTONE_SLAB)
				.add(Blocks.CUT_RED_SANDSTONE_SLAB)
				.add(Blocks.SMOOTH_RED_SANDSTONE_SLAB);
		getOrCreateTagBuilder(ConventionalBlockTags.RED_SANDSTONE_STAIRS)
				.add(Blocks.RED_SANDSTONE_STAIRS)
				.add(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);

		getOrCreateTagBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS)
				.add(Blocks.SANDSTONE)
				.add(Blocks.CUT_SANDSTONE)
				.add(Blocks.SMOOTH_SANDSTONE)
				.add(Blocks.CHISELED_SANDSTONE);
		getOrCreateTagBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS)
				.add(Blocks.SANDSTONE_SLAB)
				.add(Blocks.CUT_SANDSTONE_SLAB)
				.add(Blocks.SMOOTH_SANDSTONE_SLAB);
		getOrCreateTagBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS)
				.add(Blocks.SANDSTONE_STAIRS)
				.add(Blocks.SMOOTH_SANDSTONE_STAIRS);
	}

	private void generateBuddingTags() {
		getOrCreateTagBuilder(ConventionalBlockTags.BUDDING_BLOCKS)
				.add(Blocks.BUDDING_AMETHYST);
		getOrCreateTagBuilder(ConventionalBlockTags.BUDS)
				.add(Blocks.SMALL_AMETHYST_BUD)
				.add(Blocks.MEDIUM_AMETHYST_BUD)
				.add(Blocks.LARGE_AMETHYST_BUD);
		getOrCreateTagBuilder(ConventionalBlockTags.CLUSTERS)
				.add(Blocks.AMETHYST_CLUSTER);
	}

	private void generateGlassTags() {
		getOrCreateTagBuilder(ConventionalBlockTags.GLASS_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_CHEAP)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_TINTED);
		getOrCreateTagBuilder(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS)
				.add(Blocks.GLASS);
		getOrCreateTagBuilder(ConventionalBlockTags.GLASS_BLOCKS_CHEAP)
				.add(Blocks.GLASS)
				.add(Blocks.WHITE_STAINED_GLASS)
				.add(Blocks.ORANGE_STAINED_GLASS)
				.add(Blocks.MAGENTA_STAINED_GLASS)
				.add(Blocks.LIGHT_BLUE_STAINED_GLASS)
				.add(Blocks.YELLOW_STAINED_GLASS)
				.add(Blocks.LIME_STAINED_GLASS)
				.add(Blocks.PINK_STAINED_GLASS)
				.add(Blocks.GRAY_STAINED_GLASS)
				.add(Blocks.LIGHT_GRAY_STAINED_GLASS)
				.add(Blocks.CYAN_STAINED_GLASS)
				.add(Blocks.PURPLE_STAINED_GLASS)
				.add(Blocks.BLUE_STAINED_GLASS)
				.add(Blocks.BROWN_STAINED_GLASS)
				.add(Blocks.GREEN_STAINED_GLASS)
				.add(Blocks.BLACK_STAINED_GLASS)
				.add(Blocks.RED_STAINED_GLASS);
		getOrCreateTagBuilder(ConventionalBlockTags.GLASS_BLOCKS_TINTED)
				.add(Blocks.TINTED_GLASS);
		getOrCreateTagBuilder(ConventionalBlockTags.GLASS_PANES)
				.add(Blocks.WHITE_STAINED_GLASS_PANE)
				.add(Blocks.ORANGE_STAINED_GLASS_PANE)
				.add(Blocks.MAGENTA_STAINED_GLASS_PANE)
				.add(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)
				.add(Blocks.YELLOW_STAINED_GLASS_PANE)
				.add(Blocks.LIME_STAINED_GLASS_PANE)
				.add(Blocks.PINK_STAINED_GLASS_PANE)
				.add(Blocks.GRAY_STAINED_GLASS_PANE)
				.add(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE)
				.add(Blocks.CYAN_STAINED_GLASS_PANE)
				.add(Blocks.PURPLE_STAINED_GLASS_PANE)
				.add(Blocks.BLUE_STAINED_GLASS_PANE)
				.add(Blocks.BROWN_STAINED_GLASS_PANE)
				.add(Blocks.GREEN_STAINED_GLASS_PANE)
				.add(Blocks.BLACK_STAINED_GLASS_PANE)
				.add(Blocks.RED_STAINED_GLASS_PANE)
				.addOptionalTag(ConventionalBlockTags.GLASS_PANES_COLORLESS);
		getOrCreateTagBuilder(ConventionalBlockTags.GLASS_PANES_COLORLESS)
				.add(Blocks.GLASS_PANE);
	}

	private void generateGlazeTerracottaTags() {
		getOrCreateTagBuilder(ConventionalBlockTags.GLAZED_TERRACOTTAS)
				.add(Blocks.WHITE_GLAZED_TERRACOTTA)
				.add(Blocks.ORANGE_GLAZED_TERRACOTTA)
				.add(Blocks.MAGENTA_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.YELLOW_GLAZED_TERRACOTTA)
				.add(Blocks.LIME_GLAZED_TERRACOTTA)
				.add(Blocks.PINK_GLAZED_TERRACOTTA)
				.add(Blocks.GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.CYAN_GLAZED_TERRACOTTA)
				.add(Blocks.PURPLE_GLAZED_TERRACOTTA)
				.add(Blocks.BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.BROWN_GLAZED_TERRACOTTA)
				.add(Blocks.GREEN_GLAZED_TERRACOTTA)
				.add(Blocks.BLACK_GLAZED_TERRACOTTA)
				.add(Blocks.RED_GLAZED_TERRACOTTA);
		getOrCreateTagBuilder(ConventionalBlockTags.GLAZED_TERRACOTTA)
				.add(Blocks.WHITE_GLAZED_TERRACOTTA)
				.add(Blocks.ORANGE_GLAZED_TERRACOTTA)
				.add(Blocks.MAGENTA_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.YELLOW_GLAZED_TERRACOTTA)
				.add(Blocks.LIME_GLAZED_TERRACOTTA)
				.add(Blocks.PINK_GLAZED_TERRACOTTA)
				.add(Blocks.GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.CYAN_GLAZED_TERRACOTTA)
				.add(Blocks.PURPLE_GLAZED_TERRACOTTA)
				.add(Blocks.BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.BROWN_GLAZED_TERRACOTTA)
				.add(Blocks.GREEN_GLAZED_TERRACOTTA)
				.add(Blocks.BLACK_GLAZED_TERRACOTTA)
				.add(Blocks.RED_GLAZED_TERRACOTTA);
	}

	private void generateConcreteTags() {
		getOrCreateTagBuilder(ConventionalBlockTags.CONCRETES)
				.add(Blocks.WHITE_CONCRETE)
				.add(Blocks.ORANGE_CONCRETE)
				.add(Blocks.MAGENTA_CONCRETE)
				.add(Blocks.LIGHT_BLUE_CONCRETE)
				.add(Blocks.YELLOW_CONCRETE)
				.add(Blocks.LIME_CONCRETE)
				.add(Blocks.PINK_CONCRETE)
				.add(Blocks.GRAY_CONCRETE)
				.add(Blocks.LIGHT_GRAY_CONCRETE)
				.add(Blocks.CYAN_CONCRETE)
				.add(Blocks.PURPLE_CONCRETE)
				.add(Blocks.BLUE_CONCRETE)
				.add(Blocks.BROWN_CONCRETE)
				.add(Blocks.GREEN_CONCRETE)
				.add(Blocks.BLACK_CONCRETE)
				.add(Blocks.RED_CONCRETE);
		getOrCreateTagBuilder(ConventionalBlockTags.CONCRETE)
				.add(Blocks.WHITE_CONCRETE)
				.add(Blocks.ORANGE_CONCRETE)
				.add(Blocks.MAGENTA_CONCRETE)
				.add(Blocks.LIGHT_BLUE_CONCRETE)
				.add(Blocks.YELLOW_CONCRETE)
				.add(Blocks.LIME_CONCRETE)
				.add(Blocks.PINK_CONCRETE)
				.add(Blocks.GRAY_CONCRETE)
				.add(Blocks.LIGHT_GRAY_CONCRETE)
				.add(Blocks.CYAN_CONCRETE)
				.add(Blocks.PURPLE_CONCRETE)
				.add(Blocks.BLUE_CONCRETE)
				.add(Blocks.BROWN_CONCRETE)
				.add(Blocks.GREEN_CONCRETE)
				.add(Blocks.BLACK_CONCRETE)
				.add(Blocks.RED_CONCRETE);
	}

	private void generateDyedTags() {
		getOrCreateTagBuilder(ConventionalBlockTags.BLACK_DYED)
				.add(Blocks.BLACK_BANNER).add(Blocks.BLACK_BED).add(Blocks.BLACK_CANDLE).add(Blocks.BLACK_CARPET)
				.add(Blocks.BLACK_CONCRETE).add(Blocks.BLACK_CONCRETE_POWDER).add(Blocks.BLACK_GLAZED_TERRACOTTA)
				.add(Blocks.BLACK_SHULKER_BOX).add(Blocks.BLACK_STAINED_GLASS).add(Blocks.BLACK_STAINED_GLASS_PANE)
				.add(Blocks.BLACK_TERRACOTTA).add(Blocks.BLACK_WALL_BANNER).add(Blocks.BLACK_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.BLUE_DYED)
				.add(Blocks.BLUE_BANNER).add(Blocks.BLUE_BED).add(Blocks.BLUE_CANDLE).add(Blocks.BLUE_CARPET)
				.add(Blocks.BLUE_CONCRETE).add(Blocks.BLUE_CONCRETE_POWDER).add(Blocks.BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.BLUE_SHULKER_BOX).add(Blocks.BLUE_STAINED_GLASS).add(Blocks.BLUE_STAINED_GLASS_PANE)
				.add(Blocks.BLUE_TERRACOTTA).add(Blocks.BLUE_WALL_BANNER).add(Blocks.BLUE_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.BROWN_DYED)
				.add(Blocks.BROWN_BANNER).add(Blocks.BROWN_BED).add(Blocks.BROWN_CANDLE).add(Blocks.BROWN_CARPET)
				.add(Blocks.BROWN_CONCRETE).add(Blocks.BROWN_CONCRETE_POWDER).add(Blocks.BROWN_GLAZED_TERRACOTTA)
				.add(Blocks.BROWN_SHULKER_BOX).add(Blocks.BROWN_STAINED_GLASS).add(Blocks.BROWN_STAINED_GLASS_PANE)
				.add(Blocks.BROWN_TERRACOTTA).add(Blocks.BROWN_WALL_BANNER).add(Blocks.BROWN_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.CYAN_DYED)
				.add(Blocks.CYAN_BANNER).add(Blocks.CYAN_BED).add(Blocks.CYAN_CANDLE).add(Blocks.CYAN_CARPET)
				.add(Blocks.CYAN_CONCRETE).add(Blocks.CYAN_CONCRETE_POWDER).add(Blocks.CYAN_GLAZED_TERRACOTTA)
				.add(Blocks.CYAN_SHULKER_BOX).add(Blocks.CYAN_STAINED_GLASS).add(Blocks.CYAN_STAINED_GLASS_PANE)
				.add(Blocks.CYAN_TERRACOTTA).add(Blocks.CYAN_WALL_BANNER).add(Blocks.CYAN_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.GRAY_DYED)
				.add(Blocks.GRAY_BANNER).add(Blocks.GRAY_BED).add(Blocks.GRAY_CANDLE).add(Blocks.GRAY_CARPET)
				.add(Blocks.GRAY_CONCRETE).add(Blocks.GRAY_CONCRETE_POWDER).add(Blocks.GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.GRAY_SHULKER_BOX).add(Blocks.GRAY_STAINED_GLASS).add(Blocks.GRAY_STAINED_GLASS_PANE)
				.add(Blocks.GRAY_TERRACOTTA).add(Blocks.GRAY_WALL_BANNER).add(Blocks.GRAY_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.GREEN_DYED)
				.add(Blocks.GREEN_BANNER).add(Blocks.GREEN_BED).add(Blocks.GREEN_CANDLE).add(Blocks.GREEN_CARPET)
				.add(Blocks.GREEN_CONCRETE).add(Blocks.GREEN_CONCRETE_POWDER).add(Blocks.GREEN_GLAZED_TERRACOTTA)
				.add(Blocks.GREEN_SHULKER_BOX).add(Blocks.GREEN_STAINED_GLASS).add(Blocks.GREEN_STAINED_GLASS_PANE)
				.add(Blocks.GREEN_TERRACOTTA).add(Blocks.GREEN_WALL_BANNER).add(Blocks.GREEN_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.LIGHT_BLUE_DYED)
				.add(Blocks.LIGHT_BLUE_BANNER).add(Blocks.LIGHT_BLUE_BED).add(Blocks.LIGHT_BLUE_CANDLE).add(Blocks.LIGHT_BLUE_CARPET)
				.add(Blocks.LIGHT_BLUE_CONCRETE).add(Blocks.LIGHT_BLUE_CONCRETE_POWDER).add(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_BLUE_SHULKER_BOX).add(Blocks.LIGHT_BLUE_STAINED_GLASS).add(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)
				.add(Blocks.LIGHT_BLUE_TERRACOTTA).add(Blocks.LIGHT_BLUE_WALL_BANNER).add(Blocks.LIGHT_BLUE_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.LIGHT_GRAY_DYED)
				.add(Blocks.LIGHT_GRAY_BANNER).add(Blocks.LIGHT_GRAY_BED).add(Blocks.LIGHT_GRAY_CANDLE).add(Blocks.LIGHT_GRAY_CARPET)
				.add(Blocks.LIGHT_GRAY_CONCRETE).add(Blocks.LIGHT_GRAY_CONCRETE_POWDER).add(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_GRAY_SHULKER_BOX).add(Blocks.LIGHT_GRAY_STAINED_GLASS).add(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE)
				.add(Blocks.LIGHT_GRAY_TERRACOTTA).add(Blocks.LIGHT_GRAY_WALL_BANNER).add(Blocks.LIGHT_GRAY_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.LIME_DYED)
				.add(Blocks.LIME_BANNER).add(Blocks.LIME_BED).add(Blocks.LIME_CANDLE).add(Blocks.LIME_CARPET)
				.add(Blocks.LIME_CONCRETE).add(Blocks.LIME_CONCRETE_POWDER).add(Blocks.LIME_GLAZED_TERRACOTTA)
				.add(Blocks.LIME_SHULKER_BOX).add(Blocks.LIME_STAINED_GLASS).add(Blocks.LIME_STAINED_GLASS_PANE)
				.add(Blocks.LIME_TERRACOTTA).add(Blocks.LIME_WALL_BANNER).add(Blocks.LIME_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.MAGENTA_DYED)
				.add(Blocks.MAGENTA_BANNER).add(Blocks.MAGENTA_BED).add(Blocks.MAGENTA_CANDLE).add(Blocks.MAGENTA_CARPET)
				.add(Blocks.MAGENTA_CONCRETE).add(Blocks.MAGENTA_CONCRETE_POWDER).add(Blocks.MAGENTA_GLAZED_TERRACOTTA)
				.add(Blocks.MAGENTA_SHULKER_BOX).add(Blocks.MAGENTA_STAINED_GLASS).add(Blocks.MAGENTA_STAINED_GLASS_PANE)
				.add(Blocks.MAGENTA_TERRACOTTA).add(Blocks.MAGENTA_WALL_BANNER).add(Blocks.MAGENTA_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.ORANGE_DYED)
				.add(Blocks.ORANGE_BANNER).add(Blocks.ORANGE_BED).add(Blocks.ORANGE_CANDLE).add(Blocks.ORANGE_CARPET)
				.add(Blocks.ORANGE_CONCRETE).add(Blocks.ORANGE_CONCRETE_POWDER).add(Blocks.ORANGE_GLAZED_TERRACOTTA)
				.add(Blocks.ORANGE_SHULKER_BOX).add(Blocks.ORANGE_STAINED_GLASS).add(Blocks.ORANGE_STAINED_GLASS_PANE)
				.add(Blocks.ORANGE_TERRACOTTA).add(Blocks.ORANGE_WALL_BANNER).add(Blocks.ORANGE_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.PINK_DYED)
				.add(Blocks.PINK_BANNER).add(Blocks.PINK_BED).add(Blocks.PINK_CANDLE).add(Blocks.PINK_CARPET)
				.add(Blocks.PINK_CONCRETE).add(Blocks.PINK_CONCRETE_POWDER).add(Blocks.PINK_GLAZED_TERRACOTTA)
				.add(Blocks.PINK_SHULKER_BOX).add(Blocks.PINK_STAINED_GLASS).add(Blocks.PINK_STAINED_GLASS_PANE)
				.add(Blocks.PINK_TERRACOTTA).add(Blocks.PINK_WALL_BANNER).add(Blocks.PINK_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.PURPLE_DYED)
				.add(Blocks.PURPLE_BANNER).add(Blocks.PURPLE_BED).add(Blocks.PURPLE_CANDLE).add(Blocks.PURPLE_CARPET)
				.add(Blocks.PURPLE_CONCRETE).add(Blocks.PURPLE_CONCRETE_POWDER).add(Blocks.PURPLE_GLAZED_TERRACOTTA)
				.add(Blocks.PURPLE_SHULKER_BOX).add(Blocks.PURPLE_STAINED_GLASS).add(Blocks.PURPLE_STAINED_GLASS_PANE)
				.add(Blocks.PURPLE_TERRACOTTA).add(Blocks.PURPLE_WALL_BANNER).add(Blocks.PURPLE_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.RED_DYED)
				.add(Blocks.RED_BANNER).add(Blocks.RED_BED).add(Blocks.RED_CANDLE).add(Blocks.RED_CARPET)
				.add(Blocks.RED_CONCRETE).add(Blocks.RED_CONCRETE_POWDER).add(Blocks.RED_GLAZED_TERRACOTTA)
				.add(Blocks.RED_SHULKER_BOX).add(Blocks.RED_STAINED_GLASS).add(Blocks.RED_STAINED_GLASS_PANE)
				.add(Blocks.RED_TERRACOTTA).add(Blocks.RED_WALL_BANNER).add(Blocks.RED_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.WHITE_DYED)
				.add(Blocks.WHITE_BANNER).add(Blocks.WHITE_BED).add(Blocks.WHITE_CANDLE).add(Blocks.WHITE_CARPET)
				.add(Blocks.WHITE_CONCRETE).add(Blocks.WHITE_CONCRETE_POWDER).add(Blocks.WHITE_GLAZED_TERRACOTTA)
				.add(Blocks.WHITE_SHULKER_BOX).add(Blocks.WHITE_STAINED_GLASS).add(Blocks.WHITE_STAINED_GLASS_PANE)
				.add(Blocks.WHITE_TERRACOTTA).add(Blocks.WHITE_WALL_BANNER).add(Blocks.WHITE_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.YELLOW_DYED)
				.add(Blocks.YELLOW_BANNER).add(Blocks.YELLOW_BED).add(Blocks.YELLOW_CANDLE).add(Blocks.YELLOW_CARPET)
				.add(Blocks.YELLOW_CONCRETE).add(Blocks.YELLOW_CONCRETE_POWDER).add(Blocks.YELLOW_GLAZED_TERRACOTTA)
				.add(Blocks.YELLOW_SHULKER_BOX).add(Blocks.YELLOW_STAINED_GLASS).add(Blocks.YELLOW_STAINED_GLASS_PANE)
				.add(Blocks.YELLOW_TERRACOTTA).add(Blocks.YELLOW_WALL_BANNER).add(Blocks.YELLOW_WOOL);

		getOrCreateTagBuilder(ConventionalBlockTags.DYED)
				.addTag(ConventionalBlockTags.WHITE_DYED)
				.addTag(ConventionalBlockTags.ORANGE_DYED)
				.addTag(ConventionalBlockTags.MAGENTA_DYED)
				.addTag(ConventionalBlockTags.LIGHT_BLUE_DYED)
				.addTag(ConventionalBlockTags.YELLOW_DYED)
				.addTag(ConventionalBlockTags.LIME_DYED)
				.addTag(ConventionalBlockTags.PINK_DYED)
				.addTag(ConventionalBlockTags.GRAY_DYED)
				.addTag(ConventionalBlockTags.LIGHT_GRAY_DYED)
				.addTag(ConventionalBlockTags.CYAN_DYED)
				.addTag(ConventionalBlockTags.PURPLE_DYED)
				.addTag(ConventionalBlockTags.BLUE_DYED)
				.addTag(ConventionalBlockTags.BROWN_DYED)
				.addTag(ConventionalBlockTags.GREEN_DYED)
				.addTag(ConventionalBlockTags.RED_DYED)
				.addTag(ConventionalBlockTags.BLACK_DYED);
	}

	private void generateStorageTags() {
		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_BONE_MEAL)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_COAL)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_COPPER)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_DIAMOND)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_DRIED_KELP)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_EMERALD)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_GOLD)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_IRON)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_LAPIS)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_NETHERITE)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_COPPER)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_GOLD)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_REDSTONE)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_SLIME)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_WHEAT);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_BONE_MEAL)
				.add(Blocks.BONE_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_COAL)
				.add(Blocks.COAL_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_COPPER)
				.add(Blocks.COPPER_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_DIAMOND)
				.add(Blocks.DIAMOND_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_DRIED_KELP)
				.add(Blocks.DRIED_KELP_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_EMERALD)
				.add(Blocks.EMERALD_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_GOLD)
				.add(Blocks.GOLD_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_IRON)
				.add(Blocks.IRON_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_LAPIS)
				.add(Blocks.LAPIS_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_NETHERITE)
				.add(Blocks.NETHERITE_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_COPPER)
				.add(Blocks.RAW_COPPER_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_GOLD)
				.add(Blocks.RAW_GOLD_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON)
				.add(Blocks.RAW_IRON_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_REDSTONE)
				.add(Blocks.REDSTONE_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_SLIME)
				.add(Blocks.SLIME_BLOCK);

		getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_WHEAT)
				.add(Blocks.HAY_BLOCK);
	}

	private void generateLogTags() {
		getOrCreateTagBuilder(ConventionalBlockTags.STRIPPED_LOGS)
				.add(Blocks.STRIPPED_ACACIA_LOG)
				.add(Blocks.STRIPPED_BAMBOO_BLOCK)
				.add(Blocks.STRIPPED_BIRCH_LOG)
				.add(Blocks.STRIPPED_CHERRY_LOG)
				.add(Blocks.STRIPPED_DARK_OAK_LOG)
				.add(Blocks.STRIPPED_JUNGLE_LOG)
				.add(Blocks.STRIPPED_MANGROVE_LOG)
				.add(Blocks.STRIPPED_OAK_LOG)
				.add(Blocks.STRIPPED_SPRUCE_LOG);

		getOrCreateTagBuilder(ConventionalBlockTags.STRIPPED_WOODS)
				.add(Blocks.STRIPPED_ACACIA_WOOD)
				.add(Blocks.STRIPPED_BIRCH_WOOD)
				.add(Blocks.STRIPPED_CHERRY_WOOD)
				.add(Blocks.STRIPPED_DARK_OAK_WOOD)
				.add(Blocks.STRIPPED_JUNGLE_WOOD)
				.add(Blocks.STRIPPED_MANGROVE_WOOD)
				.add(Blocks.STRIPPED_OAK_WOOD)
				.add(Blocks.STRIPPED_SPRUCE_WOOD);
	}

	private void generateHeadTags() {
		getOrCreateTagBuilder(ConventionalBlockTags.SKULLS)
				.add(Blocks.SKELETON_SKULL)
				.add(Blocks.SKELETON_WALL_SKULL)
				.add(Blocks.WITHER_SKELETON_SKULL)
				.add(Blocks.WITHER_SKELETON_WALL_SKULL)
				.add(Blocks.PLAYER_HEAD)
				.add(Blocks.PLAYER_WALL_HEAD)
				.add(Blocks.ZOMBIE_HEAD)
				.add(Blocks.ZOMBIE_WALL_HEAD)
				.add(Blocks.CREEPER_HEAD)
				.add(Blocks.CREEPER_WALL_HEAD)
				.add(Blocks.PIGLIN_HEAD)
				.add(Blocks.PIGLIN_WALL_HEAD)
				.add(Blocks.DRAGON_HEAD)
				.add(Blocks.DRAGON_WALL_HEAD);
	}

	private void generateBackwardsCompatTags() {
		// Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
		// TODO: Remove backwards compat tag entries in 1.22

		getOrCreateTagBuilder(ConventionalBlockTags.RELOCATION_NOT_SUPPORTED).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "movement_restricted"));
		getOrCreateTagBuilder(ConventionalBlockTags.QUARTZ_ORES).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "quartz_ores"));
		getOrCreateTagBuilder(ConventionalBlockTags.WOODEN_BARRELS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "wooden_barrels"));
		getOrCreateTagBuilder(ConventionalBlockTags.WOODEN_CHESTS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "wooden_chests"));
		getOrCreateTagBuilder(ConventionalBlockTags.SANDSTONE_BLOCKS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "sandstone_blocks"));
		getOrCreateTagBuilder(ConventionalBlockTags.SANDSTONE_SLABS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "sandstone_slabs"));
		getOrCreateTagBuilder(ConventionalBlockTags.SANDSTONE_STAIRS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "sandstone_stairs"));
		getOrCreateTagBuilder(ConventionalBlockTags.RED_SANDSTONE_BLOCKS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "red_sandstone_blocks"));
		getOrCreateTagBuilder(ConventionalBlockTags.RED_SANDSTONE_SLABS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "red_sandstone_slabs"));
		getOrCreateTagBuilder(ConventionalBlockTags.RED_SANDSTONE_STAIRS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "red_sandstone_stairs"));
		getOrCreateTagBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "uncolored_sandstone_blocks"));
		getOrCreateTagBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "uncolored_sandstone_slabs"));
		getOrCreateTagBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "uncolored_sandstone_stairs"));
		getOrCreateTagBuilder(ConventionalBlockTags.GLAZED_TERRACOTTAS).addOptionalTag(ConventionalBlockTags.GLAZED_TERRACOTTA);
		getOrCreateTagBuilder(ConventionalBlockTags.CONCRETES).addOptionalTag(ConventionalBlockTags.CONCRETE);
	}
}
