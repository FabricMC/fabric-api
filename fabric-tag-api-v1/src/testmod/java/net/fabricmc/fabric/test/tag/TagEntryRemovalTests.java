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

package net.fabricmc.fabric.test.tag;

import static net.fabricmc.fabric.test.tag.TagTestUtils.tagKey;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public final class TagEntryRemovalTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(TagEntryRemovalTests.class);

	private static final TagKey<Enchantment> TEST_ENCHANTMENT_TAG = tagKey(RegistryKeys.ENCHANTMENT, "all_enchantments_without_durability_enchantments");
	private static final TagKey<Item> TEST_ITEM_TAG = tagKey(RegistryKeys.ITEM, "snowballs_without_bricks");

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void snowballsWithoutBricksOnlyContainsSnowballs(TestContext context) {
		DynamicRegistryManager registries = context.getWorld().getRegistryManager();
		TagTestUtils.assertTagContent(
				context,
				LOGGER,
				"Tag {} / {} contains expected entries",
				registries,
				List.of(TEST_ITEM_TAG),
				TagTestUtils::getItemKey,
				Items.SNOWBALL
		);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void snowballsWithoutBricksDoesNotContainBricks(TestContext context) {
		DynamicRegistryManager registries = context.getWorld().getRegistryManager();
		TagTestUtils.assertThrows(
				() -> TagTestUtils.assertInTag(
						context,
						LOGGER,
						"",
						registries,
						List.of(TEST_ITEM_TAG),
						TagTestUtils::getItemKey,
						Items.BRICK
				),
				"Expected %s not to contain bricks".formatted(TEST_ITEM_TAG)
		);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void snowballsWithoutBricksDoesNotContainNetherBrick(TestContext context) {
		DynamicRegistryManager registries = context.getWorld().getRegistryManager();
		TagTestUtils.assertThrows(
				() -> TagTestUtils.assertInTag(
						context,
						LOGGER,
						"",
						registries,
						List.of(TEST_ITEM_TAG),
						TagTestUtils::getItemKey,
						Items.NETHER_BRICK
				),
				"Expected %s not to contain nether bricks".formatted(TEST_ITEM_TAG)
		);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void allEnchantmentTagsWithoutDurabilityEnchantmentsDoesNotContainUnbreakingOrMending(TestContext context) {
		DynamicRegistryManager registries = context.getWorld().getRegistryManager();
		TagTestUtils.assertThrows(
				() -> TagTestUtils.assertInTag(
						context,
						LOGGER,
						"",
						registries,
						List.of(TEST_ENCHANTMENT_TAG),
						Enchantments.UNBREAKING,
						Enchantments.MENDING
				),
				"Expected %s not to contain Unbreaking or Mending".formatted(TEST_ENCHANTMENT_TAG)
		);
		context.complete();
	}
}
