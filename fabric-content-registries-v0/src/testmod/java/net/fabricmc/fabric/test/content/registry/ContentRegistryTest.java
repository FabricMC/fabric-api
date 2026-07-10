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
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperFullBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.BlockTransformerRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.fabricmc.fabric.api.registry.LandPathTypeRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.registry.VibrationFrequencyRegistry;
import net.fabricmc.fabric.api.registry.VillagerInteractionRegistries;
import net.fabricmc.fabric.api.registry.fluid.EntityFluidInteractionRegistry;
import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;

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

	public static final FlowingFluid TEST_FLUID = Registry.register(BuiltInRegistries.FLUID, id("test_fluid"), new TestFluid.Still());
	public static final FlowingFluid TEST_FLUID_FLOWING = Registry.register(BuiltInRegistries.FLUID, id("test_fluid_flowing"), new TestFluid.Flowing());
	public static final LiquidBlock TEST_FLUID_BLOCK = Registry.register(BuiltInRegistries.BLOCK, id("test_fluid"), new LiquidBlock(TEST_FLUID, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(ResourceKey.create(Registries.BLOCK, id("test_fluid")))) {
	});

	public static final TagKey<Fluid> TEST_FLUID_KEY = TagKey.create(Registries.FLUID, id("test_fluid"));

	public static final FlowingFluid WATER_LIKE_FLUID = Registry.register(BuiltInRegistries.FLUID, id("water_like_fluid"), new WaterLikeFluid.Still());
	public static final FlowingFluid WATER_LIKE_FLUID_FLOWING = Registry.register(BuiltInRegistries.FLUID, id("water_like_fluid_flowing"), new WaterLikeFluid.Flowing());
	public static final LiquidBlock WATER_LIKE_FLUID_BLOCK = Registry.register(BuiltInRegistries.BLOCK, id("water_like_fluid"), new LiquidBlock(WATER_LIKE_FLUID, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(ResourceKey.create(Registries.BLOCK, id("test_fluid")))) {
	});

	public static final TagKey<Fluid> WATER_LIKE_FLUID_KEY = TagKey.create(Registries.FLUID, id("water_like"));

	@Override
	public void onInitialize() {
		// Expected behavior:
		//  - diamond block is now flammable
		//  - sand is now flammable
		//  - custom items prefixed with 'smelting fuels included by' are valid smelting fuels
		//  - dead bush is now considered as a dangerous block like sweet berry bushes (all entities except foxes should avoid it)
		//  - copper ore, iron ore, gold ore, and diamond ore can be waxed into their deepslate variants and scraped back again
		//  - aforementioned ores can be scraped from diamond -> gold -> iron -> copper
		//  - the 'test_oxidizing' block will randomly tick to oxidize into an 'exposed_test_oxidizing' block
		//  - villagers can now collect, consume (at the same level of bread) and compost apples
		//  - villagers can now collect oak saplings
		//  - assign a loot table to the nitwit villager type
		//  - right-clicking a 'test_event' block will emit a 'test_event' game event, which will have a vibration frequency of 2
		//  - new test fluids acts as a proper liquid like water / lava
		//  - dried kelp blocks can be transformed into dead brain coral blocks by a shovel, without any sound or particle
		//  - bamboo mosaics can be transformed into bamboo blocks by a hoe, in precisely the same manner as tilling dirt into farmland
		//  - any wool stairs can be transformed into a white wool slab by an axe, in precisely the same manner as stripping a log
		//  - acacia stairs and birch stairs can be transformed into a top-half pale oak slab by a shovel, in precisely the same manner as flattening dirt into a path

		FlammableBlockRegistry.getDefaultInstance().add(Blocks.DIAMOND_BLOCK, 4, 4);
		FlammableBlockRegistry.getDefaultInstance().add(BlockTags.SAND, 4, 4);

		FuelValueEvents.BUILD.register((builder, context) -> {
			builder.add(SMELTING_FUEL_INCLUDED_BY_ITEM, context.baseSmeltTime() / 4);
			builder.add(SMELTING_FUELS_INCLUDED_BY_TAG, context.baseSmeltTime() / 2);
		});

		FuelValueEvents.EXCLUSIONS.register((builder, context) -> {
			builder.remove(SMELTING_FUELS_EXCLUDED_BY_TAG);
		});

		LandPathTypeRegistry.register(Blocks.DEAD_BUSH, PathType.DAMAGING, PathType.DAMAGING_IN_NEIGHBOR);

		OxidizableBlocksRegistry.registerNextStage(Blocks.COPPER_ORE, Blocks.IRON_ORE);
		OxidizableBlocksRegistry.registerNextStage(Blocks.IRON_ORE, Blocks.GOLD_ORE);
		OxidizableBlocksRegistry.registerNextStage(Blocks.GOLD_ORE, Blocks.DIAMOND_ORE);

		OxidizableBlocksRegistry.registerWaxable(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
		OxidizableBlocksRegistry.registerWaxable(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
		OxidizableBlocksRegistry.registerWaxable(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE);
		OxidizableBlocksRegistry.registerWaxable(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);

		// assert that OxidizableBlocksRegistry throws when registered blocks are null
		try {
			OxidizableBlocksRegistry.registerNextStage(Blocks.EMERALD_ORE, null);
			OxidizableBlocksRegistry.registerNextStage(null, Blocks.COAL_ORE);

			OxidizableBlocksRegistry.registerWaxable(null, Blocks.DEAD_BRAIN_CORAL);
			OxidizableBlocksRegistry.registerWaxable(Blocks.BRAIN_CORAL, null);

			throw new AssertionError("OxidizableBlocksRegistry didn't throw when blocks were null!");
		} catch (NullPointerException e) {
			// expected behavior
			LOGGER.info("OxidizableBlocksRegistry null test passed!");
		}

		Block testOxidizingBlock = Registry.register(BuiltInRegistries.BLOCK, TEST_OXIDIZING_BLOCK_KEY, new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.UNAFFECTED, BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).setId(TEST_OXIDIZING_BLOCK_KEY)));
		Block exposedTestOxidizingBlock = Registry.register(BuiltInRegistries.BLOCK, EXPOSED_TEST_OXIDIZING_BLOCK_KEY, new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).setId(EXPOSED_TEST_OXIDIZING_BLOCK_KEY)));

		OxidizableBlocksRegistry.registerNextStage(testOxidizingBlock, exposedTestOxidizingBlock);

		if (!testOxidizingBlock.getStateDefinition().any().isRandomlyTicking()) {
			throw new AssertionError("OxidizableBlocksRegistry didn't refresh random ticks cache for state!");
		}

		LOGGER.info("OxidizableBlocksRegistry random ticks test passed!");

		VillagerInteractionRegistries.registerFood(Items.APPLE, 4);
		VillagerInteractionRegistries.registerCompostable(Items.APPLE);

		VillagerInteractionRegistries.registerGiftLootTable(VillagerProfession.NITWIT, ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("fake_loot_table")));

		Registry.register(BuiltInRegistries.BLOCK, TEST_EVENT_BLOCK_KEY, new TestEventBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(TEST_EVENT_BLOCK_KEY)));
		VibrationFrequencyRegistry.register(TEST_EVENT.key(), 2);

		// assert that VibrationFrequencyRegistry throws when registering a frequency outside the allowed range
		try {
			VibrationFrequencyRegistry.register(GameEvent.SHRIEK.key(), 18);

			throw new AssertionError("VibrationFrequencyRegistry didn't throw when frequency was outside allowed range!");
		} catch (IllegalArgumentException e) {
			// expected behavior
			LOGGER.info("VibrationFrequencyRegistry test passed!");
		}

		EntityFluidInteractionRegistry.register(TEST_FLUID_KEY, FluidBehavior.simple()
				.allowBoats(true).allowMovingDown(true).allowSwimming(false).enableDrowning(false)
				.gravityMultiplier(-0.25f).makeMobsFloat(true).flowingPushScale(-0.02f)
				.movementSpeed(0.02f).movementSlowdown(0.8f, 0.6f).fallDistanceModifier(0.8f).build());

		EntityFluidInteractionRegistry.register(WATER_LIKE_FLUID_KEY, FluidBehavior.WATER_LIKE);

		BlockTransformerRegistry.registerShovel(BlockTransformer.BlockTransformData.builder(BlockPredicate.matchesBlocks(Blocks.DRIED_KELP_BLOCK), Blocks.DEAD_BRAIN_CORAL_BLOCK).build());
		BlockTransformerRegistry.registerTilling(Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_BLOCK);
		BlockTransformerRegistry.registerStripping(BlockTags.WOOL_STAIRS, Blocks.WOOL_SLAB.white());
		BlockTransformerRegistry.registerFlattening(new Block[]{Blocks.ACACIA_STAIRS, Blocks.BIRCH_STAIRS}, Blocks.PALE_OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP));
	}

	public static class TestEventBlock extends Block {
		public TestEventBlock(Properties properties) {
			super(properties);
		}

		@Override
		public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
			// Emit the test event
			level.gameEvent(player, TEST_EVENT, pos);
			return InteractionResult.SUCCESS;
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
