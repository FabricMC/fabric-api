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
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
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
import net.fabricmc.fabric.test.tag.TagTestUtils;

public class ClientTagGameTests implements FabricClientGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTagGameTests.class);

	private static final TagKey<Block> REMOVAL_TEST_TAG = TagTestUtils.tagKey(Registries.BLOCK, "dirt_and_mud_with_client_exclusions");

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
				context.runOnClient(ClientTagGameTests::reAddRemovedClientValue);
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
		LOGGER.info("The tests for singleplayer client tag tests passed!");
	}

	private static void clientTagDedicatedServerTests(Minecraft client) {
		// minecraft:sword_efficient should NOT exist on dirt the serverContext (can be confirmed with F3 on a dirt block),
		// but the this test should pass as minecraft:sword_efficient will contain dirt on the serverContext
		ClientTagTestUtils.assertThrows(
				() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", BlockTags.SWORD_EFFICIENT, TagTestUtils::getBlockKey, Blocks.DIRT),
				"Did not expect to find %s in %s, but it was found!"
						.formatted(Blocks.DIRT.builtInRegistryHolder().key().identifier(), BlockTags.SWORD_EFFICIENT.location())
		);
		ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "Client tag {} contains the expected entries {}", BlockTags.SWORD_EFFICIENT, TagTestUtils::getBlockKey, Blocks.COCOA);

		// Success!
		LOGGER.info("The tests for dedicated client tag tests passed!");
	}

	/**
	 * @see net.fabricmc.fabric.test.tag.TagEntryRemovalTests#reAddRemovedValue(GameTestHelper)
	 */
	private static void reAddRemovedClientValue(Minecraft client) {
		removeThenTestMelonInSwordEfficient(client);
		addThenTestMelonInSwordEfficient(client);
		removeThenTestMelonInSwordEfficient(client);
	}

	private static void removeThenTestMelonInSwordEfficient(Minecraft client) {
		client.getResourcePackRepository().removePack(ClientTagTest.ADD_BACK_MELON_PACK_ID.toString());
		client.reloadResourcePacks();

		ClientTagTestUtils.assertThrows(
				() -> ClientTagTestUtils.assertInWithLocalFallback(
						LOGGER,
						"",
						BlockTags.SWORD_EFFICIENT,
						TagTestUtils::getBlockKey,
						Blocks.MELON
				),
				"Did not expect to find %s in %s, but it was found!"
						.formatted(Blocks.COCOA.builtInRegistryHolder().key().identifier(), BlockTags.SWORD_EFFICIENT.location())
		);
	}

	private static void addThenTestMelonInSwordEfficient(Minecraft client) {
		client.getResourcePackRepository().addPack(ClientTagTest.ADD_BACK_MELON_PACK_ID.toString());
		client.reloadResourcePacks();

		ClientTagTestUtils.assertInWithLocalFallback(
				LOGGER,
				"",
				BlockTags.SWORD_EFFICIENT,
				TagTestUtils::getBlockKey,
				Blocks.MELON
		);
	}
}
