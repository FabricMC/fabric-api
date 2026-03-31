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

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.minecraft.world.item.enchantment.Enchantment;

import net.minecraft.world.item.enchantment.Enchantments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;

import static net.fabricmc.fabric.test.tag.TagTestUtils.tagKey;

public final class TagEntryRemovalTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TagEntryRemovalTest.class);

	private final TagKey<Enchantment> TEST_ENCHANTMENT_TAG = tagKey(Registries.ENCHANTMENT, "all_enchantments_without_durability_enchantments");
	private final TagKey<Item> TEST_ITEM_TAG = tagKey(Registries.ITEM, "snowballs_but_not_bricks");

	@Override
	public void onInitialize() {
		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
			if (client) {
				return;
			}

			LOGGER.info("Running tag entry removal tests...");
			TagTestUtils.assertTagContent(LOGGER, "Tag {} / {} contains expected entries", registries, List.of(TEST_ITEM_TAG), TagTestUtils::getItemKey, Items.SNOWBALL);
			TagTestUtils.assertThrows(
					() -> TagTestUtils.assertTagContent(LOGGER, "Tag {} / {} contains expected entries", registries, List.of(TEST_ITEM_TAG), TagTestUtils::getItemKey, Items.BRICK),
					"Expected %s not to contain bricks".formatted(TEST_ITEM_TAG)
			);
			TagTestUtils.assertThrows(
					() -> TagTestUtils.assertTagContent(LOGGER, "Tag {} / {} contains expected entries", registries, List.of(TEST_ENCHANTMENT_TAG), Enchantments.UNBREAKING, Enchantments.MENDING),
					"Expected %s not to contain Unbreaking or Mending".formatted(TEST_ENCHANTMENT_TAG)
			);
			LOGGER.info("The tests for tag entry removals passed!");
		});
	}
}
