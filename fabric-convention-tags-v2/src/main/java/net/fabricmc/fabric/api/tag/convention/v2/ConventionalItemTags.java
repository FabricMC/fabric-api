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

import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ColorCollection;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;

/**
 * See {@link net.minecraft.tags.ItemTags} for vanilla tags.
 * Note that addition to some vanilla tags implies having certain functionality.
 */
public final class ConventionalItemTags {
	private ConventionalItemTags() {
	}

	/**
	 * Natural stone-like blocks that can be used as a base ingredient in recipes that take stone.
	 */
	public static final TagKey<Item> STONES = ConventionalBlockItemTags.STONES.item();
	public static final TagKey<Item> COBBLESTONES = ConventionalBlockItemTags.COBBLESTONES.item();
	public static final TagKey<Item> DEEPSLATE_COBBLESTONES = ConventionalBlockItemTags.DEEPSLATE_COBBLESTONES.item();
	public static final TagKey<Item> INFESTED_COBBLESTONES = ConventionalBlockItemTags.INFESTED_COBBLESTONES.item();
	public static final TagKey<Item> MOSSY_COBBLESTONES = ConventionalBlockItemTags.MOSSY_COBBLESTONES.item();
	public static final TagKey<Item> NORMAL_COBBLESTONES = ConventionalBlockItemTags.NORMAL_COBBLESTONES.item();
	public static final TagKey<Item> NETHERRACKS = ConventionalBlockItemTags.NETHERRACKS.item();
	public static final TagKey<Item> END_STONES = ConventionalBlockItemTags.END_STONES.item();
	public static final TagKey<Item> GRAVELS = ConventionalBlockItemTags.GRAVELS.item();
	public static final TagKey<Item> OBSIDIANS = ConventionalBlockItemTags.OBSIDIANS.item();
	/**
	 * For common obsidian that has no special quirks or behaviors. Ideal for recipe use.
	 * Crying Obsidian, for example, is a light block and harder to obtain. So it gets its own tag instead of being under normal tag.
	 */
	public static final TagKey<Item> NORMAL_OBSIDIANS = ConventionalBlockItemTags.NORMAL_OBSIDIANS.item();
	public static final TagKey<Item> CRYING_OBSIDIANS = ConventionalBlockItemTags.CRYING_OBSIDIANS.item();
	/// Light-emitting blocks created when a Frog eats a Magma Cube.
	public static final TagKey<Item> FROGLIGHTS = ConventionalBlockItemTags.FROGLIGHTS.item();

	// Tool tags
	public static final TagKey<Item> TOOLS = register("tools");
	public static final TagKey<Item> SHEAR_TOOLS = register("tools/shear");
	/**
	 * For throwable stick-like tools, like Minecraft's trident.
	 * Note, other weapons like boomerangs and throwing knives are best put into their own tools tag.
	 */
	public static final TagKey<Item> TRIDENT_TOOLS = register("tools/trident");
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
	public static final TagKey<Item> WRENCH_TOOLS = register("tools/wrench");

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
	 * A tag containing all conventional armor tags.
	 * Note that this can contain armor that does not necessarily fit on a player. For that, see {@link ConventionalItemTags#HUMANOID_ARMORS}
	 */
	public static final TagKey<Item> ARMORS = register("armors");
	/**
	 * Armor that can fit on a humanoid mob like the Player. This tag collects the 4 vanilla armor tags into one parent collection for ease.
	 */
	public static final TagKey<Item> HUMANOID_ARMORS = register("armors/humanoid");
	/**
	 * A tag containing armor that can fit on a Horse.
	 */
	public static final TagKey<Item> HORSE_ARMORS = register("armors/horse");
	/**
	 * A tag containing armor that can fit on a Nautilus.
	 */
	public static final TagKey<Item> NAUTILUS_ARMORS = register("armors/nautilus");
	/**
	 * A tag containing armor that can fit on a Wolf.
	 */
	public static final TagKey<Item> WOLF_ARMORS = register("armors/wolf");

	// Tools/Armor tags
	/**
	 * Collects the many enchantable tags into one parent collection for ease.
	 */
	public static final TagKey<Item> ENCHANTABLES = register("enchantables");

	// Ores and ingots - categories
	public static final TagKey<Item> BRICKS = register("bricks");
	public static final TagKey<Item> DUSTS = register("dusts");
	public static final TagKey<Item> CLUMPS = register("clumps");
	public static final TagKey<Item> GEMS = register("gems");
	public static final TagKey<Item> INGOTS = register("ingots");
	public static final TagKey<Item> NUGGETS = register("nuggets");
	public static final TagKey<Item> ORES = ConventionalBlockItemTags.ORES.item();
	public static final TagKey<Item> RAW_MATERIALS = register("raw_materials");

	// Raw material and blocks - vanilla instances
	public static final TagKey<Item> IRON_RAW_MATERIALS = register("raw_materials/iron");
	public static final TagKey<Item> GOLD_RAW_MATERIALS = register("raw_materials/gold");
	public static final TagKey<Item> COPPER_RAW_MATERIALS = register("raw_materials/copper");

	// Bricks - vanilla instances
	public static final TagKey<Item> NORMAL_BRICKS = register("bricks/normal");
	public static final TagKey<Item> NETHER_BRICKS = register("bricks/nether");
	public static final TagKey<Item> RESIN_BRICKS = register("bricks/resin");

	// Ingots - vanilla instances
	public static final TagKey<Item> IRON_INGOTS = register("ingots/iron");
	public static final TagKey<Item> GOLD_INGOTS = register("ingots/gold");
	public static final TagKey<Item> COPPER_INGOTS = register("ingots/copper");
	public static final TagKey<Item> NETHERITE_INGOTS = register("ingots/netherite");

	// Ores - vanilla instances (All ores consolidated here for consistency)
	/**
	 * Aliased with {@link ItemTags#COAL_ORES}.
	 */
	public static final TagKey<Item> COAL_ORES = ConventionalBlockItemTags.COAL_ORES.item();
	/**
	 * Aliased with {@link ItemTags#COPPER_ORES}.
	 */
	public static final TagKey<Item> COPPER_ORES = ConventionalBlockItemTags.COPPER_ORES.item();
	/**
	 * Aliased with {@link ItemTags#DIAMOND_ORES}.
	 */
	public static final TagKey<Item> DIAMOND_ORES = ConventionalBlockItemTags.DIAMOND_ORES.item();
	/**
	 * Aliased with {@link ItemTags#EMERALD_ORES}.
	 */
	public static final TagKey<Item> EMERALD_ORES = ConventionalBlockItemTags.EMERALD_ORES.item();
	/**
	 * Aliased with {@link ItemTags#GOLD_ORES}.
	 */
	public static final TagKey<Item> GOLD_ORES = ConventionalBlockItemTags.GOLD_ORES.item();
	/**
	 * Aliased with {@link ItemTags#IRON_ORES}.
	 */
	public static final TagKey<Item> IRON_ORES = ConventionalBlockItemTags.IRON_ORES.item();
	/**
	 * Aliased with {@link ItemTags#LAPIS_ORES}.
	 */
	public static final TagKey<Item> LAPIS_ORES = ConventionalBlockItemTags.LAPIS_ORES.item();
	public static final TagKey<Item> NETHERITE_SCRAP_ORES = ConventionalBlockItemTags.NETHERITE_SCRAP_ORES.item();
	public static final TagKey<Item> QUARTZ_ORES = ConventionalBlockItemTags.QUARTZ_ORES.item();
	/**
	 * Aliased with {@link ItemTags#REDSTONE_ORES}.
	 */
	public static final TagKey<Item> REDSTONE_ORES = ConventionalBlockItemTags.REDSTONE_ORES.item();

	// Gems - vanilla instances
	public static final TagKey<Item> QUARTZ_GEMS = register("gems/quartz");
	public static final TagKey<Item> LAPIS_GEMS = register("gems/lapis");
	public static final TagKey<Item> DIAMOND_GEMS = register("gems/diamond");
	public static final TagKey<Item> AMETHYST_GEMS = register("gems/amethyst");
	public static final TagKey<Item> EMERALD_GEMS = register("gems/emerald");
	public static final TagKey<Item> PRISMARINE_GEMS = register("gems/prismarine");

	// Nuggets - vanilla instances
	public static final TagKey<Item> COPPER_NUGGETS = register("nuggets/copper");
	public static final TagKey<Item> IRON_NUGGETS = register("nuggets/iron");
	public static final TagKey<Item> GOLD_NUGGETS = register("nuggets/gold");

	// Dusts and Misc - vanilla instances
	public static final TagKey<Item> REDSTONE_DUSTS = register("dusts/redstone");
	public static final TagKey<Item> GLOWSTONE_DUSTS = register("dusts/glowstone");

	public static final TagKey<Item> RESIN_CLUMPS = register("clumps/resin");

	// Consumables
	/**
	 * Items that can hold various potion effects by making use of {@link net.minecraft.core.component.DataComponents#POTION_CONTENTS}.
	 * Contents of this tag may not always be a kind of bottle. Buckets of potions could go here.
	 * The subtags would be the name of the container that is holding the potion effects such as `c:potions/bucket` or `c:potions/vial` as examples.
	 */
	public static final TagKey<Item> POTIONS = register("potions");
	/**
	 * Variations of the potion bottle that can hold various effects by using {@link net.minecraft.core.component.DataComponents#POTION_CONTENTS}.
	 * Examples are splash and lingering potions from vanilla.
	 * If a mod adds a new variant like seeking potion that applies effect to the closest entity at impact, that would in this tag.
	 */
	public static final TagKey<Item> BOTTLE_POTIONS = register("potions/bottle");

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
	/// For all doughs regardless of type, specific types of dough should fall under their respective sub-tag.
	///
	/// For example:
	/// - Wheat dough (which generally results in bread) would go in "#c:foods/dough/wheat"
	/// - Rye dough (which has rye as it's main ingredient) would go in "#c:foods/dough/rye"
	/// - Sub-tags should also be added to this tag, for example: "#c:foods/dough/wheat" should be added to "#c:foods/dough"
	///
	/// **There are some important assumptions that should be kept in mind.**
	/// - It is assumed that "1 dough = result", which in the case of wheat dough would be "1 dough = 1 bread"
	/// - It is assumed that this dough can be baked into another item
	/// - It is **not** assumed that all doughs result in bread, there can be doughs in this tag that result in things like pizza, etc.
	/// This means that this tag should **not** be used for furnace recipes, mods should add their own dough to result recipes for their respective items.
	public static final TagKey<Item> DOUGH_FOODS = register("foods/dough");
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

	// Drinks
	/**
	 * Drinks are defined as (1) consumable items that (2) use the
	 * {@linkplain net.minecraft.world.item.ItemUseAnimation#DRINK drink use animation}, (3) can be consumed regardless of the
	 * player's current hunger.
	 *
	 * <p>Drinks may provide nutrition and saturation, but are not required to do so.
	 *
	 * <p>More specific types of drinks, such as Water, Milk, or Juice should be placed in a sub-tag, such as
	 * {@code #c:drinks/water}, {@code #c:drinks/milk}, and {@code #c:drinks/juice}.
	 */
	public static final TagKey<Item> DRINKS = register("drinks");
	/**
	 * For consumable drinks that contain only water.
	 */
	public static final TagKey<Item> WATER_DRINKS = register("drinks/water");
	/**
	 * For consumable drinks that are generally watery (such as potions).
	 */
	public static final TagKey<Item> WATERY_DRINKS = register("drinks/watery");
	public static final TagKey<Item> MILK_DRINKS = register("drinks/milk");
	public static final TagKey<Item> HONEY_DRINKS = register("drinks/honey");
	/**
	 * For consumable drinks that are magic in nature and usually grant at least one
	 * {@link net.minecraft.world.effect.MobEffect} when consumed.
	 */
	public static final TagKey<Item> MAGIC_DRINKS = register("drinks/magic");
	/**
	 * For drinks that always grant the {@linkplain net.minecraft.world.effect.MobEffects#BAD_OMEN Bad Omen} effect.
	 */
	public static final TagKey<Item> OMINOUS_DRINKS = register("drinks/ominous");
	/**
	 * Non-alcoholic, plant based fruit and vegetable juices belong in this tag, for example apple juice and carrot juice.
	 *
	 * <p>If tags for specific types of juices are desired, they may go in a sub-tag, using their regular name such as
	 * {@code #c:drinks/apple_juice}.
	 */
	public static final TagKey<Item> JUICE_DRINKS = register("drinks/juice");

	// Drink containing items
	/**
	 * For non-empty buckets that are {@linkplain #DRINKS drinkable}.
	 */
	public static final TagKey<Item> DRINK_CONTAINING_BUCKET = register("drink_containing/bucket");
	/**
	 * For non-empty bottles that are {@linkplain #DRINKS drinkable}.
	 */
	public static final TagKey<Item> DRINK_CONTAINING_BOTTLE = register("drink_containing/bottle");

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
	public static final TagKey<Item> ENTITY_DRY_BUCKETS = register("buckets/entity_dry");

	public static final TagKey<Item> BARRELS = ConventionalBlockItemTags.BARRELS.item();
	public static final TagKey<Item> WOODEN_BARRELS = ConventionalBlockItemTags.WOODEN_BARRELS.item();
	public static final TagKey<Item> BOOKSHELVES = ConventionalBlockItemTags.BOOKSHELVES.item();
	public static final TagKey<Item> CHESTS = ConventionalBlockItemTags.CHESTS.item();
	public static final TagKey<Item> WOODEN_CHESTS = ConventionalBlockItemTags.WOODEN_CHESTS.item();
	public static final TagKey<Item> TRAPPED_CHESTS = ConventionalBlockItemTags.TRAPPED_CHESTS.item();
	public static final TagKey<Item> ENDER_CHESTS = ConventionalBlockItemTags.ENDER_CHESTS.item();
	public static final TagKey<Item> GLASS_BLOCKS = ConventionalBlockItemTags.GLASS_BLOCKS.item();
	public static final TagKey<Item> GLASS_BLOCKS_COLORLESS = ConventionalBlockItemTags.GLASS_BLOCKS_COLORLESS.item();
	/**
	 * Glass which is made from cheap resources like sand and only minor additional ingredients like dyes.
	 */
	public static final TagKey<Item> GLASS_BLOCKS_CHEAP = ConventionalBlockItemTags.GLASS_BLOCKS_CHEAP.item();
	public static final TagKey<Item> GLASS_BLOCKS_TINTED = ConventionalBlockItemTags.GLASS_BLOCKS_TINTED.item();
	public static final TagKey<Item> GLASS_PANES = ConventionalBlockItemTags.GLASS_PANES.item();
	public static final TagKey<Item> GLASS_PANES_COLORLESS = ConventionalBlockItemTags.GLASS_PANES_COLORLESS.item();
	/**
	 * Block tag equivalent is {@link BlockTags#SHULKER_BOXES}.
	 */
	public static final TagKey<Item> SHULKER_BOXES = register("shulker_boxes");
	public static final TagKey<Item> GLAZED_TERRACOTTAS = ConventionalBlockItemTags.GLAZED_TERRACOTTAS.item();
	public static final TagKey<Item> CONCRETES = ConventionalBlockItemTags.CONCRETES.item();
	/**
	 * Block tag equivalent is {@link BlockTags#CONCRETE_POWDERS}.
	 */
	public static final TagKey<Item> CONCRETE_POWDERS = register("concrete_powders");

	// Related to budding mechanics
	public static final TagKey<Item> BUDDING_BLOCKS = ConventionalBlockItemTags.BUDDING_BLOCKS.item();
	public static final TagKey<Item> BUDS = ConventionalBlockItemTags.BUDS.item();
	public static final TagKey<Item> CLUSTERS = ConventionalBlockItemTags.CLUSTERS.item();

	public static final TagKey<Item> VILLAGER_JOB_SITES = ConventionalBlockItemTags.VILLAGER_JOB_SITES.item();

	// Sands
	public static final TagKey<Item> SANDS = ConventionalBlockItemTags.SANDS.item();
	public static final TagKey<Item> RED_SANDS = ConventionalBlockItemTags.RED_SANDS.item();
	public static final TagKey<Item> COLORLESS_SANDS = ConventionalBlockItemTags.COLORLESS_SANDS.item();

	// Sandstone
	public static final TagKey<Item> SANDSTONE_BLOCKS = ConventionalBlockItemTags.SANDSTONE_BLOCKS.item();
	public static final TagKey<Item> SANDSTONE_SLABS = ConventionalBlockItemTags.SANDSTONE_SLABS.item();
	public static final TagKey<Item> SANDSTONE_STAIRS = ConventionalBlockItemTags.SANDSTONE_STAIRS.item();
	public static final TagKey<Item> RED_SANDSTONE_BLOCKS = ConventionalBlockItemTags.RED_SANDSTONE_BLOCKS.item();
	public static final TagKey<Item> RED_SANDSTONE_SLABS = ConventionalBlockItemTags.RED_SANDSTONE_SLABS.item();
	public static final TagKey<Item> RED_SANDSTONE_STAIRS = ConventionalBlockItemTags.RED_SANDSTONE_STAIRS.item();
	public static final TagKey<Item> UNCOLORED_SANDSTONE_BLOCKS = ConventionalBlockItemTags.UNCOLORED_SANDSTONE_BLOCKS.item();
	public static final TagKey<Item> UNCOLORED_SANDSTONE_SLABS = ConventionalBlockItemTags.UNCOLORED_SANDSTONE_SLABS.item();
	public static final TagKey<Item> UNCOLORED_SANDSTONE_STAIRS = ConventionalBlockItemTags.UNCOLORED_SANDSTONE_STAIRS.item();

	// Flower
	/**
	 * Contains living ground-based flowers that are 1 block tall such as Dandelions or Poppy.
	 * Equivalent to the {@code minecraft:small_flowers} item tag.
	 * Aliased with {@link BlockItemTags#SMALL_FLOWERS}.
	 */
	public static final TagKey<Item> SMALL_FLOWERS = ConventionalBlockItemTags.SMALL_FLOWERS.item();
	/**
	 * Contains living ground-based flowers that are 2 block tall such as Rose Bush or Peony.
	 * Equivalent to the {@code minecraft:tall_flowers} item tag in past Minecraft versions.
	 */
	public static final TagKey<Item> TALL_FLOWERS = ConventionalBlockItemTags.TALL_FLOWERS.item();
	/**
	 * Contains any living plant block that contains flowers or is a flower itself.
	 * Equivalent to the {@code minecraft:flowers} item tag in past Minecraft versions.
	 */
	public static final TagKey<Item> FLOWERS = ConventionalBlockItemTags.FLOWERS.item();

	// Fences and Fence Gates
	/**
	 * Aliased with {@link BlockItemTags#FENCES}.
	 */
	public static final TagKey<Item> FENCES = ConventionalBlockItemTags.FENCES.item();
	/**
	 * Aliased with {@link ItemTags#WOODEN_FENCES}.
	 */
	public static final TagKey<Item> WOODEN_FENCES = ConventionalBlockItemTags.WOODEN_FENCES.item();
	public static final TagKey<Item> NETHER_BRICK_FENCES = ConventionalBlockItemTags.NETHER_BRICK_FENCES.item();
	/**
	 * Aliased with {@link ItemTags#FENCE_GATES}.
	 */
	public static final TagKey<Item> FENCE_GATES = ConventionalBlockItemTags.FENCE_GATES.item();
	public static final TagKey<Item> WOODEN_FENCE_GATES = ConventionalBlockItemTags.WOODEN_FENCE_GATES.item();

	// Bars
	/**
	 * Aliased with {@link BlockItemTags#BARS}.
	 */
	public static final TagKey<Item> BARS = ConventionalBlockItemTags.BARS.item();
	public static final TagKey<Item> IRON_BARS = ConventionalBlockItemTags.IRON_BARS.item();
	public static final TagKey<Item> COPPER_BARS = ConventionalBlockItemTags.COPPER_BARS.item();

	// Pumpkins
	public static final TagKey<Item> PUMPKINS = ConventionalBlockItemTags.PUMPKINS.item();
	/**
	 * For pumpkins that are not carved.
	 */
	public static final TagKey<Item> NORMAL_PUMPKINS = ConventionalBlockItemTags.NORMAL_PUMPKINS.item();
	/**
	 * For pumpkins that are already carved but not a light source.
	 */
	public static final TagKey<Item> CARVED_PUMPKINS = ConventionalBlockItemTags.CARVED_PUMPKINS.item();

	/**
	 * For pumpkins that are already carved and a light source.
	 */
	public static final TagKey<Item> JACK_O_LANTERNS_PUMPKINS = ConventionalBlockItemTags.JACK_O_LANTERNS_PUMPKINS.item();

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

	/**
	 * Dye color tags as a color collection, for convenience.
	 */
	public static final ColorCollection<TagKey<Item>> COLOR_DYES = new ColorCollection<>(
			WHITE_DYES,
			ORANGE_DYES,
			MAGENTA_DYES,
			LIGHT_BLUE_DYES,
			YELLOW_DYES,
			LIME_DYES,
			PINK_DYES,
			GRAY_DYES,
			LIGHT_GRAY_DYES,
			CYAN_DYES,
			PURPLE_DYES,
			BLUE_DYES,
			BROWN_DYES,
			GREEN_DYES,
			RED_DYES,
			BLACK_DYES
	);

	// Items created with dyes
	/**
	 * Tag that holds all blocks and items which are dyed a specific color.
	 * (Does not include color blending items like leather armor)
	 *
	 * <p>Note: Use custom ingredients in recipes to do tag intersections and/or tag exclusions
	 * to make more powerful recipes utilizing multiple tags such as dyed tags for an ingredient.
	 * See {@link net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients}
	 * children classes for various custom ingredients available that can also be used in data generation.
	 */
	public static final TagKey<Item> DYED = ConventionalBlockItemTags.DYED.item();
	public static final TagKey<Item> BLACK_DYED = ConventionalBlockItemTags.BLACK_DYED.item();
	public static final TagKey<Item> BLUE_DYED = ConventionalBlockItemTags.BLUE_DYED.item();
	public static final TagKey<Item> BROWN_DYED = ConventionalBlockItemTags.BROWN_DYED.item();
	public static final TagKey<Item> CYAN_DYED = ConventionalBlockItemTags.CYAN_DYED.item();
	public static final TagKey<Item> GRAY_DYED = ConventionalBlockItemTags.GRAY_DYED.item();
	public static final TagKey<Item> GREEN_DYED = ConventionalBlockItemTags.GREEN_DYED.item();
	public static final TagKey<Item> LIGHT_BLUE_DYED = ConventionalBlockItemTags.LIGHT_BLUE_DYED.item();
	public static final TagKey<Item> LIGHT_GRAY_DYED = ConventionalBlockItemTags.LIGHT_GRAY_DYED.item();
	public static final TagKey<Item> LIME_DYED = ConventionalBlockItemTags.LIME_DYED.item();
	public static final TagKey<Item> MAGENTA_DYED = ConventionalBlockItemTags.MAGENTA_DYED.item();
	public static final TagKey<Item> ORANGE_DYED = ConventionalBlockItemTags.ORANGE_DYED.item();
	public static final TagKey<Item> PINK_DYED = ConventionalBlockItemTags.PINK_DYED.item();
	public static final TagKey<Item> PURPLE_DYED = ConventionalBlockItemTags.PURPLE_DYED.item();
	public static final TagKey<Item> RED_DYED = ConventionalBlockItemTags.RED_DYED.item();
	public static final TagKey<Item> WHITE_DYED = ConventionalBlockItemTags.WHITE_DYED.item();
	public static final TagKey<Item> YELLOW_DYED = ConventionalBlockItemTags.YELLOW_DYED.item();

	/**
	 * Dyed color tags as a color collection, for convenience.
	 */
	public static final ColorCollection<TagKey<Item>> COLOR_DYED = new ColorCollection<>(
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
	 * Tag that holds items which can be dyed but do not have their own color already, like glass.
	 * (Does not include color blending items like leather armor)
	 */
	public static final TagKey<Item> UNDYED_SIMPLE_DYEABLE = ConventionalBlockItemTags.UNDYED_SIMPLE_DYEABLE.item();

	/**
	 * Tag that holds items which can be dyed despite already having a color, like wool.
	 * (Does not include color blending items like leather armor)
	 */
	public static final TagKey<Item> REDYEABLE_SIMPLE_DYEABLE = ConventionalBlockItemTags.REDYEABLE_SIMPLE_DYEABLE.item();

	/**
	 * Tag that holds items which can be dyed in a simple fashion without color blending, typically
	 * in the standard 16 colors, whether they have a color already or not.
	 */
	public static final TagKey<Item> SIMPLE_DYEABLE = ConventionalBlockItemTags.SIMPLE_DYEABLE.item();

	/**
	 * Tag that holds items which can be dyed in a dynamic color blending fashion, like leather armor.
	 * <br>Note this also includes Firework Stars, which store colors in a different fashion to most.
	 */
	public static final TagKey<Item> DYNAMIC_DYEABLE = ConventionalBlockItemTags.DYNAMIC_DYEABLE.item();

	/**
	 * Tag that holds items which can have dye applied to them, whether they have a color already or not.
	 */
	public static final TagKey<Item> DYEABLE = ConventionalBlockItemTags.DYEABLE.item();

	// Storage blocks - categories
	/**
	 * A storage block is generally a block that has a recipe to craft a bulk of 1 kind of resource to a block
	 * and has a mirror recipe to reverse the crafting with no loss in resources.
	 *
	 * <p>Honey Block is special in that the reversing recipe is not a perfect mirror of the crafting recipe
	 * and so, it is considered a special case and not given a storage block tag.
	 */
	public static final TagKey<Item> STORAGE_BLOCKS = ConventionalBlockItemTags.STORAGE_BLOCKS.item();
	public static final TagKey<Item> STORAGE_BLOCKS_BONE_MEAL = ConventionalBlockItemTags.STORAGE_BLOCKS_BONE_MEAL.item();
	public static final TagKey<Item> STORAGE_BLOCKS_COAL = ConventionalBlockItemTags.STORAGE_BLOCKS_COAL.item();
	public static final TagKey<Item> STORAGE_BLOCKS_COPPER = ConventionalBlockItemTags.STORAGE_BLOCKS_COPPER.item();
	public static final TagKey<Item> STORAGE_BLOCKS_DIAMOND = ConventionalBlockItemTags.STORAGE_BLOCKS_DIAMOND.item();
	public static final TagKey<Item> STORAGE_BLOCKS_DRIED_KELP = ConventionalBlockItemTags.STORAGE_BLOCKS_DRIED_KELP.item();
	public static final TagKey<Item> STORAGE_BLOCKS_EMERALD = ConventionalBlockItemTags.STORAGE_BLOCKS_EMERALD.item();
	public static final TagKey<Item> STORAGE_BLOCKS_GOLD = ConventionalBlockItemTags.STORAGE_BLOCKS_GOLD.item();
	public static final TagKey<Item> STORAGE_BLOCKS_IRON = ConventionalBlockItemTags.STORAGE_BLOCKS_IRON.item();
	public static final TagKey<Item> STORAGE_BLOCKS_LAPIS = ConventionalBlockItemTags.STORAGE_BLOCKS_LAPIS.item();
	public static final TagKey<Item> STORAGE_BLOCKS_NETHERITE = ConventionalBlockItemTags.STORAGE_BLOCKS_NETHERITE.item();
	public static final TagKey<Item> STORAGE_BLOCKS_RAW_COPPER = ConventionalBlockItemTags.STORAGE_BLOCKS_RAW_COPPER.item();
	public static final TagKey<Item> STORAGE_BLOCKS_RAW_GOLD = ConventionalBlockItemTags.STORAGE_BLOCKS_RAW_GOLD.item();
	public static final TagKey<Item> STORAGE_BLOCKS_RAW_IRON = ConventionalBlockItemTags.STORAGE_BLOCKS_RAW_IRON.item();
	public static final TagKey<Item> STORAGE_BLOCKS_REDSTONE = ConventionalBlockItemTags.STORAGE_BLOCKS_REDSTONE.item();
	public static final TagKey<Item> STORAGE_BLOCKS_RESIN = ConventionalBlockItemTags.STORAGE_BLOCKS_RESIN.item();
	public static final TagKey<Item> STORAGE_BLOCKS_SLIME = ConventionalBlockItemTags.STORAGE_BLOCKS_SLIME.item();
	public static final TagKey<Item> STORAGE_BLOCKS_WHEAT = ConventionalBlockItemTags.STORAGE_BLOCKS_WHEAT.item();

	// Logs
	/**
	 * For logs found naturally in the Overworld, does not include Stripped Logs.
	 */
	public static final TagKey<Item> OVERWORLD_NATURAL_LOGS = ConventionalBlockItemTags.OVERWORLD_NATURAL_LOGS.item();
	/**
	 * For logs, including Stems, found naturally in the Nether, does not include Stripped Logs.
	 */
	public static final TagKey<Item> NETHER_NATURAL_LOGS = ConventionalBlockItemTags.NETHER_NATURAL_LOGS.item();
	/**
	 * For logs, including Stems, found naturally that have not been stripped.
	 */
	public static final TagKey<Item> NATURAL_LOGS = ConventionalBlockItemTags.NATURAL_LOGS.item();
	/**
	 * For six-sided wood blocks, including Hyphae, found naturally that have not been stripped.
	 */
	public static final TagKey<Item> NATURAL_WOODS = ConventionalBlockItemTags.NATURAL_WOODS.item();
	/**
	 * For logs, including Stems, found naturally that have been stripped.
	 */
	public static final TagKey<Item> STRIPPED_LOGS = ConventionalBlockItemTags.STRIPPED_LOGS.item();
	/**
	 * For six-sided wood blocks, including Hyphae, found naturally that have been stripped.
	 */
	public static final TagKey<Item> STRIPPED_WOODS = ConventionalBlockItemTags.STRIPPED_WOODS.item();

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

	// Seeds
	/**
	 * For items that are explicitly seeds for use cases such as refilling a bird feeder block or certain seed-based recipes.
	 */
	public static final TagKey<Item> SEEDS = register("seeds");
	public static final TagKey<Item> BEETROOT_SEEDS = register("seeds/beetroot");
	public static final TagKey<Item> MELON_SEEDS = register("seeds/melon");
	public static final TagKey<Item> PUMPKIN_SEEDS = register("seeds/pumpkin");
	public static final TagKey<Item> TORCHFLOWER_SEEDS = register("seeds/torchflower");
	public static final TagKey<Item> PITCHER_PLANT_SEEDS = register("seeds/pitcher_plant");
	public static final TagKey<Item> WHEAT_SEEDS = register("seeds/wheat");

	// Other
	public static final TagKey<Item> PLAYER_WORKSTATIONS_CRAFTING_TABLES = ConventionalBlockItemTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES.item();
	public static final TagKey<Item> PLAYER_WORKSTATIONS_FURNACES = ConventionalBlockItemTags.PLAYER_WORKSTATIONS_FURNACES.item();
	public static final TagKey<Item> STRINGS = register("strings");
	public static final TagKey<Item> LEATHERS = register("leathers");
	public static final TagKey<Item> BONES = register("bones");
	/**
	 * For eggs to use for culinary purposes in recipes such as baking a cake.
	 */
	public static final TagKey<Item> EGGS = register("eggs");
	public static final TagKey<Item> FEATHERS = register("feathers");
	public static final TagKey<Item> GUNPOWDERS = register("gunpowders");
	/**
	 * Small mushroom items. Not the full block forms.
	 */
	public static final TagKey<Item> MUSHROOMS = register("mushrooms");
	public static final TagKey<Item> NETHER_STARS = register("nether_stars");
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
	public static final TagKey<Item> ROPES = ConventionalBlockItemTags.ROPES.item();
	public static final TagKey<Item> CHAINS = ConventionalBlockItemTags.CHAINS.item();
	public static final TagKey<Item> ENDER_PEARLS = register("ender_pearls");
	public static final TagKey<Item> SLIME_BALLS = register("slime_balls");
	/**
	 * For bonemeal-like items that can grow plants.
	 * (Note: Could include durability-based modded bonemeal-like items. Check for durability {@link net.minecraft.core.component.DataComponents#DAMAGE} to handle them properly)
	 */
	public static final TagKey<Item> FERTILIZERS = register("fertilizers");

	/**
	 * Tag that holds all items that recipe viewers should not show to users.
	 */
	public static final TagKey<Item> HIDDEN_FROM_RECIPE_VIEWERS = ConventionalBlockItemTags.HIDDEN_FROM_RECIPE_VIEWERS.item();

	/**
	 * Blocks which are often replaced by deepslate ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_DEEPSLATE}, during world generation.
	 * (The block's registry name is used as the tag name)
	 */
	public static final TagKey<Item> ORE_BEARING_GROUND_DEEPSLATE = ConventionalBlockItemTags.ORE_BEARING_GROUND_DEEPSLATE.item();
	/**
	 * Blocks which are often replaced by netherrack ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_NETHERRACK}, during world generation.
	 * (The block's registry name is used as the tag name)
	 */
	public static final TagKey<Item> ORE_BEARING_GROUND_NETHERRACK = ConventionalBlockItemTags.ORE_BEARING_GROUND_NETHERRACK.item();
	/**
	 * Blocks which are often replaced by stone ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_STONE}, during world generation.
	 * (The block's registry name is used as the tag name)
	 */
	public static final TagKey<Item> ORE_BEARING_GROUND_STONE = ConventionalBlockItemTags.ORE_BEARING_GROUND_STONE.item();
	/**
	 * Ores which on average result in more than one resource worth of materials ignoring fortune and other modifiers.
	 * (example, Copper Ore)
	 */
	public static final TagKey<Item> ORE_RATES_DENSE = ConventionalBlockItemTags.ORE_RATES_DENSE.item();
	/**
	 * Ores which on average result in one resource worth of materials ignoring fortune and other modifiers.
	 * (Example, Iron Ore)
	 */
	public static final TagKey<Item> ORE_RATES_SINGULAR = ConventionalBlockItemTags.ORE_RATES_SINGULAR.item();
	/**
	 * Ores which on average result in less than one resource worth of materials ignoring fortune and other modifiers.
	 * (Example, Nether Gold Ore as it drops 2 to 6 Gold Nuggets which is less than normal Gold Ore's Raw Gold drop)
	 */
	public static final TagKey<Item> ORE_RATES_SPARSE = ConventionalBlockItemTags.ORE_RATES_SPARSE.item();
	/**
	 * Ores in deepslate (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_DEEPSLATE}) which could logically use deepslate as recipe input or output.
	 * (The block's registry name is used as the tag name)
	 */
	public static final TagKey<Item> ORES_IN_GROUND_DEEPSLATE = ConventionalBlockItemTags.ORES_IN_GROUND_DEEPSLATE.item();
	/**
	 * Ores in netherrack (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_NETHERRACK}) which could logically use netherrack as recipe input or output.
	 * (The block's registry name is used as the tag name)
	 */
	public static final TagKey<Item> ORES_IN_GROUND_NETHERRACK = ConventionalBlockItemTags.ORES_IN_GROUND_NETHERRACK.item();
	/**
	 * Ores in stone (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_STONE}) which could logically use stone as recipe input or output.
	 * (The block's registry name is used as the tag name)
	 */
	public static final TagKey<Item> ORES_IN_GROUND_STONE = ConventionalBlockItemTags.ORES_IN_GROUND_STONE.item();

	private static TagKey<Item> register(String tagId) {
		return TagRegistration.ITEM_TAG.registerC(tagId);
	}
}
