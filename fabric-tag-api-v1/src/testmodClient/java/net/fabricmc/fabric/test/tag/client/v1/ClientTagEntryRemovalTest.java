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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.test.tag.TagTestUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class ClientTagEntryRemovalTest implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTagEntryRemovalTest.class);

	private static final TagKey<Block> REMOVAL_TEST_TAG = TagTestUtils.tagKey(Registries.BLOCK, "dirt_and_mud_with_client_exclusions");

	@Override
	public void onInitializeClient() {
		final ModContainer container = FabricLoader.getInstance().getModContainer(ClientTagTest.MOD_ID).get();

		if (!ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(ClientTagTest.MOD_ID, "removal_test"),
				container, PackActivationType.ALWAYS_ENABLED)) {
			throw new IllegalStateException("Could not register 'removal_test' built-in resource pack.");
		}

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			ClientTagTestUtils.assertThrows(
					() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", REMOVAL_TEST_TAG, Blocks.DIRT),
					"Did not expect to find %s in %s, but it was found!"
							.formatted(Blocks.DIRT.builtInRegistryHolder().key().identifier(), REMOVAL_TEST_TAG.location())
			);

			ClientTagTestUtils.assertThrows(
					() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", REMOVAL_TEST_TAG, Blocks.MUD),
					"Did not expect to find %s in %s, but it was found!"
							.formatted(Blocks.MUD.builtInRegistryHolder().key().identifier(), REMOVAL_TEST_TAG.location())
			);

			ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", REMOVAL_TEST_TAG, Blocks.ROOTED_DIRT);

			ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", REMOVAL_TEST_TAG, Blocks.MUDDY_MANGROVE_ROOTS);

			ClientTagTestUtils.assertThrows(
					() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", BlockTags.SWORD_EFFICIENT, Blocks.COCOA),
					"Did not expect to find %s in %s, but it was found!"
							.formatted(Blocks.COCOA.builtInRegistryHolder().key().identifier(), BlockTags.SWORD_EFFICIENT.location())
			);

			// Success!
			LOGGER.info("The tests for client tag entry removals passed!");
		});
	}
}
