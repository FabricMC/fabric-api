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

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColorCollection;

/**
 * See {@link net.minecraft.tags.BlockItemTags} for vanilla tags.
 * Note that addition to some vanilla tags implies having certain functionality.
 */
public final class ConventionalBlockItemTags {
	private ConventionalBlockItemTags() {
	}

	/**
	 * Natural stone-like blocks that can be used as a base ingredient in recipes that take stone.
	 */
	public static final BlockItemTagId STONES = register("stones");
	public static final BlockItemTagId COBBLESTONES = register("cobblestones");
	public static final BlockItemTagId DEEPSLATE_COBBLESTONES = register("cobblestones/deepslate");
	public static final BlockItemTagId INFESTED_COBBLESTONES = register("cobblestones/infested");
	public static final BlockItemTagId MOSSY_COBBLESTONES = register("cobblestones/mossy");
	public static final BlockItemTagId NORMAL_COBBLESTONES = register("cobblestones/normal");
	public static final BlockItemTagId NETHERRACKS = register("netherracks");
	public static final BlockItemTagId END_STONES = register("end_stones");
	public static final BlockItemTagId GRAVELS = register("gravels");
	public static final BlockItemTagId OBSIDIANS = register("obsidians");
	/**
	 * For common obsidian that has no special quirks or behaviors. Ideal for recipe use.
	 * Crying Obsidian, for example, is a light block and harder to obtain. So it gets its own tag instead of being under normal tag.
	 */
	public static final BlockItemTagId NORMAL_OBSIDIANS = register("obsidians/normal");
	public static final BlockItemTagId CRYING_OBSIDIANS = register("obsidians/crying");
	/// Light-emitting blocks created when a Frog eats a Magma Cube.
	public static final BlockItemTagId FROGLIGHTS = register("froglights");

	// Ores - broad categories
	public static final BlockItemTagId ORES = register("ores");

	// Ores - vanilla instances (All ores consolidated here for consistency)
	/**
	 * Aliased with {@link BlockItemTags#COAL_ORES}.
	 */
	public static final BlockItemTagId COAL_ORES = register("ores/coal");
	/**
	 * Aliased with {@link BlockTags#COPPER_ORES}.
	 */
	public static final BlockItemTagId COPPER_ORES = register("ores/copper");
	/**
	 * Aliased with {@link BlockItemTags#DIAMOND_ORES}.
	 */
	public static final BlockItemTagId DIAMOND_ORES = register("ores/diamond");
	/**
	 * Aliased with {@link BlockItemTags#EMERALD_ORES}.
	 */
	public static final BlockItemTagId EMERALD_ORES = register("ores/emerald");
	/**
	 * Aliased with {@link BlockTags#GOLD_ORES}.
	 */
	public static final BlockItemTagId GOLD_ORES = register("ores/gold");
	/**
	 * Aliased with {@link BlockTags#IRON_ORES}.
	 */
	public static final BlockItemTagId IRON_ORES = register("ores/iron");
	/**
	 * Aliased with {@link BlockItemTags#LAPIS_ORES}.
	 */
	public static final BlockItemTagId LAPIS_ORES = register("ores/lapis");
	public static final BlockItemTagId NETHERITE_SCRAP_ORES = register("ores/netherite_scrap");
	public static final BlockItemTagId QUARTZ_ORES = register("ores/quartz");
	/**
	 * Aliased with {@link BlockItemTags#REDSTONE_ORES}.
	 */
	public static final BlockItemTagId REDSTONE_ORES = register("ores/redstone");

	public static final BlockItemTagId BARRELS = register("barrels");
	public static final BlockItemTagId WOODEN_BARRELS = register("barrels/wooden");
	public static final BlockItemTagId BOOKSHELVES = register("bookshelves");
	public static final BlockItemTagId CHESTS = register("chests");
	public static final BlockItemTagId WOODEN_CHESTS = register("chests/wooden");
	public static final BlockItemTagId TRAPPED_CHESTS = register("chests/trapped");
	public static final BlockItemTagId ENDER_CHESTS = register("chests/ender");
	public static final BlockItemTagId GLASS_BLOCKS = register("glass_blocks");
	public static final BlockItemTagId GLASS_BLOCKS_COLORLESS = register("glass_blocks/colorless");
	/**
	 * Glass which is made from cheap resources like sand and only minor additional ingredients like dyes.
	 */
	public static final BlockItemTagId GLASS_BLOCKS_CHEAP = register("glass_blocks/cheap");
	public static final BlockItemTagId GLASS_BLOCKS_TINTED = register("glass_blocks/tinted");
	public static final BlockItemTagId GLASS_PANES = register("glass_panes");
	public static final BlockItemTagId GLASS_PANES_COLORLESS = register("glass_panes/colorless");
	public static final BlockItemTagId GLAZED_TERRACOTTAS = register("glazed_terracottas");
	public static final BlockItemTagId CONCRETES = register("concretes");

	// Related to budding mechanics
	/**
	 * For blocks that are similar to amethyst where their budding block produces buds and cluster blocks.
	 */
	public static final BlockItemTagId BUDDING_BLOCKS = register("budding_blocks");
	/**
	 * For blocks that are similar to amethyst where they have buddings forming from budding blocks.
	 */
	public static final BlockItemTagId BUDS = register("buds");
	/**
	 * For blocks that are similar to amethyst where they have clusters forming from budding blocks.
	 */
	public static final BlockItemTagId CLUSTERS = register("clusters");

	public static final BlockItemTagId VILLAGER_JOB_SITES = register("villager_job_sites");

	// Sand
	public static final BlockItemTagId SANDS = register("sands");
	public static final BlockItemTagId RED_SANDS = register("sands/red");
	public static final BlockItemTagId COLORLESS_SANDS = register("sands/colorless");

	// Flower
	/**
	 * Contains living ground-based flowers that are 1 block tall such as Dandelions or Poppy.
	 * Equivalent to the "minecraft:small_flowers" block tag.
	 * This is NOT aliased with {@link BlockTags#SMALL_FLOWERS} because the vanilla tag is used to make the block weak to swords.
	 */
	public static final BlockItemTagId SMALL_FLOWERS = register("flowers/small");
	/**
	 * Contains living ground-based flowers that are 2 block tall such as Rose Bush or Peony.
	 * Equivalent to the "minecraft:tall_flowers" block tag in past Minecraft version.
	 */
	public static final BlockItemTagId TALL_FLOWERS = register("flowers/tall");
	/**
	 * Contains any living plant block that contains flowers or is a flower itself.
	 * Equivalent to the "minecraft:flowers" block tag.
	 * Aliased with {@link BlockTags#FLOWERS}.
	 */
	public static final BlockItemTagId FLOWERS = register("flowers");

	// Sandstone
	public static final BlockItemTagId SANDSTONE_BLOCKS = register("sandstone/blocks");
	public static final BlockItemTagId SANDSTONE_SLABS = register("sandstone/slabs");
	public static final BlockItemTagId SANDSTONE_STAIRS = register("sandstone/stairs");
	public static final BlockItemTagId RED_SANDSTONE_BLOCKS = register("sandstone/red_blocks");
	public static final BlockItemTagId RED_SANDSTONE_SLABS = register("sandstone/red_slabs");
	public static final BlockItemTagId RED_SANDSTONE_STAIRS = register("sandstone/red_stairs");
	public static final BlockItemTagId UNCOLORED_SANDSTONE_BLOCKS = register("sandstone/uncolored_blocks");
	public static final BlockItemTagId UNCOLORED_SANDSTONE_SLABS = register("sandstone/uncolored_slabs");
	public static final BlockItemTagId UNCOLORED_SANDSTONE_STAIRS = register("sandstone/uncolored_stairs");

	// Fences and Fence Gates
	/**
	 * Aliased with {@link BlockTags#FENCES}.
	 */
	public static final BlockItemTagId FENCES = register("fences");
	/**
	 * Aliased with {@link BlockTags#WOODEN_FENCES}.
	 */
	public static final BlockItemTagId WOODEN_FENCES = register("fences/wooden");
	public static final BlockItemTagId NETHER_BRICK_FENCES = register("fences/nether_brick");
	/**
	 * Aliased with {@link BlockTags#FENCE_GATES}.
	 */
	public static final BlockItemTagId FENCE_GATES = register("fence_gates");
	public static final BlockItemTagId WOODEN_FENCE_GATES = register("fence_gates/wooden");

	// Bars
	/**
	 * Aliased with {@link BlockTags#BARS}.
	 */
	public static final BlockItemTagId BARS = register("bars");
	public static final BlockItemTagId IRON_BARS = register("bars/iron");
	public static final BlockItemTagId COPPER_BARS = register("bars/copper");

	// Pumpkins
	public static final BlockItemTagId PUMPKINS = register("pumpkins");
	/**
	 * For pumpkins that are not carved.
	 */
	public static final BlockItemTagId NORMAL_PUMPKINS = register("pumpkins/normal");
	/**
	 * For pumpkins that are already carved but not a light source.
	 */
	public static final BlockItemTagId CARVED_PUMPKINS = register("pumpkins/carved");

	/**
	 * For pumpkins that are already carved and a light source.
	 */
	public static final BlockItemTagId JACK_O_LANTERNS_PUMPKINS = register("pumpkins/jack_o_lanterns");

	// Blocks created with dyes
	/**
	 * Tag that holds all blocks which can be dyed a specific color.
	 * (Does not include color blending blocks which would behave similarly to leather armor items)
	 */
	public static final BlockItemTagId DYED = register("dyed");
	public static final BlockItemTagId BLACK_DYED = register("dyed/black");
	public static final BlockItemTagId BLUE_DYED = register("dyed/blue");
	public static final BlockItemTagId BROWN_DYED = register("dyed/brown");
	public static final BlockItemTagId CYAN_DYED = register("dyed/cyan");
	public static final BlockItemTagId GRAY_DYED = register("dyed/gray");
	public static final BlockItemTagId GREEN_DYED = register("dyed/green");
	public static final BlockItemTagId LIGHT_BLUE_DYED = register("dyed/light_blue");
	public static final BlockItemTagId LIGHT_GRAY_DYED = register("dyed/light_gray");
	public static final BlockItemTagId LIME_DYED = register("dyed/lime");
	public static final BlockItemTagId MAGENTA_DYED = register("dyed/magenta");
	public static final BlockItemTagId ORANGE_DYED = register("dyed/orange");
	public static final BlockItemTagId PINK_DYED = register("dyed/pink");
	public static final BlockItemTagId PURPLE_DYED = register("dyed/purple");
	public static final BlockItemTagId RED_DYED = register("dyed/red");
	public static final BlockItemTagId WHITE_DYED = register("dyed/white");
	public static final BlockItemTagId YELLOW_DYED = register("dyed/yellow");

	/**
	 * Dyed color tags as a color collection, for convenience.
	 */
	public static final ColorCollection<BlockItemTagId> COLOR_DYED = new ColorCollection<>(
			WHITE_DYED,
			ORANGE_DYED,
			MAGENTA_DYED,
			LIGHT_BLUE_DYED,
			YELLOW_DYED,
			LIME_DYED,
			PINK_DYED,
			GRAY_DYED,
			LIGHT_GRAY_DYED,
			CYAN_DYED,
			PURPLE_DYED,
			BLUE_DYED,
			BROWN_DYED,
			GREEN_DYED,
			RED_DYED,
			BLACK_DYED
	);

	/**
	 * Tag that holds blocks which can be dyed but do not have their own color already, like glass.
	 * (Does not include color blending blocks which would behave similarly to leather armor items)
	 */
	public static final BlockItemTagId UNDYED_SIMPLE_DYEABLE = register("dyeable/simple/undyed");

	/**
	 * Tag that holds blocks which can be dyed despite already having a color, like wool.
	 * (Does not include color blending blocks which would behave similarly to leather armor items)
	 */
	public static final BlockItemTagId REDYEABLE_SIMPLE_DYEABLE = register("dyeable/simple/redyeable");

	/**
	 * Tag that holds blocks which can be dyed in a simple fashion without color blending, typically
	 * in the standard 16 colors, whether they have a color already or not.
	 */
	public static final BlockItemTagId SIMPLE_DYEABLE = register("dyeable/simple");

	/**
	 * Tag that holds blocks which can be dyed in a dynamic color blending fashion, similarly to leather armor items.
	 */
	public static final BlockItemTagId DYNAMIC_DYEABLE = register("dyeable/dynamic");

	/**
	 * Tag that holds blocks which can have dye applied to them, whether they have a color already or not.
	 */
	public static final BlockItemTagId DYEABLE = register("dyeable");

	// Blocks that are for storing resources
	/**
	 * A storage block is generally a block that has a recipe to craft a bulk of 1 kind of resource to a block
	 * and has a mirror recipe to reverse the crafting with no loss in resources.
	 *
	 * <p>Honey Block is special in that the reversing recipe is not a perfect mirror of the crafting recipe
	 * and so, it is considered a special case and not given a storage block tag.
	 */
	public static final BlockItemTagId STORAGE_BLOCKS = register("storage_blocks");
	public static final BlockItemTagId STORAGE_BLOCKS_BONE_MEAL = register("storage_blocks/bone_meal");
	public static final BlockItemTagId STORAGE_BLOCKS_COAL = register("storage_blocks/coal");
	public static final BlockItemTagId STORAGE_BLOCKS_COPPER = register("storage_blocks/copper");
	public static final BlockItemTagId STORAGE_BLOCKS_DIAMOND = register("storage_blocks/diamond");
	public static final BlockItemTagId STORAGE_BLOCKS_DRIED_KELP = register("storage_blocks/dried_kelp");
	public static final BlockItemTagId STORAGE_BLOCKS_EMERALD = register("storage_blocks/emerald");
	public static final BlockItemTagId STORAGE_BLOCKS_GOLD = register("storage_blocks/gold");
	public static final BlockItemTagId STORAGE_BLOCKS_IRON = register("storage_blocks/iron");
	public static final BlockItemTagId STORAGE_BLOCKS_LAPIS = register("storage_blocks/lapis");
	public static final BlockItemTagId STORAGE_BLOCKS_NETHERITE = register("storage_blocks/netherite");
	public static final BlockItemTagId STORAGE_BLOCKS_RAW_COPPER = register("storage_blocks/raw_copper");
	public static final BlockItemTagId STORAGE_BLOCKS_RAW_GOLD = register("storage_blocks/raw_gold");
	public static final BlockItemTagId STORAGE_BLOCKS_RAW_IRON = register("storage_blocks/raw_iron");
	public static final BlockItemTagId STORAGE_BLOCKS_REDSTONE = register("storage_blocks/redstone");
	public static final BlockItemTagId STORAGE_BLOCKS_RESIN = register("storage_blocks/resin");
	public static final BlockItemTagId STORAGE_BLOCKS_SLIME = register("storage_blocks/slime");
	public static final BlockItemTagId STORAGE_BLOCKS_WHEAT = register("storage_blocks/wheat");

	// Logs
	/**
	 * For logs found naturally in the Overworld, does not include Stripped Logs.
	 * Aliased with {@link BlockTags#OVERWORLD_NATURAL_LOGS} for consistency.
	 */
	public static final BlockItemTagId OVERWORLD_NATURAL_LOGS = register("natural_logs/overworld");
	/**
	 * For logs, including Stems, found naturally in the Nether, does not include Stripped Logs.
	 */
	public static final BlockItemTagId NETHER_NATURAL_LOGS = register("natural_logs/nether");
	/**
	 * For logs, including Stems, found naturally that have not been stripped.
	 */
	public static final BlockItemTagId NATURAL_LOGS = register("natural_logs");
	/**
	 * For six-sided wood blocks, including Hyphae, found naturally that have not been stripped.
	 */
	public static final BlockItemTagId NATURAL_WOODS = register("natural_woods");
	/**
	 * For logs, including Stems, found naturally that have been stripped.
	 */
	public static final BlockItemTagId STRIPPED_LOGS = register("stripped_logs");
	/**
	 * For six-sided wood blocks, including Hyphae, found naturally that have been stripped.
	 */
	public static final BlockItemTagId STRIPPED_WOODS = register("stripped_woods");

	// Misc
	public static final BlockItemTagId PLAYER_WORKSTATIONS_CRAFTING_TABLES = register("player_workstations/crafting_tables");
	public static final BlockItemTagId PLAYER_WORKSTATIONS_FURNACES = register("player_workstations/furnaces");
	/**
	 * Tag that holds all head based blocks such as Skeleton Skull or Player Head. (Named skulls to match minecraft:skulls item tag)
	 */
	public static final BlockItemTagId SKULLS = register(ConventionalBlockTags.SKULLS, ItemTags.SKULLS);
	public static final BlockItemTagId ROPES = register("ropes");
	public static final BlockItemTagId CHAINS = register("chains");

	/**
	 * Tag that holds all blocks that recipe viewers should not show to users.
	 * Recipe viewers may use this to automatically find the corresponding BlockItem to hide.
	 */
	public static final BlockItemTagId HIDDEN_FROM_RECIPE_VIEWERS = register("hidden_from_recipe_viewers");

	/**
	 * Blocks which are often replaced by deepslate ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_DEEPSLATE}, during world generation.
	 * (The block's registry name is used as the tag name)
	 */
	public static final BlockItemTagId ORE_BEARING_GROUND_DEEPSLATE = register("ore_bearing_ground/deepslate");
	/**
	 * Blocks which are often replaced by netherrack ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_NETHERRACK}, during world generation.
	 * (The block's registry name is used as the tag name)
	 */
	public static final BlockItemTagId ORE_BEARING_GROUND_NETHERRACK = register("ore_bearing_ground/netherrack");
	/**
	 * Blocks which are often replaced by stone ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_STONE}, during world generation.
	 * (The block's registry name is used as the tag name)
	 */
	public static final BlockItemTagId ORE_BEARING_GROUND_STONE = register("ore_bearing_ground/stone");
	/**
	 * Ores which on average result in more than one resource worth of materials ignoring fortune and other modifiers.
	 * (example, Copper Ore)
	 */
	public static final BlockItemTagId ORE_RATES_DENSE = register("ore_rates/dense");
	/**
	 * Ores which on average result in one resource worth of materials ignoring fortune and other modifiers.
	 * (Example, Iron Ore)
	 */
	public static final BlockItemTagId ORE_RATES_SINGULAR = register("ore_rates/singular");
	/**
	 * Ores which on average result in less than one resource worth of materials ignoring fortune and other modifiers.
	 * (Example, Nether Gold Ore as it drops 2 to 6 Gold Nuggets which is less than normal Gold Ore's Raw Gold drop)
	 */
	public static final BlockItemTagId ORE_RATES_SPARSE = register("ore_rates/sparse");
	/**
	 * Ores in deepslate (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_DEEPSLATE}) which could logically use deepslate as recipe input or output.
	 * (The block's registry name is used as the tag name)
	 */
	public static final BlockItemTagId ORES_IN_GROUND_DEEPSLATE = register("ores_in_ground/deepslate");
	/**
	 * Ores in netherrack (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_NETHERRACK}) which could logically use netherrack as recipe input or output.
	 * (The block's registry name is used as the tag name)
	 */
	public static final BlockItemTagId ORES_IN_GROUND_NETHERRACK = register("ores_in_ground/netherrack");
	/**
	 * Ores in stone (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_STONE}) which could logically use stone as recipe input or output.
	 * (The block's registry name is used as the tag name)
	 */
	public static final BlockItemTagId ORES_IN_GROUND_STONE = register("ores_in_ground/stone");

	private static BlockItemTagId register(String tagId) {
		Identifier id = Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, tagId);
		return BlockItemTagId.create(id, id);
	}

	private static BlockItemTagId register(TagKey<Block> blockTagKey, TagKey<Item> itemTagKey) {
		return new BlockItemTagId(blockTagKey, itemTagKey);
	}
}
