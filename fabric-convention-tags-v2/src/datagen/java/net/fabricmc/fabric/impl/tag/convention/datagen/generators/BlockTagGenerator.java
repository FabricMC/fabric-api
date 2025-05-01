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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
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
		valueLookupBuilder(ConventionalBlockTags.STONES)
				.add(Blocks.STONE)
				.add(Blocks.ANDESITE)
				.add(Blocks.DIORITE)
				.add(Blocks.GRANITE)
				.add(Blocks.TUFF)
				.add(Blocks.DEEPSLATE);
		valueLookupBuilder(ConventionalBlockTags.NORMAL_COBBLESTONES)
				.add(Blocks.COBBLESTONE);
		valueLookupBuilder(ConventionalBlockTags.MOSSY_COBBLESTONES)
				.add(Blocks.MOSSY_COBBLESTONE);
		valueLookupBuilder(ConventionalBlockTags.INFESTED_COBBLESTONES)
				.add(Blocks.INFESTED_COBBLESTONE);
		valueLookupBuilder(ConventionalBlockTags.DEEPSLATE_COBBLESTONES)
				.add(Blocks.COBBLED_DEEPSLATE);
		valueLookupBuilder(ConventionalBlockTags.COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.NORMAL_COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.MOSSY_COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.INFESTED_COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.DEEPSLATE_COBBLESTONES);
		valueLookupBuilder(ConventionalBlockTags.NETHERRACKS)
				.add(Blocks.NETHERRACK);
		valueLookupBuilder(ConventionalBlockTags.END_STONES)
				.add(Blocks.END_STONE);
		valueLookupBuilder(ConventionalBlockTags.GRAVELS)
				.add(Blocks.GRAVEL);
		valueLookupBuilder(ConventionalBlockTags.NORMAL_OBSIDIANS)
				.add(Blocks.OBSIDIAN);
		valueLookupBuilder(ConventionalBlockTags.CRYING_OBSIDIANS)
				.add(Blocks.CRYING_OBSIDIAN);
		valueLookupBuilder(ConventionalBlockTags.OBSIDIANS)
				.addOptionalTag(ConventionalBlockTags.NORMAL_OBSIDIANS)
				.addOptionalTag(ConventionalBlockTags.CRYING_OBSIDIANS);

		valueLookupBuilder(ConventionalBlockTags.COAL_ORES)
				.addOptionalTag(BlockTags.COAL_ORES);
		valueLookupBuilder(ConventionalBlockTags.COPPER_ORES)
				.addOptionalTag(BlockTags.COPPER_ORES);
		valueLookupBuilder(ConventionalBlockTags.DIAMOND_ORES)
				.addOptionalTag(BlockTags.DIAMOND_ORES);
		valueLookupBuilder(ConventionalBlockTags.EMERALD_ORES)
				.addOptionalTag(BlockTags.EMERALD_ORES);
		valueLookupBuilder(ConventionalBlockTags.GOLD_ORES)
				.addOptionalTag(BlockTags.GOLD_ORES);
		valueLookupBuilder(ConventionalBlockTags.IRON_ORES)
				.addOptionalTag(BlockTags.IRON_ORES);
		valueLookupBuilder(ConventionalBlockTags.LAPIS_ORES)
				.addOptionalTag(BlockTags.LAPIS_ORES);
		valueLookupBuilder(ConventionalBlockTags.NETHERITE_SCRAP_ORES)
				.add(Blocks.ANCIENT_DEBRIS);
		valueLookupBuilder(ConventionalBlockTags.REDSTONE_ORES)
				.addOptionalTag(BlockTags.REDSTONE_ORES);
		valueLookupBuilder(ConventionalBlockTags.QUARTZ_ORES)
				.add(Blocks.NETHER_QUARTZ_ORE);
		valueLookupBuilder(ConventionalBlockTags.ORES)
				.addOptionalTag(ConventionalBlockTags.COAL_ORES)
				.addOptionalTag(ConventionalBlockTags.COPPER_ORES)
				.addOptionalTag(ConventionalBlockTags.DIAMOND_ORES)
				.addOptionalTag(ConventionalBlockTags.EMERALD_ORES)
				.addOptionalTag(ConventionalBlockTags.GOLD_ORES)
				.addOptionalTag(ConventionalBlockTags.IRON_ORES)
				.addOptionalTag(ConventionalBlockTags.LAPIS_ORES)
				.addOptionalTag(ConventionalBlockTags.NETHERITE_SCRAP_ORES)
				.addOptionalTag(ConventionalBlockTags.REDSTONE_ORES)
				.addOptionalTag(ConventionalBlockTags.QUARTZ_ORES);

		valueLookupBuilder(ConventionalBlockTags.ORE_BEARING_GROUND_DEEPSLATE)
				.add(Blocks.DEEPSLATE);
		valueLookupBuilder(ConventionalBlockTags.ORE_BEARING_GROUND_NETHERRACK)
				.add(Blocks.NETHERRACK);
		valueLookupBuilder(ConventionalBlockTags.ORE_BEARING_GROUND_STONE)
				.add(Blocks.STONE);
		valueLookupBuilder(ConventionalBlockTags.ORE_RATES_DENSE)
				.add(Blocks.COPPER_ORE)
				.add(Blocks.DEEPSLATE_COPPER_ORE)
				.add(Blocks.DEEPSLATE_LAPIS_ORE)
				.add(Blocks.DEEPSLATE_REDSTONE_ORE)
				.add(Blocks.LAPIS_ORE)
				.add(Blocks.REDSTONE_ORE);
		valueLookupBuilder(ConventionalBlockTags.ORE_RATES_SINGULAR)
				.add(Blocks.ANCIENT_DEBRIS)
				.add(Blocks.COAL_ORE)
				.add(Blocks.DEEPSLATE_COAL_ORE)
				.add(Blocks.DEEPSLATE_DIAMOND_ORE)
				.add(Blocks.DEEPSLATE_EMERALD_ORE)
				.add(Blocks.DEEPSLATE_GOLD_ORE)
				.add(Blocks.DEEPSLATE_IRON_ORE)
				.add(Blocks.DIAMOND_ORE)
				.add(Blocks.EMERALD_ORE)
				.add(Blocks.GOLD_ORE)
				.add(Blocks.IRON_ORE)
				.add(Blocks.NETHER_QUARTZ_ORE);
		valueLookupBuilder(ConventionalBlockTags.ORE_RATES_SPARSE)
				.add(Blocks.NETHER_GOLD_ORE);
		valueLookupBuilder(ConventionalBlockTags.ORES_IN_GROUND_DEEPSLATE)
				.add(Blocks.DEEPSLATE_COAL_ORE)
				.add(Blocks.DEEPSLATE_COPPER_ORE)
				.add(Blocks.DEEPSLATE_DIAMOND_ORE)
				.add(Blocks.DEEPSLATE_EMERALD_ORE)
				.add(Blocks.DEEPSLATE_GOLD_ORE)
				.add(Blocks.DEEPSLATE_IRON_ORE)
				.add(Blocks.DEEPSLATE_LAPIS_ORE)
				.add(Blocks.DEEPSLATE_REDSTONE_ORE);
		valueLookupBuilder(ConventionalBlockTags.ORES_IN_GROUND_NETHERRACK)
				.add(Blocks.NETHER_GOLD_ORE)
				.add(Blocks.NETHER_QUARTZ_ORE);
		valueLookupBuilder(ConventionalBlockTags.ORES_IN_GROUND_STONE)
				.add(Blocks.COAL_ORE)
				.add(Blocks.COPPER_ORE)
				.add(Blocks.DIAMOND_ORE)
				.add(Blocks.EMERALD_ORE)
				.add(Blocks.GOLD_ORE)
				.add(Blocks.IRON_ORE)
				.add(Blocks.LAPIS_ORE)
				.add(Blocks.REDSTONE_ORE);

		valueLookupBuilder(ConventionalBlockTags.WOODEN_CHESTS)
				.add(Blocks.CHEST)
				.add(Blocks.TRAPPED_CHEST);
		valueLookupBuilder(ConventionalBlockTags.TRAPPED_CHESTS)
				.add(Blocks.TRAPPED_CHEST);
		valueLookupBuilder(ConventionalBlockTags.ENDER_CHESTS)
				.add(Blocks.ENDER_CHEST);
		valueLookupBuilder(ConventionalBlockTags.CHESTS)
				.addTag(ConventionalBlockTags.WOODEN_CHESTS)
				.addTag(ConventionalBlockTags.TRAPPED_CHESTS)
				.addTag(ConventionalBlockTags.ENDER_CHESTS);
		valueLookupBuilder(ConventionalBlockTags.BOOKSHELVES)
				.add(Blocks.BOOKSHELF);
		generateGlassTags();
		generateGlazeTerracottaTags();
		generateConcreteTags();
		valueLookupBuilder(ConventionalBlockTags.WOODEN_BARRELS)
				.add(Blocks.BARREL);
		valueLookupBuilder(ConventionalBlockTags.BARRELS)
				.addTag(ConventionalBlockTags.WOODEN_BARRELS);

		generateBuddingTags();

		generateSandstoneTags();

		generateFenceAndFenceGateTags();

		generateDyedTags();

		generateStorageTags();

		generateLogTags();

		generateHeadTags();

		generateFlowerTags();

		generateMiscTags();

		generateTagAlias();

		generateBackwardsCompatTags();
	}

	private void generateFlowerTags() {
		valueLookupBuilder(ConventionalBlockTags.SMALL_FLOWERS)
				.add(Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID,
						Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP,
						Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP,
						Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY,
						Blocks.WITHER_ROSE, Blocks.TORCHFLOWER, Blocks.OPEN_EYEBLOSSOM,
						Blocks.CLOSED_EYEBLOSSOM
				);

		valueLookupBuilder(ConventionalBlockTags.TALL_FLOWERS)
				.add(Blocks.SUNFLOWER, Blocks.LILAC, Blocks.PEONY,
						Blocks.ROSE_BUSH, Blocks.PITCHER_PLANT
				);

		valueLookupBuilder(ConventionalBlockTags.FLOWERS)
				.add(Blocks.CHERRY_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES, Blocks.FLOWERING_AZALEA,
						Blocks.MANGROVE_PROPAGULE, Blocks.PINK_PETALS, Blocks.WILDFLOWERS, Blocks.CHORUS_FLOWER,
						Blocks.SPORE_BLOSSOM, Blocks.CACTUS_FLOWER
				).addOptionalTag(ConventionalBlockTags.SMALL_FLOWERS)
				.addOptionalTag(ConventionalBlockTags.TALL_FLOWERS);
	}

	private void generateMiscTags() {
		valueLookupBuilder(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
				.add(Blocks.CRAFTING_TABLE);
		valueLookupBuilder(ConventionalBlockTags.PLAYER_WORKSTATIONS_FURNACES)
				.add(Blocks.FURNACE);

		VILLAGER_JOB_SITE_BLOCKS.forEach(valueLookupBuilder(ConventionalBlockTags.VILLAGER_JOB_SITES)::add);

		valueLookupBuilder(ConventionalBlockTags.RELOCATION_NOT_SUPPORTED); // Generate tag so others can see it exists through JSON.

		valueLookupBuilder(ConventionalBlockTags.ROPES); // Generate tag so others can see it exists through JSON.

		valueLookupBuilder(ConventionalBlockTags.CHAINS)
				.add(Blocks.CHAIN);

		valueLookupBuilder(ConventionalBlockTags.HIDDEN_FROM_RECIPE_VIEWERS); // Generate tag so others can see it exists through JSON.
	}

	private void generateFenceAndFenceGateTags() {
		valueLookupBuilder(ConventionalBlockTags.WOODEN_FENCES)
				.add(Blocks.OAK_FENCE)
				.add(Blocks.SPRUCE_FENCE)
				.add(Blocks.BIRCH_FENCE)
				.add(Blocks.JUNGLE_FENCE)
				.add(Blocks.ACACIA_FENCE)
				.add(Blocks.DARK_OAK_FENCE)
				.add(Blocks.CRIMSON_FENCE)
				.add(Blocks.WARPED_FENCE)
				.add(Blocks.MANGROVE_FENCE)
				.add(Blocks.BAMBOO_FENCE)
				.add(Blocks.CHERRY_FENCE)
				.add(Blocks.PALE_OAK_FENCE);
		valueLookupBuilder(ConventionalBlockTags.NETHER_BRICK_FENCES)
				.add(Blocks.NETHER_BRICK_FENCE);
		valueLookupBuilder(ConventionalBlockTags.FENCES)
				.addOptionalTag(ConventionalBlockTags.WOODEN_FENCES)
				.addOptionalTag(ConventionalBlockTags.NETHER_BRICK_FENCES);
		valueLookupBuilder(ConventionalBlockTags.WOODEN_FENCE_GATES)
				.add(Blocks.OAK_FENCE_GATE)
				.add(Blocks.SPRUCE_FENCE_GATE)
				.add(Blocks.BIRCH_FENCE_GATE)
				.add(Blocks.JUNGLE_FENCE_GATE)
				.add(Blocks.ACACIA_FENCE_GATE)
				.add(Blocks.DARK_OAK_FENCE_GATE)
				.add(Blocks.CRIMSON_FENCE_GATE)
				.add(Blocks.WARPED_FENCE_GATE)
				.add(Blocks.MANGROVE_FENCE_GATE)
				.add(Blocks.BAMBOO_FENCE_GATE)
				.add(Blocks.CHERRY_FENCE_GATE)
				.add(Blocks.PALE_OAK_FENCE_GATE);
		valueLookupBuilder(ConventionalBlockTags.FENCE_GATES)
				.addOptionalTag(ConventionalBlockTags.WOODEN_FENCE_GATES);
		valueLookupBuilder(ConventionalBlockTags.PUMPKINS)
				.addTag(ConventionalBlockTags.NORMAL_PUMPKINS)
				.addTag(ConventionalBlockTags.CARVED_PUMPKINS)
				.addTag(ConventionalBlockTags.JACK_O_LANTERNS_PUMPKINS);
		valueLookupBuilder(ConventionalBlockTags.NORMAL_PUMPKINS)
				.add(Blocks.PUMPKIN);
		valueLookupBuilder(ConventionalBlockTags.CARVED_PUMPKINS)
				.add(Blocks.CARVED_PUMPKIN);
		valueLookupBuilder(ConventionalBlockTags.JACK_O_LANTERNS_PUMPKINS)
				.add(Blocks.JACK_O_LANTERN);
	}

	private void generateSandstoneTags() {
		valueLookupBuilder(ConventionalBlockTags.COLORLESS_SANDS)
				.add(Blocks.SAND);
		valueLookupBuilder(ConventionalBlockTags.RED_SANDS)
				.add(Blocks.RED_SAND);
		valueLookupBuilder(ConventionalBlockTags.SANDS)
				.addOptionalTag(ConventionalBlockTags.COLORLESS_SANDS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDS);

		valueLookupBuilder(ConventionalBlockTags.SANDSTONE_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_BLOCKS);
		valueLookupBuilder(ConventionalBlockTags.SANDSTONE_SLABS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_SLABS);
		valueLookupBuilder(ConventionalBlockTags.SANDSTONE_STAIRS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_STAIRS);

		valueLookupBuilder(ConventionalBlockTags.RED_SANDSTONE_BLOCKS)
				.add(Blocks.RED_SANDSTONE)
				.add(Blocks.CUT_RED_SANDSTONE)
				.add(Blocks.SMOOTH_RED_SANDSTONE)
				.add(Blocks.CHISELED_RED_SANDSTONE);
		valueLookupBuilder(ConventionalBlockTags.RED_SANDSTONE_SLABS)
				.add(Blocks.RED_SANDSTONE_SLAB)
				.add(Blocks.CUT_RED_SANDSTONE_SLAB)
				.add(Blocks.SMOOTH_RED_SANDSTONE_SLAB);
		valueLookupBuilder(ConventionalBlockTags.RED_SANDSTONE_STAIRS)
				.add(Blocks.RED_SANDSTONE_STAIRS)
				.add(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);

		valueLookupBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS)
				.add(Blocks.SANDSTONE)
				.add(Blocks.CUT_SANDSTONE)
				.add(Blocks.SMOOTH_SANDSTONE)
				.add(Blocks.CHISELED_SANDSTONE);
		valueLookupBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS)
				.add(Blocks.SANDSTONE_SLAB)
				.add(Blocks.CUT_SANDSTONE_SLAB)
				.add(Blocks.SMOOTH_SANDSTONE_SLAB);
		valueLookupBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS)
				.add(Blocks.SANDSTONE_STAIRS)
				.add(Blocks.SMOOTH_SANDSTONE_STAIRS);
	}

	private void generateBuddingTags() {
		valueLookupBuilder(ConventionalBlockTags.BUDDING_BLOCKS)
				.add(Blocks.BUDDING_AMETHYST);
		valueLookupBuilder(ConventionalBlockTags.BUDS)
				.add(Blocks.SMALL_AMETHYST_BUD)
				.add(Blocks.MEDIUM_AMETHYST_BUD)
				.add(Blocks.LARGE_AMETHYST_BUD);
		valueLookupBuilder(ConventionalBlockTags.CLUSTERS)
				.add(Blocks.AMETHYST_CLUSTER);
	}

	private void generateGlassTags() {
		valueLookupBuilder(ConventionalBlockTags.GLASS_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_CHEAP)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_TINTED);
		valueLookupBuilder(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS)
				.add(Blocks.GLASS);
		valueLookupBuilder(ConventionalBlockTags.GLASS_BLOCKS_CHEAP)
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
		valueLookupBuilder(ConventionalBlockTags.GLASS_BLOCKS_TINTED)
				.add(Blocks.TINTED_GLASS);
		valueLookupBuilder(ConventionalBlockTags.GLASS_PANES)
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
		valueLookupBuilder(ConventionalBlockTags.GLASS_PANES_COLORLESS)
				.add(Blocks.GLASS_PANE);
	}

	private void generateGlazeTerracottaTags() {
		valueLookupBuilder(ConventionalBlockTags.GLAZED_TERRACOTTAS)
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
		valueLookupBuilder(ConventionalBlockTags.GLAZED_TERRACOTTA)
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
		valueLookupBuilder(ConventionalBlockTags.CONCRETES)
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
		valueLookupBuilder(ConventionalBlockTags.CONCRETE)
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
		valueLookupBuilder(ConventionalBlockTags.BLACK_DYED)
				.add(Blocks.BLACK_BANNER).add(Blocks.BLACK_BED).add(Blocks.BLACK_CANDLE).add(Blocks.BLACK_CARPET)
				.add(Blocks.BLACK_CONCRETE).add(Blocks.BLACK_CONCRETE_POWDER).add(Blocks.BLACK_GLAZED_TERRACOTTA)
				.add(Blocks.BLACK_SHULKER_BOX).add(Blocks.BLACK_STAINED_GLASS).add(Blocks.BLACK_STAINED_GLASS_PANE)
				.add(Blocks.BLACK_TERRACOTTA).add(Blocks.BLACK_WALL_BANNER).add(Blocks.BLACK_WOOL);

		valueLookupBuilder(ConventionalBlockTags.BLUE_DYED)
				.add(Blocks.BLUE_BANNER).add(Blocks.BLUE_BED).add(Blocks.BLUE_CANDLE).add(Blocks.BLUE_CARPET)
				.add(Blocks.BLUE_CONCRETE).add(Blocks.BLUE_CONCRETE_POWDER).add(Blocks.BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.BLUE_SHULKER_BOX).add(Blocks.BLUE_STAINED_GLASS).add(Blocks.BLUE_STAINED_GLASS_PANE)
				.add(Blocks.BLUE_TERRACOTTA).add(Blocks.BLUE_WALL_BANNER).add(Blocks.BLUE_WOOL);

		valueLookupBuilder(ConventionalBlockTags.BROWN_DYED)
				.add(Blocks.BROWN_BANNER).add(Blocks.BROWN_BED).add(Blocks.BROWN_CANDLE).add(Blocks.BROWN_CARPET)
				.add(Blocks.BROWN_CONCRETE).add(Blocks.BROWN_CONCRETE_POWDER).add(Blocks.BROWN_GLAZED_TERRACOTTA)
				.add(Blocks.BROWN_SHULKER_BOX).add(Blocks.BROWN_STAINED_GLASS).add(Blocks.BROWN_STAINED_GLASS_PANE)
				.add(Blocks.BROWN_TERRACOTTA).add(Blocks.BROWN_WALL_BANNER).add(Blocks.BROWN_WOOL);

		valueLookupBuilder(ConventionalBlockTags.CYAN_DYED)
				.add(Blocks.CYAN_BANNER).add(Blocks.CYAN_BED).add(Blocks.CYAN_CANDLE).add(Blocks.CYAN_CARPET)
				.add(Blocks.CYAN_CONCRETE).add(Blocks.CYAN_CONCRETE_POWDER).add(Blocks.CYAN_GLAZED_TERRACOTTA)
				.add(Blocks.CYAN_SHULKER_BOX).add(Blocks.CYAN_STAINED_GLASS).add(Blocks.CYAN_STAINED_GLASS_PANE)
				.add(Blocks.CYAN_TERRACOTTA).add(Blocks.CYAN_WALL_BANNER).add(Blocks.CYAN_WOOL);

		valueLookupBuilder(ConventionalBlockTags.GRAY_DYED)
				.add(Blocks.GRAY_BANNER).add(Blocks.GRAY_BED).add(Blocks.GRAY_CANDLE).add(Blocks.GRAY_CARPET)
				.add(Blocks.GRAY_CONCRETE).add(Blocks.GRAY_CONCRETE_POWDER).add(Blocks.GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.GRAY_SHULKER_BOX).add(Blocks.GRAY_STAINED_GLASS).add(Blocks.GRAY_STAINED_GLASS_PANE)
				.add(Blocks.GRAY_TERRACOTTA).add(Blocks.GRAY_WALL_BANNER).add(Blocks.GRAY_WOOL);

		valueLookupBuilder(ConventionalBlockTags.GREEN_DYED)
				.add(Blocks.GREEN_BANNER).add(Blocks.GREEN_BED).add(Blocks.GREEN_CANDLE).add(Blocks.GREEN_CARPET)
				.add(Blocks.GREEN_CONCRETE).add(Blocks.GREEN_CONCRETE_POWDER).add(Blocks.GREEN_GLAZED_TERRACOTTA)
				.add(Blocks.GREEN_SHULKER_BOX).add(Blocks.GREEN_STAINED_GLASS).add(Blocks.GREEN_STAINED_GLASS_PANE)
				.add(Blocks.GREEN_TERRACOTTA).add(Blocks.GREEN_WALL_BANNER).add(Blocks.GREEN_WOOL);

		valueLookupBuilder(ConventionalBlockTags.LIGHT_BLUE_DYED)
				.add(Blocks.LIGHT_BLUE_BANNER).add(Blocks.LIGHT_BLUE_BED).add(Blocks.LIGHT_BLUE_CANDLE).add(Blocks.LIGHT_BLUE_CARPET)
				.add(Blocks.LIGHT_BLUE_CONCRETE).add(Blocks.LIGHT_BLUE_CONCRETE_POWDER).add(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_BLUE_SHULKER_BOX).add(Blocks.LIGHT_BLUE_STAINED_GLASS).add(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)
				.add(Blocks.LIGHT_BLUE_TERRACOTTA).add(Blocks.LIGHT_BLUE_WALL_BANNER).add(Blocks.LIGHT_BLUE_WOOL);

		valueLookupBuilder(ConventionalBlockTags.LIGHT_GRAY_DYED)
				.add(Blocks.LIGHT_GRAY_BANNER).add(Blocks.LIGHT_GRAY_BED).add(Blocks.LIGHT_GRAY_CANDLE).add(Blocks.LIGHT_GRAY_CARPET)
				.add(Blocks.LIGHT_GRAY_CONCRETE).add(Blocks.LIGHT_GRAY_CONCRETE_POWDER).add(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_GRAY_SHULKER_BOX).add(Blocks.LIGHT_GRAY_STAINED_GLASS).add(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE)
				.add(Blocks.LIGHT_GRAY_TERRACOTTA).add(Blocks.LIGHT_GRAY_WALL_BANNER).add(Blocks.LIGHT_GRAY_WOOL);

		valueLookupBuilder(ConventionalBlockTags.LIME_DYED)
				.add(Blocks.LIME_BANNER).add(Blocks.LIME_BED).add(Blocks.LIME_CANDLE).add(Blocks.LIME_CARPET)
				.add(Blocks.LIME_CONCRETE).add(Blocks.LIME_CONCRETE_POWDER).add(Blocks.LIME_GLAZED_TERRACOTTA)
				.add(Blocks.LIME_SHULKER_BOX).add(Blocks.LIME_STAINED_GLASS).add(Blocks.LIME_STAINED_GLASS_PANE)
				.add(Blocks.LIME_TERRACOTTA).add(Blocks.LIME_WALL_BANNER).add(Blocks.LIME_WOOL);

		valueLookupBuilder(ConventionalBlockTags.MAGENTA_DYED)
				.add(Blocks.MAGENTA_BANNER).add(Blocks.MAGENTA_BED).add(Blocks.MAGENTA_CANDLE).add(Blocks.MAGENTA_CARPET)
				.add(Blocks.MAGENTA_CONCRETE).add(Blocks.MAGENTA_CONCRETE_POWDER).add(Blocks.MAGENTA_GLAZED_TERRACOTTA)
				.add(Blocks.MAGENTA_SHULKER_BOX).add(Blocks.MAGENTA_STAINED_GLASS).add(Blocks.MAGENTA_STAINED_GLASS_PANE)
				.add(Blocks.MAGENTA_TERRACOTTA).add(Blocks.MAGENTA_WALL_BANNER).add(Blocks.MAGENTA_WOOL);

		valueLookupBuilder(ConventionalBlockTags.ORANGE_DYED)
				.add(Blocks.ORANGE_BANNER).add(Blocks.ORANGE_BED).add(Blocks.ORANGE_CANDLE).add(Blocks.ORANGE_CARPET)
				.add(Blocks.ORANGE_CONCRETE).add(Blocks.ORANGE_CONCRETE_POWDER).add(Blocks.ORANGE_GLAZED_TERRACOTTA)
				.add(Blocks.ORANGE_SHULKER_BOX).add(Blocks.ORANGE_STAINED_GLASS).add(Blocks.ORANGE_STAINED_GLASS_PANE)
				.add(Blocks.ORANGE_TERRACOTTA).add(Blocks.ORANGE_WALL_BANNER).add(Blocks.ORANGE_WOOL);

		valueLookupBuilder(ConventionalBlockTags.PINK_DYED)
				.add(Blocks.PINK_BANNER).add(Blocks.PINK_BED).add(Blocks.PINK_CANDLE).add(Blocks.PINK_CARPET)
				.add(Blocks.PINK_CONCRETE).add(Blocks.PINK_CONCRETE_POWDER).add(Blocks.PINK_GLAZED_TERRACOTTA)
				.add(Blocks.PINK_SHULKER_BOX).add(Blocks.PINK_STAINED_GLASS).add(Blocks.PINK_STAINED_GLASS_PANE)
				.add(Blocks.PINK_TERRACOTTA).add(Blocks.PINK_WALL_BANNER).add(Blocks.PINK_WOOL);

		valueLookupBuilder(ConventionalBlockTags.PURPLE_DYED)
				.add(Blocks.PURPLE_BANNER).add(Blocks.PURPLE_BED).add(Blocks.PURPLE_CANDLE).add(Blocks.PURPLE_CARPET)
				.add(Blocks.PURPLE_CONCRETE).add(Blocks.PURPLE_CONCRETE_POWDER).add(Blocks.PURPLE_GLAZED_TERRACOTTA)
				.add(Blocks.PURPLE_SHULKER_BOX).add(Blocks.PURPLE_STAINED_GLASS).add(Blocks.PURPLE_STAINED_GLASS_PANE)
				.add(Blocks.PURPLE_TERRACOTTA).add(Blocks.PURPLE_WALL_BANNER).add(Blocks.PURPLE_WOOL);

		valueLookupBuilder(ConventionalBlockTags.RED_DYED)
				.add(Blocks.RED_BANNER).add(Blocks.RED_BED).add(Blocks.RED_CANDLE).add(Blocks.RED_CARPET)
				.add(Blocks.RED_CONCRETE).add(Blocks.RED_CONCRETE_POWDER).add(Blocks.RED_GLAZED_TERRACOTTA)
				.add(Blocks.RED_SHULKER_BOX).add(Blocks.RED_STAINED_GLASS).add(Blocks.RED_STAINED_GLASS_PANE)
				.add(Blocks.RED_TERRACOTTA).add(Blocks.RED_WALL_BANNER).add(Blocks.RED_WOOL);

		valueLookupBuilder(ConventionalBlockTags.WHITE_DYED)
				.add(Blocks.WHITE_BANNER).add(Blocks.WHITE_BED).add(Blocks.WHITE_CANDLE).add(Blocks.WHITE_CARPET)
				.add(Blocks.WHITE_CONCRETE).add(Blocks.WHITE_CONCRETE_POWDER).add(Blocks.WHITE_GLAZED_TERRACOTTA)
				.add(Blocks.WHITE_SHULKER_BOX).add(Blocks.WHITE_STAINED_GLASS).add(Blocks.WHITE_STAINED_GLASS_PANE)
				.add(Blocks.WHITE_TERRACOTTA).add(Blocks.WHITE_WALL_BANNER).add(Blocks.WHITE_WOOL);

		valueLookupBuilder(ConventionalBlockTags.YELLOW_DYED)
				.add(Blocks.YELLOW_BANNER).add(Blocks.YELLOW_BED).add(Blocks.YELLOW_CANDLE).add(Blocks.YELLOW_CARPET)
				.add(Blocks.YELLOW_CONCRETE).add(Blocks.YELLOW_CONCRETE_POWDER).add(Blocks.YELLOW_GLAZED_TERRACOTTA)
				.add(Blocks.YELLOW_SHULKER_BOX).add(Blocks.YELLOW_STAINED_GLASS).add(Blocks.YELLOW_STAINED_GLASS_PANE)
				.add(Blocks.YELLOW_TERRACOTTA).add(Blocks.YELLOW_WALL_BANNER).add(Blocks.YELLOW_WOOL);

		valueLookupBuilder(ConventionalBlockTags.DYED)
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
		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS)
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
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_RESIN)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_SLIME)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_WHEAT);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_BONE_MEAL)
				.add(Blocks.BONE_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_COAL)
				.add(Blocks.COAL_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_COPPER)
				.add(Blocks.COPPER_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_DIAMOND)
				.add(Blocks.DIAMOND_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_DRIED_KELP)
				.add(Blocks.DRIED_KELP_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_EMERALD)
				.add(Blocks.EMERALD_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_GOLD)
				.add(Blocks.GOLD_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_IRON)
				.add(Blocks.IRON_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_LAPIS)
				.add(Blocks.LAPIS_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_NETHERITE)
				.add(Blocks.NETHERITE_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_COPPER)
				.add(Blocks.RAW_COPPER_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_GOLD)
				.add(Blocks.RAW_GOLD_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON)
				.add(Blocks.RAW_IRON_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_REDSTONE)
				.add(Blocks.REDSTONE_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_RESIN)
				.add(Blocks.RESIN_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_SLIME)
				.add(Blocks.SLIME_BLOCK);

		valueLookupBuilder(ConventionalBlockTags.STORAGE_BLOCKS_WHEAT)
				.add(Blocks.HAY_BLOCK);
	}

	private void generateLogTags() {
		valueLookupBuilder(ConventionalBlockTags.STRIPPED_LOGS)
				.add(Blocks.STRIPPED_ACACIA_LOG)
				.add(Blocks.STRIPPED_BAMBOO_BLOCK)
				.add(Blocks.STRIPPED_BIRCH_LOG)
				.add(Blocks.STRIPPED_CHERRY_LOG)
				.add(Blocks.STRIPPED_DARK_OAK_LOG)
				.add(Blocks.STRIPPED_JUNGLE_LOG)
				.add(Blocks.STRIPPED_MANGROVE_LOG)
				.add(Blocks.STRIPPED_OAK_LOG)
				.add(Blocks.STRIPPED_PALE_OAK_LOG)
				.add(Blocks.STRIPPED_SPRUCE_LOG)
				.add(Blocks.STRIPPED_CRIMSON_STEM)
				.add(Blocks.STRIPPED_WARPED_STEM);

		valueLookupBuilder(ConventionalBlockTags.STRIPPED_WOODS)
				.add(Blocks.STRIPPED_ACACIA_WOOD)
				.add(Blocks.STRIPPED_BIRCH_WOOD)
				.add(Blocks.STRIPPED_CHERRY_WOOD)
				.add(Blocks.STRIPPED_DARK_OAK_WOOD)
				.add(Blocks.STRIPPED_JUNGLE_WOOD)
				.add(Blocks.STRIPPED_MANGROVE_WOOD)
				.add(Blocks.STRIPPED_OAK_WOOD)
				.add(Blocks.STRIPPED_PALE_OAK_WOOD)
				.add(Blocks.STRIPPED_SPRUCE_WOOD)
				.add(Blocks.STRIPPED_CRIMSON_HYPHAE)
				.add(Blocks.STRIPPED_WARPED_HYPHAE);
	}

	private void generateHeadTags() {
		valueLookupBuilder(ConventionalBlockTags.SKULLS)
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

	private void generateTagAlias() {
		aliasGroup("ores/coal").add(BlockTags.COAL_ORES, ConventionalBlockTags.COAL_ORES);
		aliasGroup("ores/copper").add(BlockTags.COPPER_ORES, ConventionalBlockTags.COPPER_ORES);
		aliasGroup("ores/diamond").add(BlockTags.DIAMOND_ORES, ConventionalBlockTags.DIAMOND_ORES);
		aliasGroup("ores/emerald").add(BlockTags.EMERALD_ORES, ConventionalBlockTags.EMERALD_ORES);
		aliasGroup("ores/gold").add(BlockTags.GOLD_ORES, ConventionalBlockTags.GOLD_ORES);
		aliasGroup("ores/iron").add(BlockTags.IRON_ORES, ConventionalBlockTags.IRON_ORES);
		aliasGroup("ores/lapis").add(BlockTags.LAPIS_ORES, ConventionalBlockTags.LAPIS_ORES);
		aliasGroup("ores/redstone").add(BlockTags.REDSTONE_ORES, ConventionalBlockTags.REDSTONE_ORES);

		aliasGroup("fences").add(BlockTags.FENCES, ConventionalBlockTags.FENCES);
		aliasGroup("fences/wooden").add(BlockTags.WOODEN_FENCES, ConventionalBlockTags.WOODEN_FENCES);
		aliasGroup("fence_gates").add(BlockTags.FENCE_GATES, ConventionalBlockTags.FENCE_GATES);

		aliasGroup("flowers").add(BlockTags.FLOWERS, ConventionalBlockTags.FLOWERS);
	}

	private void generateBackwardsCompatTags() {
		// Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
		// TODO: Remove backwards compat tag entries in 1.22

		valueLookupBuilder(ConventionalBlockTags.RELOCATION_NOT_SUPPORTED).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "movement_restricted")));
		valueLookupBuilder(ConventionalBlockTags.QUARTZ_ORES).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "quartz_ores")));
		valueLookupBuilder(ConventionalBlockTags.WOODEN_BARRELS).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "wooden_barrels")));
		valueLookupBuilder(ConventionalBlockTags.WOODEN_CHESTS).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "wooden_chests")));
		valueLookupBuilder(ConventionalBlockTags.SANDSTONE_BLOCKS).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "sandstone_blocks")));
		valueLookupBuilder(ConventionalBlockTags.SANDSTONE_SLABS).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "sandstone_slabs")));
		valueLookupBuilder(ConventionalBlockTags.SANDSTONE_STAIRS).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "sandstone_stairs")));
		valueLookupBuilder(ConventionalBlockTags.RED_SANDSTONE_BLOCKS).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "red_sandstone_blocks")));
		valueLookupBuilder(ConventionalBlockTags.RED_SANDSTONE_SLABS).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "red_sandstone_slabs")));
		valueLookupBuilder(ConventionalBlockTags.RED_SANDSTONE_STAIRS).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "red_sandstone_stairs")));
		valueLookupBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "uncolored_sandstone_blocks")));
		valueLookupBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "uncolored_sandstone_slabs")));
		valueLookupBuilder(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS).addOptionalTag(TagKey.of(RegistryKeys.BLOCK, Identifier.of(TagUtil.C_TAG_NAMESPACE, "uncolored_sandstone_stairs")));
		valueLookupBuilder(ConventionalBlockTags.GLAZED_TERRACOTTAS).addOptionalTag(ConventionalBlockTags.GLAZED_TERRACOTTA);
		valueLookupBuilder(ConventionalBlockTags.CONCRETES).addOptionalTag(ConventionalBlockTags.CONCRETE);
	}
}
