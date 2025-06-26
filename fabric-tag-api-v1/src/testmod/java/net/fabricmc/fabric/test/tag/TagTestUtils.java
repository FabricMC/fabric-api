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

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TagTestUtils {
	static <T> TagKey<T> tagKey(RegistryKey<? extends Registry<T>> registryRef, String name) {
		return TagKey.of(registryRef, Identifier.of("fabric-tag-api-v1-testmod", name));
	}

	static RegistryKey<Block> getBlockKey(Block block) {
		return block.getRegistryEntry().registryKey();
	}

	static RegistryKey<Item> getItemKey(Item item) {
		return item.getRegistryEntry().registryKey();
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
	static <T> void assertTagContent(Logger logger, String successFmtStr, RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, Function<T, RegistryKey<T>> keyExtractor, T... expected) {
		Set<RegistryKey<T>> keys = Arrays.stream(expected)
				.map(keyExtractor)
				.collect(Collectors.toSet());
		assertTagContent(logger, successFmtStr, registries, tags, keys);
	}

	@SafeVarargs
	static <T> void assertTagContent(Logger logger, String successFmtStr, RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, RegistryKey<T>... expected) {
		assertTagContent(logger, successFmtStr, registries, tags, Set.of(expected));
	}

	static <T> void assertTagContent(Logger logger, String successFmtStr, RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, Set<RegistryKey<T>> expected) {
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

		logger.info(successFmtStr, tags.getFirst().registryRef().getValue(), tags.stream()
				.map(TagKey::id)
				.map(Identifier::toString)
				.collect(Collectors.joining(", ")));
	}
}
