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

package net.fabricmc.fabric.test.content.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.OxidizableBlock;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FlattenableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.fabricmc.fabric.api.registry.LandPathNodeTypesRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.registry.SculkSensorFrequencyRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.registry.TillableBlockRegistry;
import net.fabricmc.fabric.api.registry.VillagerInteractionRegistries;

public final class ContentRegistryTest implements ModInitializer {
	public static final String MOD_ID = "fabric-content-registries-v0-testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(ContentRegistryTest.class);

	public static final Item SMELTING_FUEL_INCLUDED_BY_ITEM = registerItem("smelting_fuel_included_by_item");
	public static final Item SMELTING_FUEL_INCLUDED_BY_TAG = registerItem("smelting_fuel_included_by_tag");
	public static final Item SMELTING_FUEL_EXCLUDED_BY_TAG = registerItem("smelting_fuel_excluded_by_tag");
	public static final Item SMELTING_FUEL_EXCLUDED_BY_VANILLA_TAG = registerItem("smelting_fuel_excluded_by_vanilla_tag");

	private static final TagKey<Item> SMELTING_FUELS_INCLUDED_BY_TAG = itemTag("smelting_fuels_included_by_tag");
	private static final TagKey<Item> SMELTING_FUELS_EXCLUDED_BY_TAG = itemTag("smelting_fuels_excluded_by_tag");

	public static final Identifier TEST_EVENT_ID = id("test_event");
	public static final RegistryKey<Block> TEST_EVENT_BLOCK_KEY = RegistryKey.of(RegistryKeys.BLOCK, TEST_EVENT_ID);
	public static final RegistryEntry.Reference<GameEvent> TEST_EVENT = Registry.registerReference(Registries.GAME_EVENT, TEST_EVENT_ID, new GameEvent(GameEvent.DEFAULT_RANGE));

	public static final RegistryKey<Block> TEST_OXIDIZING_BLOCK_KEY = RegistryKey.of(RegistryKeys.BLOCK, id("test_oxidizing"));
	public static final RegistryKey<Block> EXPOSED_TEST_OXIDIZING_BLOCK_KEY = RegistryKey.of(RegistryKeys.BLOCK, id("exposed_test_oxidizing"));

	@Override
	public void onInitialize() {
		// Expected behavior:
		//  - obsidian is now compostable
		//  - diamond block is now flammable
		//  - sand is now flammable
		//  - red wool is flattenable to yellow wool
		//  - custom items prefixed with 'smelting fuels included by' are valid smelting fuels
		//  - dead bush is now considered as a dangerous block like sweet berry bushes (all entities except foxes should avoid it)
		//  - quartz pillars are strippable to hay blocks
		//  - hay blocks are strippable to tnt
		//  - oak stairs are strippable to spruce stairs, while preserving all block state properties
		//  - green wool is tillable to lime wool
		//  - copper ore, iron ore, gold ore, and diamond ore can be waxed into their deepslate variants and scraped back again
		//  - aforementioned ores can be scraped from diamond -> gold -> iron -> copper
		//  - the 'test_oxidizing' block will randomly tick to oxidize into an 'exposed_test_oxidizing' block
		//  - villagers can now collect, consume (at the same level of bread) and compost apples
		//  - villagers can now collect oak saplings
		//  - assign a loot table to the nitwit villager type
		//  - right-clicking a 'test_event' block will emit a 'test_event' game event, which will have a sculk sensor frequency of 2
		//  - instant health potions can be brewed from awkward potions with any item in the 'minecraft:small_flowers' tag
		//  - if Redstone Experiments experiment is enabled, luck potions can be brewed from awkward potions with a bundle
		//  - dirty potions can be brewed by adding any item in the 'minecraft:dirt' tag to any standard potion

		CompostingChanceRegistry.INSTANCE.add(Items.OBSIDIAN, 0.5F);
		FlammableBlockRegistry.getDefaultInstance().add(Blocks.DIAMOND_BLOCK, 4, 4);
		FlammableBlockRegistry.getDefaultInstance().add(BlockTags.SAND, 4, 4);
		FlattenableBlockRegistry.register(Blocks.RED_WOOL, Blocks.YELLOW_WOOL.getDefaultState());

		FuelRegistryEvents.BUILD.register((builder, context) -> {
			builder.add(SMELTING_FUEL_INCLUDED_BY_ITEM, context.baseSmeltTime() / 4);
			builder.add(SMELTING_FUELS_INCLUDED_BY_TAG, context.baseSmeltTime() / 2);
		});

		FuelRegistryEvents.EXCLUSIONS.register((builder, context) -> {
			builder.remove(SMELTING_FUELS_EXCLUDED_BY_TAG);
		});

		LandPathNodeTypesRegistry.register(Blocks.DEAD_BUSH, PathNodeType.DAMAGE_OTHER, PathNodeType.DANGER_OTHER);
		StrippableBlockRegistry.register(Blocks.QUARTZ_PILLAR, Blocks.HAY_BLOCK);
		StrippableBlockRegistry.register(Blocks.HAY_BLOCK, Blocks.TNT);
		StrippableBlockRegistry.registerCopyState(Blocks.OAK_STAIRS, Blocks.SPRUCE_STAIRS);

		TillableBlockRegistry.register(Blocks.GREEN_WOOL, context -> true, HoeItem.createTillAction(Blocks.LIME_WOOL.getDefaultState()));

		OxidizableBlocksRegistry.registerOxidizableBlockPair(Blocks.COPPER_ORE, Blocks.IRON_ORE);
		OxidizableBlocksRegistry.registerOxidizableBlockPair(Blocks.IRON_ORE, Blocks.GOLD_ORE);
		OxidizableBlocksRegistry.registerOxidizableBlockPair(Blocks.GOLD_ORE, Blocks.DIAMOND_ORE);

		OxidizableBlocksRegistry.registerWaxableBlockPair(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
		OxidizableBlocksRegistry.registerWaxableBlockPair(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
		OxidizableBlocksRegistry.registerWaxableBlockPair(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE);
		OxidizableBlocksRegistry.registerWaxableBlockPair(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);

		// assert that OxidizableBlocksRegistry throws when registered blocks are null
		try {
			OxidizableBlocksRegistry.registerOxidizableBlockPair(Blocks.EMERALD_ORE, null);
			OxidizableBlocksRegistry.registerOxidizableBlockPair(null, Blocks.COAL_ORE);

			OxidizableBlocksRegistry.registerWaxableBlockPair(null, Blocks.DEAD_BRAIN_CORAL);
			OxidizableBlocksRegistry.registerWaxableBlockPair(Blocks.BRAIN_CORAL, null);

			throw new AssertionError("OxidizableBlocksRegistry didn't throw when blocks were null!");
		} catch (NullPointerException e) {
			// expected behavior
			LOGGER.info("OxidizableBlocksRegistry null test passed!");
		}

		Block testOxidizingBlock = Registry.register(Registries.BLOCK, TEST_OXIDIZING_BLOCK_KEY, new OxidizableBlock(Oxidizable.OxidationLevel.UNAFFECTED, AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).registryKey(TEST_OXIDIZING_BLOCK_KEY)));
		Block exposedTestOxidizingBlock = Registry.register(Registries.BLOCK, EXPOSED_TEST_OXIDIZING_BLOCK_KEY, new OxidizableBlock(Oxidizable.OxidationLevel.EXPOSED, AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER).registryKey(EXPOSED_TEST_OXIDIZING_BLOCK_KEY)));

		OxidizableBlocksRegistry.registerOxidizableBlockPair(testOxidizingBlock, exposedTestOxidizingBlock);

		if (!testOxidizingBlock.getStateManager().getDefaultState().hasRandomTicks()) {
			throw new AssertionError("OxidizableBlocksRegistry didn't refresh random ticks cache for state!");
		}

		LOGGER.info("OxidizableBlocksRegistry random ticks test passed!");

		VillagerInteractionRegistries.registerFood(Items.APPLE, 4);
		VillagerInteractionRegistries.registerCompostable(Items.APPLE);

		VillagerInteractionRegistries.registerGiftLootTable(VillagerProfession.NITWIT, RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.ofVanilla("fake_loot_table")));

		Registry.register(Registries.BLOCK, TEST_EVENT_BLOCK_KEY, new TestEventBlock(AbstractBlock.Settings.copy(Blocks.STONE).registryKey(TEST_EVENT_BLOCK_KEY)));
		SculkSensorFrequencyRegistry.register(TEST_EVENT.registryKey(), 2);

		// assert that SculkSensorFrequencyRegistry throws when registering a frequency outside the allowed range
		try {
			SculkSensorFrequencyRegistry.register(GameEvent.SHRIEK.registryKey(), 18);

			throw new AssertionError("SculkSensorFrequencyRegistry didn't throw when frequency was outside allowed range!");
		} catch (IllegalArgumentException e) {
			// expected behavior
			LOGGER.info("SculkSensorFrequencyRegistry test passed!");
		}

		RegistryKey<Item> dirtyPotionKey = RegistryKey.of(RegistryKeys.ITEM, id("dirty_potion"));
		var dirtyPotion = new DirtyPotionItem(new Item.Settings().maxCount(1).registryKey(dirtyPotionKey));
		Registry.register(Registries.ITEM, dirtyPotionKey, dirtyPotion);
		/* Mods should use BrewingRecipeRegistry.registerPotionType(Item), which is access widened by fabric-transitive-access-wideners-v1
		 * This testmod uses an accessor due to Loom limitations that prevent TAWs from applying across Gradle subproject boundaries */
		FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
			builder.registerPotionType(dirtyPotion);
			builder.registerItemRecipe(Items.POTION, Ingredient.ofTag(Registries.ITEM.getOrThrow(ItemTags.DIRT)), dirtyPotion);
			builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofTag(Registries.ITEM.getOrThrow(ItemTags.SMALL_FLOWERS)), Potions.HEALING);

			if (builder.getEnabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS)) {
				builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItems(Items.BUNDLE), Potions.LUCK);
			}
		});
	}

	public static class TestEventBlock extends Block {
		public TestEventBlock(Settings settings) {
			super(settings);
		}

		@Override
		public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
			// Emit the test event
			world.emitGameEvent(player, TEST_EVENT, pos);
			return ActionResult.SUCCESS;
		}
	}

	public static class DirtyPotionItem extends PotionItem {
		public DirtyPotionItem(Settings settings) {
			super(settings);
		}

		@Override
		public Text getName(ItemStack stack) {
			return Text.literal("Dirty ").append(Items.POTION.getName(stack));
		}
	}

	private static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	private static Item registerItem(String path) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id(path));
		return Registry.register(Registries.ITEM, key, new Item(new Item.Settings().registryKey(key)));
	}

	private static TagKey<Item> itemTag(String path) {
		return TagKey.of(RegistryKeys.ITEM, id(path));
	}
}
