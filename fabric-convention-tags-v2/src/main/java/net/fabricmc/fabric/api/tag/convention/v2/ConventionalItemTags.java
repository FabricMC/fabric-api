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

import net.minecraft.item.Item;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;

/**
 * See {@link net.minecraft.registry.tag.ItemTags} for vanilla tags.
 * Note that addition to some vanilla tags implies having certain functionality.
 */
public final class ConventionalItemTags {
	private ConventionalItemTags() {
	}

	/**
	 * Natural stone-like blocks that can be used as a base ingredient in recipes that take stone.
	 */
	public static final TagKey<Item> STONES = register("stones");
	public static final TagKey<Item> COBBLESTONES = register("cobblestones");
	public static final TagKey<Item> OBSIDIANS = register("obsidians");
	/**
	 * For common obsidian that has no special quirks or behaviors. Ideal for recipe use.
	 * Crying Obsidian, for example, is a light block and harder to obtain. So it gets its own tag instead of being under normal tag.
	 */
	public static final TagKey<Item> NORMAL_OBSIDIANS = register("obsidians/normal");
	public static final TagKey<Item> CRYING_OBSIDIANS = register("obsidians/crying");

	// Tool tags
	public static final TagKey<Item> TOOLS = register("tools");
	public static final TagKey<Item> SHEAR_TOOLS = register("tools/shear");
	/**
	 * For spear tools, like Minecraft's tridents.
	 * Note, other weapons like boomerangs and throwing knives are best put into their own tools tag.
	 */
	public static final TagKey<Item> SPEAR_TOOLS = register("tools/spear");
	public static final TagKey<Item> BOW_TOOLS = register("tools/bow");
	public static final TagKey<Item> CROSSBOW_TOOLS = register("tools/crossbow");
	public static final TagKey<Item> SHIELD_TOOLS = register("tools/shield");
	public static final TagKey<Item> FISHING_ROD_TOOLS = register("tools/fishing_rod");
	public static final TagKey<Item> BRUSH_TOOLS = register("tools/brush");
	/**
	 * A tag containing all existing fire starting tools such as Flint and Steel.
	 * Fire Charge is not a tool (no durability) and thus, does not go in this tag.
	 */
	public static final TagKey<Item> IGNITER_TOOLS = register("tools/igniter");
	public static final TagKey<Item> MACE_TOOLS = register("tools/mace");

	// Action-based tool tags
	/**
	 * A tag containing melee-based weapons for recipes and loot tables.
	 * Tools are considered melee if they are intentionally intended to be used for melee attack as a primary purpose.
	 * (In other words, Pickaxes are not melee weapons as they are not intended to be a weapon as a primary purpose)
	 */
	public static final TagKey<Item> MELEE_WEAPON_TOOLS = register("tools/melee_weapon");
	/**
	 * A tag containing ranged-based weapons for recipes and loot tables.
	 * Tools are considered ranged if they can damage entities beyond the weapon's and player's melee attack range.
	 */
	public static final TagKey<Item> RANGED_WEAPON_TOOLS = register("tools/ranged_weapon");
	/**
	 * A tag containing mining-based tools for recipes and loot tables.
	 */
	public static final TagKey<Item> MINING_TOOL_TOOLS = register("tools/mining_tool");

	// Armor tags
	/**
	 * Collects the 4 vanilla armor tags into one parent collection for ease.
	 */
	public static final TagKey<Item> ARMORS = register("armors");

	// Tools/Armor tags
	/**
	 * Collects the many enchantable tags into one parent collection for ease.
	 */
	public static final TagKey<Item> ENCHANTABLES = register("enchantables");

	// Ores and ingots - categories
	public static final TagKey<Item> BRICKS = register("bricks");
	public static final TagKey<Item> DUSTS = register("dusts");
	public static final TagKey<Item> GEMS = register("gems");
	public static final TagKey<Item> INGOTS = register("ingots");
	public static final TagKey<Item> NUGGETS = register("nuggets");
	public static final TagKey<Item> ORES = register("ores");
	public static final TagKey<Item> RAW_MATERIALS = register("raw_materials");

	// Raw material and blocks - vanilla instances
	public static final TagKey<Item> IRON_RAW_MATERIALS = register("raw_materials/iron");
	public static final TagKey<Item> GOLD_RAW_MATERIALS = register("raw_materials/gold");
	public static final TagKey<Item> COPPER_RAW_MATERIALS = register("raw_materials/copper");

	// Bricks - vanilla instances
	public static final TagKey<Item> NORMAL_BRICKS = register("bricks/normal");
	public static final TagKey<Item> NETHER_BRICKS = register("bricks/nether");

	// Ingots - vanilla instances
	public static final TagKey<Item> IRON_INGOTS = register("ingots/iron");
	public static final TagKey<Item> GOLD_INGOTS = register("ingots/gold");
	public static final TagKey<Item> COPPER_INGOTS = register("ingots/copper");
	public static final TagKey<Item> NETHERITE_INGOTS = register("ingots/netherite");

	// Ores - vanilla instances
	public static final TagKey<Item> NETHERITE_SCRAP_ORES = register("ores/netherite_scrap");
	public static final TagKey<Item> QUARTZ_ORES = register("ores/quartz");

	// Gems - vanilla instances
	public static final TagKey<Item> QUARTZ_GEMS = register("gems/quartz");
	public static final TagKey<Item> LAPIS_GEMS = register("gems/lapis");
	public static final TagKey<Item> DIAMOND_GEMS = register("gems/diamond");
	public static final TagKey<Item> AMETHYST_GEMS = register("gems/amethyst");
	public static final TagKey<Item> EMERALD_GEMS = register("gems/emerald");
	public static final TagKey<Item> PRISMARINE_GEMS = register("gems/prismarine");

	// Nuggets - vanilla instances
	public static final TagKey<Item> IRON_NUGGETS = register("nuggets/iron");
	public static final TagKey<Item> GOLD_NUGGETS = register("nuggets/gold");

	// Dusts and Misc - vanilla instances
	public static final TagKey<Item> REDSTONE_DUSTS = register("dusts/redstone");
	public static final TagKey<Item> GLOWSTONE_DUSTS = register("dusts/glowstone");

	// Consumables
	public static final TagKey<Item> POTIONS = register("potions");

	// Foods
	public static final TagKey<Item> FOODS = register("foods");
	/**
	 * All foods edible by animals excluding poisonous foods. (Does not include {@link ItemTags#PARROT_POISONOUS_FOOD})
	 */
	public static final TagKey<Item> ANIMAL_FOODS = register("animal_foods");
	/**
	 * Apples and other foods that are considered fruits in the culinary field belong in this tag.
	 * Cherries would go here as they are considered a "stone fruit" within culinary fields.
	 */
	public static final TagKey<Item> FRUIT_FOODS = register("foods/fruit");
	/**
	 * Tomatoes and other foods that are considered vegetables in the culinary field belong in this tag.
	 */
	public static final TagKey<Item> VEGETABLE_FOODS = register("foods/vegetable");
	/**
	 * Strawberries, raspberries, and other berry foods belong in this tag.
	 * Cherries would NOT go here as they are considered a "stone fruit" within culinary fields.
	 */
	public static final TagKey<Item> BERRY_FOODS = register("foods/berry");
	public static final TagKey<Item> BREAD_FOODS = register("foods/bread");
	public static final TagKey<Item> COOKIE_FOODS = register("foods/cookie");
	public static final TagKey<Item> RAW_MEAT_FOODS = register("foods/raw_meat");
	public static final TagKey<Item> COOKED_MEAT_FOODS = register("foods/cooked_meat");
	public static final TagKey<Item> RAW_FISH_FOODS = register("foods/raw_fish");
	public static final TagKey<Item> COOKED_FISH_FOODS = register("foods/cooked_fish");
	/**
	 * Soups, stews, and other liquid food in bowls belongs in this tag.
	 */
	public static final TagKey<Item> SOUP_FOODS = register("foods/soup");
	/**
	 * Sweets and candies like lollipops or chocolate belong in this tag.
	 */
	public static final TagKey<Item> CANDY_FOODS = register("foods/candy");
	/**
	 * Pies and other pie-like foods belong in this tag.
	 */
	public static final TagKey<Item> PIE_FOODS = register("foods/pie");
	/**
	 * Any gold-based foods would go in this tag. Such as Golden Apples or Glistering Melon Slice.
	 */
	public static final TagKey<Item> GOLDEN_FOODS = register("foods/golden");
	/**
	 * Foods like cake that can be eaten when placed in the world belong in this tag.
	 */
	public static final TagKey<Item> EDIBLE_WHEN_PLACED_FOODS = register("foods/edible_when_placed");
	/**
	 * For foods that inflict food poisoning-like effects.
	 * Examples are Rotten Flesh's Hunger or Pufferfish's Nausea, or Poisonous Potato's Poison.
	 */
	public static final TagKey<Item> FOOD_POISONING_FOODS = register("foods/food_poisoning");

	// Buckets
	public static final TagKey<Item> BUCKETS = register("buckets");
	public static final TagKey<Item> EMPTY_BUCKETS = register("buckets/empty");
	/**
	 * Does not include entity water buckets.
	 */
	public static final TagKey<Item> WATER_BUCKETS = register("buckets/water");
	public static final TagKey<Item> LAVA_BUCKETS = register("buckets/lava");
	public static final TagKey<Item> MILK_BUCKETS = register("buckets/milk");
	public static final TagKey<Item> POWDER_SNOW_BUCKETS = register("buckets/powder_snow");
	public static final TagKey<Item> ENTITY_WATER_BUCKETS = register("buckets/entity_water");

	public static final TagKey<Item> BARRELS = register("barrels");
	public static final TagKey<Item> WOODEN_BARRELS = register("barrels/wooden");
	public static final TagKey<Item> BOOKSHELVES = register("bookshelves");
	public static final TagKey<Item> CHESTS = register("chests");
	public static final TagKey<Item> WOODEN_CHESTS = register("chests/wooden");
	public static final TagKey<Item> GLASS_BLOCKS = register("glass_blocks");
	public static final TagKey<Item> GLASS_BLOCKS_COLORLESS = register("glass_blocks/colorless");
	/**
	 * Glass which is made from cheap resources like sand and only minor additional ingredients like dyes.
	 */
	public static final TagKey<Item> GLASS_BLOCKS_CHEAP = register("glass_blocks/cheap");
	public static final TagKey<Item> GLASS_BLOCKS_TINTED = register("glass_blocks/tinted");
	public static final TagKey<Item> GLASS_PANES = register("glass_panes");
	public static final TagKey<Item> GLASS_PANES_COLORLESS = register("glass_panes/colorless");
	/**
	 * Block tag equivalent is {@link BlockTags#SHULKER_BOXES}.
	 */
	public static final TagKey<Item> SHULKER_BOXES = register("shulker_boxes");
	public static final TagKey<Item> GLAZED_TERRACOTTAS = register("glazed_terracottas");
	public static final TagKey<Item> CONCRETES = register("concretes");
	/**
	 * Block tag equivalent is {@link BlockTags#CONCRETE_POWDERS}.
	 */
	public static final TagKey<Item> CONCRETE_POWDERS = register("concrete_powders");

	// Related to budding mechanics
	public static final TagKey<Item> BUDDING_BLOCKS = register("budding_blocks");
	public static final TagKey<Item> BUDS = register("buds");
	public static final TagKey<Item> CLUSTERS = register("clusters");

	public static final TagKey<Item> VILLAGER_JOB_SITES = register("villager_job_sites");

	// Sandstone
	public static final TagKey<Item> SANDSTONE_BLOCKS = register("sandstone/blocks");
	public static final TagKey<Item> SANDSTONE_SLABS = register("sandstone/slabs");
	public static final TagKey<Item> SANDSTONE_STAIRS = register("sandstone/stairs");
	public static final TagKey<Item> RED_SANDSTONE_BLOCKS = register("sandstone/red_blocks");
	public static final TagKey<Item> RED_SANDSTONE_SLABS = register("sandstone/red_slabs");
	public static final TagKey<Item> RED_SANDSTONE_STAIRS = register("sandstone/red_stairs");
	public static final TagKey<Item> UNCOLORED_SANDSTONE_BLOCKS = register("sandstone/uncolored_blocks");
	public static final TagKey<Item> UNCOLORED_SANDSTONE_SLABS = register("sandstone/uncolored_slabs");
	public static final TagKey<Item> UNCOLORED_SANDSTONE_STAIRS = register("sandstone/uncolored_stairs");

	// Dyes
	public static final TagKey<Item> DYES = register("dyes");
	public static final TagKey<Item> BLACK_DYES = register("dyes/black");
	public static final TagKey<Item> BLUE_DYES = register("dyes/blue");
	public static final TagKey<Item> BROWN_DYES = register("dyes/brown");
	public static final TagKey<Item> CYAN_DYES = register("dyes/cyan");
	public static final TagKey<Item> GRAY_DYES = register("dyes/gray");
	public static final TagKey<Item> GREEN_DYES = register("dyes/green");
	public static final TagKey<Item> LIGHT_BLUE_DYES = register("dyes/light_blue");
	public static final TagKey<Item> LIGHT_GRAY_DYES = register("dyes/light_gray");
	public static final TagKey<Item> LIME_DYES = register("dyes/lime");
	public static final TagKey<Item> MAGENTA_DYES = register("dyes/magenta");
	public static final TagKey<Item> ORANGE_DYES = register("dyes/orange");
	public static final TagKey<Item> PINK_DYES = register("dyes/pink");
	public static final TagKey<Item> PURPLE_DYES = register("dyes/purple");
	public static final TagKey<Item> RED_DYES = register("dyes/red");
	public static final TagKey<Item> WHITE_DYES = register("dyes/white");
	public static final TagKey<Item> YELLOW_DYES = register("dyes/yellow");

	// Items created with dyes
	/**
	 * Tag that holds all blocks and items that can be dyed a specific color.
	 * (Does not include color blending items like leather armor.
	 * Use {@link net.minecraft.registry.tag.ItemTags#DYEABLE} tag instead for color blending items)
	 * <p></p>
	 * Note: Use custom ingredients in recipes to do tag intersections and/or tag exclusions
	 * to make more powerful recipes utilizing multiple tags such as dyed tags for an ingredient.
	 * See {@link net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients}
	 * children classes for various custom ingredients available that can also be used in data generation.
	 */
	public static final TagKey<Item> DYED = register("dyed");
	public static final TagKey<Item> BLACK_DYED = register("dyed/black");
	public static final TagKey<Item> BLUE_DYED = register("dyed/blue");
	public static final TagKey<Item> BROWN_DYED = register("dyed/brown");
	public static final TagKey<Item> CYAN_DYED = register("dyed/cyan");
	public static final TagKey<Item> GRAY_DYED = register("dyed/gray");
	public static final TagKey<Item> GREEN_DYED = register("dyed/green");
	public static final TagKey<Item> LIGHT_BLUE_DYED = register("dyed/light_blue");
	public static final TagKey<Item> LIGHT_GRAY_DYED = register("dyed/light_gray");
	public static final TagKey<Item> LIME_DYED = register("dyed/lime");
	public static final TagKey<Item> MAGENTA_DYED = register("dyed/magenta");
	public static final TagKey<Item> ORANGE_DYED = register("dyed/orange");
	public static final TagKey<Item> PINK_DYED = register("dyed/pink");
	public static final TagKey<Item> PURPLE_DYED = register("dyed/purple");
	public static final TagKey<Item> RED_DYED = register("dyed/red");
	public static final TagKey<Item> WHITE_DYED = register("dyed/white");
	public static final TagKey<Item> YELLOW_DYED = register("dyed/yellow");

	// Storage blocks - categories
	/**
	 * A storage block is generally a block that has a recipe to craft a bulk of 1 kind of resource to a block
	 * and has a mirror recipe to reverse the crafting with no loss in resources.
	 * <p></p>
	 * Honey Block is special in that the reversing recipe is not a perfect mirror of the crafting recipe
	 * and so, it is considered a special case and not given a storage block tag.
	 */
	public static final TagKey<Item> STORAGE_BLOCKS = register("storage_blocks");
	public static final TagKey<Item> STORAGE_BLOCKS_BONE_MEAL = register("storage_blocks/bone_meal");
	public static final TagKey<Item> STORAGE_BLOCKS_COAL = register("storage_blocks/coal");
	public static final TagKey<Item> STORAGE_BLOCKS_COPPER = register("storage_blocks/copper");
	public static final TagKey<Item> STORAGE_BLOCKS_DIAMOND = register("storage_blocks/diamond");
	public static final TagKey<Item> STORAGE_BLOCKS_DRIED_KELP = register("storage_blocks/dried_kelp");
	public static final TagKey<Item> STORAGE_BLOCKS_EMERALD = register("storage_blocks/emerald");
	public static final TagKey<Item> STORAGE_BLOCKS_GOLD = register("storage_blocks/gold");
	public static final TagKey<Item> STORAGE_BLOCKS_IRON = register("storage_blocks/iron");
	public static final TagKey<Item> STORAGE_BLOCKS_LAPIS = register("storage_blocks/lapis");
	public static final TagKey<Item> STORAGE_BLOCKS_NETHERITE = register("storage_blocks/netherite");
	public static final TagKey<Item> STORAGE_BLOCKS_RAW_COPPER = register("storage_blocks/raw_copper");
	public static final TagKey<Item> STORAGE_BLOCKS_RAW_GOLD = register("storage_blocks/raw_gold");
	public static final TagKey<Item> STORAGE_BLOCKS_RAW_IRON = register("storage_blocks/raw_iron");
	public static final TagKey<Item> STORAGE_BLOCKS_REDSTONE = register("storage_blocks/redstone");
	public static final TagKey<Item> STORAGE_BLOCKS_SLIME = register("storage_blocks/slime");
	public static final TagKey<Item> STORAGE_BLOCKS_WHEAT = register("storage_blocks/wheat");

	// Logs
	public static final TagKey<Item> STRIPPED_LOGS = register("stripped_logs");
	public static final TagKey<Item> STRIPPED_WOODS = register("stripped_woods");

	// Crops
	/**
	 * For raw materials harvested from growable plants. Crop items can be edible like carrots or non-edible like
	 * wheat and cocoa beans.
	 */
	public static final TagKey<Item> CROPS = register("crops");
	public static final TagKey<Item> BEETROOT_CROPS = register("crops/beetroot");
	public static final TagKey<Item> CACTUS_CROPS = register("crops/cactus");
	public static final TagKey<Item> CARROT_CROPS = register("crops/carrot");
	public static final TagKey<Item> COCOA_BEAN_CROPS = register("crops/cocoa_bean");
	public static final TagKey<Item> MELON_CROPS = register("crops/melon");
	public static final TagKey<Item> NETHER_WART_CROPS = register("crops/nether_wart");
	public static final TagKey<Item> POTATO_CROPS = register("crops/potato");
	public static final TagKey<Item> PUMPKIN_CROPS = register("crops/pumpkin");
	public static final TagKey<Item> SUGAR_CANE_CROPS = register("crops/sugar_cane");
	public static final TagKey<Item> WHEAT_CROPS = register("crops/wheat");

	// Other
	public static final TagKey<Item> PLAYER_WORKSTATIONS_CRAFTING_TABLES = register("player_workstations/crafting_tables");
	public static final TagKey<Item> PLAYER_WORKSTATIONS_FURNACES = register("player_workstations/furnaces");
	public static final TagKey<Item> STRINGS = register("strings");
	public static final TagKey<Item> LEATHERS = register("leathers");
	/**
	 * For music disc-like materials to be used in recipes.
	 * A pancake with a JUKEBOX_PLAYABLE component attached to play in Jukeboxes as an Easter Egg is not a music disc and would not go in this tag.
	 */
	public static final TagKey<Item> MUSIC_DISCS = register("music_discs");
	/**
	 * For rod-like materials to be used in recipes.
	 */
	public static final TagKey<Item> RODS = register("rods");
	/**
	 * For stick-like materials to be used in recipes.
	 * One example is a mod adds stick variants such as Spruce Sticks but would like stick recipes to be able to use it.
	 */
	public static final TagKey<Item> WOODEN_RODS = register("rods/wooden");
	public static final TagKey<Item> BLAZE_RODS = register("rods/blaze");
	public static final TagKey<Item> BREEZE_RODS = register("rods/breeze");
	public static final TagKey<Item> ROPES = register("ropes");
	public static final TagKey<Item> CHAINS = register("chains");
	public static final TagKey<Item> ENDER_PEARLS = register("ender_pearls");
	public static final TagKey<Item> SLIME_BALLS = register("slime_balls");
	/**
	 * For bonemeal-like items that can grow plants.
	 * (Note: Could include durability-based modded bonemeal-like items. Check for durability {@link net.minecraft.component.DataComponentTypes#DAMAGE} to handle them properly)
	 */
	public static final TagKey<Item> FERTILIZERS = register("fertilizers");

	/**
	 * Tag that holds all items that recipe viewers should not show to users.
	 */
	public static final TagKey<Item> HIDDEN_FROM_RECIPE_VIEWERS = register("hidden_from_recipe_viewers");

	/**
	 * This tag is redundant. Please use {@link ConventionalItemTags#STORAGE_BLOCKS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> RAW_BLOCKS = register("raw_blocks");
	/**
	 * This tag is redundant. Please use {@link ConventionalItemTags#STORAGE_BLOCKS_RAW_IRON} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> IRON_RAW_BLOCKS = register("raw_blocks/iron");
	/**
	 * This tag is redundant. Please use {@link ConventionalItemTags#STORAGE_BLOCKS_RAW_GOLD} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> GOLD_RAW_BLOCKS = register("raw_blocks/gold");
	/**
	 * This tag is redundant. Please use {@link ConventionalItemTags#STORAGE_BLOCKS_RAW_COPPER} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> COPPER_RAW_BLOCKS = register("raw_blocks/copper");
	/**
	 * This tag is redundant. Please use {@link net.minecraft.registry.tag.ItemTags#COALS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> COAL = register("coal");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#SHEAR_TOOLS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> SHEARS_TOOLS = register("tools/shears");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#SPEAR_TOOLS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> SPEARS_TOOLS = register("tools/spears");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#BOW_TOOLS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> BOWS_TOOLS = register("tools/bows");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#CROSSBOW_TOOLS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> CROSSBOWS_TOOLS = register("tools/crossbows");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#SHIELD_TOOLS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> SHIELDS_TOOLS = register("tools/shields");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#FISHING_ROD_TOOLS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> FISHING_RODS_TOOLS = register("tools/fishing_rods");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#BRUSH_TOOLS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> BRUSHES_TOOLS = register("tools/brushes");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#MELEE_WEAPON_TOOLS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> MELEE_WEAPONS_TOOLS = register("tools/melee_weapons");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#RANGED_WEAPON_TOOLS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> RANGED_WEAPONS_TOOLS = register("tools/ranged_weapons");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#MINING_TOOL_TOOLS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> MINING_TOOLS = register("tools/mining_tools");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#GLAZED_TERRACOTTAS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> GLAZED_TERRACOTTA = register("glazed_terracotta");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#CONCRETES} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> CONCRETE = register("concrete");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#CONCRETE_POWDERS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> CONCRETE_POWDER = register("concrete_powder");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#FRUIT_FOODS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> FRUITS_FOODS = register("foods/fruits");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#VEGETABLE_FOODS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> VEGETABLES_FOODS = register("foods/vegetables");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#BERRY_FOODS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> BERRIES_FOODS = register("foods/berries");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#BREAD_FOODS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> BREADS_FOODS = register("foods/breads");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#COOKIE_FOODS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> COOKIES_FOODS = register("foods/cookies");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#RAW_MEAT_FOODS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> RAW_MEATS_FOODS = register("foods/raw_meats");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#COOKED_MEAT_FOODS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> COOKED_MEATS_FOODS = register("foods/cooked_meats");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#RAW_FISH_FOODS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> RAW_FISHES_FOODS = register("foods/raw_fishes");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#COOKED_FISH_FOODS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> COOKED_FISHES_FOODS = register("foods/cooked_fishes");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#SOUP_FOODS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> SOUPS_FOODS = register("foods/soups");
	/**
	 * This tag was typoed. Please use {@link ConventionalItemTags#CANDY_FOODS} tag instead.
	 */
	@Deprecated
	public static final TagKey<Item> CANDIES_FOODS = register("foods/candies");

	private static TagKey<Item> register(String tagId) {
		return TagRegistration.ITEM_TAG.registerC(tagId);
	}
}
