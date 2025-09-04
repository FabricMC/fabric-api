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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class ContentRegistryGameTest {
	@GameTest
	public void testCompostingChanceRegistry(TestContext context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.COMPOSTER);
		ItemStack obsidian = new ItemStack(Items.OBSIDIAN, 64);
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, obsidian);
		// If on level 0, composting always increases composter level
		context.useBlock(pos, player);
		context.expectBlockProperty(pos, ComposterBlock.LEVEL, 1);
		context.assertEquals(obsidian.getCount(), 63, Text.literal("obsidian stack count"));
		context.complete();
	}

	@GameTest
	public void testFlattenableBlockRegistry(TestContext context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.RED_WOOL);
		ItemStack shovel = new ItemStack(Items.NETHERITE_SHOVEL);
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, shovel);
		context.useBlock(pos, player);
		context.expectBlock(Blocks.YELLOW_WOOL, pos);
		context.assertEquals(shovel.getDamage(), 1, Text.literal("shovel damage"));
		context.complete();
	}

	private void smelt(TestContext context, ItemStack fuelStack, BiConsumer<AbstractFurnaceBlockEntity, HopperBlockEntity> callback) {
		// Create a furnace to simulate smelting in
		// A blast furnace will smelt twice as fast, so it is used here
		var furnacePos = new BlockPos(0, 1, 0);
		BlockState furnaceState = Blocks.BLAST_FURNACE.getDefaultState();

		context.setBlockState(furnacePos, furnaceState);
		AbstractFurnaceBlockEntity furnace = context.getBlockEntity(furnacePos, AbstractFurnaceBlockEntity.class);

		// Create a hopper that attempts to insert fuel into the furnace
		BlockPos hopperPos = furnacePos.east();
		BlockState hopperState = Blocks.HOPPER.getDefaultState()
				.with(HopperBlock.FACING, context.getRotation().rotate(Direction.WEST));

		context.setBlockState(hopperPos, hopperState);
		HopperBlockEntity hopper = context.getBlockEntity(hopperPos, HopperBlockEntity.class);

		// Insert the fuel into the hopper, which transfers it into the furnace
		hopper.setStack(0, fuelStack.copy());

		// Insert the item that should be smelted into the furnace
		// Smelting a single item takes 200 fuel time
		furnace.setStack(0, new ItemStack(Items.RAW_IRON, 1));

		context.waitAndRun(105, () -> callback.accept(furnace, hopper));
	}

	private void smeltCompleted(TestContext context, ItemStack fuelStack) {
		smelt(context, fuelStack, (furnace, hopper) -> {
			context.assertTrue(hopper.isEmpty(), Text.literal("fuel hopper should have been emptied"));

			context.assertTrue(furnace.getStack(0).isEmpty(), Text.literal("furnace input slot should have been emptied"));
			context.assertTrue(furnace.getStack(0).isEmpty(), Text.literal("furnace fuel slot should have been emptied"));
			context.assertTrue(ItemStack.areEqual(furnace.getStack(2), new ItemStack(Items.IRON_INGOT, 1)), Text.literal("one iron ingot should have been smelted and placed into the furnace output slot"));

			context.complete();
		});
	}

	private void smeltFailed(TestContext context, ItemStack fuelStack) {
		smelt(context, fuelStack, (furnace, hopper) -> {
			context.assertTrue(ItemStack.areEqual(hopper.getStack(0), fuelStack), Text.literal("fuel hopper should not have been emptied"));

			context.assertTrue(ItemStack.areEqual(furnace.getStack(0), new ItemStack(Items.RAW_IRON, 1)), Text.literal("furnace input slot should not have been emptied"));
			context.assertTrue(furnace.getStack(1).isEmpty(), Text.literal("furnace fuel slot should not have been filled"));
			context.assertTrue(furnace.getStack(2).isEmpty(), Text.literal("furnace output slot should not have been filled"));

			context.complete();
		});
	}

	@GameTest(maxTicks = 110)
	public void testSmeltingFuelIncludedByItem(TestContext context) {
		// Item with 50 fuel time x4 = 200 fuel time
		smeltCompleted(context, new ItemStack(ContentRegistryTest.SMELTING_FUEL_INCLUDED_BY_ITEM, 4));
	}

	@GameTest(maxTicks = 110)
	public void testSmeltingFuelIncludedByTag(TestContext context) {
		// Item in tag with 100 fuel time x2 = 200 fuel time
		smeltCompleted(context, new ItemStack(ContentRegistryTest.SMELTING_FUEL_INCLUDED_BY_TAG, 2));
	}

	@GameTest(maxTicks = 110)
	public void testSmeltingFuelExcludedByTag(TestContext context) {
		// Item is in both the smelting fuels tag and the excluded smithing fuels tag
		smeltFailed(context, new ItemStack(ContentRegistryTest.SMELTING_FUEL_EXCLUDED_BY_TAG));
	}

	@GameTest(maxTicks = 110)
	public void testSmeltingFuelExcludedByVanillaTag(TestContext context) {
		// Item is in both the smelting fuel tag and vanilla's excluded non-flammable wood tag
		smeltFailed(context, new ItemStack(ContentRegistryTest.SMELTING_FUEL_EXCLUDED_BY_VANILLA_TAG));
	}

	@GameTest
	public void testStrippableBlockRegistry(TestContext context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.QUARTZ_PILLAR);
		ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, axe);
		context.useBlock(pos, player);
		context.expectBlock(Blocks.HAY_BLOCK, pos);
		context.assertEquals(axe.getDamage(), 1, Text.literal("axe damage"));
		context.useBlock(pos, player);
		context.expectBlock(Blocks.TNT, pos);
		BlockState oakStairState = Blocks.OAK_STAIRS.getDefaultState().with(StairsBlock.WATERLOGGED, true).with(StairsBlock.HALF, BlockHalf.TOP);
		context.setBlockState(pos, oakStairState);
		context.useBlock(pos, player);
		context.expectBlockState(pos, Blocks.SPRUCE_STAIRS.getStateWithProperties(oakStairState));
		context.complete();
	}

	@GameTest
	public void testTillableBlockRegistry(TestContext context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.GREEN_WOOL);
		ItemStack hoe = new ItemStack(Items.NETHERITE_HOE);
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, hoe);
		context.useBlock(pos, player);
		context.expectBlock(Blocks.LIME_WOOL, pos);
		context.assertEquals(hoe.getDamage(), 1, Text.literal("hoe damage"));
		context.complete();
	}

	@GameTest
	public void testOxidizableBlocksRegistry(TestContext context) {
		// Test de-oxidation. (the registry does not make the blocks oxidize.)
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.DIAMOND_ORE);
		ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
		player.setStackInHand(Hand.MAIN_HAND, axe);
		context.useBlock(pos, player);
		context.expectBlock(Blocks.GOLD_ORE, pos);
		context.assertEquals(axe.getDamage(), 1, Text.literal("axe damage"));
		context.useBlock(pos, player);
		context.expectBlock(Blocks.IRON_ORE, pos);
		context.useBlock(pos, player);
		context.expectBlock(Blocks.COPPER_ORE, pos);
		context.complete();
	}

	@GameTest
	public void testWaxableBlocksRegistry(TestContext context) {
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.DIAMOND_ORE);
		ItemStack honeycomb = new ItemStack(Items.HONEYCOMB, 64);
		player.setStackInHand(Hand.MAIN_HAND, honeycomb);
		context.useBlock(pos, player);
		context.expectBlock(Blocks.DEEPSLATE_DIAMOND_ORE, pos);
		context.assertEquals(honeycomb.getCount(), 63, Text.literal("honeycomb count"));
		ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
		player.setStackInHand(Hand.MAIN_HAND, axe);
		context.useBlock(pos, player);
		context.expectBlock(Blocks.DIAMOND_ORE, pos);
		context.assertEquals(axe.getDamage(), 1, Text.literal("axe damage"));
		context.complete();
	}

	private void brew(TestContext context, ItemStack input, ItemStack bottle, Consumer<BrewingStandBlockEntity> callback) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.BREWING_STAND);
		BrewingStandBlockEntity brewingStand = context.getBlockEntity(pos, BrewingStandBlockEntity.class);

		brewingStand.setStack(0, bottle);
		brewingStand.setStack(3, input);
		brewingStand.setStack(4, new ItemStack(Items.BLAZE_POWDER, 64));
		context.waitAndRun(401, () -> callback.accept(brewingStand));
	}

	@GameTest(maxTicks = 410)
	public void testBrewingFlower(TestContext context) {
		brew(context, new ItemStack(Items.DANDELION), PotionContentsComponent.createStack(Items.POTION, Potions.AWKWARD), brewingStand -> {
			ItemStack bottle = brewingStand.getStack(0);
			PotionContentsComponent potion = bottle.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
			context.assertEquals(potion.potion().orElseThrow(), Potions.HEALING, Text.literal("brewed potion"));
			context.complete();
		});
	}

	@GameTest(maxTicks = 410)
	public void testBrewingDirt(TestContext context) {
		brew(context, new ItemStack(Items.DIRT), PotionContentsComponent.createStack(Items.POTION, Potions.AWKWARD), brewingStand -> {
			ItemStack bottle = brewingStand.getStack(0);
			context.assertTrue(bottle.getItem() instanceof ContentRegistryTest.DirtyPotionItem, Text.literal("potion became dirty"));
			context.complete();
		});
	}
}
