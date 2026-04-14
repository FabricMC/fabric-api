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
				.add(BlockItemIds.STONE.block())
				.add(BlockItemIds.ANDESITE.block())
				.add(BlockItemIds.DIORITE.block())
				.add(BlockItemIds.GRANITE.block())
				.add(BlockItemIds.TUFF.block())
				.add(BlockItemIds.DEEPSLATE.block());
		builder(ConventionalBlockTags.NORMAL_COBBLESTONES)
				.add(BlockItemIds.COBBLESTONE.block());
		builder(ConventionalBlockTags.MOSSY_COBBLESTONES)
				.add(BlockItemIds.MOSSY_COBBLESTONE.block());
		builder(ConventionalBlockTags.INFESTED_COBBLESTONES)
				.add(BlockItemIds.INFESTED_COBBLESTONE.block());
		builder(ConventionalBlockTags.DEEPSLATE_COBBLESTONES)
				.add(BlockItemIds.COBBLED_DEEPSLATE.block());
		builder(ConventionalBlockTags.COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.NORMAL_COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.MOSSY_COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.INFESTED_COBBLESTONES)
				.addOptionalTag(ConventionalBlockTags.DEEPSLATE_COBBLESTONES);
		builder(ConventionalBlockTags.NETHERRACKS)
				.add(BlockItemIds.NETHERRACK.block());
		builder(ConventionalBlockTags.END_STONES)
				.add(BlockItemIds.END_STONE.block());
		builder(ConventionalBlockTags.GRAVELS)
				.add(BlockItemIds.GRAVEL.block());
		builder(ConventionalBlockTags.NORMAL_OBSIDIANS)
				.add(BlockItemIds.OBSIDIAN.block());
		builder(ConventionalBlockTags.CRYING_OBSIDIANS)
				.add(BlockItemIds.CRYING_OBSIDIAN.block());
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
				.add(BlockItemIds.ANCIENT_DEBRIS.block());
		builder(ConventionalBlockTags.REDSTONE_ORES)
				.addOptionalTag(BlockItemTags.REDSTONE_ORES.block());
		builder(ConventionalBlockTags.QUARTZ_ORES)
				.add(BlockItemIds.NETHER_QUARTZ_ORE.block());
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
				.add(BlockItemIds.DEEPSLATE.block());
		builder(ConventionalBlockTags.ORE_BEARING_GROUND_NETHERRACK)
				.add(BlockItemIds.NETHERRACK.block());
		builder(ConventionalBlockTags.ORE_BEARING_GROUND_STONE)
				.add(BlockItemIds.STONE.block());
		builder(ConventionalBlockTags.ORE_RATES_DENSE)
				.add(BlockItemIds.COPPER_ORE.block())
				.add(BlockItemIds.DEEPSLATE_COPPER_ORE.block())
				.add(BlockItemIds.DEEPSLATE_LAPIS_ORE.block())
				.add(BlockItemIds.DEEPSLATE_REDSTONE_ORE.block())
				.add(BlockItemIds.LAPIS_ORE.block())
				.add(BlockItemIds.REDSTONE_ORE.block());
		builder(ConventionalBlockTags.ORE_RATES_SINGULAR)
				.add(BlockItemIds.ANCIENT_DEBRIS.block())
				.add(BlockItemIds.COAL_ORE.block())
				.add(BlockItemIds.DEEPSLATE_COAL_ORE.block())
				.add(BlockItemIds.DEEPSLATE_DIAMOND_ORE.block())
				.add(BlockItemIds.DEEPSLATE_EMERALD_ORE.block())
				.add(BlockItemIds.DEEPSLATE_GOLD_ORE.block())
				.add(BlockItemIds.DEEPSLATE_IRON_ORE.block())
				.add(BlockItemIds.DIAMOND_ORE.block())
				.add(BlockItemIds.EMERALD_ORE.block())
				.add(BlockItemIds.GOLD_ORE.block())
				.add(BlockItemIds.IRON_ORE.block())
				.add(BlockItemIds.NETHER_QUARTZ_ORE.block());
		builder(ConventionalBlockTags.ORE_RATES_SPARSE)
				.add(BlockItemIds.NETHER_GOLD_ORE.block());
		builder(ConventionalBlockTags.ORES_IN_GROUND_DEEPSLATE)
				.add(BlockItemIds.DEEPSLATE_COAL_ORE.block())
				.add(BlockItemIds.DEEPSLATE_COPPER_ORE.block())
				.add(BlockItemIds.DEEPSLATE_DIAMOND_ORE.block())
				.add(BlockItemIds.DEEPSLATE_EMERALD_ORE.block())
				.add(BlockItemIds.DEEPSLATE_GOLD_ORE.block())
				.add(BlockItemIds.DEEPSLATE_IRON_ORE.block())
				.add(BlockItemIds.DEEPSLATE_LAPIS_ORE.block())
				.add(BlockItemIds.DEEPSLATE_REDSTONE_ORE.block());
		builder(ConventionalBlockTags.ORES_IN_GROUND_NETHERRACK)
				.add(BlockItemIds.NETHER_GOLD_ORE.block())
				.add(BlockItemIds.NETHER_QUARTZ_ORE.block());
		builder(ConventionalBlockTags.ORES_IN_GROUND_STONE)
				.add(BlockItemIds.COAL_ORE.block())
				.add(BlockItemIds.COPPER_ORE.block())
				.add(BlockItemIds.DIAMOND_ORE.block())
				.add(BlockItemIds.EMERALD_ORE.block())
				.add(BlockItemIds.GOLD_ORE.block())
				.add(BlockItemIds.IRON_ORE.block())
				.add(BlockItemIds.LAPIS_ORE.block())
				.add(BlockItemIds.REDSTONE_ORE.block());

		builder(ConventionalBlockTags.WOODEN_CHESTS)
				.add(BlockItemIds.CHEST.block())
				.add(BlockItemIds.TRAPPED_CHEST.block());
		builder(ConventionalBlockTags.TRAPPED_CHESTS)
				.add(BlockItemIds.TRAPPED_CHEST.block());
		builder(ConventionalBlockTags.ENDER_CHESTS)
				.add(BlockItemIds.ENDER_CHEST.block());
		builder(ConventionalBlockTags.CHESTS)
				.addTag(ConventionalBlockTags.WOODEN_CHESTS)
				.addTag(ConventionalBlockTags.TRAPPED_CHESTS)
				.addTag(ConventionalBlockTags.ENDER_CHESTS)
				.addOptionalTag(BlockTags.COPPER_CHESTS);
		builder(ConventionalBlockTags.BOOKSHELVES)
				.add(BlockItemIds.BOOKSHELF.block());
		generateGlassTags();
		generateGlazeTerracottaTags();
		generateConcreteTags();
		builder(ConventionalBlockTags.WOODEN_BARRELS)
				.add(BlockItemIds.BARREL.block());
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
				.add(BlockItemIds.DANDELION.block(), BlockItemIds.POPPY.block(), BlockItemIds.BLUE_ORCHID.block(),
						BlockItemIds.ALLIUM.block(), BlockItemIds.AZURE_BLUET.block(), BlockItemIds.RED_TULIP.block(),
						BlockItemIds.ORANGE_TULIP.block(), BlockItemIds.WHITE_TULIP.block(), BlockItemIds.PINK_TULIP.block(),
						BlockItemIds.OXEYE_DAISY.block(), BlockItemIds.CORNFLOWER.block(), BlockItemIds.LILY_OF_THE_VALLEY.block(),
						BlockItemIds.WITHER_ROSE.block(), BlockItemIds.TORCHFLOWER.block(), BlockItemIds.OPEN_EYEBLOSSOM.block(),
						BlockItemIds.CLOSED_EYEBLOSSOM.block()
				);

		builder(ConventionalBlockTags.TALL_FLOWERS)
				.add(BlockItemIds.SUNFLOWER.block(), BlockItemIds.LILAC.block(), BlockItemIds.PEONY.block(),
						BlockItemIds.ROSE_BUSH.block(), BlockItemIds.PITCHER_PLANT.block()
				);

		builder(ConventionalBlockTags.FLOWERS)
				.add(BlockItemIds.CHERRY_LEAVES.block(), BlockItemIds.FLOWERING_AZALEA_LEAVES.block(), BlockItemIds.FLOWERING_AZALEA.block(),
						BlockItemIds.MANGROVE_PROPAGULE.block(), BlockItemIds.PINK_PETALS.block(), BlockItemIds.WILDFLOWERS.block(), BlockItemIds.CHORUS_FLOWER.block(),
						BlockItemIds.SPORE_BLOSSOM.block(), BlockItemIds.CACTUS_FLOWER.block()
				).addOptionalTag(ConventionalBlockTags.SMALL_FLOWERS)
				.addOptionalTag(ConventionalBlockTags.TALL_FLOWERS);
	}

	private void generateMiscTags() {
		builder(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
				.add(BlockItemIds.CRAFTING_TABLE.block());
		builder(ConventionalBlockTags.PLAYER_WORKSTATIONS_FURNACES)
				.add(BlockItemIds.FURNACE.block());

		builder(ConventionalBlockTags.VILLAGER_JOB_SITES)
				.addAll(VILLAGER_JOB_SITE_BLOCKS.stream().map(BlockItemId::block))
				.addAll(VILLAGER_JOB_SITE_BLOCKS_WITHOUT_ITEMS);

		builder(ConventionalBlockTags.RELOCATION_NOT_SUPPORTED); // Generate tag so others can see it exists through JSON.

		builder(ConventionalBlockTags.ROPES); // Generate tag so others can see it exists through JSON.

		builder(ConventionalBlockTags.CHAINS)
				.add(BlockItemIds.IRON_CHAIN.block())
				.addAll(BlockItemIds.COPPER_CHAIN.asList().stream().map(BlockItemId::block));

		builder(ConventionalBlockTags.HIDDEN_FROM_RECIPE_VIEWERS); // Generate tag so others can see it exists through JSON.
	}

	private void generateFenceAndFenceGateTags() {
		builder(ConventionalBlockTags.WOODEN_FENCES)
				.add(BlockItemIds.OAK_FENCE.block())
				.add(BlockItemIds.SPRUCE_FENCE.block())
				.add(BlockItemIds.BIRCH_FENCE.block())
				.add(BlockItemIds.JUNGLE_FENCE.block())
				.add(BlockItemIds.ACACIA_FENCE.block())
				.add(BlockItemIds.DARK_OAK_FENCE.block())
				.add(BlockItemIds.CRIMSON_FENCE.block())
				.add(BlockItemIds.WARPED_FENCE.block())
				.add(BlockItemIds.MANGROVE_FENCE.block())
				.add(BlockItemIds.BAMBOO_FENCE.block())
				.add(BlockItemIds.CHERRY_FENCE.block())
				.add(BlockItemIds.PALE_OAK_FENCE.block());
		builder(ConventionalBlockTags.NETHER_BRICK_FENCES)
				.add(BlockItemIds.NETHER_BRICK_FENCE.block());
		builder(ConventionalBlockTags.FENCES)
				.addOptionalTag(ConventionalBlockTags.WOODEN_FENCES)
				.addOptionalTag(ConventionalBlockTags.NETHER_BRICK_FENCES);
		builder(ConventionalBlockTags.WOODEN_FENCE_GATES)
				.add(BlockItemIds.OAK_FENCE_GATE.block())
				.add(BlockItemIds.SPRUCE_FENCE_GATE.block())
				.add(BlockItemIds.BIRCH_FENCE_GATE.block())
				.add(BlockItemIds.JUNGLE_FENCE_GATE.block())
				.add(BlockItemIds.ACACIA_FENCE_GATE.block())
				.add(BlockItemIds.DARK_OAK_FENCE_GATE.block())
				.add(BlockItemIds.CRIMSON_FENCE_GATE.block())
				.add(BlockItemIds.WARPED_FENCE_GATE.block())
				.add(BlockItemIds.MANGROVE_FENCE_GATE.block())
				.add(BlockItemIds.BAMBOO_FENCE_GATE.block())
				.add(BlockItemIds.CHERRY_FENCE_GATE.block())
				.add(BlockItemIds.PALE_OAK_FENCE_GATE.block());
		builder(ConventionalBlockTags.FENCE_GATES)
				.addOptionalTag(ConventionalBlockTags.WOODEN_FENCE_GATES);
		builder(ConventionalBlockTags.PUMPKINS)
				.addTag(ConventionalBlockTags.NORMAL_PUMPKINS)
				.addTag(ConventionalBlockTags.CARVED_PUMPKINS)
				.addTag(ConventionalBlockTags.JACK_O_LANTERNS_PUMPKINS);
		builder(ConventionalBlockTags.NORMAL_PUMPKINS)
				.add(BlockItemIds.PUMPKIN.block());
		builder(ConventionalBlockTags.CARVED_PUMPKINS)
				.add(BlockItemIds.CARVED_PUMPKIN.block());
		builder(ConventionalBlockTags.JACK_O_LANTERNS_PUMPKINS)
				.add(BlockItemIds.JACK_O_LANTERN.block());
	}

	private void generateSandstoneTags() {
		builder(ConventionalBlockTags.COLORLESS_SANDS)
				.add(BlockItemIds.SAND.block());
		builder(ConventionalBlockTags.RED_SANDS)
				.add(BlockItemIds.RED_SAND.block());
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
				.add(BlockItemIds.RED_SANDSTONE.block())
				.add(BlockItemIds.CUT_RED_SANDSTONE.block())
				.add(BlockItemIds.SMOOTH_RED_SANDSTONE.block())
				.add(BlockItemIds.CHISELED_RED_SANDSTONE.block());
		builder(ConventionalBlockTags.RED_SANDSTONE_SLABS)
				.add(BlockItemIds.RED_SANDSTONE_SLAB.block())
				.add(BlockItemIds.CUT_RED_SANDSTONE_SLAB.block())
				.add(BlockItemIds.SMOOTH_RED_SANDSTONE_SLAB.block());
		builder(ConventionalBlockTags.RED_SANDSTONE_STAIRS)
				.add(BlockItemIds.RED_SANDSTONE_STAIRS.block())
				.add(BlockItemIds.SMOOTH_RED_SANDSTONE_STAIRS.block());

		builder(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS)
				.add(BlockItemIds.SANDSTONE.block())
				.add(BlockItemIds.CUT_SANDSTONE.block())
				.add(BlockItemIds.SMOOTH_SANDSTONE.block())
				.add(BlockItemIds.CHISELED_SANDSTONE.block());
		builder(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS)
				.add(BlockItemIds.SANDSTONE_SLAB.block())
				.add(BlockItemIds.CUT_SANDSTONE_SLAB.block())
				.add(BlockItemIds.SMOOTH_SANDSTONE_SLAB.block());
		builder(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS)
				.add(BlockItemIds.SANDSTONE_STAIRS.block())
				.add(BlockItemIds.SMOOTH_SANDSTONE_STAIRS.block());
	}

	private void generateBuddingTags() {
		builder(ConventionalBlockTags.BUDDING_BLOCKS)
				.add(BlockItemIds.BUDDING_AMETHYST.block());
		builder(ConventionalBlockTags.BUDS)
				.add(BlockItemIds.SMALL_AMETHYST_BUD.block())
				.add(BlockItemIds.MEDIUM_AMETHYST_BUD.block())
				.add(BlockItemIds.LARGE_AMETHYST_BUD.block());
		builder(ConventionalBlockTags.CLUSTERS)
				.add(BlockItemIds.AMETHYST_CLUSTER.block());
	}

	private void generateGlassTags() {
		builder(ConventionalBlockTags.GLASS_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_CHEAP)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_TINTED);
		builder(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS)
				.add(BlockItemIds.GLASS.block());
		builder(ConventionalBlockTags.GLASS_BLOCKS_CHEAP)
				.add(BlockItemIds.GLASS.block())
				.addAll(BlockItemIds.STAINED_GLASS.asList().stream().map(BlockItemId::block));
		builder(ConventionalBlockTags.GLASS_BLOCKS_TINTED)
				.add(BlockItemIds.TINTED_GLASS.block());
		builder(ConventionalBlockTags.GLASS_PANES)
				.addAll(BlockItemIds.STAINED_GLASS_PANE.asList().stream().map(BlockItemId::block))
				.addOptionalTag(ConventionalBlockTags.GLASS_PANES_COLORLESS);
		builder(ConventionalBlockTags.GLASS_PANES_COLORLESS)
				.add(BlockItemIds.GLASS_PANE.block());
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
				.add(BlockItemIds.BANNER.black().block()).add(BlockItemIds.BED.black().block()).add(BlockItemIds.DYED_CANDLE.black().block()).add(BlockItemIds.CARPET.black().block())
				.add(BlockItemIds.CONCRETE.black().block()).add(BlockItemIds.CONCRETE_POWDER.black().block()).add(BlockItemIds.GLAZED_TERRACOTTA.black().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.black().block()).add(BlockItemIds.STAINED_GLASS.black().block()).add(BlockItemIds.STAINED_GLASS_PANE.black().block())
				.add(BlockItemIds.DYED_TERRACOTTA.black().block()).add(BlockIds.WALL_BANNER.black()).add(BlockItemIds.WOOL.black().block());

		builder(ConventionalBlockTags.BLUE_DYED)
				.add(BlockItemIds.BANNER.blue().block()).add(BlockItemIds.BED.blue().block()).add(BlockItemIds.DYED_CANDLE.blue().block()).add(BlockItemIds.CARPET.blue().block())
				.add(BlockItemIds.CONCRETE.blue().block()).add(BlockItemIds.CONCRETE_POWDER.blue().block()).add(BlockItemIds.GLAZED_TERRACOTTA.blue().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.blue().block()).add(BlockItemIds.STAINED_GLASS.blue().block()).add(BlockItemIds.STAINED_GLASS_PANE.blue().block())
				.add(BlockItemIds.DYED_TERRACOTTA.blue().block()).add(BlockIds.WALL_BANNER.blue()).add(BlockItemIds.WOOL.blue().block());

		builder(ConventionalBlockTags.BROWN_DYED)
				.add(BlockItemIds.BANNER.brown().block()).add(BlockItemIds.BED.brown().block()).add(BlockItemIds.DYED_CANDLE.brown().block()).add(BlockItemIds.CARPET.brown().block())
				.add(BlockItemIds.CONCRETE.brown().block()).add(BlockItemIds.CONCRETE_POWDER.brown().block()).add(BlockItemIds.GLAZED_TERRACOTTA.brown().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.brown().block()).add(BlockItemIds.STAINED_GLASS.brown().block()).add(BlockItemIds.STAINED_GLASS_PANE.brown().block())
				.add(BlockItemIds.DYED_TERRACOTTA.brown().block()).add(BlockIds.WALL_BANNER.brown()).add(BlockItemIds.WOOL.brown().block());

		builder(ConventionalBlockTags.CYAN_DYED)
				.add(BlockItemIds.BANNER.cyan().block()).add(BlockItemIds.BED.cyan().block()).add(BlockItemIds.DYED_CANDLE.cyan().block()).add(BlockItemIds.CARPET.cyan().block())
				.add(BlockItemIds.CONCRETE.cyan().block()).add(BlockItemIds.CONCRETE_POWDER.cyan().block()).add(BlockItemIds.GLAZED_TERRACOTTA.cyan().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.cyan().block()).add(BlockItemIds.STAINED_GLASS.cyan().block()).add(BlockItemIds.STAINED_GLASS_PANE.cyan().block())
				.add(BlockItemIds.DYED_TERRACOTTA.cyan().block()).add(BlockIds.WALL_BANNER.cyan()).add(BlockItemIds.WOOL.cyan().block());

		builder(ConventionalBlockTags.GRAY_DYED)
				.add(BlockItemIds.BANNER.gray().block()).add(BlockItemIds.BED.gray().block()).add(BlockItemIds.DYED_CANDLE.gray().block()).add(BlockItemIds.CARPET.gray().block())
				.add(BlockItemIds.CONCRETE.gray().block()).add(BlockItemIds.CONCRETE_POWDER.gray().block()).add(BlockItemIds.GLAZED_TERRACOTTA.gray().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.gray().block()).add(BlockItemIds.STAINED_GLASS.gray().block()).add(BlockItemIds.STAINED_GLASS_PANE.gray().block())
				.add(BlockItemIds.DYED_TERRACOTTA.gray().block()).add(BlockIds.WALL_BANNER.gray()).add(BlockItemIds.WOOL.gray().block());

		builder(ConventionalBlockTags.GREEN_DYED)
				.add(BlockItemIds.BANNER.green().block()).add(BlockItemIds.BED.green().block()).add(BlockItemIds.DYED_CANDLE.green().block()).add(BlockItemIds.CARPET.green().block())
				.add(BlockItemIds.CONCRETE.green().block()).add(BlockItemIds.CONCRETE_POWDER.green().block()).add(BlockItemIds.GLAZED_TERRACOTTA.green().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.green().block()).add(BlockItemIds.STAINED_GLASS.green().block()).add(BlockItemIds.STAINED_GLASS_PANE.green().block())
				.add(BlockItemIds.DYED_TERRACOTTA.green().block()).add(BlockIds.WALL_BANNER.green()).add(BlockItemIds.WOOL.green().block());

		builder(ConventionalBlockTags.LIGHT_BLUE_DYED)
				.add(BlockItemIds.BANNER.lightBlue().block()).add(BlockItemIds.BED.lightBlue().block()).add(BlockItemIds.DYED_CANDLE.lightBlue().block()).add(BlockItemIds.CARPET.lightBlue().block())
				.add(BlockItemIds.CONCRETE.lightBlue().block()).add(BlockItemIds.CONCRETE_POWDER.lightBlue().block()).add(BlockItemIds.GLAZED_TERRACOTTA.lightBlue().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.lightBlue().block()).add(BlockItemIds.STAINED_GLASS.lightBlue().block()).add(BlockItemIds.STAINED_GLASS_PANE.lightBlue().block())
				.add(BlockItemIds.DYED_TERRACOTTA.lightBlue().block()).add(BlockIds.WALL_BANNER.lightBlue()).add(BlockItemIds.WOOL.lightBlue().block());

		builder(ConventionalBlockTags.LIGHT_GRAY_DYED)
				.add(BlockItemIds.BANNER.lightGray().block()).add(BlockItemIds.BED.lightGray().block()).add(BlockItemIds.DYED_CANDLE.lightGray().block()).add(BlockItemIds.CARPET.lightGray().block())
				.add(BlockItemIds.CONCRETE.lightGray().block()).add(BlockItemIds.CONCRETE_POWDER.lightGray().block()).add(BlockItemIds.GLAZED_TERRACOTTA.lightGray().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.lightGray().block()).add(BlockItemIds.STAINED_GLASS.lightGray().block()).add(BlockItemIds.STAINED_GLASS_PANE.lightGray().block())
				.add(BlockItemIds.DYED_TERRACOTTA.lightGray().block()).add(BlockIds.WALL_BANNER.lightGray()).add(BlockItemIds.WOOL.lightGray().block());

		builder(ConventionalBlockTags.LIME_DYED)
				.add(BlockItemIds.BANNER.lime().block()).add(BlockItemIds.BED.lime().block()).add(BlockItemIds.DYED_CANDLE.lime().block()).add(BlockItemIds.CARPET.lime().block())
				.add(BlockItemIds.CONCRETE.lime().block()).add(BlockItemIds.CONCRETE_POWDER.lime().block()).add(BlockItemIds.GLAZED_TERRACOTTA.lime().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.lime().block()).add(BlockItemIds.STAINED_GLASS.lime().block()).add(BlockItemIds.STAINED_GLASS_PANE.lime().block())
				.add(BlockItemIds.DYED_TERRACOTTA.lime().block()).add(BlockIds.WALL_BANNER.lime()).add(BlockItemIds.WOOL.lime().block());

		builder(ConventionalBlockTags.MAGENTA_DYED)
				.add(BlockItemIds.BANNER.magenta().block()).add(BlockItemIds.BED.magenta().block()).add(BlockItemIds.DYED_CANDLE.magenta().block()).add(BlockItemIds.CARPET.magenta().block())
				.add(BlockItemIds.CONCRETE.magenta().block()).add(BlockItemIds.CONCRETE_POWDER.magenta().block()).add(BlockItemIds.GLAZED_TERRACOTTA.magenta().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.magenta().block()).add(BlockItemIds.STAINED_GLASS.magenta().block()).add(BlockItemIds.STAINED_GLASS_PANE.magenta().block())
				.add(BlockItemIds.DYED_TERRACOTTA.magenta().block()).add(BlockIds.WALL_BANNER.magenta()).add(BlockItemIds.WOOL.magenta().block());

		builder(ConventionalBlockTags.ORANGE_DYED)
				.add(BlockItemIds.BANNER.orange().block()).add(BlockItemIds.BED.orange().block()).add(BlockItemIds.DYED_CANDLE.orange().block()).add(BlockItemIds.CARPET.orange().block())
				.add(BlockItemIds.CONCRETE.orange().block()).add(BlockItemIds.CONCRETE_POWDER.orange().block()).add(BlockItemIds.GLAZED_TERRACOTTA.orange().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.orange().block()).add(BlockItemIds.STAINED_GLASS.orange().block()).add(BlockItemIds.STAINED_GLASS_PANE.orange().block())
				.add(BlockItemIds.DYED_TERRACOTTA.orange().block()).add(BlockIds.WALL_BANNER.orange()).add(BlockItemIds.WOOL.orange().block());

		builder(ConventionalBlockTags.PINK_DYED)
				.add(BlockItemIds.BANNER.pink().block()).add(BlockItemIds.BED.pink().block()).add(BlockItemIds.DYED_CANDLE.pink().block()).add(BlockItemIds.CARPET.pink().block())
				.add(BlockItemIds.CONCRETE.pink().block()).add(BlockItemIds.CONCRETE_POWDER.pink().block()).add(BlockItemIds.GLAZED_TERRACOTTA.pink().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.pink().block()).add(BlockItemIds.STAINED_GLASS.pink().block()).add(BlockItemIds.STAINED_GLASS_PANE.pink().block())
				.add(BlockItemIds.DYED_TERRACOTTA.pink().block()).add(BlockIds.WALL_BANNER.pink()).add(BlockItemIds.WOOL.pink().block());

		builder(ConventionalBlockTags.PURPLE_DYED)
				.add(BlockItemIds.BANNER.purple().block()).add(BlockItemIds.BED.purple().block()).add(BlockItemIds.DYED_CANDLE.purple().block()).add(BlockItemIds.CARPET.purple().block())
				.add(BlockItemIds.CONCRETE.purple().block()).add(BlockItemIds.CONCRETE_POWDER.purple().block()).add(BlockItemIds.GLAZED_TERRACOTTA.purple().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.purple().block()).add(BlockItemIds.STAINED_GLASS.purple().block()).add(BlockItemIds.STAINED_GLASS_PANE.purple().block())
				.add(BlockItemIds.DYED_TERRACOTTA.purple().block()).add(BlockIds.WALL_BANNER.purple()).add(BlockItemIds.WOOL.purple().block());

		builder(ConventionalBlockTags.RED_DYED)
				.add(BlockItemIds.BANNER.red().block()).add(BlockItemIds.BED.red().block()).add(BlockItemIds.DYED_CANDLE.red().block()).add(BlockItemIds.CARPET.red().block())
				.add(BlockItemIds.CONCRETE.red().block()).add(BlockItemIds.CONCRETE_POWDER.red().block()).add(BlockItemIds.GLAZED_TERRACOTTA.red().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.red().block()).add(BlockItemIds.STAINED_GLASS.red().block()).add(BlockItemIds.STAINED_GLASS_PANE.red().block())
				.add(BlockItemIds.DYED_TERRACOTTA.red().block()).add(BlockIds.WALL_BANNER.red()).add(BlockItemIds.WOOL.red().block());

		builder(ConventionalBlockTags.WHITE_DYED)
				.add(BlockItemIds.BANNER.white().block()).add(BlockItemIds.BED.white().block()).add(BlockItemIds.DYED_CANDLE.white().block()).add(BlockItemIds.CARPET.white().block())
				.add(BlockItemIds.CONCRETE.white().block()).add(BlockItemIds.CONCRETE_POWDER.white().block()).add(BlockItemIds.GLAZED_TERRACOTTA.white().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.white().block()).add(BlockItemIds.STAINED_GLASS.white().block()).add(BlockItemIds.STAINED_GLASS_PANE.white().block())
				.add(BlockItemIds.DYED_TERRACOTTA.white().block()).add(BlockIds.WALL_BANNER.white()).add(BlockItemIds.WOOL.white().block());

		builder(ConventionalBlockTags.YELLOW_DYED)
				.add(BlockItemIds.BANNER.yellow().block()).add(BlockItemIds.BED.yellow().block()).add(BlockItemIds.DYED_CANDLE.yellow().block()).add(BlockItemIds.CARPET.yellow().block())
				.add(BlockItemIds.CONCRETE.yellow().block()).add(BlockItemIds.CONCRETE_POWDER.yellow().block()).add(BlockItemIds.GLAZED_TERRACOTTA.yellow().block())
				.add(BlockItemIds.DYED_SHULKER_BOX.yellow().block()).add(BlockItemIds.STAINED_GLASS.yellow().block()).add(BlockItemIds.STAINED_GLASS_PANE.yellow().block())
				.add(BlockItemIds.DYED_TERRACOTTA.yellow().block()).add(BlockIds.WALL_BANNER.yellow()).add(BlockItemIds.WOOL.yellow().block());

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
				.add(BlockItemIds.BONE_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_COAL)
				.add(BlockItemIds.COAL_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_COPPER)
				.add(BlockItemIds.COPPER_BLOCK.weathering().unaffected().block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_DIAMOND)
				.add(BlockItemIds.DIAMOND_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_DRIED_KELP)
				.add(BlockItemIds.DRIED_KELP_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_EMERALD)
				.add(BlockItemIds.EMERALD_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_GOLD)
				.add(BlockItemIds.GOLD_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_IRON)
				.add(BlockItemIds.IRON_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_LAPIS)
				.add(BlockItemIds.LAPIS_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_NETHERITE)
				.add(BlockItemIds.NETHERITE_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_COPPER)
				.add(BlockItemIds.RAW_COPPER_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_GOLD)
				.add(BlockItemIds.RAW_GOLD_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON)
				.add(BlockItemIds.RAW_IRON_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_REDSTONE)
				.add(BlockItemIds.REDSTONE_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_RESIN)
				.add(BlockItemIds.RESIN_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_SLIME)
				.add(BlockItemIds.SLIME_BLOCK.block());

		builder(ConventionalBlockTags.STORAGE_BLOCKS_WHEAT)
				.add(BlockItemIds.HAY_BLOCK.block());
	}

	private void generateLogTags() {
		builder(ConventionalBlockTags.OVERWORLD_NATURAL_LOGS)
				.add(BlockItemIds.ACACIA_LOG.block())
				.add(BlockItemIds.BAMBOO_BLOCK.block())
				.add(BlockItemIds.BIRCH_LOG.block())
				.add(BlockItemIds.CHERRY_LOG.block())
				.add(BlockItemIds.DARK_OAK_LOG.block())
				.add(BlockItemIds.JUNGLE_LOG.block())
				.add(BlockItemIds.MANGROVE_LOG.block())
				.add(BlockItemIds.OAK_LOG.block())
				.add(BlockItemIds.PALE_OAK_LOG.block())
				.add(BlockItemIds.SPRUCE_LOG.block());

		builder(ConventionalBlockTags.NETHER_NATURAL_LOGS)
				.add(BlockItemIds.CRIMSON_STEM.block())
				.add(BlockItemIds.WARPED_STEM.block());

		builder(ConventionalBlockTags.NATURAL_LOGS)
				.addOptionalTag(ConventionalBlockTags.OVERWORLD_NATURAL_LOGS)
				.addOptionalTag(ConventionalBlockTags.NETHER_NATURAL_LOGS);

		builder(ConventionalBlockTags.NATURAL_WOODS)
				.add(BlockItemIds.ACACIA_WOOD.block())
				.add(BlockItemIds.BIRCH_WOOD.block())
				.add(BlockItemIds.CHERRY_WOOD.block())
				.add(BlockItemIds.DARK_OAK_WOOD.block())
				.add(BlockItemIds.JUNGLE_WOOD.block())
				.add(BlockItemIds.MANGROVE_WOOD.block())
				.add(BlockItemIds.OAK_WOOD.block())
				.add(BlockItemIds.PALE_OAK_WOOD.block())
				.add(BlockItemIds.SPRUCE_WOOD.block())
				.add(BlockItemIds.CRIMSON_HYPHAE.block())
				.add(BlockItemIds.WARPED_HYPHAE.block());

		builder(ConventionalBlockTags.STRIPPED_LOGS)
				.add(BlockItemIds.STRIPPED_ACACIA_LOG.block())
				.add(BlockItemIds.STRIPPED_BAMBOO_BLOCK.block())
				.add(BlockItemIds.STRIPPED_BIRCH_LOG.block())
				.add(BlockItemIds.STRIPPED_CHERRY_LOG.block())
				.add(BlockItemIds.STRIPPED_DARK_OAK_LOG.block())
				.add(BlockItemIds.STRIPPED_JUNGLE_LOG.block())
				.add(BlockItemIds.STRIPPED_MANGROVE_LOG.block())
				.add(BlockItemIds.STRIPPED_OAK_LOG.block())
				.add(BlockItemIds.STRIPPED_PALE_OAK_LOG.block())
				.add(BlockItemIds.STRIPPED_SPRUCE_LOG.block())
				.add(BlockItemIds.STRIPPED_CRIMSON_STEM.block())
				.add(BlockItemIds.STRIPPED_WARPED_STEM.block());

		builder(ConventionalBlockTags.STRIPPED_WOODS)
				.add(BlockItemIds.STRIPPED_ACACIA_WOOD.block())
				.add(BlockItemIds.STRIPPED_BIRCH_WOOD.block())
				.add(BlockItemIds.STRIPPED_CHERRY_WOOD.block())
				.add(BlockItemIds.STRIPPED_DARK_OAK_WOOD.block())
				.add(BlockItemIds.STRIPPED_JUNGLE_WOOD.block())
				.add(BlockItemIds.STRIPPED_MANGROVE_WOOD.block())
				.add(BlockItemIds.STRIPPED_OAK_WOOD.block())
				.add(BlockItemIds.STRIPPED_PALE_OAK_WOOD.block())
				.add(BlockItemIds.STRIPPED_SPRUCE_WOOD.block())
				.add(BlockItemIds.STRIPPED_CRIMSON_HYPHAE.block())
				.add(BlockItemIds.STRIPPED_WARPED_HYPHAE.block());
	}

	private void generateHeadTags() {
		builder(ConventionalBlockTags.SKULLS)
				.add(BlockItemIds.SKELETON_SKULL.block())
				.add(BlockIds.SKELETON_WALL_SKULL)
				.add(BlockItemIds.WITHER_SKELETON_SKULL.block())
				.add(BlockIds.WITHER_SKELETON_WALL_SKULL)
				.add(BlockItemIds.PLAYER_HEAD.block())
				.add(BlockIds.PLAYER_WALL_HEAD)
				.add(BlockItemIds.ZOMBIE_HEAD.block())
				.add(BlockIds.ZOMBIE_WALL_HEAD)
				.add(BlockItemIds.CREEPER_HEAD.block())
				.add(BlockIds.CREEPER_WALL_HEAD)
				.add(BlockItemIds.PIGLIN_HEAD.block())
				.add(BlockIds.PIGLIN_WALL_HEAD)
				.add(BlockItemIds.DRAGON_HEAD.block())
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

		aliasGroup("flowers").add(BlockTags.FLOWERS, ConventionalBlockTags.FLOWERS);
	}
}
