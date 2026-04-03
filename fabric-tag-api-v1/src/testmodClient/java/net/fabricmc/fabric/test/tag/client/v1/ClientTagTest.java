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

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEnchantmentTags;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class ClientTagTest implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTagTest.class);

	public static final String MOD_ID = "fabric-tag-api-v1-testmod";

	@Override
	public void onInitializeClient() {
		final ModContainer container = FabricLoader.getInstance().getModContainer(MOD_ID).get();

		if (!ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MOD_ID, "test2"),
				container, PackActivationType.ALWAYS_ENABLED)) {
			throw new IllegalStateException("Could not register 'test2' built-in resource pack.");
		}

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			if (ClientTags.getOrCreateLocalTag(ConventionalEnchantmentTags.INCREASE_BLOCK_DROPS) == null) {
				throw new AssertionError("Expected to load c:increase_block_drops, but it was not found!");
			}

			ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "Tag {} contains the expected entries {}", ConventionalBlockTags.ORES, Blocks.DIAMOND_ORE);

			ClientTagTestUtils.assertThrows(
					() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", ConventionalBlockTags.ORES, Blocks.DIAMOND_BLOCK),
					"Did not expect to find %s in %s, but it was found!"
							.formatted(Blocks.DIAMOND_BLOCK.builtInRegistryHolder().key().identifier(), ConventionalBlockTags.ORES.location())
			);

			ClientTagTestUtils.assertInLocal(LOGGER, "Tag {} contains the expected entries {}", ConventionalBiomeTags.IS_FOREST, Biomes.FOREST);

			ClientTagTestUtils.assertThrows(
					() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", BlockTags.SWORD_EFFICIENT, Blocks.DIRT),
					"Did not expect to find %s in %s, but it was found!"
							.formatted(Blocks.DIRT.builtInRegistryHolder().key().identifier(), ConventionalBlockTags.ORES.location())
			);

			// Success!
			LOGGER.info("The tests for client tags passed!");
		});

		if (true) {
			return;
		}

		// This should be tested on a server with the datapack from the builtin resourcepack.
		// That is, minecraft:sword_efficient should NOT exist on dirt the server (can be confirmed with F3 on a dirt block),
		// but the this test should pass as minecraft:sword_efficient will contain dirt on the server
		CommonLifecycleEvents.TAGS_LOADED.register((registryAccess, client) -> {
			ClientTagTestUtils.assertThrows(
					() -> ClientTagTestUtils.assertInWithLocalFallback(LOGGER, "", BlockTags.SWORD_EFFICIENT, Blocks.DIRT),
					"Did not expect to find %s in %s, but it was found!"
							.formatted(Blocks.DIRT.builtInRegistryHolder().key().identifier(), BlockTags.SWORD_EFFICIENT.location())
			);
		});
	}
}
