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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;

public final class BlockTagsGenerator extends FabricTagsProvider.BlockTagsProvider {
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

	public BlockTagsGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
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
				.addTag(ConventionalBlockTags.ENDER_CHESTS)
				.addOptionalTag(BlockTags.COPPER_CHESTS);
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
				.add(Blocks.IRON_CHAIN)
				.addAll(Blocks.COPPER_CHAIN.asList());

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
				.addAll(Blocks.STAINED_GLASS.asList());
		valueLookupBuilder(ConventionalBlockTags.GLASS_BLOCKS_TINTED)
				.add(Blocks.TINTED_GLASS);
		valueLookupBuilder(ConventionalBlockTags.GLASS_PANES)
				.addAll(Blocks.STAINED_GLASS_PANE.asList())
				.addOptionalTag(ConventionalBlockTags.GLASS_PANES_COLORLESS);
		valueLookupBuilder(ConventionalBlockTags.GLASS_PANES_COLORLESS)
				.add(Blocks.GLASS_PANE);
	}

	private void generateGlazeTerracottaTags() {
		valueLookupBuilder(ConventionalBlockTags.GLAZED_TERRACOTTAS)
				.addAll(Blocks.GLAZED_TERRACOTTA.asList());
	}

	private void generateConcreteTags() {
		valueLookupBuilder(ConventionalBlockTags.CONCRETES)
				.addAll(Blocks.CONCRETE.asList());
	}

	private void generateDyedTags() {
		valueLookupBuilder(ConventionalBlockTags.BLACK_DYED)
				.add(Blocks.BANNER.black()).add(Blocks.BED.black()).add(Blocks.DYED_CANDLE.black()).add(Blocks.CARPET.black())
				.add(Blocks.CONCRETE.black()).add(Blocks.CONCRETE_POWDER.black()).add(Blocks.GLAZED_TERRACOTTA.black())
				.add(Blocks.DYED_SHULKER_BOX.black()).add(Blocks.STAINED_GLASS.black()).add(Blocks.STAINED_GLASS_PANE.black())
				.add(Blocks.DYED_TERRACOTTA.black()).add(Blocks.WALL_BANNER.black()).add(Blocks.WOOL.black());

		valueLookupBuilder(ConventionalBlockTags.BLUE_DYED)
				.add(Blocks.BANNER.blue()).add(Blocks.BED.blue()).add(Blocks.DYED_CANDLE.blue()).add(Blocks.CARPET.blue())
				.add(Blocks.CONCRETE.blue()).add(Blocks.CONCRETE_POWDER.blue()).add(Blocks.GLAZED_TERRACOTTA.blue())
				.add(Blocks.DYED_SHULKER_BOX.blue()).add(Blocks.STAINED_GLASS.blue()).add(Blocks.STAINED_GLASS_PANE.blue())
				.add(Blocks.DYED_TERRACOTTA.blue()).add(Blocks.WALL_BANNER.blue()).add(Blocks.WOOL.blue());

		valueLookupBuilder(ConventionalBlockTags.BROWN_DYED)
				.add(Blocks.BANNER.brown()).add(Blocks.BED.brown()).add(Blocks.DYED_CANDLE.brown()).add(Blocks.CARPET.brown())
				.add(Blocks.CONCRETE.brown()).add(Blocks.CONCRETE_POWDER.brown()).add(Blocks.GLAZED_TERRACOTTA.brown())
				.add(Blocks.DYED_SHULKER_BOX.brown()).add(Blocks.STAINED_GLASS.brown()).add(Blocks.STAINED_GLASS_PANE.brown())
				.add(Blocks.DYED_TERRACOTTA.brown()).add(Blocks.WALL_BANNER.brown()).add(Blocks.WOOL.brown());

		valueLookupBuilder(ConventionalBlockTags.CYAN_DYED)
				.add(Blocks.BANNER.cyan()).add(Blocks.BED.cyan()).add(Blocks.DYED_CANDLE.cyan()).add(Blocks.CARPET.cyan())
				.add(Blocks.CONCRETE.cyan()).add(Blocks.CONCRETE_POWDER.cyan()).add(Blocks.GLAZED_TERRACOTTA.cyan())
				.add(Blocks.DYED_SHULKER_BOX.cyan()).add(Blocks.STAINED_GLASS.cyan()).add(Blocks.STAINED_GLASS_PANE.cyan())
				.add(Blocks.DYED_TERRACOTTA.cyan()).add(Blocks.WALL_BANNER.cyan()).add(Blocks.WOOL.cyan());

		valueLookupBuilder(ConventionalBlockTags.GRAY_DYED)
				.add(Blocks.BANNER.gray()).add(Blocks.BED.gray()).add(Blocks.DYED_CANDLE.gray()).add(Blocks.CARPET.gray())
				.add(Blocks.CONCRETE.gray()).add(Blocks.CONCRETE_POWDER.gray()).add(Blocks.GLAZED_TERRACOTTA.gray())
				.add(Blocks.DYED_SHULKER_BOX.gray()).add(Blocks.STAINED_GLASS.gray()).add(Blocks.STAINED_GLASS_PANE.gray())
				.add(Blocks.DYED_TERRACOTTA.gray()).add(Blocks.WALL_BANNER.gray()).add(Blocks.WOOL.gray());

		valueLookupBuilder(ConventionalBlockTags.GREEN_DYED)
				.add(Blocks.BANNER.green()).add(Blocks.BED.green()).add(Blocks.DYED_CANDLE.green()).add(Blocks.CARPET.green())
				.add(Blocks.CONCRETE.green()).add(Blocks.CONCRETE_POWDER.green()).add(Blocks.GLAZED_TERRACOTTA.green())
				.add(Blocks.DYED_SHULKER_BOX.green()).add(Blocks.STAINED_GLASS.green()).add(Blocks.STAINED_GLASS_PANE.green())
				.add(Blocks.DYED_TERRACOTTA.green()).add(Blocks.WALL_BANNER.green()).add(Blocks.WOOL.green());

		valueLookupBuilder(ConventionalBlockTags.LIGHT_BLUE_DYED)
				.add(Blocks.BANNER.lightBlue()).add(Blocks.BED.lightBlue()).add(Blocks.DYED_CANDLE.lightBlue()).add(Blocks.CARPET.lightBlue())
				.add(Blocks.CONCRETE.lightBlue()).add(Blocks.CONCRETE_POWDER.lightBlue()).add(Blocks.GLAZED_TERRACOTTA.lightBlue())
				.add(Blocks.DYED_SHULKER_BOX.lightBlue()).add(Blocks.STAINED_GLASS.lightBlue()).add(Blocks.STAINED_GLASS_PANE.lightBlue())
				.add(Blocks.DYED_TERRACOTTA.lightBlue()).add(Blocks.WALL_BANNER.lightBlue()).add(Blocks.WOOL.lightBlue());

		valueLookupBuilder(ConventionalBlockTags.LIGHT_GRAY_DYED)
				.add(Blocks.BANNER.lightGray()).add(Blocks.BED.lightGray()).add(Blocks.DYED_CANDLE.lightGray()).add(Blocks.CARPET.lightGray())
				.add(Blocks.CONCRETE.lightGray()).add(Blocks.CONCRETE_POWDER.lightGray()).add(Blocks.GLAZED_TERRACOTTA.lightGray())
				.add(Blocks.DYED_SHULKER_BOX.lightGray()).add(Blocks.STAINED_GLASS.lightGray()).add(Blocks.STAINED_GLASS_PANE.lightGray())
				.add(Blocks.DYED_TERRACOTTA.lightGray()).add(Blocks.WALL_BANNER.lightGray()).add(Blocks.WOOL.lightGray());

		valueLookupBuilder(ConventionalBlockTags.LIME_DYED)
				.add(Blocks.BANNER.lime()).add(Blocks.BED.lime()).add(Blocks.DYED_CANDLE.lime()).add(Blocks.CARPET.lime())
				.add(Blocks.CONCRETE.lime()).add(Blocks.CONCRETE_POWDER.lime()).add(Blocks.GLAZED_TERRACOTTA.lime())
				.add(Blocks.DYED_SHULKER_BOX.lime()).add(Blocks.STAINED_GLASS.lime()).add(Blocks.STAINED_GLASS_PANE.lime())
				.add(Blocks.DYED_TERRACOTTA.lime()).add(Blocks.WALL_BANNER.lime()).add(Blocks.WOOL.lime());

		valueLookupBuilder(ConventionalBlockTags.MAGENTA_DYED)
				.add(Blocks.BANNER.magenta()).add(Blocks.BED.magenta()).add(Blocks.DYED_CANDLE.magenta()).add(Blocks.CARPET.magenta())
				.add(Blocks.CONCRETE.magenta()).add(Blocks.CONCRETE_POWDER.magenta()).add(Blocks.GLAZED_TERRACOTTA.magenta())
				.add(Blocks.DYED_SHULKER_BOX.magenta()).add(Blocks.STAINED_GLASS.magenta()).add(Blocks.STAINED_GLASS_PANE.magenta())
				.add(Blocks.DYED_TERRACOTTA.magenta()).add(Blocks.WALL_BANNER.magenta()).add(Blocks.WOOL.magenta());

		valueLookupBuilder(ConventionalBlockTags.ORANGE_DYED)
				.add(Blocks.BANNER.orange()).add(Blocks.BED.orange()).add(Blocks.DYED_CANDLE.orange()).add(Blocks.CARPET.orange())
				.add(Blocks.CONCRETE.orange()).add(Blocks.CONCRETE_POWDER.orange()).add(Blocks.GLAZED_TERRACOTTA.orange())
				.add(Blocks.DYED_SHULKER_BOX.orange()).add(Blocks.STAINED_GLASS.orange()).add(Blocks.STAINED_GLASS_PANE.orange())
				.add(Blocks.DYED_TERRACOTTA.orange()).add(Blocks.WALL_BANNER.orange()).add(Blocks.WOOL.orange());

		valueLookupBuilder(ConventionalBlockTags.PINK_DYED)
				.add(Blocks.BANNER.pink()).add(Blocks.BED.pink()).add(Blocks.DYED_CANDLE.pink()).add(Blocks.CARPET.pink())
				.add(Blocks.CONCRETE.pink()).add(Blocks.CONCRETE_POWDER.pink()).add(Blocks.GLAZED_TERRACOTTA.pink())
				.add(Blocks.DYED_SHULKER_BOX.pink()).add(Blocks.STAINED_GLASS.pink()).add(Blocks.STAINED_GLASS_PANE.pink())
				.add(Blocks.DYED_TERRACOTTA.pink()).add(Blocks.WALL_BANNER.pink()).add(Blocks.WOOL.pink());

		valueLookupBuilder(ConventionalBlockTags.PURPLE_DYED)
				.add(Blocks.BANNER.purple()).add(Blocks.BED.purple()).add(Blocks.DYED_CANDLE.purple()).add(Blocks.CARPET.purple())
				.add(Blocks.CONCRETE.purple()).add(Blocks.CONCRETE_POWDER.purple()).add(Blocks.GLAZED_TERRACOTTA.purple())
				.add(Blocks.DYED_SHULKER_BOX.purple()).add(Blocks.STAINED_GLASS.purple()).add(Blocks.STAINED_GLASS_PANE.purple())
				.add(Blocks.DYED_TERRACOTTA.purple()).add(Blocks.WALL_BANNER.purple()).add(Blocks.WOOL.purple());

		valueLookupBuilder(ConventionalBlockTags.RED_DYED)
				.add(Blocks.BANNER.red()).add(Blocks.BED.red()).add(Blocks.DYED_CANDLE.red()).add(Blocks.CARPET.red())
				.add(Blocks.CONCRETE.red()).add(Blocks.CONCRETE_POWDER.red()).add(Blocks.GLAZED_TERRACOTTA.red())
				.add(Blocks.DYED_SHULKER_BOX.red()).add(Blocks.STAINED_GLASS.red()).add(Blocks.STAINED_GLASS_PANE.red())
				.add(Blocks.DYED_TERRACOTTA.red()).add(Blocks.WALL_BANNER.red()).add(Blocks.WOOL.red());

		valueLookupBuilder(ConventionalBlockTags.WHITE_DYED)
				.add(Blocks.BANNER.white()).add(Blocks.BED.white()).add(Blocks.DYED_CANDLE.white()).add(Blocks.CARPET.white())
				.add(Blocks.CONCRETE.white()).add(Blocks.CONCRETE_POWDER.white()).add(Blocks.GLAZED_TERRACOTTA.white())
				.add(Blocks.DYED_SHULKER_BOX.white()).add(Blocks.STAINED_GLASS.white()).add(Blocks.STAINED_GLASS_PANE.white())
				.add(Blocks.DYED_TERRACOTTA.white()).add(Blocks.WALL_BANNER.white()).add(Blocks.WOOL.white());

		valueLookupBuilder(ConventionalBlockTags.YELLOW_DYED)
				.add(Blocks.BANNER.yellow()).add(Blocks.BED.yellow()).add(Blocks.DYED_CANDLE.yellow()).add(Blocks.CARPET.yellow())
				.add(Blocks.CONCRETE.yellow()).add(Blocks.CONCRETE_POWDER.yellow()).add(Blocks.GLAZED_TERRACOTTA.yellow())
				.add(Blocks.DYED_SHULKER_BOX.yellow()).add(Blocks.STAINED_GLASS.yellow()).add(Blocks.STAINED_GLASS_PANE.yellow())
				.add(Blocks.DYED_TERRACOTTA.yellow()).add(Blocks.WALL_BANNER.yellow()).add(Blocks.WOOL.yellow());

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
				.add(Blocks.COPPER_BLOCK.unaffected());

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
		valueLookupBuilder(ConventionalBlockTags.OVERWORLD_NATURAL_LOGS)
				.add(Blocks.ACACIA_LOG)
				.add(Blocks.BAMBOO_BLOCK)
				.add(Blocks.BIRCH_LOG)
				.add(Blocks.CHERRY_LOG)
				.add(Blocks.DARK_OAK_LOG)
				.add(Blocks.JUNGLE_LOG)
				.add(Blocks.MANGROVE_LOG)
				.add(Blocks.OAK_LOG)
				.add(Blocks.PALE_OAK_LOG)
				.add(Blocks.SPRUCE_LOG);

		valueLookupBuilder(ConventionalBlockTags.NETHER_NATURAL_LOGS)
				.add(Blocks.CRIMSON_STEM)
				.add(Blocks.WARPED_STEM);

		valueLookupBuilder(ConventionalBlockTags.NATURAL_LOGS)
				.addOptionalTag(ConventionalBlockTags.OVERWORLD_NATURAL_LOGS)
				.addOptionalTag(ConventionalBlockTags.NETHER_NATURAL_LOGS);

		valueLookupBuilder(ConventionalBlockTags.NATURAL_WOODS)
				.add(Blocks.ACACIA_WOOD)
				.add(Blocks.BIRCH_WOOD)
				.add(Blocks.CHERRY_WOOD)
				.add(Blocks.DARK_OAK_WOOD)
				.add(Blocks.JUNGLE_WOOD)
				.add(Blocks.MANGROVE_WOOD)
				.add(Blocks.OAK_WOOD)
				.add(Blocks.PALE_OAK_WOOD)
				.add(Blocks.SPRUCE_WOOD)
				.add(Blocks.CRIMSON_HYPHAE)
				.add(Blocks.WARPED_HYPHAE);

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
		aliasGroup("natural_logs/overworld").add(BlockTags.OVERWORLD_NATURAL_LOGS, ConventionalBlockTags.OVERWORLD_NATURAL_LOGS);

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
}
