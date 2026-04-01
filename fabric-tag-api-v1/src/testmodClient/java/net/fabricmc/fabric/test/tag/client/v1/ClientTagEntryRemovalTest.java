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
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.fabricmc.fabric.test.tag.TagTestUtils;

public class ClientTagEntryRemovalTest implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTagEntryRemovalTest.class);

	private static final TagKey<Block> REMOVAL_TEST_TAG = TagTestUtils.tagKey(Registries.BLOCK, "dirt_and_mud_with_client_exclusions");

	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			if (ClientTags.isInWithLocalFallback(REMOVAL_TEST_TAG, Blocks.DIRT)) {
				throw new AssertionError("Expected not to find dirt in fabric-tag-api-v1-testmod:dirt_and_mud_with_client_exclusions, but it was found!");
			}

			if (ClientTags.isInWithLocalFallback(REMOVAL_TEST_TAG, Blocks.MUD)) {
				throw new AssertionError("Expected not to find mud in fabric-tag-api-v1-testmod:dirt_and_mud_with_client_exclusions, but it was found!");
			}

			if (!ClientTags.isInWithLocalFallback(REMOVAL_TEST_TAG, Blocks.ROOTED_DIRT)) {
				throw new AssertionError("Expected to find rooted_dirt in fabric-tag-api-v1-testmod:dirt_and_mud_with_client_exclusions, but it was not found!");
			}

			if (!ClientTags.isInWithLocalFallback(REMOVAL_TEST_TAG, Blocks.MUDDY_MANGROVE_ROOTS)) {
				throw new AssertionError("Expected to find muddy_mangrove_roots in fabric-tag-api-v1-testmod:dirt_and_mud_with_client_exclusions, but it was not found!");
			}

			// Success!
			LOGGER.info("The tests for client tag entry removals passed!");
		});
	}
}
