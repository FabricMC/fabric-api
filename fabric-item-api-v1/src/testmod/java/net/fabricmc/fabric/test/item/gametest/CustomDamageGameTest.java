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

import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.test.item.CustomDamageTest;

public class CustomDamageGameTest {
	@GameTest
	public void breakLastItem(GameTestHelper helper) {
		breakItem(helper, 1);
	}

	@GameTest
	public void breakItemInStack(GameTestHelper helper) {
		breakItem(helper, 2);
	}

	private void breakItem(GameTestHelper helper, int count) {
		ItemStack stack = new ItemStack(CustomDamageTest.BREAKING_ITEM, count);
		stack.setDamageValue(3);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal("Custom break test"));
		ItemStack expectedBrokenStack = stack.copy();

		class TestArmorStand extends ArmorStand {
			private int breakCount;

			TestArmorStand() {
				super(helper.getLevel(), 0, 0, 0);
			}

			@Override
			public void onEquippedItemBroken(ItemStack brokenStack, EquipmentSlot slot) {
				breakCount++;
				helper.assertTrue(brokenStack != stack, Component.literal("Break callback must receive a copy"));
				helper.assertTrue(ItemStack.matches(brokenStack, expectedBrokenStack), Component.literal("Break callback must receive the stack before shrinking, including its components"));
				helper.assertTrue(slot == EquipmentSlot.MAINHAND, Component.literal("Break callback must receive the damaged slot"));
				helper.assertTrue(stack.getCount() == count - 1, Component.literal("Stack must shrink before the break callback"));
				super.onEquippedItemBroken(brokenStack, slot);
			}
		}

		TestArmorStand owner = new TestArmorStand();
		stack.hurtAndBreak(1, owner, EquipmentSlot.MAINHAND);

		helper.assertTrue(owner.breakCount == 1, Component.literal("Break callback must run exactly once"));
		helper.assertTrue(stack.getCount() == count - 1, Component.literal("Custom damage handler must break exactly one item"));

		if (!stack.isEmpty()) {
			helper.assertTrue(stack.getDamageValue() == 3, Component.literal("Custom damage handler return value must be ignored after breaking"));
		}

		helper.succeed();
	}
}
