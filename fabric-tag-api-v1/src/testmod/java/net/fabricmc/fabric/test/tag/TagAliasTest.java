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

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;

public final class TagAliasTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TagAliasTest.class);

	// Test 1: Alias two non-empty tags
	public static final TagKey<Item> GEMS = tagKey(RegistryKeys.ITEM, "gems");
	public static final TagKey<Item> EXPENSIVE_ROCKS = tagKey(RegistryKeys.ITEM, "expensive_rocks");

	// Test 2: Alias a non-empty tag and an empty tag
	public static final TagKey<Item> REDSTONE_DUSTS = tagKey(RegistryKeys.ITEM, "redstone_dusts");
	public static final TagKey<Item> REDSTONE_POWDERS = tagKey(RegistryKeys.ITEM, "redstone_powders");

	// Test 3: Alias a non-empty tag and a missing tag
	public static final TagKey<Item> BEETROOTS = tagKey(RegistryKeys.ITEM, "beetroots");
	public static final TagKey<Item> MISSING_BEETROOTS = tagKey(RegistryKeys.ITEM, "missing_beetroots");

	// Test 4: Given tags A, B, C, make alias groups A+B and B+C. They should get merged.
	public static final TagKey<Block> BRICK_BLOCKS = tagKey(RegistryKeys.BLOCK, "brick_blocks");
	public static final TagKey<Block> MORE_BRICK_BLOCKS = tagKey(RegistryKeys.BLOCK, "more_brick_blocks");
	public static final TagKey<Block> BRICKS = tagKey(RegistryKeys.BLOCK, "bricks");

	// Test 5: Merge tags from a world generation dynamic registry
	public static final TagKey<Biome> CLASSIC_BIOMES = tagKey(RegistryKeys.BIOME, "classic");
	public static final TagKey<Biome> TRADITIONAL_BIOMES = tagKey(RegistryKeys.BIOME, "traditional");

	// Test 6: Merge tags from a reloadable registry
	public static final TagKey<LootTable> NETHER_BRICKS_1 = tagKey(RegistryKeys.LOOT_TABLE, "nether_bricks_1");
	public static final TagKey<LootTable> NETHER_BRICKS_2 = tagKey(RegistryKeys.LOOT_TABLE, "nether_bricks_2");

	private static <T> TagKey<T> tagKey(RegistryKey<? extends Registry<T>> registryRef, String name) {
		return TagKey.of(registryRef, Identifier.of("fabric-tag-api-v1-testmod", name));
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
					BiomeKeys.PLAINS, BiomeKeys.DESERT);

			// The loot table registry isn't synced to the client.
			if (!client) {
				assertTagContent(registries, List.of(NETHER_BRICKS_1, NETHER_BRICKS_2),
						Blocks.NETHER_BRICKS.getLootTableKey().orElseThrow(),
						Blocks.RED_NETHER_BRICKS.getLootTableKey().orElseThrow());
			}

			LOGGER.info("Tag alias tests completed successfully!");
		});
	}

	private static RegistryKey<Block> getBlockKey(Block block) {
		return block.getRegistryEntry().registryKey();
	}

	private static RegistryKey<Item> getItemKey(Item item) {
		return item.getRegistryEntry().registryKey();
	}

	@SafeVarargs
	private static <T> void assertTagContent(RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, Function<T, RegistryKey<T>> keyExtractor, T... expected) {
		Set<RegistryKey<T>> keys = Arrays.stream(expected)
				.map(keyExtractor)
				.collect(Collectors.toSet());
		assertTagContent(registries, tags, keys);
	}

	@SafeVarargs
	private static <T> void assertTagContent(RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, RegistryKey<T>... expected) {
		assertTagContent(registries, tags, Set.of(expected));
	}

	private static <T> void assertTagContent(RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, Set<RegistryKey<T>> expected) {
		RegistryEntryLookup<T> lookup = registries.getOrThrow(tags.getFirst().registryRef());

		for (TagKey<T> tag : tags) {
			RegistryEntryList.Named<T> tagEntryList = lookup.getOrThrow(tag);
			Set<RegistryKey<T>> actual = tagEntryList.entries
					.stream()
					.map(entry -> entry.getKey().orElseThrow())
					.collect(Collectors.toSet());

			if (!actual.equals(expected)) {
				throw new AssertionError("Expected tag %s to have contents %s, but it had %s instead"
						.formatted(tag, expected, actual));
			}
		}

		LOGGER.info("Tags {} / {} were successfully aliased together", tags.getFirst().registryRef().getValue(), tags.stream()
				.map(TagKey::id)
				.map(Identifier::toString)
				.collect(Collectors.joining(", ")));
	}
}
