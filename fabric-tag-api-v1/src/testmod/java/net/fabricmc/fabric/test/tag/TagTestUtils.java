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

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import org.apache.commons.lang3.function.FailableRunnable;
import org.slf4j.Logger;

public class TagTestUtils {
	public static <T> TagKey<T> tagKey(ResourceKey<? extends Registry<T>> registryRef, String name) {
		return TagKey.create(registryRef, Identifier.fromNamespaceAndPath("fabric-tag-api-v1-testmod", name));
	}

	static ResourceKey<Block> getBlockKey(Block block) {
		return block.builtInRegistryHolder().key();
	}

	static ResourceKey<Item> getItemKey(Item item) {
		return item.builtInRegistryHolder().key();
	}

	static void assertThrows(FailableRunnable<AssertionError> action, String message) {
		boolean threw = false;

		try {
			action.run();
		} catch (AssertionError err) {
			threw = true;
		}

		if (!threw) {
			throw new AssertionError(message);
		}
	}

	@SafeVarargs
	static <T> void assertTagContent(Logger logger, String successFmtStr, RegistryAccess registries, List<TagKey<T>> tags, Function<T, ResourceKey<T>> keyExtractor, T... expected) {
		Set<ResourceKey<T>> keys = Arrays.stream(expected)
				.map(keyExtractor)
				.collect(Collectors.toSet());
		assertTagContent(logger, successFmtStr, registries, tags, keys);
	}

	@SafeVarargs
	static <T> void assertTagContent(Logger logger, String successFmtStr, RegistryAccess registries, List<TagKey<T>> tags, ResourceKey<T>... expected) {
		assertTagContent(logger, successFmtStr, registries, tags, Set.of(expected));
	}

	static <T> void assertTagContent(Logger logger, String successFmtStr, RegistryAccess registries, List<TagKey<T>> tags, Set<ResourceKey<T>> expected) {
		HolderLookup<T> lookup = registries.lookupOrThrow(tags.getFirst().registry());

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

		logger.info(successFmtStr, tags.getFirst().registry().identifier(), tags.stream()
				.map(TagKey::location)
				.map(Identifier::toString)
				.collect(Collectors.joining(", ")));
	}
}
