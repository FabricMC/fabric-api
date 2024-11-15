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

package net.fabricmc.fabric.api.tag.convention.v2;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;

/**
 * See {@link net.minecraft.registry.tag.BlockTags} for vanilla tags.
 * Note that addition to some vanilla tags implies having certain functionality.
 */
public final class ConventionalBlockTags {
	private ConventionalBlockTags() {
	}

	/**
	 * Natural stone-like blocks that can be used as a base ingredient in recipes that take stone.
	 */
	public static final TagKey<Block> STONES = register("stones");
	public static final TagKey<Block> COBBLESTONES = register("cobblestones");
	public static final TagKey<Block> OBSIDIANS = register("obsidians");
	/**
	 * For common obsidian that has no special quirks or behaviors. Ideal for recipe use.
	 * Crying Obsidian, for example, is a light block and harder to obtain. So it gets its own tag instead of being under normal tag.
	 */
	public static final TagKey<Block> NORMAL_OBSIDIANS = register("obsidians/normal");
	public static final TagKey<Block> CRYING_OBSIDIANS = register("obsidians/crying");

	// Ores - broad categories
	public static final TagKey<Block> ORES = register("ores");

	// Ores - vanilla instances
	public static final TagKey<Block> QUARTZ_ORES = register("ores/quartz");
	public static final TagKey<Block> NETHERITE_SCRAP_ORES = register("ores/netherite_scrap");

	public static final TagKey<Block> BARRELS = register("barrels");
	public static final TagKey<Block> WOODEN_BARRELS = register("barrels/wooden");
	public static final TagKey<Block> BOOKSHELVES = register("bookshelves");
	public static final TagKey<Block> CHESTS = register("chests");
	public static final TagKey<Block> WOODEN_CHESTS = register("chests/wooden");
	public static final TagKey<Block> GLASS_BLOCKS = register("glass_blocks");
	public static final TagKey<Block> GLASS_BLOCKS_COLORLESS = register("glass_blocks/colorless");
	/**
	 * Glass which is made from cheap resources like sand and only minor additional ingredients like dyes.
	 */
	public static final TagKey<Block> GLASS_BLOCKS_CHEAP = register("glass_blocks/cheap");
	public static final TagKey<Block> GLASS_BLOCKS_TINTED = register("glass_blocks/tinted");
	public static final TagKey<Block> GLASS_PANES = register("glass_panes");
	public static final TagKey<Block> GLASS_PANES_COLORLESS = register("glass_panes/colorless");
	public static final TagKey<Block> GLAZED_TERRACOTTAS = register("glazed_terracottas");
	public static final TagKey<Block> CONCRETES = register("concretes");

	// Related to budding mechanics
	/**
	 * For blocks that are similar to amethyst where their budding block produces buds and cluster blocks.
	 */
	public static final TagKey<Block> BUDDING_BLOCKS = register("budding_blocks");
	/**
	 * For blocks that are similar to amethyst where they have buddings forming from budding blocks.
	 */
	public static final TagKey<Block> BUDS = register("buds");
	/**
	 * For blocks that are similar to amethyst where they have clusters forming from budding blocks.
	 */
	public static final TagKey<Block> CLUSTERS = register("clusters");

	public static final TagKey<Block> VILLAGER_JOB_SITES = register("villager_job_sites");

	// Sandstone
	public static final TagKey<Block> SANDSTONE_BLOCKS = register("sandstone/blocks");
	public static final TagKey<Block> SANDSTONE_SLABS = register("sandstone/slabs");
	public static final TagKey<Block> SANDSTONE_STAIRS = register("sandstone/stairs");
	public static final TagKey<Block> RED_SANDSTONE_BLOCKS = register("sandstone/red_blocks");
	public static final TagKey<Block> RED_SANDSTONE_SLABS = register("sandstone/red_slabs");
	public static final TagKey<Block> RED_SANDSTONE_STAIRS = register("sandstone/red_stairs");
	public static final TagKey<Block> UNCOLORED_SANDSTONE_BLOCKS = register("sandstone/uncolored_blocks");
	public static final TagKey<Block> UNCOLORED_SANDSTONE_SLABS = register("sandstone/uncolored_slabs");
	public static final TagKey<Block> UNCOLORED_SANDSTONE_STAIRS = register("sandstone/uncolored_stairs");

	// Blocks created with dyes
	/**
	 * Tag that holds all blocks that can be dyed a specific color.
	 * (Does not include color blending blocks that would behave similar to leather armor item)
	 */
	public static final TagKey<Block> DYED = register("dyed");
	public static final TagKey<Block> BLACK_DYED = register("dyed/black");
	public static final TagKey<Block> BLUE_DYED = register("dyed/blue");
	public static final TagKey<Block> BROWN_DYED = register("dyed/brown");
	public static final TagKey<Block> CYAN_DYED = register("dyed/cyan");
	public static final TagKey<Block> GRAY_DYED = register("dyed/gray");
	public static final TagKey<Block> GREEN_DYED = register("dyed/green");
	public static final TagKey<Block> LIGHT_BLUE_DYED = register("dyed/light_blue");
	public static final TagKey<Block> LIGHT_GRAY_DYED = register("dyed/light_gray");
	public static final TagKey<Block> LIME_DYED = register("dyed/lime");
	public static final TagKey<Block> MAGENTA_DYED = register("dyed/magenta");
	public static final TagKey<Block> ORANGE_DYED = register("dyed/orange");
	public static final TagKey<Block> PINK_DYED = register("dyed/pink");
	public static final TagKey<Block> PURPLE_DYED = register("dyed/purple");
	public static final TagKey<Block> RED_DYED = register("dyed/red");
	public static final TagKey<Block> WHITE_DYED = register("dyed/white");
	public static final TagKey<Block> YELLOW_DYED = register("dyed/yellow");

	// Blocks that are for storing resources
	/**
	 * A storage block is generally a block that has a recipe to craft a bulk of 1 kind of resource to a block
	 * and has a mirror recipe to reverse the crafting with no loss in resources.
	 * <p></p>
	 * Honey Block is special in that the reversing recipe is not a perfect mirror of the crafting recipe
	 * and so, it is considered a special case and not given a storage block tag.
	 */
	public static final TagKey<Block> STORAGE_BLOCKS = register("storage_blocks");
	public static final TagKey<Block> STORAGE_BLOCKS_BONE_MEAL = register("storage_blocks/bone_meal");
	public static final TagKey<Block> STORAGE_BLOCKS_COAL = register("storage_blocks/coal");
	public static final TagKey<Block> STORAGE_BLOCKS_COPPER = register("storage_blocks/copper");
	public static final TagKey<Block> STORAGE_BLOCKS_DIAMOND = register("storage_blocks/diamond");
	public static final TagKey<Block> STORAGE_BLOCKS_DRIED_KELP = register("storage_blocks/dried_kelp");
	public static final TagKey<Block> STORAGE_BLOCKS_EMERALD = register("storage_blocks/emerald");
	public static final TagKey<Block> STORAGE_BLOCKS_GOLD = register("storage_blocks/gold");
	public static final TagKey<Block> STORAGE_BLOCKS_IRON = register("storage_blocks/iron");
	public static final TagKey<Block> STORAGE_BLOCKS_LAPIS = register("storage_blocks/lapis");
	public static final TagKey<Block> STORAGE_BLOCKS_NETHERITE = register("storage_blocks/netherite");
	public static final TagKey<Block> STORAGE_BLOCKS_RAW_COPPER = register("storage_blocks/raw_copper");
	public static final TagKey<Block> STORAGE_BLOCKS_RAW_GOLD = register("storage_blocks/raw_gold");
	public static final TagKey<Block> STORAGE_BLOCKS_RAW_IRON = register("storage_blocks/raw_iron");
	public static final TagKey<Block> STORAGE_BLOCKS_REDSTONE = register("storage_blocks/redstone");
	public static final TagKey<Block> STORAGE_BLOCKS_SLIME = register("storage_blocks/slime");
	public static final TagKey<Block> STORAGE_BLOCKS_WHEAT = register("storage_blocks/wheat");

	// Logs
	public static final TagKey<Block> STRIPPED_LOGS = register("stripped_logs");
	public static final TagKey<Block> STRIPPED_WOODS = register("stripped_woods");

	// Misc
	public static final TagKey<Block> PLAYER_WORKSTATIONS_CRAFTING_TABLES = register("player_workstations/crafting_tables");
	public static final TagKey<Block> PLAYER_WORKSTATIONS_FURNACES = register("player_workstations/furnaces");
	/**
	 * Blocks should be included in this tag if their movement/relocation can cause serious issues such
	 * as world corruption upon being moved or for balance reason where the block should not be able to be relocated.
	 * Example: Chunk loaders or pipes where other mods that move blocks do not respect
	 * {@link AbstractBlock.AbstractBlockState#getPistonBehavior}.
	 */
	public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = register("relocation_not_supported");
	/**
	 * Tag that holds all head based blocks such as Skeleton Skull or Player Head. (Named skulls to match minecraft:skulls item tag)
	 */
	public static final TagKey<Block> SKULLS = register("skulls");
	public static final TagKey<Block> ROPES = register("ropes");
	public static final TagKey<Block> CHAINS = register("chains");

	/**
	 * Tag that holds all blocks that recipe viewers should not show to users.
	 * Recipe viewers may use this to automatically find the corresponding BlockItem to hide.
	 */
	public static final TagKey<Block> HIDDEN_FROM_RECIPE_VIEWERS = register("hidden_from_recipe_viewers");

	/**
	 * This tag is redundant. Please use {@link net.minecraft.registry.tag.BlockTags#SHULKER_BOXES} tag instead.
	 */
	@Deprecated
	public static final TagKey<Block> SHULKER_BOXES = register("shulker_boxes");
	/**
	 * This tag was typoed. Please use {@link ConventionalBlockTags#GLAZED_TERRACOTTAS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Block> GLAZED_TERRACOTTA = register("glazed_terracotta");
	/**
	 * This tag was typoed. Please use {@link ConventionalBlockTags#CONCRETES} tag instead.
	 */
	@Deprecated
	public static final TagKey<Block> CONCRETE = register("concrete");

	private static TagKey<Block> register(String tagId) {
		return TagRegistration.BLOCK_TAG.registerC(tagId);
	}
}
