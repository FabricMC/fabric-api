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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;

public final class TagAliasTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TagAliasTest.class);

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

	@Override
	public void onInitialize() {
		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
			LOGGER.info("Running tag alias tests on the {}...", client ? "client" : "server");

			assertTagContent(registries, List.of(GEMS, EXPENSIVE_ROCKS), TagAliasTest::getItemKey,
					Items.DIAMOND, Items.EMERALD);
			assertTagContent(registries, List.of(REDSTONE_DUSTS, REDSTONE_POWDERS), TagAliasTest::getItemKey,
					Items.REDSTONE);
			assertTagContent(registries, List.of(BEETROOTS, MISSING_BEETROOTS), TagAliasTest::getItemKey,
					Items.BEETROOT);
			assertTagContent(registries, List.of(BRICK_BLOCKS, MORE_BRICK_BLOCKS, BRICKS), TagAliasTest::getBlockKey,
					Blocks.BRICKS, Blocks.STONE_BRICKS, Blocks.NETHER_BRICKS, Blocks.RED_NETHER_BRICKS);
			assertTagContent(registries, List.of(CLASSIC_BIOMES, TRADITIONAL_BIOMES),
					Biomes.PLAINS, Biomes.DESERT);

			// The loot table registry isn't synced to the client.
			if (!client) {
				assertTagContent(registries, List.of(NETHER_BRICKS_1, NETHER_BRICKS_2),
						Blocks.NETHER_BRICKS.getLootTable().orElseThrow(),
						Blocks.RED_NETHER_BRICKS.getLootTable().orElseThrow());
			}

			LOGGER.info("Tag alias tests completed successfully!");
		});
	}

	private static ResourceKey<Block> getBlockKey(Block block) {
		return block.builtInRegistryHolder().key();
	}

	private static ResourceKey<Item> getItemKey(Item item) {
		return item.builtInRegistryHolder().key();
	}

	@SafeVarargs
	private static <T> void assertTagContent(HolderLookup.Provider registries, List<TagKey<T>> tags, Function<T, ResourceKey<T>> keyExtractor, T... expected) {
		Set<ResourceKey<T>> keys = Arrays.stream(expected)
				.map(keyExtractor)
				.collect(Collectors.toSet());
		assertTagContent(registries, tags, keys);
	}

	@SafeVarargs
	private static <T> void assertTagContent(HolderLookup.Provider registries, List<TagKey<T>> tags, ResourceKey<T>... expected) {
		assertTagContent(registries, tags, Set.of(expected));
	}

	private static <T> void assertTagContent(HolderLookup.Provider registries, List<TagKey<T>> tags, Set<ResourceKey<T>> expected) {
		HolderGetter<T> lookup = registries.lookupOrThrow(tags.getFirst().registry());

		for (TagKey<T> tag : tags) {
			HolderSet.Named<T> tagEntryList = lookup.getOrThrow(tag);
			Set<ResourceKey<T>> actual = tagEntryList.contents
					.stream()
					.map(entry -> entry.unwrapKey().orElseThrow())
					.collect(Collectors.toSet());

			if (!actual.equals(expected)) {
				throw new AssertionError("Expected tag %s to have contents %s, but it had %s instead"
						.formatted(tag, expected, actual));
			}
		}

		LOGGER.info("Tags {} / {} were successfully aliased together", tags.getFirst().registry().identifier(), tags.stream()
				.map(TagKey::location)
				.map(Identifier::toString)
				.collect(Collectors.joining(", ")));
	}
}
