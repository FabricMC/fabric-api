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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperFullBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.BlockHitResult;

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
	public static final ResourceKey<Block> TEST_EVENT_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, TEST_EVENT_ID);
	public static final Holder.Reference<GameEvent> TEST_EVENT = Registry.registerForHolder(BuiltInRegistries.GAME_EVENT, TEST_EVENT_ID, new GameEvent(GameEvent.DEFAULT_NOTIFICATION_RADIUS));

	public static final ResourceKey<Block> TEST_OXIDIZING_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, id("test_oxidizing"));
	public static final ResourceKey<Block> EXPOSED_TEST_OXIDIZING_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, id("exposed_test_oxidizing"));

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
		FlattenableBlockRegistry.register(Blocks.RED_WOOL, Blocks.YELLOW_WOOL.defaultBlockState());

		FuelRegistryEvents.BUILD.register((builder, context) -> {
			builder.add(SMELTING_FUEL_INCLUDED_BY_ITEM, context.baseSmeltTime() / 4);
			builder.add(SMELTING_FUELS_INCLUDED_BY_TAG, context.baseSmeltTime() / 2);
		});

		FuelRegistryEvents.EXCLUSIONS.register((builder, context) -> {
			builder.remove(SMELTING_FUELS_EXCLUDED_BY_TAG);
		});

		LandPathNodeTypesRegistry.register(Blocks.DEAD_BUSH, PathType.DAMAGE_OTHER, PathType.DANGER_OTHER);
		StrippableBlockRegistry.register(Blocks.QUARTZ_PILLAR, Blocks.HAY_BLOCK);
		StrippableBlockRegistry.register(Blocks.HAY_BLOCK, Blocks.TNT);
		StrippableBlockRegistry.registerCopyState(Blocks.OAK_STAIRS, Blocks.SPRUCE_STAIRS);

		TillableBlockRegistry.register(Blocks.GREEN_WOOL, context -> true, HoeItem.changeIntoState(Blocks.LIME_WOOL.defaultBlockState()));

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

		Block testOxidizingBlock = Registry.register(BuiltInRegistries.BLOCK, TEST_OXIDIZING_BLOCK_KEY, new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.UNAFFECTED, BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).setId(TEST_OXIDIZING_BLOCK_KEY)));
		Block exposedTestOxidizingBlock = Registry.register(BuiltInRegistries.BLOCK, EXPOSED_TEST_OXIDIZING_BLOCK_KEY, new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER).setId(EXPOSED_TEST_OXIDIZING_BLOCK_KEY)));

		OxidizableBlocksRegistry.registerOxidizableBlockPair(testOxidizingBlock, exposedTestOxidizingBlock);

		if (!testOxidizingBlock.getStateDefinition().any().isRandomlyTicking()) {
			throw new AssertionError("OxidizableBlocksRegistry didn't refresh random ticks cache for state!");
		}

		LOGGER.info("OxidizableBlocksRegistry random ticks test passed!");

		VillagerInteractionRegistries.registerFood(Items.APPLE, 4);
		VillagerInteractionRegistries.registerCompostable(Items.APPLE);

		VillagerInteractionRegistries.registerGiftLootTable(VillagerProfession.NITWIT, ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("fake_loot_table")));

		Registry.register(BuiltInRegistries.BLOCK, TEST_EVENT_BLOCK_KEY, new TestEventBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(TEST_EVENT_BLOCK_KEY)));
		SculkSensorFrequencyRegistry.register(TEST_EVENT.key(), 2);

		// assert that SculkSensorFrequencyRegistry throws when registering a frequency outside the allowed range
		try {
			SculkSensorFrequencyRegistry.register(GameEvent.SHRIEK.key(), 18);

			throw new AssertionError("SculkSensorFrequencyRegistry didn't throw when frequency was outside allowed range!");
		} catch (IllegalArgumentException e) {
			// expected behavior
			LOGGER.info("SculkSensorFrequencyRegistry test passed!");
		}

		ResourceKey<Item> dirtyPotionKey = ResourceKey.create(Registries.ITEM, id("dirty_potion"));
		var dirtyPotion = new DirtyPotionItem(new Item.Properties().stacksTo(1).setId(dirtyPotionKey));
		Registry.register(BuiltInRegistries.ITEM, dirtyPotionKey, dirtyPotion);
		/* Mods should use BrewingRecipeRegistry.registerPotionType(Item), which is access widened by fabric-transitive-access-wideners-v1
		 * This testmod uses an accessor due to Loom limitations that prevent TAWs from applying across Gradle subproject boundaries */
		FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
			builder.addContainer(dirtyPotion);
			builder.registerItemRecipe(Items.POTION, Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.DIRT)), dirtyPotion);
			builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.SMALL_FLOWERS)), Potions.HEALING);

			if (builder.getEnabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS)) {
				builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(Items.BUNDLE), Potions.LUCK);
			}
		});
	}

	public static class TestEventBlock extends Block {
		public TestEventBlock(Properties settings) {
			super(settings);
		}

		@Override
		public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
			// Emit the test event
			world.gameEvent(player, TEST_EVENT, pos);
			return InteractionResult.SUCCESS;
		}
	}

	public static class DirtyPotionItem extends PotionItem {
		public DirtyPotionItem(Properties settings) {
			super(settings);
		}

		@Override
		public Component getName(ItemStack stack) {
			return Component.literal("Dirty ").append(Items.POTION.getName(stack));
		}
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	private static Item registerItem(String path) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(path));
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key)));
	}

	private static TagKey<Item> itemTag(String path) {
		return TagKey.create(Registries.ITEM, id(path));
	}
}
