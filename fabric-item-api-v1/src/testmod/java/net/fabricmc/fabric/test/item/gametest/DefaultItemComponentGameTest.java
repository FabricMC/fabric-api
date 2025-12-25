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

import java.util.function.Consumer;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class DefaultItemComponentGameTest {
	@GameTest
	public void modify(GameTestHelper helper) {
		Consumer<Component> checkComponent = component -> {
			if (component == null) {
				throw helper.assertionException("Item name component not found on gold ingot");
			}

			if (!"Fool's Gold".equals(component.getString())) {
				throw helper.assertionException("Item name component on gold ingot is not set");
			}
		};

		Component component = Items.GOLD_INGOT.components().get(DataComponents.ITEM_NAME);
		checkComponent.accept(component);

		component = new ItemStack(Items.GOLD_INGOT).getComponents().get(DataComponents.ITEM_NAME);
		checkComponent.accept(component);

		boolean isBeefFood = Items.BEEF.components().has(DataComponents.FOOD);

		if (isBeefFood) {
			throw helper.assertionException("Food component not removed from beef");
		}

		helper.succeed();
	}

	@GameTest
	public void afterModify(GameTestHelper helper) {
		Fireworks fireworksComponent = Items.GOLD_NUGGET.components().get(DataComponents.FIREWORKS);

		if (fireworksComponent == null) {
			throw helper.assertionException("Fireworks component not found on gold nugget");
		}

		Boolean enchantGlint = Items.GOLD_NUGGET.components().get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

		if (enchantGlint != Boolean.TRUE) {
			throw helper.assertionException("Enchantment glint override not set on gold nugget");
		}

		helper.succeed();
	}

	@GameTest
	public void diamondPickaxeIsRenamed(GameTestHelper helper) {
		Item testItem = Items.DIAMOND_PICKAXE;
		ItemStack stack = testItem.getDefaultInstance();

		String itemName = stack.getHoverName().getString();
		String expectedName = "Modified Diamond Pickaxe";

		String errorMessage = "Expected '%s' to be contained in '%s', but it was not!";

		// if they contain each other, then they are equal
		helper.assertTrue(itemName.contains(expectedName), Component.literal(errorMessage.formatted(expectedName, itemName)));
		helper.assertTrue(expectedName.contains(itemName), Component.literal(errorMessage.formatted(itemName, expectedName)));
		helper.succeed();
	}

	@GameTest
	public void emptyComponentMapDoesNotContainUnbreakable(GameTestHelper helper) {
		DataComponentMap.Builder builder = DataComponentMap.builder();

		helper.assertFalse(builder.contains(DataComponents.UNBREAKABLE), Component.literal("Empty component map contains unbreakable type"));
		helper.succeed();
	}

	@GameTest
	public void componentMapWithItemNameDoesNotContainUnbreakable(GameTestHelper helper) {
		DataComponentMap.Builder builder = DataComponentMap.builder();

		builder.set(DataComponents.ITEM_NAME, Component.nullToEmpty("Weird Name"));

		helper.assertFalse(builder.contains(DataComponents.UNBREAKABLE), Component.literal("Component map should not contain unbreakable type"));
		helper.succeed();
	}

	@GameTest
	public void componentMapWithUnbreakableContainsUnbreakable(GameTestHelper helper) {
		DataComponentMap.Builder builder = DataComponentMap.builder();

		builder.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);

		helper.assertTrue(builder.contains(DataComponents.UNBREAKABLE), Component.literal("Component map does not contain unbreakable type"));
		helper.succeed();
	}
}
