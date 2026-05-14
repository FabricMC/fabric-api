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

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public final class TagEntryRemovalTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(TagEntryRemovalTests.class);

	private static final TagKey<Enchantment> TEST_ENCHANTMENT_TAG = tagKey(Registries.ENCHANTMENT, "all_enchantments_without_durability_enchantments");
	private static final TagKey<Item> TEST_ITEM_TAG = tagKey(Registries.ITEM, "snowballs_without_bricks");

	@GameTest
	public void snowballsWithoutBricksOnlyContainsSnowballs(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		TagTestUtils.assertTagContent(
				helper,
				LOGGER,
				"Tag {} / {} contains expected entries",
				registries,
				List.of(TEST_ITEM_TAG),
				TagTestUtils::getItemKey,
				Items.SNOWBALL
		);
		helper.succeed();
	}

	@GameTest
	public void snowballsWithoutBricksDoesNotContainBricks(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		TagTestUtils.assertThrows(
				helper,
				() -> TagTestUtils.assertInTag(
						helper,
						LOGGER,
						"",
						registries,
						List.of(TEST_ITEM_TAG),
						TagTestUtils::getItemKey,
						Items.BRICK
				),
				"Expected %s not to contain bricks".formatted(TEST_ITEM_TAG)
		);
		helper.succeed();
	}

	@GameTest
	public void allEnchantmentTagsWithoutDurabilityEnchantmentsDoesNotContainUnbreakingOrMending(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		TagTestUtils.assertThrows(
				helper,
				() -> TagTestUtils.assertInTag(
						helper,
						LOGGER,
						"",
						registries,
						List.of(TEST_ENCHANTMENT_TAG),
						Enchantments.UNBREAKING,
						Enchantments.MENDING
				),
				"Expected %s not to contain Unbreaking or Mending".formatted(TEST_ENCHANTMENT_TAG)
		);
		helper.succeed();
	}

	@GameTest
	public void reAddRemovedValue(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();

		// Run this hook to make sure that failed runs with the 'remove_and_add_test' data pack do not error due to having it enabled.
		removeThenTestSnowballInHappyGhastFood(helper, server);
		addThenTestSnowballInHappyGhastFood(helper, server);
		// Remove it again to make sure that we have a default state for other tests.
		removeThenTestSnowballInHappyGhastFood(helper, server);

		helper.succeed();
	}

	private static void removeThenTestSnowballInHappyGhastFood(GameTestHelper helper, MinecraftServer server) {
		PackRepository repository = server.getPackRepository();

		repository.removePack(TagTest.REMOVE_AND_ADD_TEST_PACK_ID.toString());
		TagTestUtils.reloadResources(
				helper,
				server,
				h -> h.assertionException("Failed to reload after removing '%s' data pack", TagTest.REMOVE_AND_ADD_TEST_PACK_ID)
		);

		TagTestUtils.assertThrows(
				helper,
				() -> TagTestUtils.assertTagContent(
						helper,
						LOGGER,
						"",
						server.registryAccess(),
						List.of(ItemTags.HAPPY_GHAST_FOOD),
						TagTestUtils::getItemKey,
						Items.SNOWBALL
				),
				"Expected %s not to contain Snowball".formatted(TEST_ENCHANTMENT_TAG)
		);
	}

	private static void addThenTestSnowballInHappyGhastFood(GameTestHelper helper, MinecraftServer server) {
		PackRepository repository = server.getPackRepository();

		repository.addPack(TagTest.REMOVE_AND_ADD_TEST_PACK_ID.toString());
		TagTestUtils.reloadResources(
				helper,
				server,
				h -> h.assertionException("Failed to reload after adding '%s' data pack", TagTest.REMOVE_AND_ADD_TEST_PACK_ID)
		);

		TagTestUtils.assertTagContent(
				helper,
				LOGGER,
				"Tag {} / {} contains expected entries",
				server.registryAccess(),
				List.of(ItemTags.HAPPY_GHAST_FOOD),
				TagTestUtils::getItemKey,
				Items.SNOWBALL
		);
	}
}
