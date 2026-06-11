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

package net.fabricmc.fabric.test.holder.component;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.pig.PigVariants;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class HolderComponentGameTest {
	@GameTest
	public void testComponentChangesFromJSON(GameTestHelper helper) {
		// We test for the addition a pig variant to ensure that dynamic registries are present
		// when holder components are loaded from JSON
		Holder<PigVariant> pigVariant = Items.WHEAT_SEEDS.get(DataComponents.PIG_VARIANT);
		helper.assertTrue(
				pigVariant != null && pigVariant.is(PigVariants.TEMPERATE),
				"Wheat Seeds must have 'pig/variant' component with value 'temperate'"
		);
		helper.assertFalse(
				Items.MELON_SEEDS.has(DataComponents.CUSTOM_NAME),
				"Wheat Seeds must have its 'custom_name' component removed"
		);

		helper.succeed();
	}

	@GameTest
	public void testComponentChangesFromInitializer(GameTestHelper helper) {
		// We test for the addition a pig variant to ensure that DataComponentInitializers can both
		// modify components and access dynamic registries
		Holder<PigVariant> pigVariant = Items.MELON_SEEDS.get(DataComponents.PIG_VARIANT);
		helper.assertTrue(
				pigVariant != null && pigVariant.is(PigVariants.TEMPERATE),
				"Melon Seeds must have 'pig/variant' component with value 'temperate'"
		);
		helper.assertFalse(
				Items.MELON_SEEDS.has(DataComponents.CUSTOM_NAME),
				"Melon Seeds must have its 'custom_name' component removed"
		);

		helper.succeed();
	}

	@GameTest
	public void testInitializerOrdering(GameTestHelper helper) {
		helper.assertValueEqual(
				Blocks.DIRT.getOrDefault(DataComponents.CAT_COLLAR, DyeColor.WHITE),
				DyeColor.RED,
				"red_cat_collar"
		);
		helper.assertValueEqual(
				Blocks.DIRT.getOrDefault(DataComponents.SHEEP_COLOR, DyeColor.WHITE),
				DyeColor.GREEN,
				"green_sheep_color"
		);
		helper.assertValueEqual(
				Blocks.DIRT.getOrDefault(DataComponents.SHULKER_COLOR, DyeColor.WHITE),
				DyeColor.BLUE,
				"blue_shulker_color"
		);

		helper.succeed();
	}
}
