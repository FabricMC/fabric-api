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
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.test.item.CustomDamageTest;

public class BrewingStandGameTest {
	private static final int BREWING_TIME = 800;
	private static final BlockPos POS = new BlockPos(0, 1, 0);

	@GameTest
	public void basicBrewing(GameTestHelper context) {
		context.setBlock(POS, Blocks.BREWING_STAND);
		BrewingStandBlockEntity blockEntity = context.getBlockEntity(POS, BrewingStandBlockEntity.class);

		loadFuel(blockEntity, context);

		prepareForBrewing(blockEntity, new ItemStack(Items.NETHER_WART, 8),
				setPotion(new ItemStack(Items.POTION), Potions.WATER));

		brew(blockEntity, context);
		assertInventory(blockEntity, "Testing vanilla brewing.",
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				new ItemStack(Items.NETHER_WART, 7),
				ItemStack.EMPTY);

		context.succeed();
	}

	@GameTest
	public void vanillaRemainderTest(GameTestHelper context) {
		context.setBlock(POS, Blocks.BREWING_STAND);
		BrewingStandBlockEntity blockEntity = context.getBlockEntity(POS, BrewingStandBlockEntity.class);

		loadFuel(blockEntity, context);

		prepareForBrewing(blockEntity, new ItemStack(Items.DRAGON_BREATH),
				setPotion(new ItemStack(Items.SPLASH_POTION), Potions.AWKWARD));

		brew(blockEntity, context);
		assertInventory(blockEntity, "Testing vanilla brewing recipe remainder.",
				setPotion(new ItemStack(Items.LINGERING_POTION), Potions.AWKWARD),
				setPotion(new ItemStack(Items.LINGERING_POTION), Potions.AWKWARD),
				setPotion(new ItemStack(Items.LINGERING_POTION), Potions.AWKWARD),
				ItemStack.EMPTY,
				ItemStack.EMPTY);

		context.succeed();
	}

	//@GameTest(templateName = EMPTY_STRUCTURE)
	// Skip see: https://github.com/FabricMC/fabric/pull/2874
	public void fabricRemainderTest(GameTestHelper context) {
		context.setBlock(POS, Blocks.BREWING_STAND);
		BrewingStandBlockEntity blockEntity = context.getBlockEntity(POS, BrewingStandBlockEntity.class);

		loadFuel(blockEntity, context);

		prepareForBrewing(blockEntity, new ItemStack(CustomDamageTest.WEIRD_PICK),
				setPotion(new ItemStack(Items.POTION), Potions.WATER));

		brew(blockEntity, context);
		assertInventory(blockEntity, "Testing fabric brewing recipe remainder.",
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				RecipeGameTest.withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK), 1),
				ItemStack.EMPTY);

		prepareForBrewing(blockEntity, RecipeGameTest.withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK), 10),
				setPotion(new ItemStack(Items.POTION), Potions.WATER));

		brew(blockEntity, context);
		assertInventory(blockEntity, "Testing fabric brewing recipe remainder.",
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				RecipeGameTest.withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK), 11),
				ItemStack.EMPTY);

		prepareForBrewing(blockEntity, RecipeGameTest.withDamage(new ItemStack(CustomDamageTest.WEIRD_PICK), 31),
				setPotion(new ItemStack(Items.POTION), Potions.WATER));

		brew(blockEntity, context);
		assertInventory(blockEntity, "Testing fabric brewing recipe remainder.",
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
				ItemStack.EMPTY,
				ItemStack.EMPTY);

		context.succeed();
	}

	private void prepareForBrewing(BrewingStandBlockEntity blockEntity, ItemStack ingredient, ItemStack potion) {
		blockEntity.setItem(0, potion.copy());
		blockEntity.setItem(1, potion.copy());
		blockEntity.setItem(2, potion.copy());
		blockEntity.setItem(3, ingredient);
	}

	private void assertInventory(BrewingStandBlockEntity blockEntity, String extraErrorInfo, ItemStack... stacks) {
		for (int i = 0; i < stacks.length; i++) {
			ItemStack currentStack = blockEntity.getItem(i);
			ItemStack expectedStack = stacks[i];

			RecipeGameTest.assertStacks(currentStack, expectedStack, extraErrorInfo);
		}
	}

	private void loadFuel(BrewingStandBlockEntity blockEntity, GameTestHelper context) {
		blockEntity.setItem(4, new ItemStack(Items.BLAZE_POWDER));
		BrewingStandBlockEntity.serverTick(context.getLevel(), POS, context.getBlockState(POS), blockEntity);
	}

	private void brew(BrewingStandBlockEntity blockEntity, GameTestHelper context) {
		for (int i = 0; i < BREWING_TIME; i++) {
			BrewingStandBlockEntity.serverTick(context.getLevel(), POS, context.getBlockState(POS), blockEntity);
		}
	}

	private static ItemStack setPotion(ItemStack itemStack, Holder<Potion> potion) {
		itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
		return itemStack;
	}
}
