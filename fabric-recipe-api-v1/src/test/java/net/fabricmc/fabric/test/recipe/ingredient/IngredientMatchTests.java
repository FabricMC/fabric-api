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

package net.fabricmc.fabric.test.recipe.ingredient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;

public class IngredientMatchTests {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	public void testAllIngredient() {
		Ingredient allIngredient = DefaultCustomIngredients.all(Ingredient.of(Items.APPLE, Items.CARROT), Ingredient.of(Items.STICK, Items.CARROT));

		assertEquals(1, allIngredient.items().toList().size());
		assertEquals(Items.CARROT, allIngredient.items().toList().getFirst().value());
		assertFalse(allIngredient.items().toList().isEmpty());

		assertFalse(allIngredient.test(new ItemStack(Items.APPLE)));
		assertTrue(allIngredient.test(new ItemStack(Items.CARROT)));
		assertFalse(allIngredient.test(new ItemStack(Items.STICK)));

		Ingredient emptyAllIngredient = DefaultCustomIngredients.all(Ingredient.of(Items.APPLE), Ingredient.of(Items.STICK));

		assertEquals(0, emptyAllIngredient.items().toList().size());
		assertTrue(emptyAllIngredient.items().toList().isEmpty());

		assertFalse(emptyAllIngredient.test(new ItemStack(Items.APPLE)));
		assertFalse(emptyAllIngredient.test(new ItemStack(Items.STICK)));
	}

	@Test
	public void testAnyIngredient() {
		Ingredient anyIngredient = DefaultCustomIngredients.any(Ingredient.of(Items.APPLE, Items.CARROT), Ingredient.of(Items.STICK, Items.CARROT));

		assertEquals(4, anyIngredient.items().toList().size());
		assertEquals(Items.APPLE, anyIngredient.items().toList().getFirst().value());
		assertEquals(Items.CARROT, anyIngredient.items().toList().get(1).value());
		assertEquals(Items.STICK, anyIngredient.items().toList().get(2).value());
		assertEquals(Items.CARROT, anyIngredient.items().toList().get(3).value());
		assertFalse(anyIngredient.items().toList().isEmpty());

		assertTrue(anyIngredient.test(new ItemStack(Items.APPLE)));
		assertTrue(anyIngredient.test(new ItemStack(Items.CARROT)));
		assertTrue(anyIngredient.test(new ItemStack(Items.STICK)));
	}

	@Test
	public void testDifferenceIngredient() {
		Ingredient differenceIngredient = DefaultCustomIngredients.difference(Ingredient.of(Items.APPLE, Items.CARROT), Ingredient.of(Items.STICK, Items.CARROT));

		assertEquals(1, differenceIngredient.items().toList().size());
		assertEquals(Items.APPLE, differenceIngredient.items().toList().getFirst().value());
		assertFalse(differenceIngredient.items().toList().isEmpty());

		assertTrue(differenceIngredient.test(new ItemStack(Items.APPLE)));
		assertFalse(differenceIngredient.test(new ItemStack(Items.CARROT)));
		assertFalse(differenceIngredient.test(new ItemStack(Items.STICK)));
	}

	@Test
	public void testComponentIngredient() {
		final Ingredient baseIngredient = Ingredient.of(Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE, Items.STICK);
		final Ingredient undamagedIngredient = DefaultCustomIngredients.components(
				baseIngredient,
				builder -> builder.set(DataComponents.DAMAGE, 0)
		);
		final Ingredient noNameUndamagedIngredient = DefaultCustomIngredients.components(
				baseIngredient,
				builder -> builder
						.set(DataComponents.DAMAGE, 0)
						.remove(DataComponents.CUSTOM_NAME)
		);

		ItemStack renamedUndamagedDiamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
		renamedUndamagedDiamondPickaxe.set(DataComponents.CUSTOM_NAME, Component.literal("Renamed"));
		assertTrue(undamagedIngredient.test(renamedUndamagedDiamondPickaxe));
		assertFalse(noNameUndamagedIngredient.test(renamedUndamagedDiamondPickaxe));

		assertEquals(3, undamagedIngredient.items().toList().size());
		ItemStack result0 = undamagedIngredient.items().toList().getFirst().value().getDefaultInstance();
		ItemStack result1 = undamagedIngredient.items().toList().get(1).value().getDefaultInstance();

		assertEquals(Items.DIAMOND_PICKAXE, result0.getItem());
		assertEquals(Items.NETHERITE_PICKAXE, result1.getItem());
		assertEquals(DataComponentPatch.EMPTY, result0.getComponentsPatch());
		assertEquals(DataComponentPatch.EMPTY, result1.getComponentsPatch());
		assertFalse(undamagedIngredient.items().toList().isEmpty());

		// Undamaged is fine
		assertTrue(undamagedIngredient.test(new ItemStack(Items.DIAMOND_PICKAXE)));
		assertTrue(undamagedIngredient.test(new ItemStack(Items.NETHERITE_PICKAXE)));

		// Damaged is not fine
		ItemStack damagedDiamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
		damagedDiamondPickaxe.setDamageValue(10);
		assertFalse(undamagedIngredient.test(damagedDiamondPickaxe));

		// Checking for DAMAGE component requires the item is damageable in the first place
		assertFalse(undamagedIngredient.test(new ItemStack(Items.STICK)));

		// Custom data is strictly matched, like any other component with multiple fields
		final CompoundTag requiredData = new CompoundTag();
		requiredData.putInt("keyA", 1);
		final CompoundTag extraData = requiredData.copy();
		extraData.putInt("keyB", 2);

		final Ingredient customDataIngredient = DefaultCustomIngredients.components(
				baseIngredient,
				builder -> builder.set(DataComponents.CUSTOM_DATA, CustomData.of(requiredData))
		);
		ItemStack requiredDataStack = new ItemStack(Items.DIAMOND_PICKAXE);
		requiredDataStack.set(DataComponents.CUSTOM_DATA, CustomData.of(requiredData));
		ItemStack extraDataStack = new ItemStack(Items.DIAMOND_PICKAXE);
		extraDataStack.set(DataComponents.CUSTOM_DATA, CustomData.of(extraData));
		assertTrue(customDataIngredient.test(requiredDataStack));
		assertFalse(customDataIngredient.test(extraDataStack));

		// Default value is ignored in components(ItemStack)
		final Ingredient damagedPickaxeIngredient = DefaultCustomIngredients.components(renamedUndamagedDiamondPickaxe);
		ItemStack renamedDamagedDiamondPickaxe = renamedUndamagedDiamondPickaxe.copy();
		renamedDamagedDiamondPickaxe.setDamageValue(10);
		assertTrue(damagedPickaxeIngredient.test(renamedUndamagedDiamondPickaxe));
		assertTrue(damagedPickaxeIngredient.test(renamedDamagedDiamondPickaxe));
	}

	@Test
	public void testCustomDataIngredient() {
		final CompoundTag requiredNbt = Util.make(new CompoundTag(), nbt -> {
			nbt.putInt("keyA", 1);
		});
		final CompoundTag acceptedNbt = Util.make(requiredNbt.copy(), nbt -> {
			nbt.putInt("keyB", 2);
		});
		final CompoundTag rejectedNbt1 = Util.make(new CompoundTag(), nbt -> {
			nbt.putInt("keyA", -1);
		});
		final CompoundTag rejectedNbt2 = Util.make(new CompoundTag(), nbt -> {
			nbt.putInt("keyB", 2);
		});

		final Ingredient baseIngredient = Ingredient.of(Items.STICK);
		final Ingredient customDataIngredient = DefaultCustomIngredients.customData(baseIngredient, requiredNbt);

		ItemStack stack = new ItemStack(Items.STICK);
		assertFalse(customDataIngredient.test(stack));
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(requiredNbt));
		assertTrue(customDataIngredient.test(stack));
		// This is a non-strict matching
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(acceptedNbt));
		assertTrue(customDataIngredient.test(stack));
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(rejectedNbt1));
		assertFalse(customDataIngredient.test(stack));
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(rejectedNbt2));
		assertFalse(customDataIngredient.test(stack));

		List<Holder<Item>> matchingItems = customDataIngredient.items().toList();
		assertEquals(1, matchingItems.size());
		assertEquals(Items.STICK, matchingItems.getFirst().value());
		// Test disabled as the vanilla API no longer exposes the stack with data.
		// assertEquals(NbtComponent.of(requiredNbt), matchingItems.getFirst().value().getDefaultStack().get(DataComponentTypes.CUSTOM_DATA));
	}
}
