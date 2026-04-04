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

import org.apache.commons.lang3.function.FailableRunnable;
import org.slf4j.Logger;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TagTestUtils {
	public static <T> TagKey<T> tagKey(ResourceKey<? extends Registry<T>> registryRef, String name) {
		return TagKey.create(registryRef, Identifier.fromNamespaceAndPath("fabric-tag-api-v1-testmod", name));
	}

	public static ResourceKey<Block> getBlockKey(Block block) {
		return block.builtInRegistryHolder().key();
	}

	public static ResourceKey<Item> getItemKey(Item item) {
		return item.builtInRegistryHolder().key();
	}

	static void assertThrows(GameTestHelper helper, FailableRunnable<GameTestAssertException> action, String message) {
		boolean threw = false;

		try {
			action.run();
		} catch (GameTestAssertException err) {
			threw = true;
		}

		if (!threw) {
			throw helper.assertionException(message);
		}
	}

	@SafeVarargs
	static <T> void assertInTag(GameTestHelper helper, Logger logger, String successFmtStr, HolderLookup.Provider registries, List<TagKey<T>> tags, Function<T, ResourceKey<T>> keyExtractor, T... expected) throws GameTestAssertException {
		assertInTag(helper, logger, successFmtStr, registries, tags, Arrays.stream(expected).map(keyExtractor).collect(Collectors.toSet()));
	}

	@SafeVarargs
	static <T> void assertInTag(GameTestHelper helper, Logger logger, String successFmtStr, HolderLookup.Provider registries, List<TagKey<T>> tags, ResourceKey<T>... expected) throws GameTestAssertException {
		assertInTag(helper, logger, successFmtStr, registries, tags, Set.of(expected));
	}

	static <T> void assertInTag(GameTestHelper helper, Logger logger, String successFmtStr, HolderLookup.Provider registries, List<TagKey<T>> tags, Set<ResourceKey<T>> expected) throws GameTestAssertException {
		HolderLookup<T> lookup = registries.lookupOrThrow(tags.getFirst().registry());

		for (TagKey<T> tag : tags) {
			HolderSet.Named<T> holderSet = lookup.getOrThrow(tag);
			Set<ResourceKey<T>> actual = holderSet.contents
					.stream()
					.map(entry -> entry.unwrapKey().orElseThrow())
					.collect(Collectors.toSet());

			for (ResourceKey<T> key : expected) {
				if (!actual.contains(key)) {
					throw helper.assertionException("Expected to find %s in %s, but it was not found!",
							key, tag.location());
				}
			}
		}

		if (!successFmtStr.isBlank()) {
			logger.info(successFmtStr, tags.getFirst().registry().identifier(), expected.stream()
					.map(ResourceKey::identifier)
					.map(Identifier::toString)
					.collect(Collectors.joining(", ")));
		}
	}

	@SafeVarargs
	static <T> void assertTagContent(GameTestHelper helper, Logger logger, String successFmtStr, HolderLookup.Provider registries, List<TagKey<T>> tags, Function<T, ResourceKey<T>> keyExtractor, T... expected) throws GameTestAssertException {
		Set<ResourceKey<T>> keys = Arrays.stream(expected)
				.map(keyExtractor)
				.collect(Collectors.toSet());
		assertTagContent(helper, logger, successFmtStr, registries, tags, keys);
	}

	@SafeVarargs
	static <T> void assertTagContent(GameTestHelper helper, Logger logger, String successFmtStr, HolderLookup.Provider registries, List<TagKey<T>> tags, ResourceKey<T>... expected) throws GameTestAssertException {
		assertTagContent(helper, logger, successFmtStr, registries, tags, Set.of(expected));
	}

	static <T> void assertTagContent(GameTestHelper helper, Logger logger, String successFmtStr, HolderLookup.Provider registries, List<TagKey<T>> tags, Set<ResourceKey<T>> expected) throws GameTestAssertException {
		HolderLookup<T> lookup = registries.lookupOrThrow(tags.getFirst().registry());

		for (TagKey<T> tag : tags) {
			HolderSet.Named<T> holderSet = lookup.getOrThrow(tag);
			Set<ResourceKey<T>> actual = holderSet.contents
					.stream()
					.map(entry -> entry.unwrapKey().orElseThrow())
					.collect(Collectors.toSet());

			if (!actual.equals(expected)) {
				throw helper.assertionException("Expected tag %s to have contents %s, but it had %s instead",
						tag, expected, actual);
			}
		}

		if (!successFmtStr.isBlank()) {
			logger.info(successFmtStr, tags.getFirst().registry().identifier(), tags.stream()
					.map(TagKey::location)
					.map(Identifier::toString)
					.collect(Collectors.joining(", ")));
		}
	}
}
