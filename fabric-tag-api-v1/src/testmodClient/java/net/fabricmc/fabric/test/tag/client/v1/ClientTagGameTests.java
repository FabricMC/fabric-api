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

package net.fabricmc.fabric.test.tag.client.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEnchantmentTags;
import net.fabricmc.fabric.test.tag.TagTest;
import net.fabricmc.fabric.test.tag.TagTestUtils;

public class ClientTagGameTests implements FabricClientGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTagGameTests.class);

	private static final TagKey<Block> REMOVAL_TEST_TAG = TagTestUtils.tagKey(Registries.BLOCK, "dirt_and_mud_with_client_exclusions");
	private static final TagKey<Block> READD_MELONS_TEST_TAG = TagTestUtils.tagKey(Registries.BLOCK, "readd_melons");
	private static final TagKey<Item> HAPPY_GHAST_FOOD_TAG = ItemTags.HAPPY_GHAST_FOOD;

	@Override
	public void runTest(ClientGameTestContext context) {
		context.runOnClient(ClientTagGameTests::clientTagTests);
		context.runOnClient(ClientTagGameTests::clientTagRemovalTests);

		try (
				TestSingleplayerContext singleplayerContext = context.worldBuilder()
						.create()
		) {
			context.runOnClient(ClientTagGameTests::clientTagSingleplayerTests);
		}

		try (
				TestDedicatedServerContext serverContext = context.worldBuilder()
						.createServer()
		) {
			serverContext.runCommand("datapack disable \"fabric-tag-api-v1-testmod:test\"");

			try (TestServerConnection connection = serverContext.connect()) {
				context.runOnClient(ClientTagGameTests::clientTagDedicatedServerTests);
				serverContext.runOnServer(ClientTagGameTests::reloadAndAddServerTagTests);
			}
		}

		try (TestDedicatedServerContext serverContext = context.worldBuilder().createServer()) {
			try (TestServerConnection connection = serverContext.connect()) {
				serverContext.runOnServer(ClientTagGameTests::reAddRemovedValueTests);
			}
		}
	}

	private static void clientTagTests(Minecraft client) {
		if (ClientTags.getOrCreateLocalTag(ConventionalEnchantmentTags.INCREASE_BLOCK_DROPS) == null) {
			throw new AssertionError("Expected to load c:increase_block_drops, but it was not found!");
		}

		ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "Client tag {} contains the expected entries {}", ConventionalBlockTags.ORES, TagTestUtils::getBlockKey, Blocks.DIAMOND_ORE);

		ClientTagTestUtils.assertThrows(
				() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", ConventionalBlockTags.ORES, TagTestUtils::getBlockKey, Blocks.DIAMOND_BLOCK),
				"Did not expect to find %s in %s, but it was found!"
						.formatted(Blocks.DIAMOND_BLOCK.builtInRegistryHolder().key().identifier(), ConventionalBlockTags.ORES.location())
		);

		ClientTagTestUtils.assertInLocal(LOGGER, "Client tag {} contains the expected entries {}", ConventionalBiomeTags.IS_FOREST, Biomes.FOREST);

		// Success!
		LOGGER.info("The tests for client tags passed!");
	}

	private static void clientTagRemovalTests(Minecraft client) {
		ClientTagTestUtils.assertThrows(
				() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", REMOVAL_TEST_TAG, TagTestUtils::getBlockKey, Blocks.DIRT),
				"Did not expect to find %s in %s, but it was found!"
						.formatted(Blocks.DIRT.builtInRegistryHolder().key().identifier(), REMOVAL_TEST_TAG.location())
		);

		ClientTagTestUtils.assertThrows(
				() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", REMOVAL_TEST_TAG, TagTestUtils::getBlockKey, Blocks.MUD),
				"Did not expect to find %s in %s, but it was found!"
						.formatted(Blocks.MUD.builtInRegistryHolder().key().identifier(), REMOVAL_TEST_TAG.location())
		);

		ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", REMOVAL_TEST_TAG, TagTestUtils::getBlockKey, Blocks.ROOTED_DIRT);

		ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", REMOVAL_TEST_TAG, TagTestUtils::getBlockKey, Blocks.MUDDY_MANGROVE_ROOTS);

		// Success!
		LOGGER.info("The tests for client tag entry removals passed!");
	}

	private static void clientTagSingleplayerTests(Minecraft client) {
		ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "Client tag {} contains the expected entries {}", BlockTags.SWORD_EFFICIENT, TagTestUtils::getBlockKey, Blocks.DIRT);
		ClientTagTestUtils.assertThrows(
				() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", BlockTags.SWORD_EFFICIENT, TagTestUtils::getBlockKey, Blocks.COCOA),
				"Did not expect to find %s in %s, but it was found!"
						.formatted(Blocks.COCOA.builtInRegistryHolder().key().identifier(), BlockTags.SWORD_EFFICIENT.location())
		);

		// Success!
		LOGGER.info("The tests for singleplayer client tags passed!");
	}

	private static void clientTagDedicatedServerTests(Minecraft client) {
		// minecraft:sword_efficient should NOT exist on dirt the client context (can be confirmed with F3 on a dirt block),
		// but the this test should pass as minecraft:sword_efficient will contain dirt on the server context
		ClientTagTestUtils.assertThrows(
				() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", BlockTags.SWORD_EFFICIENT, TagTestUtils::getBlockKey, Blocks.DIRT),
				"Did not expect to find %s in %s, but it was found!"
						.formatted(Blocks.DIRT.builtInRegistryHolder().key().identifier(), BlockTags.SWORD_EFFICIENT.location())
		);
		ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "Client tag {} contains the expected entries {}", BlockTags.SWORD_EFFICIENT, TagTestUtils::getBlockKey, Blocks.COCOA);

		// Success!
		LOGGER.info("The tests for dedicated client tags passed!");
	}

	private static void reloadAndAddServerTagTests(MinecraftServer server) {
		// fabric-tag-api-v1-testmod:add_back_melon is assumed to not exist on the server whilst the pack is removed for this test.
		// Client tags are only read from the root data directory in the JAR, so you are unable to modify their values using built-in packs.
		removeThenTestMelonInReAddMelonsTestTag(server);
		addThenTestMelonInReAddMelonsTestTag(server);
		removeThenTestMelonInReAddMelonsTestTag(server);

		// Success!
		LOGGER.info("The tests for adding tags to the server passed!");
	}

	private static void reAddRemovedValueTests(MinecraftServer server) {
		// Run this hook to make sure that failed runs with the 'remove_and_add_test' data pack do not error due to having it enabled.
		removeThenTestSnowballInHappyGhastFood(server);
		addThenTestSnowballInHappyGhastFood(server);
		// Remove it again to make sure that we have a default state for other tests.
		removeThenTestSnowballInHappyGhastFood(server);

		LOGGER.info("The tests for re-adding removed tag values passed!");
	}

	private static void removeThenTestMelonInReAddMelonsTestTag(MinecraftServer server) {
		server.getPackRepository().removePack(ClientTagTest.ADD_BACK_MELON_PACK_ID.toString());
		ClientTagTestUtils.reloadResources(
				server,
				() -> new AssertionError("Failed to reload after removing '%s' data pack".formatted(ClientTagTest.ADD_BACK_MELON_PACK_ID))
		);

		ClientTagTestUtils.assertThrows(
				() -> ClientTagTestUtils.assertInWithLocalFallback(
						LOGGER,
						"",
						READD_MELONS_TEST_TAG,
						TagTestUtils::getBlockKey,
						Blocks.MELON
				),
				"Did not expect to find %s in %s, but it was found!"
						.formatted(Blocks.MELON.builtInRegistryHolder().key().identifier(), BlockTags.SWORD_EFFICIENT.location())
		);
	}

	private static void addThenTestMelonInReAddMelonsTestTag(MinecraftServer server) {
		server.getPackRepository().addPack(ClientTagTest.ADD_BACK_MELON_PACK_ID.toString());
		ClientTagTestUtils.reloadResources(
				server,
				() -> new AssertionError("Failed to reload after adding '%s' data pack".formatted(ClientTagTest.ADD_BACK_MELON_PACK_ID))
		);

		ClientTagTestUtils.assertInWithLocalFallback(
				LOGGER,
				"",
				READD_MELONS_TEST_TAG,
				TagTestUtils::getBlockKey,
				Blocks.MELON
		);
	}

	private static void removeThenTestSnowballInHappyGhastFood(MinecraftServer server) {
		server.getPackRepository().removePack(TagTest.REMOVE_AND_ADD_TEST_PACK_ID.toString());
		ClientTagTestUtils.reloadResources(
				server,
				() -> new AssertionError("Failed to reload after removing '%s' data pack".formatted(TagTest.REMOVE_AND_ADD_TEST_PACK_ID))
		);

		RegistryAccess registries = server.registryAccess();
		ClientTagTestUtils.assertThrows(
				() -> assertSnowballInHappyGhastFood(registries),
				"Expected %s not to contain snowball after removing pack".formatted(HAPPY_GHAST_FOOD_TAG)
		);
	}

	private static void addThenTestSnowballInHappyGhastFood(MinecraftServer server) {
		server.getPackRepository().addPack(TagTest.REMOVE_AND_ADD_TEST_PACK_ID.toString());
		ClientTagTestUtils.reloadResources(
				server,
				() -> new AssertionError("Failed to reload after adding '%s' data pack".formatted(TagTest.REMOVE_AND_ADD_TEST_PACK_ID))
		);

		assertSnowballInHappyGhastFood(server.registryAccess());
		LOGGER.info("Tag {} contains snowball after adding pack", HAPPY_GHAST_FOOD_TAG);
	}

	private static void assertSnowballInHappyGhastFood(RegistryAccess registries) {
		Registry<Item> lookup = registries.lookupOrThrow(Registries.ITEM);
		HolderSet.Named<Item> holderSet = lookup.getOrThrow(HAPPY_GHAST_FOOD_TAG);
		boolean contains = holderSet.stream().anyMatch(h -> h.value() == Items.SNOWBALL);

		if (!contains) {
			throw new AssertionError("Expected %s to contain snowball".formatted(HAPPY_GHAST_FOOD_TAG));
		}
	}
}
