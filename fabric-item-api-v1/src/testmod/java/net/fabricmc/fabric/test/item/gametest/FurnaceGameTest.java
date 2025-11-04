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

package net.fabricmc.fabric.test.item.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.test.item.CustomDamageTest;

public class FurnaceGameTest {
	private static final int COOK_TIME = 200;
	private static final BlockPos POS = new BlockPos(0, 1, 0);

	@GameTest
	public void basicSmelt(GameTestHelper context) {
		context.setBlock(POS, Blocks.FURNACE);
		FurnaceBlockEntity blockEntity = context.getBlockEntity(POS, FurnaceBlockEntity.class);

		setInputs(blockEntity, new ItemStack(Blocks.COBBLESTONE, 8), new ItemStack(Items.COAL, 2));

		cook(blockEntity, context, 1);
		assertInventory(blockEntity, "Testing vanilla smelting.",
				new ItemStack(Blocks.COBBLESTONE, 7),
				new ItemStack(Items.COAL, 1),
				new ItemStack(Blocks.STONE, 1));

		cook(blockEntity, context, 7);
		assertInventory(blockEntity, "Testing vanilla smelting.",
				ItemStack.EMPTY,
				new ItemStack(Items.COAL, 1),
				new ItemStack(Blocks.STONE, 8));

		context.succeed();
	}

	@GameTest
	public void vanillaRemainderTest(GameTestHelper context) {
		context.setBlock(POS, Blocks.FURNACE);
		FurnaceBlockEntity blockEntity = context.getBlockEntity(POS, FurnaceBlockEntity.class);

		setInputs(blockEntity, new ItemStack(Blocks.COBBLESTONE, 64), new ItemStack(Items.LAVA_BUCKET));

		cook(blockEntity, context, 64);
		assertInventory(blockEntity, "Testing vanilla smelting recipe remainder.",
				ItemStack.EMPTY,
				new ItemStack(Items.BUCKET),
				new ItemStack(Blocks.STONE, 64));

		context.succeed();
	}

	@GameTest
	public void fabricRemainderTest(GameTestHelper context) {
		context.setBlock(POS, Blocks.FURNACE);
		FurnaceBlockEntity blockEntity = context.getBlockEntity(POS, FurnaceBlockEntity.class);

		setInputs(blockEntity, new ItemStack(Blocks.COBBLESTONE, 32), new ItemStack(CustomDamageTest.WEIRD_PICK));

		cook(blockEntity, context, 1);
		assertInventory(blockEntity, "Testing fabric smelting recipe remainder.",
				new ItemStack(Blocks.COBBLESTONE, 31),
				RecipeGameTest.withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK), 1),
				new ItemStack(Blocks.STONE, 1));

		cook(blockEntity, context, 30);
		assertInventory(blockEntity, "Testing fabric smelting recipe remainder.",
				new ItemStack(Blocks.COBBLESTONE, 1),
				RecipeGameTest.withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK), 31),
				new ItemStack(Blocks.STONE, 31));

		cook(blockEntity, context, 1);
		assertInventory(blockEntity, "Testing fabric smelting recipe remainder.",
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Blocks.STONE, 32));

		context.succeed();
	}

	private void setInputs(FurnaceBlockEntity blockEntity, ItemStack ingredient, ItemStack fuel) {
		blockEntity.setItem(0, ingredient);
		blockEntity.setItem(1, fuel);
	}

	private void assertInventory(FurnaceBlockEntity blockEntity, String extraErrorInfo, ItemStack... stacks) {
		for (int i = 0; i < stacks.length; i++) {
			ItemStack currentStack = blockEntity.getItem(i);
			ItemStack expectedStack = stacks[i];

			RecipeGameTest.assertStacks(currentStack, expectedStack, extraErrorInfo);
		}
	}

	private void cook(FurnaceBlockEntity blockEntity, GameTestHelper context, int items) {
		for (int i = 0; i < COOK_TIME * items; i++) {
			AbstractFurnaceBlockEntity.serverTick(context.getLevel(), POS, context.getBlockState(POS), blockEntity);
		}
	}
}
