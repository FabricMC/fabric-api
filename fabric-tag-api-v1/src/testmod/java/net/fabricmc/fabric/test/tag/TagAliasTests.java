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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public final class TagAliasTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(TagAliasTests.class);

	// Test 1: Alias two non-empty tags
	public static final TagKey<Item> GEMS = tagKey(Registries.ITEM, "gems");
	public static final TagKey<Item> EXPENSIVE_ROCKS = tagKey(Registries.ITEM, "expensive_rocks");

	// Test 2: Alias a non-empty tag and an empty tag
	public static final TagKey<Item> REDSTONE_DUSTS = tagKey(Registries.ITEM, "redstone_dusts");
	public static final TagKey<Item> REDSTONE_POWDERS = tagKey(Registries.ITEM, "redstone_powders");

	// Test 3: Alias a non-empty tag and a missing tag
	public static final TagKey<Item> BEETROOTS = tagKey(Registries.ITEM, "beetroots");
	public static final TagKey<Item> MISSING_BEETROOTS = tagKey(Registries.ITEM, "missing_beetroots");

	// Test 4: Given tags A, B, C, make alias groups A+B and B+C. They should get merged.
	public static final TagKey<Block> BRICK_BLOCKS = tagKey(Registries.BLOCK, "brick_blocks");
	public static final TagKey<Block> MORE_BRICK_BLOCKS = tagKey(Registries.BLOCK, "more_brick_blocks");
	public static final TagKey<Block> BRICKS = tagKey(Registries.BLOCK, "bricks");

	// Test 5: Merge tags from a world generation dynamic registry
	public static final TagKey<Biome> CLASSIC_BIOMES = tagKey(Registries.BIOME, "classic");
	public static final TagKey<Biome> TRADITIONAL_BIOMES = tagKey(Registries.BIOME, "traditional");

	// Test 6: Merge tags from a reloadable registry
	public static final TagKey<LootTable> NETHER_BRICKS_1 = tagKey(Registries.LOOT_TABLE, "nether_bricks_1");
	public static final TagKey<LootTable> NETHER_BRICKS_2 = tagKey(Registries.LOOT_TABLE, "nether_bricks_2");

	private static <T> TagKey<T> tagKey(ResourceKey<? extends Registry<T>> registryRef, String name) {
		return TagKey.create(registryRef, Identifier.fromNamespaceAndPath("fabric-tag-api-v1-testmod", name));
	}

	@GameTest
	public void nonEmptyTagAlias(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		TagTestUtils.assertTagContent(helper, LOGGER, "Tags {} / {} were successfully aliased together", registries, List.of(GEMS, EXPENSIVE_ROCKS), TagTestUtils::getItemKey,
				Items.DIAMOND, Items.EMERALD);
		helper.succeed();
	}

	@GameTest
	public void nonEmptyAndEmptyTagAlias(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		TagTestUtils.assertTagContent(helper, LOGGER, "Tags {} / {} were successfully aliased together", registries, List.of(REDSTONE_DUSTS, REDSTONE_POWDERS), TagTestUtils::getItemKey,
				Items.REDSTONE);
		helper.succeed();
	}

	@GameTest
	public void nonEmptyAndMissingTagAlias(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		TagTestUtils.assertTagContent(helper, LOGGER, "Tags {} / {} were successfully aliased together", registries, List.of(BEETROOTS, MISSING_BEETROOTS), TagTestUtils::getItemKey,
				Items.BEETROOT);
		helper.succeed();
	}

	@GameTest
	public void abcTagAlias(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		TagTestUtils.assertTagContent(helper, LOGGER, "Tags {} / {} were successfully aliased together", registries, List.of(BRICK_BLOCKS, MORE_BRICK_BLOCKS, BRICKS), TagTestUtils::getBlockKey,
				Blocks.BRICKS, Blocks.STONE_BRICKS, Blocks.NETHER_BRICKS, Blocks.RED_NETHER_BRICKS);
		helper.succeed();
	}

	@GameTest
	public void worldGenDynamicRegistryTagAlias(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		TagTestUtils.assertTagContent(helper, LOGGER, "Tags {} / {} were successfully aliased together", registries, List.of(CLASSIC_BIOMES, TRADITIONAL_BIOMES),
				Biomes.PLAINS, Biomes.DESERT);
		helper.succeed();
	}

	@GameTest
	public void reloadableRegistryTagAlias(GameTestHelper helper) {
		HolderLookup.Provider registries = helper.getLevel().getServer().reloadableRegistries().lookup();
		TagTestUtils.assertTagContent(helper, LOGGER, "Tags {} / {} were successfully aliased together", registries, List.of(NETHER_BRICKS_1, NETHER_BRICKS_2),
				Blocks.NETHER_BRICKS.getLootTable().orElseThrow(),
				Blocks.RED_NETHER_BRICKS.getLootTable().orElseThrow());
		helper.succeed();
	}
}
