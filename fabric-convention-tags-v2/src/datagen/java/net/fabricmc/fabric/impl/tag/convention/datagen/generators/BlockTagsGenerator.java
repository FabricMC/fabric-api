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
import net.minecraft.references.BlockIds;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;

public final class BlockTagsGenerator extends FabricTagsProvider.BlockTagsProvider {
	static List<BlockItemId> VILLAGER_JOB_SITE_BLOCKS = List.of(
			BlockItemIds.BARREL,
			BlockItemIds.BLAST_FURNACE,
			BlockItemIds.BREWING_STAND,
			BlockItemIds.CARTOGRAPHY_TABLE,
			BlockItemIds.CAULDRON,
			BlockItemIds.COMPOSTER,
			BlockItemIds.FLETCHING_TABLE,
			BlockItemIds.GRINDSTONE,
			BlockItemIds.LECTERN,
			BlockItemIds.LOOM,
			BlockItemIds.SMITHING_TABLE,
			BlockItemIds.SMOKER,
			BlockItemIds.STONECUTTER
	);

	static List<ResourceKey<Block>> VILLAGER_JOB_SITE_BLOCKS_WITHOUT_ITEMS = List.of(
			BlockIds.LAVA_CAULDRON,
			BlockIds.WATER_CAULDRON,
			BlockIds.POWDER_SNOW_CAULDRON
	);

	public BlockTagsGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		builder(ConventionalBlockTags.STONES)
				.add(BlockItemIds.STONE)
				.add(BlockItemIds.ANDESITE)
				.add(BlockItemIds.DIORITE)
				.add(BlockItemIds.GRANITE)
				.add(BlockItemIds.TUFF)
				.add(BlockItemIds.DEEPSLATE);
		builder(ConventionalBlockTags.NORMAL_COBBLESTONES)
				.add(BlockItemIds.COBBLESTONE);
		builder(ConventionalBlockTags.MOSSY_COBBLESTONES)
				.add(BlockItemIds.MOSSY_COBBLESTONE);
		builder(ConventionalBlockTags.INFESTED_COBBLESTONES)
				.add(BlockItemIds.INFESTED_COBBLESTONE);
		builder(ConventionalBlockTags.DEEPSLATE_COBBLESTONES)
				.add(BlockItemIds.COBBLED_DEEPSLATE);
		builder(ConventionalBlockTags.COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.NORMAL_COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.MOSSY_COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.INFESTED_COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.DEEPSLATE_COBBLESTONES);
		builder(ConventionalBlockTags.NETHERRACKS)
				.add(BlockItemIds.NETHERRACK);
		builder(ConventionalBlockTags.END_STONES)
				.add(BlockItemIds.END_STONE);
		builder(ConventionalBlockTags.GRAVELS)
				.add(BlockItemIds.GRAVEL);
		builder(ConventionalBlockTags.NORMAL_OBSIDIANS)
				.add(BlockItemIds.OBSIDIAN);
		builder(ConventionalBlockTags.CRYING_OBSIDIANS)
				.add(BlockItemIds.CRYING_OBSIDIAN);
		builder(ConventionalBlockTags.OBSIDIANS)
				.addOptionalTag(ConventionalBlockTags.NORMAL_OBSIDIANS)
				.addOptionalTag(ConventionalBlockTags.CRYING_OBSIDIANS);

		builder(ConventionalBlockTags.COAL_ORES)
				.addOptionalTag(BlockItemTags.COAL_ORES.block());
		builder(ConventionalBlockTags.COPPER_ORES)
				.addOptionalTag(BlockTags.COPPER_ORES);
		builder(ConventionalBlockTags.DIAMOND_ORES)
				.addOptionalTag(BlockItemTags.DIAMOND_ORES.block());
		builder(ConventionalBlockTags.EMERALD_ORES)
				.addOptionalTag(BlockItemTags.EMERALD_ORES.block());
		builder(ConventionalBlockTags.GOLD_ORES)
				.addOptionalTag(BlockTags.GOLD_ORES);
		builder(ConventionalBlockTags.IRON_ORES)
				.addOptionalTag(BlockTags.IRON_ORES);
		builder(ConventionalBlockTags.LAPIS_ORES)
				.addOptionalTag(BlockItemTags.LAPIS_ORES.block());
		builder(ConventionalBlockTags.NETHERITE_SCRAP_ORES)
				.add(BlockItemIds.ANCIENT_DEBRIS);
		builder(ConventionalBlockTags.REDSTONE_ORES)
				.addOptionalTag(BlockItemTags.REDSTONE_ORES.block());
		builder(ConventionalBlockTags.QUARTZ_ORES)
				.add(BlockItemIds.NETHER_QUARTZ_ORE);
		builder(ConventionalBlockTags.ORES)
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

		builder(ConventionalBlockTags.ORE_BEARING_GROUND_DEEPSLATE)
				.add(BlockItemIds.DEEPSLATE);
		builder(ConventionalBlockTags.ORE_BEARING_GROUND_NETHERRACK)
				.add(BlockItemIds.NETHERRACK);
		builder(ConventionalBlockTags.ORE_BEARING_GROUND_STONE)
				.add(BlockItemIds.STONE);
		builder(ConventionalBlockTags.ORE_RATES_DENSE)
				.add(BlockItemIds.COPPER_ORE)
				.add(BlockItemIds.DEEPSLATE_COPPER_ORE)
				.add(BlockItemIds.DEEPSLATE_LAPIS_ORE)
				.add(BlockItemIds.DEEPSLATE_REDSTONE_ORE)
				.add(BlockItemIds.LAPIS_ORE)
				.add(BlockItemIds.REDSTONE_ORE);
		builder(ConventionalBlockTags.ORE_RATES_SINGULAR)
				.add(BlockItemIds.ANCIENT_DEBRIS)
				.add(BlockItemIds.COAL_ORE)
				.add(BlockItemIds.DEEPSLATE_COAL_ORE)
				.add(BlockItemIds.DEEPSLATE_DIAMOND_ORE)
				.add(BlockItemIds.DEEPSLATE_EMERALD_ORE)
				.add(BlockItemIds.DEEPSLATE_GOLD_ORE)
				.add(BlockItemIds.DEEPSLATE_IRON_ORE)
				.add(BlockItemIds.DIAMOND_ORE)
				.add(BlockItemIds.EMERALD_ORE)
				.add(BlockItemIds.GOLD_ORE)
				.add(BlockItemIds.IRON_ORE)
				.add(BlockItemIds.NETHER_QUARTZ_ORE);
		builder(ConventionalBlockTags.ORE_RATES_SPARSE)
				.add(BlockItemIds.NETHER_GOLD_ORE);
		builder(ConventionalBlockTags.ORES_IN_GROUND_DEEPSLATE)
				.add(BlockItemIds.DEEPSLATE_COAL_ORE)
				.add(BlockItemIds.DEEPSLATE_COPPER_ORE)
				.add(BlockItemIds.DEEPSLATE_DIAMOND_ORE)
				.add(BlockItemIds.DEEPSLATE_EMERALD_ORE)
				.add(BlockItemIds.DEEPSLATE_GOLD_ORE)
				.add(BlockItemIds.DEEPSLATE_IRON_ORE)
				.add(BlockItemIds.DEEPSLATE_LAPIS_ORE)
				.add(BlockItemIds.DEEPSLATE_REDSTONE_ORE);
		builder(ConventionalBlockTags.ORES_IN_GROUND_NETHERRACK)
				.add(BlockItemIds.NETHER_GOLD_ORE)
				.add(BlockItemIds.NETHER_QUARTZ_ORE);
		builder(ConventionalBlockTags.ORES_IN_GROUND_STONE)
				.add(BlockItemIds.COAL_ORE)
				.add(BlockItemIds.COPPER_ORE)
				.add(BlockItemIds.DIAMOND_ORE)
				.add(BlockItemIds.EMERALD_ORE)
				.add(BlockItemIds.GOLD_ORE)
				.add(BlockItemIds.IRON_ORE)
				.add(BlockItemIds.LAPIS_ORE)
				.add(BlockItemIds.REDSTONE_ORE);

		builder(ConventionalBlockTags.WOODEN_CHESTS)
				.add(BlockItemIds.CHEST)
				.add(BlockItemIds.TRAPPED_CHEST);
		builder(ConventionalBlockTags.TRAPPED_CHESTS)
				.add(BlockItemIds.TRAPPED_CHEST);
		builder(ConventionalBlockTags.ENDER_CHESTS)
				.add(BlockItemIds.ENDER_CHEST);
		builder(ConventionalBlockTags.CHESTS)
				.addTag(ConventionalBlockTags.WOODEN_CHESTS)
				.addTag(ConventionalBlockTags.TRAPPED_CHESTS)
				.addTag(ConventionalBlockTags.ENDER_CHESTS)
				.addOptionalTag(BlockTags.COPPER_CHESTS);
		builder(ConventionalBlockTags.BOOKSHELVES)
				.add(BlockItemIds.BOOKSHELF);
		generateGlassTags();
		generateGlazeTerracottaTags();
		generateConcreteTags();
		builder(ConventionalBlockTags.WOODEN_BARRELS)
				.add(BlockItemIds.BARREL);
		builder(ConventionalBlockTags.BARRELS)
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
		builder(ConventionalBlockTags.SMALL_FLOWERS)
				.add(BlockItemIds.DANDELION, BlockItemIds.POPPY, BlockItemIds.BLUE_ORCHID,
						BlockItemIds.ALLIUM, BlockItemIds.AZURE_BLUET, BlockItemIds.RED_TULIP,
						BlockItemIds.ORANGE_TULIP, BlockItemIds.WHITE_TULIP, BlockItemIds.PINK_TULIP,
						BlockItemIds.OXEYE_DAISY, BlockItemIds.CORNFLOWER, BlockItemIds.LILY_OF_THE_VALLEY,
						BlockItemIds.WITHER_ROSE, BlockItemIds.TORCHFLOWER, BlockItemIds.OPEN_EYEBLOSSOM,
						BlockItemIds.CLOSED_EYEBLOSSOM
				);

		builder(ConventionalBlockTags.TALL_FLOWERS)
				.add(BlockItemIds.SUNFLOWER, BlockItemIds.LILAC, BlockItemIds.PEONY,
						BlockItemIds.ROSE_BUSH, BlockItemIds.PITCHER_PLANT
				);

		builder(ConventionalBlockTags.FLOWERS)
				.add(BlockItemIds.CHERRY_LEAVES, BlockItemIds.FLOWERING_AZALEA_LEAVES, BlockItemIds.FLOWERING_AZALEA,
						BlockItemIds.MANGROVE_PROPAGULE, BlockItemIds.PINK_PETALS, BlockItemIds.WILDFLOWERS, BlockItemIds.CHORUS_FLOWER,
						BlockItemIds.SPORE_BLOSSOM, BlockItemIds.CACTUS_FLOWER
				).addOptionalTag(ConventionalBlockTags.SMALL_FLOWERS)
				.addOptionalTag(ConventionalBlockTags.TALL_FLOWERS);
	}

	private void generateMiscTags() {
		builder(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
				.add(BlockItemIds.CRAFTING_TABLE);
		builder(ConventionalBlockTags.PLAYER_WORKSTATIONS_FURNACES)
				.add(BlockItemIds.FURNACE);

		builder(ConventionalBlockTags.VILLAGER_JOB_SITES)
				.addAll(VILLAGER_JOB_SITE_BLOCKS.stream().map(BlockItemId::block))
				.addAll(VILLAGER_JOB_SITE_BLOCKS_WITHOUT_ITEMS);

		builder(ConventionalBlockTags.RELOCATION_NOT_SUPPORTED); // Generate tag so others can see it exists through JSON.

		builder(ConventionalBlockTags.ROPES); // Generate tag so others can see it exists through JSON.

		builder(ConventionalBlockTags.CHAINS)
				.add(BlockItemIds.IRON_CHAIN)
				.addAll(BlockItemIds.COPPER_CHAIN.asList().stream().map(BlockItemId::block));

		builder(ConventionalBlockTags.HIDDEN_FROM_RECIPE_VIEWERS); // Generate tag so others can see it exists through JSON.
	}

	private void generateFenceAndFenceGateTags() {
		builder(ConventionalBlockTags.WOODEN_FENCES)
				.add(BlockItemIds.OAK_FENCE)
				.add(BlockItemIds.SPRUCE_FENCE)
				.add(BlockItemIds.BIRCH_FENCE)
				.add(BlockItemIds.JUNGLE_FENCE)
				.add(BlockItemIds.ACACIA_FENCE)
				.add(BlockItemIds.DARK_OAK_FENCE)
				.add(BlockItemIds.CRIMSON_FENCE)
				.add(BlockItemIds.WARPED_FENCE)
				.add(BlockItemIds.MANGROVE_FENCE)
				.add(BlockItemIds.BAMBOO_FENCE)
				.add(BlockItemIds.CHERRY_FENCE)
				.add(BlockItemIds.PALE_OAK_FENCE);
		builder(ConventionalBlockTags.NETHER_BRICK_FENCES)
				.add(BlockItemIds.NETHER_BRICK_FENCE);
		builder(ConventionalBlockTags.FENCES)
				.addOptionalTag(ConventionalBlockTags.WOODEN_FENCES)
				.addOptionalTag(ConventionalBlockTags.NETHER_BRICK_FENCES);
		builder(ConventionalBlockTags.WOODEN_FENCE_GATES)
				.add(BlockItemIds.OAK_FENCE_GATE)
				.add(BlockItemIds.SPRUCE_FENCE_GATE)
				.add(BlockItemIds.BIRCH_FENCE_GATE)
				.add(BlockItemIds.JUNGLE_FENCE_GATE)
				.add(BlockItemIds.ACACIA_FENCE_GATE)
				.add(BlockItemIds.DARK_OAK_FENCE_GATE)
				.add(BlockItemIds.CRIMSON_FENCE_GATE)
				.add(BlockItemIds.WARPED_FENCE_GATE)
				.add(BlockItemIds.MANGROVE_FENCE_GATE)
				.add(BlockItemIds.BAMBOO_FENCE_GATE)
				.add(BlockItemIds.CHERRY_FENCE_GATE)
				.add(BlockItemIds.PALE_OAK_FENCE_GATE);
		builder(ConventionalBlockTags.FENCE_GATES)
				.addOptionalTag(ConventionalBlockTags.WOODEN_FENCE_GATES);
		builder(ConventionalBlockTags.IRON_BARS)
				.add(BlockItemIds.IRON_BARS);
		builder(ConventionalBlockTags.COPPER_BARS)
				.addAll(BlockItemIds.COPPER_BARS.asList().stream().map(BlockItemId::block));
		builder(ConventionalBlockTags.BARS)
				.addTag(ConventionalBlockTags.IRON_BARS)
				.addTag(ConventionalBlockTags.COPPER_BARS);
		builder(ConventionalBlockTags.PUMPKINS)
				.addTag(ConventionalBlockTags.NORMAL_PUMPKINS)
				.addTag(ConventionalBlockTags.CARVED_PUMPKINS)
				.addTag(ConventionalBlockTags.JACK_O_LANTERNS_PUMPKINS);
		builder(ConventionalBlockTags.NORMAL_PUMPKINS)
				.add(BlockItemIds.PUMPKIN);
		builder(ConventionalBlockTags.CARVED_PUMPKINS)
				.add(BlockItemIds.CARVED_PUMPKIN);
		builder(ConventionalBlockTags.JACK_O_LANTERNS_PUMPKINS)
				.add(BlockItemIds.JACK_O_LANTERN);
	}

	private void generateSandstoneTags() {
		builder(ConventionalBlockTags.COLORLESS_SANDS)
				.add(BlockItemIds.SAND);
		builder(ConventionalBlockTags.RED_SANDS)
				.add(BlockItemIds.RED_SAND);
		builder(ConventionalBlockTags.SANDS)
				.addOptionalTag(ConventionalBlockTags.COLORLESS_SANDS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDS);

		builder(ConventionalBlockTags.SANDSTONE_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_BLOCKS);
		builder(ConventionalBlockTags.SANDSTONE_SLABS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_SLABS);
		builder(ConventionalBlockTags.SANDSTONE_STAIRS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_STAIRS);

		builder(ConventionalBlockTags.RED_SANDSTONE_BLOCKS)
				.add(BlockItemIds.RED_SANDSTONE)
				.add(BlockItemIds.CUT_RED_SANDSTONE)
				.add(BlockItemIds.SMOOTH_RED_SANDSTONE)
				.add(BlockItemIds.CHISELED_RED_SANDSTONE);
		builder(ConventionalBlockTags.RED_SANDSTONE_SLABS)
				.add(BlockItemIds.RED_SANDSTONE_SLAB)
				.add(BlockItemIds.CUT_RED_SANDSTONE_SLAB)
				.add(BlockItemIds.SMOOTH_RED_SANDSTONE_SLAB);
		builder(ConventionalBlockTags.RED_SANDSTONE_STAIRS)
				.add(BlockItemIds.RED_SANDSTONE_STAIRS)
				.add(BlockItemIds.SMOOTH_RED_SANDSTONE_STAIRS);

		builder(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS)
				.add(BlockItemIds.SANDSTONE)
				.add(BlockItemIds.CUT_SANDSTONE)
				.add(BlockItemIds.SMOOTH_SANDSTONE)
				.add(BlockItemIds.CHISELED_SANDSTONE);
		builder(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS)
				.add(BlockItemIds.SANDSTONE_SLAB)
				.add(BlockItemIds.CUT_SANDSTONE_SLAB)
				.add(BlockItemIds.SMOOTH_SANDSTONE_SLAB);
		builder(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS)
				.add(BlockItemIds.SANDSTONE_STAIRS)
				.add(BlockItemIds.SMOOTH_SANDSTONE_STAIRS);
	}

	private void generateBuddingTags() {
		builder(ConventionalBlockTags.BUDDING_BLOCKS)
				.add(BlockItemIds.BUDDING_AMETHYST);
		builder(ConventionalBlockTags.BUDS)
				.add(BlockItemIds.SMALL_AMETHYST_BUD)
				.add(BlockItemIds.MEDIUM_AMETHYST_BUD)
				.add(BlockItemIds.LARGE_AMETHYST_BUD);
		builder(ConventionalBlockTags.CLUSTERS)
				.add(BlockItemIds.AMETHYST_CLUSTER);
	}

	private void generateGlassTags() {
		builder(ConventionalBlockTags.GLASS_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_CHEAP)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_TINTED);
		builder(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS)
				.add(BlockItemIds.GLASS);
		builder(ConventionalBlockTags.GLASS_BLOCKS_CHEAP)
				.add(BlockItemIds.GLASS)
				.addAll(BlockItemIds.STAINED_GLASS.asList().stream().map(BlockItemId::block));
		builder(ConventionalBlockTags.GLASS_BLOCKS_TINTED)
				.add(BlockItemIds.TINTED_GLASS);
		builder(ConventionalBlockTags.GLASS_PANES)
				.addAll(BlockItemIds.STAINED_GLASS_PANE.asList().stream().map(BlockItemId::block))
				.addOptionalTag(ConventionalBlockTags.GLASS_PANES_COLORLESS);
		builder(ConventionalBlockTags.GLASS_PANES_COLORLESS)
				.add(BlockItemIds.GLASS_PANE);
	}

	private void generateGlazeTerracottaTags() {
		builder(ConventionalBlockTags.GLAZED_TERRACOTTAS)
				.addAll(BlockItemIds.GLAZED_TERRACOTTA.asList().stream().map(BlockItemId::block));
	}

	private void generateConcreteTags() {
		builder(ConventionalBlockTags.CONCRETES)
				.addAll(BlockItemIds.CONCRETE.asList().stream().map(BlockItemId::block));
	}

	private void generateDyedTags() {
		builder(ConventionalBlockTags.BLACK_DYED)
				.add(BlockItemIds.BANNER.black()).add(BlockItemIds.BED.black()).add(BlockItemIds.DYED_CANDLE.black()).add(BlockItemIds.CARPET.black())
				.add(BlockItemIds.CONCRETE.black()).add(BlockItemIds.CONCRETE_POWDER.black()).add(BlockItemIds.GLAZED_TERRACOTTA.black())
				.add(BlockItemIds.DYED_SHULKER_BOX.black()).add(BlockItemIds.STAINED_GLASS.black()).add(BlockItemIds.STAINED_GLASS_PANE.black())
				.add(BlockItemIds.DYED_TERRACOTTA.black()).add(BlockIds.WALL_BANNER.black()).add(BlockItemIds.WOOL.black());

		builder(ConventionalBlockTags.BLUE_DYED)
				.add(BlockItemIds.BANNER.blue()).add(BlockItemIds.BED.blue()).add(BlockItemIds.DYED_CANDLE.blue()).add(BlockItemIds.CARPET.blue())
				.add(BlockItemIds.CONCRETE.blue()).add(BlockItemIds.CONCRETE_POWDER.blue()).add(BlockItemIds.GLAZED_TERRACOTTA.blue())
				.add(BlockItemIds.DYED_SHULKER_BOX.blue()).add(BlockItemIds.STAINED_GLASS.blue()).add(BlockItemIds.STAINED_GLASS_PANE.blue())
				.add(BlockItemIds.DYED_TERRACOTTA.blue()).add(BlockIds.WALL_BANNER.blue()).add(BlockItemIds.WOOL.blue());

		builder(ConventionalBlockTags.BROWN_DYED)
				.add(BlockItemIds.BANNER.brown()).add(BlockItemIds.BED.brown()).add(BlockItemIds.DYED_CANDLE.brown()).add(BlockItemIds.CARPET.brown())
				.add(BlockItemIds.CONCRETE.brown()).add(BlockItemIds.CONCRETE_POWDER.brown()).add(BlockItemIds.GLAZED_TERRACOTTA.brown())
				.add(BlockItemIds.DYED_SHULKER_BOX.brown()).add(BlockItemIds.STAINED_GLASS.brown()).add(BlockItemIds.STAINED_GLASS_PANE.brown())
				.add(BlockItemIds.DYED_TERRACOTTA.brown()).add(BlockIds.WALL_BANNER.brown()).add(BlockItemIds.WOOL.brown());

		builder(ConventionalBlockTags.CYAN_DYED)
				.add(BlockItemIds.BANNER.cyan()).add(BlockItemIds.BED.cyan()).add(BlockItemIds.DYED_CANDLE.cyan()).add(BlockItemIds.CARPET.cyan())
				.add(BlockItemIds.CONCRETE.cyan()).add(BlockItemIds.CONCRETE_POWDER.cyan()).add(BlockItemIds.GLAZED_TERRACOTTA.cyan())
				.add(BlockItemIds.DYED_SHULKER_BOX.cyan()).add(BlockItemIds.STAINED_GLASS.cyan()).add(BlockItemIds.STAINED_GLASS_PANE.cyan())
				.add(BlockItemIds.DYED_TERRACOTTA.cyan()).add(BlockIds.WALL_BANNER.cyan()).add(BlockItemIds.WOOL.cyan());

		builder(ConventionalBlockTags.GRAY_DYED)
				.add(BlockItemIds.BANNER.gray()).add(BlockItemIds.BED.gray()).add(BlockItemIds.DYED_CANDLE.gray()).add(BlockItemIds.CARPET.gray())
				.add(BlockItemIds.CONCRETE.gray()).add(BlockItemIds.CONCRETE_POWDER.gray()).add(BlockItemIds.GLAZED_TERRACOTTA.gray())
				.add(BlockItemIds.DYED_SHULKER_BOX.gray()).add(BlockItemIds.STAINED_GLASS.gray()).add(BlockItemIds.STAINED_GLASS_PANE.gray())
				.add(BlockItemIds.DYED_TERRACOTTA.gray()).add(BlockIds.WALL_BANNER.gray()).add(BlockItemIds.WOOL.gray());

		builder(ConventionalBlockTags.GREEN_DYED)
				.add(BlockItemIds.BANNER.green()).add(BlockItemIds.BED.green()).add(BlockItemIds.DYED_CANDLE.green()).add(BlockItemIds.CARPET.green())
				.add(BlockItemIds.CONCRETE.green()).add(BlockItemIds.CONCRETE_POWDER.green()).add(BlockItemIds.GLAZED_TERRACOTTA.green())
				.add(BlockItemIds.DYED_SHULKER_BOX.green()).add(BlockItemIds.STAINED_GLASS.green()).add(BlockItemIds.STAINED_GLASS_PANE.green())
				.add(BlockItemIds.DYED_TERRACOTTA.green()).add(BlockIds.WALL_BANNER.green()).add(BlockItemIds.WOOL.green());

		builder(ConventionalBlockTags.LIGHT_BLUE_DYED)
				.add(BlockItemIds.BANNER.lightBlue()).add(BlockItemIds.BED.lightBlue()).add(BlockItemIds.DYED_CANDLE.lightBlue()).add(BlockItemIds.CARPET.lightBlue())
				.add(BlockItemIds.CONCRETE.lightBlue()).add(BlockItemIds.CONCRETE_POWDER.lightBlue()).add(BlockItemIds.GLAZED_TERRACOTTA.lightBlue())
				.add(BlockItemIds.DYED_SHULKER_BOX.lightBlue()).add(BlockItemIds.STAINED_GLASS.lightBlue()).add(BlockItemIds.STAINED_GLASS_PANE.lightBlue())
				.add(BlockItemIds.DYED_TERRACOTTA.lightBlue()).add(BlockIds.WALL_BANNER.lightBlue()).add(BlockItemIds.WOOL.lightBlue());

		builder(ConventionalBlockTags.LIGHT_GRAY_DYED)
				.add(BlockItemIds.BANNER.lightGray()).add(BlockItemIds.BED.lightGray()).add(BlockItemIds.DYED_CANDLE.lightGray()).add(BlockItemIds.CARPET.lightGray())
				.add(BlockItemIds.CONCRETE.lightGray()).add(BlockItemIds.CONCRETE_POWDER.lightGray()).add(BlockItemIds.GLAZED_TERRACOTTA.lightGray())
				.add(BlockItemIds.DYED_SHULKER_BOX.lightGray()).add(BlockItemIds.STAINED_GLASS.lightGray()).add(BlockItemIds.STAINED_GLASS_PANE.lightGray())
				.add(BlockItemIds.DYED_TERRACOTTA.lightGray()).add(BlockIds.WALL_BANNER.lightGray()).add(BlockItemIds.WOOL.lightGray());

		builder(ConventionalBlockTags.LIME_DYED)
				.add(BlockItemIds.BANNER.lime()).add(BlockItemIds.BED.lime()).add(BlockItemIds.DYED_CANDLE.lime()).add(BlockItemIds.CARPET.lime())
				.add(BlockItemIds.CONCRETE.lime()).add(BlockItemIds.CONCRETE_POWDER.lime()).add(BlockItemIds.GLAZED_TERRACOTTA.lime())
				.add(BlockItemIds.DYED_SHULKER_BOX.lime()).add(BlockItemIds.STAINED_GLASS.lime()).add(BlockItemIds.STAINED_GLASS_PANE.lime())
				.add(BlockItemIds.DYED_TERRACOTTA.lime()).add(BlockIds.WALL_BANNER.lime()).add(BlockItemIds.WOOL.lime());

		builder(ConventionalBlockTags.MAGENTA_DYED)
				.add(BlockItemIds.BANNER.magenta()).add(BlockItemIds.BED.magenta()).add(BlockItemIds.DYED_CANDLE.magenta()).add(BlockItemIds.CARPET.magenta())
				.add(BlockItemIds.CONCRETE.magenta()).add(BlockItemIds.CONCRETE_POWDER.magenta()).add(BlockItemIds.GLAZED_TERRACOTTA.magenta())
				.add(BlockItemIds.DYED_SHULKER_BOX.magenta()).add(BlockItemIds.STAINED_GLASS.magenta()).add(BlockItemIds.STAINED_GLASS_PANE.magenta())
				.add(BlockItemIds.DYED_TERRACOTTA.magenta()).add(BlockIds.WALL_BANNER.magenta()).add(BlockItemIds.WOOL.magenta());

		builder(ConventionalBlockTags.ORANGE_DYED)
				.add(BlockItemIds.BANNER.orange()).add(BlockItemIds.BED.orange()).add(BlockItemIds.DYED_CANDLE.orange()).add(BlockItemIds.CARPET.orange())
				.add(BlockItemIds.CONCRETE.orange()).add(BlockItemIds.CONCRETE_POWDER.orange()).add(BlockItemIds.GLAZED_TERRACOTTA.orange())
				.add(BlockItemIds.DYED_SHULKER_BOX.orange()).add(BlockItemIds.STAINED_GLASS.orange()).add(BlockItemIds.STAINED_GLASS_PANE.orange())
				.add(BlockItemIds.DYED_TERRACOTTA.orange()).add(BlockIds.WALL_BANNER.orange()).add(BlockItemIds.WOOL.orange());

		builder(ConventionalBlockTags.PINK_DYED)
				.add(BlockItemIds.BANNER.pink()).add(BlockItemIds.BED.pink()).add(BlockItemIds.DYED_CANDLE.pink()).add(BlockItemIds.CARPET.pink())
				.add(BlockItemIds.CONCRETE.pink()).add(BlockItemIds.CONCRETE_POWDER.pink()).add(BlockItemIds.GLAZED_TERRACOTTA.pink())
				.add(BlockItemIds.DYED_SHULKER_BOX.pink()).add(BlockItemIds.STAINED_GLASS.pink()).add(BlockItemIds.STAINED_GLASS_PANE.pink())
				.add(BlockItemIds.DYED_TERRACOTTA.pink()).add(BlockIds.WALL_BANNER.pink()).add(BlockItemIds.WOOL.pink());

		builder(ConventionalBlockTags.PURPLE_DYED)
				.add(BlockItemIds.BANNER.purple()).add(BlockItemIds.BED.purple()).add(BlockItemIds.DYED_CANDLE.purple()).add(BlockItemIds.CARPET.purple())
				.add(BlockItemIds.CONCRETE.purple()).add(BlockItemIds.CONCRETE_POWDER.purple()).add(BlockItemIds.GLAZED_TERRACOTTA.purple())
				.add(BlockItemIds.DYED_SHULKER_BOX.purple()).add(BlockItemIds.STAINED_GLASS.purple()).add(BlockItemIds.STAINED_GLASS_PANE.purple())
				.add(BlockItemIds.DYED_TERRACOTTA.purple()).add(BlockIds.WALL_BANNER.purple()).add(BlockItemIds.WOOL.purple());

		builder(ConventionalBlockTags.RED_DYED)
				.add(BlockItemIds.BANNER.red()).add(BlockItemIds.BED.red()).add(BlockItemIds.DYED_CANDLE.red()).add(BlockItemIds.CARPET.red())
				.add(BlockItemIds.CONCRETE.red()).add(BlockItemIds.CONCRETE_POWDER.red()).add(BlockItemIds.GLAZED_TERRACOTTA.red())
				.add(BlockItemIds.DYED_SHULKER_BOX.red()).add(BlockItemIds.STAINED_GLASS.red()).add(BlockItemIds.STAINED_GLASS_PANE.red())
				.add(BlockItemIds.DYED_TERRACOTTA.red()).add(BlockIds.WALL_BANNER.red()).add(BlockItemIds.WOOL.red());

		builder(ConventionalBlockTags.WHITE_DYED)
				.add(BlockItemIds.BANNER.white()).add(BlockItemIds.BED.white()).add(BlockItemIds.DYED_CANDLE.white()).add(BlockItemIds.CARPET.white())
				.add(BlockItemIds.CONCRETE.white()).add(BlockItemIds.CONCRETE_POWDER.white()).add(BlockItemIds.GLAZED_TERRACOTTA.white())
				.add(BlockItemIds.DYED_SHULKER_BOX.white()).add(BlockItemIds.STAINED_GLASS.white()).add(BlockItemIds.STAINED_GLASS_PANE.white())
				.add(BlockItemIds.DYED_TERRACOTTA.white()).add(BlockIds.WALL_BANNER.white()).add(BlockItemIds.WOOL.white());

		builder(ConventionalBlockTags.YELLOW_DYED)
				.add(BlockItemIds.BANNER.yellow()).add(BlockItemIds.BED.yellow()).add(BlockItemIds.DYED_CANDLE.yellow()).add(BlockItemIds.CARPET.yellow())
				.add(BlockItemIds.CONCRETE.yellow()).add(BlockItemIds.CONCRETE_POWDER.yellow()).add(BlockItemIds.GLAZED_TERRACOTTA.yellow())
				.add(BlockItemIds.DYED_SHULKER_BOX.yellow()).add(BlockItemIds.STAINED_GLASS.yellow()).add(BlockItemIds.STAINED_GLASS_PANE.yellow())
				.add(BlockItemIds.DYED_TERRACOTTA.yellow()).add(BlockIds.WALL_BANNER.yellow()).add(BlockItemIds.WOOL.yellow());

		builder(ConventionalBlockTags.DYED)
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
		builder(ConventionalBlockTags.STORAGE_BLOCKS)
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

		builder(ConventionalBlockTags.STORAGE_BLOCKS_BONE_MEAL)
				.add(BlockItemIds.BONE_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_COAL)
				.add(BlockItemIds.COAL_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_COPPER)
				.add(BlockItemIds.COPPER_BLOCK.weathering().unaffected());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_DIAMOND)
				.add(BlockItemIds.DIAMOND_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_DRIED_KELP)
				.add(BlockItemIds.DRIED_KELP_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_EMERALD)
				.add(BlockItemIds.EMERALD_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_GOLD)
				.add(BlockItemIds.GOLD_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_IRON)
				.add(BlockItemIds.IRON_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_LAPIS)
				.add(BlockItemIds.LAPIS_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_NETHERITE)
				.add(BlockItemIds.NETHERITE_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_COPPER)
				.add(BlockItemIds.RAW_COPPER_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_GOLD)
				.add(BlockItemIds.RAW_GOLD_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON)
				.add(BlockItemIds.RAW_IRON_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_REDSTONE)
				.add(BlockItemIds.REDSTONE_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_RESIN)
				.add(BlockItemIds.RESIN_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_SLIME)
				.add(BlockItemIds.SLIME_BLOCK);

		builder(ConventionalBlockTags.STORAGE_BLOCKS_WHEAT)
				.add(BlockItemIds.HAY_BLOCK);
	}

	private void generateLogTags() {
		builder(ConventionalBlockTags.OVERWORLD_NATURAL_LOGS)
				.add(BlockItemIds.ACACIA_LOG)
				.add(BlockItemIds.BAMBOO_BLOCK)
				.add(BlockItemIds.BIRCH_LOG)
				.add(BlockItemIds.CHERRY_LOG)
				.add(BlockItemIds.DARK_OAK_LOG)
				.add(BlockItemIds.JUNGLE_LOG)
				.add(BlockItemIds.MANGROVE_LOG)
				.add(BlockItemIds.OAK_LOG)
				.add(BlockItemIds.PALE_OAK_LOG)
				.add(BlockItemIds.SPRUCE_LOG);

		builder(ConventionalBlockTags.NETHER_NATURAL_LOGS)
				.add(BlockItemIds.CRIMSON_STEM)
				.add(BlockItemIds.WARPED_STEM);

		builder(ConventionalBlockTags.NATURAL_LOGS)
				.addOptionalTag(ConventionalBlockTags.OVERWORLD_NATURAL_LOGS)
				.addOptionalTag(ConventionalBlockTags.NETHER_NATURAL_LOGS);

		builder(ConventionalBlockTags.NATURAL_WOODS)
				.add(BlockItemIds.ACACIA_WOOD)
				.add(BlockItemIds.BIRCH_WOOD)
				.add(BlockItemIds.CHERRY_WOOD)
				.add(BlockItemIds.DARK_OAK_WOOD)
				.add(BlockItemIds.JUNGLE_WOOD)
				.add(BlockItemIds.MANGROVE_WOOD)
				.add(BlockItemIds.OAK_WOOD)
				.add(BlockItemIds.PALE_OAK_WOOD)
				.add(BlockItemIds.SPRUCE_WOOD)
				.add(BlockItemIds.CRIMSON_HYPHAE)
				.add(BlockItemIds.WARPED_HYPHAE);

		builder(ConventionalBlockTags.STRIPPED_LOGS)
				.add(BlockItemIds.STRIPPED_ACACIA_LOG)
				.add(BlockItemIds.STRIPPED_BAMBOO_BLOCK)
				.add(BlockItemIds.STRIPPED_BIRCH_LOG)
				.add(BlockItemIds.STRIPPED_CHERRY_LOG)
				.add(BlockItemIds.STRIPPED_DARK_OAK_LOG)
				.add(BlockItemIds.STRIPPED_JUNGLE_LOG)
				.add(BlockItemIds.STRIPPED_MANGROVE_LOG)
				.add(BlockItemIds.STRIPPED_OAK_LOG)
				.add(BlockItemIds.STRIPPED_PALE_OAK_LOG)
				.add(BlockItemIds.STRIPPED_SPRUCE_LOG)
				.add(BlockItemIds.STRIPPED_CRIMSON_STEM)
				.add(BlockItemIds.STRIPPED_WARPED_STEM);

		builder(ConventionalBlockTags.STRIPPED_WOODS)
				.add(BlockItemIds.STRIPPED_ACACIA_WOOD)
				.add(BlockItemIds.STRIPPED_BIRCH_WOOD)
				.add(BlockItemIds.STRIPPED_CHERRY_WOOD)
				.add(BlockItemIds.STRIPPED_DARK_OAK_WOOD)
				.add(BlockItemIds.STRIPPED_JUNGLE_WOOD)
				.add(BlockItemIds.STRIPPED_MANGROVE_WOOD)
				.add(BlockItemIds.STRIPPED_OAK_WOOD)
				.add(BlockItemIds.STRIPPED_PALE_OAK_WOOD)
				.add(BlockItemIds.STRIPPED_SPRUCE_WOOD)
				.add(BlockItemIds.STRIPPED_CRIMSON_HYPHAE)
				.add(BlockItemIds.STRIPPED_WARPED_HYPHAE);
	}

	private void generateHeadTags() {
		builder(ConventionalBlockTags.SKULLS)
				.add(BlockItemIds.SKELETON_SKULL)
				.add(BlockIds.SKELETON_WALL_SKULL)
				.add(BlockItemIds.WITHER_SKELETON_SKULL)
				.add(BlockIds.WITHER_SKELETON_WALL_SKULL)
				.add(BlockItemIds.PLAYER_HEAD)
				.add(BlockIds.PLAYER_WALL_HEAD)
				.add(BlockItemIds.ZOMBIE_HEAD)
				.add(BlockIds.ZOMBIE_WALL_HEAD)
				.add(BlockItemIds.CREEPER_HEAD)
				.add(BlockIds.CREEPER_WALL_HEAD)
				.add(BlockItemIds.PIGLIN_HEAD)
				.add(BlockIds.PIGLIN_WALL_HEAD)
				.add(BlockItemIds.DRAGON_HEAD)
				.add(BlockIds.DRAGON_WALL_HEAD);
	}

	private void generateTagAlias() {
		aliasGroup("natural_logs/overworld").add(BlockTags.OVERWORLD_NATURAL_LOGS, ConventionalBlockTags.OVERWORLD_NATURAL_LOGS);

		aliasGroup("ores/coal").add(BlockItemTags.COAL_ORES.block(), ConventionalBlockTags.COAL_ORES);
		aliasGroup("ores/copper").add(BlockTags.COPPER_ORES, ConventionalBlockTags.COPPER_ORES);
		aliasGroup("ores/diamond").add(BlockItemTags.DIAMOND_ORES.block(), ConventionalBlockTags.DIAMOND_ORES);
		aliasGroup("ores/emerald").add(BlockItemTags.EMERALD_ORES.block(), ConventionalBlockTags.EMERALD_ORES);
		aliasGroup("ores/gold").add(BlockTags.GOLD_ORES, ConventionalBlockTags.GOLD_ORES);
		aliasGroup("ores/iron").add(BlockTags.IRON_ORES, ConventionalBlockTags.IRON_ORES);
		aliasGroup("ores/lapis").add(BlockItemTags.LAPIS_ORES.block(), ConventionalBlockTags.LAPIS_ORES);
		aliasGroup("ores/redstone").add(BlockItemTags.REDSTONE_ORES.block(), ConventionalBlockTags.REDSTONE_ORES);

		aliasGroup("fences").add(BlockTags.FENCES, ConventionalBlockTags.FENCES);
		aliasGroup("fences/wooden").add(BlockTags.WOODEN_FENCES, ConventionalBlockTags.WOODEN_FENCES);
		aliasGroup("fence_gates").add(BlockTags.FENCE_GATES, ConventionalBlockTags.FENCE_GATES);

		aliasGroup("bars").add(BlockTags.BARS, ConventionalBlockTags.BARS);

		aliasGroup("flowers").add(BlockTags.FLOWERS, ConventionalBlockTags.FLOWERS);
	}
}
