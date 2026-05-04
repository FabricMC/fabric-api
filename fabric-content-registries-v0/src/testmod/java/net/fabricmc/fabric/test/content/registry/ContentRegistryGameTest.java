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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class ContentRegistryGameTest {
	@GameTest
	public void testCompostingChanceRegistry(GameTestHelper helper) {
		BlockPos pos = new BlockPos(0, 1, 0);
		helper.setBlock(pos, Blocks.COMPOSTER);
		ItemStack obsidian = new ItemStack(Items.OBSIDIAN, 64);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, obsidian);
		// If on level 0, composting always increases composter level
		helper.useBlock(pos, player);
		helper.assertBlockProperty(pos, ComposterBlock.LEVEL, 1);
		helper.assertValueEqual(obsidian.getCount(), 63, Component.literal("obsidian stack count"));
		helper.succeed();
	}

	@GameTest
	public void testFlattenableBlockRegistry(GameTestHelper helper) {
		BlockPos pos = new BlockPos(0, 1, 0);
		helper.setBlock(pos, Blocks.RED_WOOL);
		ItemStack shovel = new ItemStack(Items.NETHERITE_SHOVEL);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, shovel);
		helper.useBlock(pos, player);
		helper.assertBlockPresent(Blocks.YELLOW_WOOL, pos);
		helper.assertValueEqual(shovel.getDamageValue(), 1, Component.literal("shovel damage"));
		helper.succeed();
	}

	private void smelt(GameTestHelper helper, ItemStack fuelStack, BiConsumer<AbstractFurnaceBlockEntity, HopperBlockEntity> callback) {
		// Create a furnace to simulate smelting in
		// A blast furnace will smelt twice as fast, so it is used here
		var furnacePos = new BlockPos(0, 1, 0);
		BlockState furnaceState = Blocks.BLAST_FURNACE.defaultBlockState();

		helper.setBlock(furnacePos, furnaceState);
		AbstractFurnaceBlockEntity furnace = helper.getBlockEntity(furnacePos, AbstractFurnaceBlockEntity.class);

		// Create a hopper that attempts to insert fuel into the furnace
		BlockPos hopperPos = furnacePos.east();
		BlockState hopperState = Blocks.HOPPER.defaultBlockState()
				.setValue(HopperBlock.FACING, helper.getTestRotation().rotate(Direction.WEST));

		helper.setBlock(hopperPos, hopperState);
		HopperBlockEntity hopper = helper.getBlockEntity(hopperPos, HopperBlockEntity.class);

		// Insert the fuel into the hopper, which transfers it into the furnace
		hopper.setItem(0, fuelStack.copy());

		// Insert the item that should be smelted into the furnace
		// Smelting a single item takes 200 fuel time
		furnace.setItem(0, new ItemStack(Items.RAW_IRON, 1));

		helper.runAfterDelay(105, () -> callback.accept(furnace, hopper));
	}

	private void smeltCompleted(GameTestHelper helper, ItemStack fuelStack) {
		smelt(helper, fuelStack, (furnace, hopper) -> {
			helper.assertTrue(hopper.isEmpty(), Component.literal("fuel hopper should have been emptied"));

			helper.assertTrue(furnace.getItem(0).isEmpty(), Component.literal("furnace input slot should have been emptied"));
			helper.assertTrue(furnace.getItem(0).isEmpty(), Component.literal("furnace fuel slot should have been emptied"));
			helper.assertTrue(ItemStack.matches(furnace.getItem(2), new ItemStack(Items.IRON_INGOT, 1)), Component.literal("one iron ingot should have been smelted and placed into the furnace output slot"));

			helper.succeed();
		});
	}

	private void smeltFailed(GameTestHelper helper, ItemStack fuelStack) {
		smelt(helper, fuelStack, (furnace, hopper) -> {
			helper.assertTrue(ItemStack.matches(hopper.getItem(0), fuelStack), Component.literal("fuel hopper should not have been emptied"));

			helper.assertTrue(ItemStack.matches(furnace.getItem(0), new ItemStack(Items.RAW_IRON, 1)), Component.literal("furnace input slot should not have been emptied"));
			helper.assertTrue(furnace.getItem(1).isEmpty(), Component.literal("furnace fuel slot should not have been filled"));
			helper.assertTrue(furnace.getItem(2).isEmpty(), Component.literal("furnace output slot should not have been filled"));

			helper.succeed();
		});
	}

	@GameTest(maxTicks = 110)
	public void testSmeltingFuelIncludedByItem(GameTestHelper helper) {
		// Item with 50 fuel time x4 = 200 fuel time
		smeltCompleted(helper, new ItemStack(ContentRegistryTest.SMELTING_FUEL_INCLUDED_BY_ITEM, 4));
	}

	@GameTest(maxTicks = 110)
	public void testSmeltingFuelIncludedByTag(GameTestHelper helper) {
		// Item in tag with 100 fuel time x2 = 200 fuel time
		smeltCompleted(helper, new ItemStack(ContentRegistryTest.SMELTING_FUEL_INCLUDED_BY_TAG, 2));
	}

	@GameTest(maxTicks = 110)
	public void testSmeltingFuelExcludedByTag(GameTestHelper helper) {
		// Item is in both the smelting fuels tag and the excluded smithing fuels tag
		smeltFailed(helper, new ItemStack(ContentRegistryTest.SMELTING_FUEL_EXCLUDED_BY_TAG));
	}

	@GameTest(maxTicks = 110)
	public void testSmeltingFuelExcludedByVanillaTag(GameTestHelper helper) {
		// Item is in both the smelting fuel tag and vanilla's excluded non-flammable wood tag
		smeltFailed(helper, new ItemStack(ContentRegistryTest.SMELTING_FUEL_EXCLUDED_BY_VANILLA_TAG));
	}

	@GameTest
	public void testStrippableBlockRegistry(GameTestHelper helper) {
		BlockPos pos = new BlockPos(0, 1, 0);
		helper.setBlock(pos, Blocks.QUARTZ_PILLAR);
		ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, axe);
		helper.useBlock(pos, player);
		helper.assertBlockPresent(Blocks.HAY_BLOCK, pos);
		helper.assertValueEqual(axe.getDamageValue(), 1, Component.literal("axe damage"));
		helper.useBlock(pos, player);
		helper.assertBlockPresent(Blocks.TNT, pos);
		BlockState oakStairState = Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.WATERLOGGED, true).setValue(StairBlock.HALF, Half.TOP);
		helper.setBlock(pos, oakStairState);
		helper.useBlock(pos, player);
		helper.assertBlockState(pos, Blocks.SPRUCE_STAIRS.withPropertiesOf(oakStairState));
		helper.succeed();
	}

	@GameTest
	public void testTillableBlockRegistry(GameTestHelper helper) {
		BlockPos pos = new BlockPos(0, 1, 0);
		helper.setBlock(pos, Blocks.GREEN_WOOL);
		ItemStack hoe = new ItemStack(Items.NETHERITE_HOE);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, hoe);
		helper.useBlock(pos, player);
		helper.assertBlockPresent(Blocks.LIME_WOOL, pos);
		helper.assertValueEqual(hoe.getDamageValue(), 1, Component.literal("hoe damage"));
		helper.succeed();
	}

	@GameTest
	public void testOxidizableBlocksRegistry(GameTestHelper helper) {
		// Test de-oxidation. (the registry does not make the blocks oxidize.)
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		BlockPos pos = new BlockPos(0, 1, 0);
		helper.setBlock(pos, Blocks.DIAMOND_ORE);
		ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
		player.setItemInHand(InteractionHand.MAIN_HAND, axe);
		helper.useBlock(pos, player);
		helper.assertBlockPresent(Blocks.GOLD_ORE, pos);
		helper.assertValueEqual(axe.getDamageValue(), 1, Component.literal("axe damage"));
		helper.useBlock(pos, player);
		helper.assertBlockPresent(Blocks.IRON_ORE, pos);
		helper.useBlock(pos, player);
		helper.assertBlockPresent(Blocks.COPPER_ORE, pos);
		helper.succeed();
	}

	@GameTest
	public void testWaxableBlocksRegistry(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		BlockPos pos = new BlockPos(0, 1, 0);
		helper.setBlock(pos, Blocks.DIAMOND_ORE);
		ItemStack honeycomb = new ItemStack(Items.HONEYCOMB, 64);
		player.setItemInHand(InteractionHand.MAIN_HAND, honeycomb);
		helper.useBlock(pos, player);
		helper.assertBlockPresent(Blocks.DEEPSLATE_DIAMOND_ORE, pos);
		helper.assertValueEqual(honeycomb.getCount(), 63, Component.literal("honeycomb count"));
		ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
		player.setItemInHand(InteractionHand.MAIN_HAND, axe);
		helper.useBlock(pos, player);
		helper.assertBlockPresent(Blocks.DIAMOND_ORE, pos);
		helper.assertValueEqual(axe.getDamageValue(), 1, Component.literal("axe damage"));
		helper.succeed();
	}

	private void brew(GameTestHelper helper, ItemStack input, ItemStack bottle, Consumer<BrewingStandBlockEntity> callback) {
		BlockPos pos = new BlockPos(0, 1, 0);
		helper.setBlock(pos, Blocks.BREWING_STAND);
		BrewingStandBlockEntity brewingStand = helper.getBlockEntity(pos, BrewingStandBlockEntity.class);

		brewingStand.setItem(0, bottle);
		brewingStand.setItem(3, input);
		brewingStand.setItem(4, new ItemStack(Items.BLAZE_POWDER, 64));
		helper.runAfterDelay(401, () -> callback.accept(brewingStand));
	}

	@GameTest(maxTicks = 410)
	public void testBrewingFlower(GameTestHelper helper) {
		brew(helper, new ItemStack(Items.DANDELION), PotionContents.createItemStack(Items.POTION, Potions.AWKWARD), brewingStand -> {
			ItemStack bottle = brewingStand.getItem(0);
			PotionContents potion = bottle.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
			helper.assertValueEqual(potion.potion().orElseThrow(), Potions.HEALING, Component.literal("brewed potion"));
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 410)
	public void testBrewingDirt(GameTestHelper helper) {
		brew(helper, new ItemStack(Items.DIRT), PotionContents.createItemStack(Items.POTION, Potions.AWKWARD), brewingStand -> {
			ItemStack bottle = brewingStand.getItem(0);
			helper.assertTrue(bottle.getItem() instanceof ContentRegistryTest.DirtyPotionItem, Component.literal("potion became dirty"));
			helper.succeed();
		});
	}

	private void setupFluidTestBoxAndEntities(GameTestHelper helper, Block block, boolean jump) {
		BlockState state = block.defaultBlockState();
		BlockState wall = Blocks.GLASS.defaultBlockState();

		int fluidHeight = jump ? 4 : 8;

		for (int x = 0; x <= 8; x++) {
			for (int z = 0; z <= 8; z++) {
				helper.setBlock(x, 0, z, wall);
				BlockState inner = x == 0 || x == 8 || z == 0 || z == 8 ? wall : state;

				for (int y = 1; y < fluidHeight; y++) {
					helper.setBlock(x, y, z, inner);
				}
			}
		}

		helper.spawn(EntityType.ACACIA_BOAT, 2, 5, 2);
		Mannequin mannequin = helper.spawn(EntityType.MANNEQUIN, 4, 5, 4);
		Villager villager = helper.spawn(EntityType.VILLAGER, 5, 5, 4);
		helper.spawn(EntityType.ARMOR_STAND, 7, 1, 7);

		if (jump) {
			helper.onEachTick(() -> {
				mannequin.setJumping(true);
				villager.setJumping(true);
			});
		} else {
			villager.removeFreeWill();
		}
	}

	@GameTest(maxTicks = 110)
	public void entityFloatInWater(GameTestHelper helper) {
		setupFluidTestBoxAndEntities(helper, Blocks.WATER, true);

		var box = new AABB(0, 4, 0, 8, 6, 8);

		helper.runAtTickTime(100, () -> {
			helper.assertEntityPresent(EntityType.ACACIA_BOAT, box);
			helper.assertEntityPresent(EntityType.MANNEQUIN, box);
			helper.assertEntityPresent(EntityType.VILLAGER, box);
			helper.assertEntityNotPresent(EntityType.ARMOR_STAND, box);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 110)
	public void entityFloatInWaterLike(GameTestHelper helper) {
		setupFluidTestBoxAndEntities(helper, ContentRegistryTest.WATER_LIKE_FLUID_BLOCK, true);

		var box = new AABB(0, 4, 0, 8, 6, 8);

		helper.runAtTickTime(100, () -> {
			helper.assertEntityPresent(EntityType.ACACIA_BOAT, box);
			helper.assertEntityPresent(EntityType.MANNEQUIN, box);
			helper.assertEntityPresent(EntityType.VILLAGER, box);
			helper.assertEntityNotPresent(EntityType.ARMOR_STAND, box);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 110)
	public void entityFloatInCustom(GameTestHelper helper) {
		setupFluidTestBoxAndEntities(helper, ContentRegistryTest.TEST_FLUID_BLOCK, true);
		var box = new AABB(0, 4, 0, 8, 6, 8);

		helper.runAtTickTime(100, () -> {
			helper.assertEntityPresent(EntityType.ACACIA_BOAT, box);
			helper.assertEntityPresent(EntityType.MANNEQUIN, box);
			helper.assertEntityPresent(EntityType.VILLAGER, box);
			helper.assertEntityPresent(EntityType.ARMOR_STAND, box);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 800)
	public void entityDrownsInWater(GameTestHelper helper) {
		setupFluidTestBoxAndEntities(helper, Blocks.WATER, false);

		helper.runAtTickTime(700, () -> {
			helper.assertEntityNotPresent(EntityType.MANNEQUIN);
			helper.assertEntityNotPresent(EntityType.VILLAGER);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 800)
	public void entityDrownsInWaterLike(GameTestHelper helper) {
		setupFluidTestBoxAndEntities(helper, ContentRegistryTest.WATER_LIKE_FLUID_BLOCK, false);

		helper.runAtTickTime(700, () -> {
			helper.assertEntityNotPresent(EntityType.MANNEQUIN);
			helper.assertEntityNotPresent(EntityType.VILLAGER);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 800)
	public void entityDrownsInCustom(GameTestHelper helper) {
		setupFluidTestBoxAndEntities(helper, ContentRegistryTest.TEST_FLUID_BLOCK, false);

		helper.runAtTickTime(700, () -> {
			helper.assertEntityPresent(EntityType.MANNEQUIN);
			helper.assertEntityPresent(EntityType.VILLAGER);
			helper.succeed();
		});
	}

	private void setupPushAndMove(GameTestHelper helper, Block block) {
		BlockState state = block.defaultBlockState();
		BlockState wall = Blocks.GLASS.defaultBlockState();

		helper.setBlock(0, 1, 4, wall);
		helper.setBlock(0, 2, 4, wall);

		for (int x = 1; x < 8; x++) {
			helper.setBlock(x, 1, 4, state.setValue(LiquidBlock.LEVEL, 8 - x));
			helper.setBlock(x, 0, 4, wall);
			helper.setBlock(x, 1, 5, wall);
			helper.setBlock(x, 1, 3, wall);
			helper.setBlock(x, 2, 5, wall);
			helper.setBlock(x, 2, 3, wall);
		}

		helper.setBlock(1, 1, 4, state.setValue(LiquidBlock.LEVEL, 0));

		helper.setBlock(8, 1, 4, wall);
		helper.setBlock(8, 2, 4, wall);

		helper.spawn(EntityType.MANNEQUIN, 4, 1, 4);
	}

	@GameTest(maxTicks = 110)
	public void entityPushingAndMovementInWater(GameTestHelper helper) {
		setupPushAndMove(helper, Blocks.WATER);
		helper.runAtTickTime(100, () -> {
			helper.assertEntityPresent(EntityType.MANNEQUIN, 7, 1, 4);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 110)
	public void entityPushingAndMovementInWaterLike(GameTestHelper helper) {
		setupPushAndMove(helper, ContentRegistryTest.WATER_LIKE_FLUID_BLOCK);
		helper.runAtTickTime(100, () -> {
			helper.assertEntityPresent(EntityType.MANNEQUIN, 7, 1, 4);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 110)
	public void entityPushingAndMovementInCustom(GameTestHelper helper) {
		setupPushAndMove(helper, ContentRegistryTest.TEST_FLUID_BLOCK);
		helper.runAtTickTime(100, () -> {
			helper.assertEntityPresent(EntityType.MANNEQUIN, new BlockPos(1, 1, 4), 1f);
			helper.succeed();
		});
	}
}
