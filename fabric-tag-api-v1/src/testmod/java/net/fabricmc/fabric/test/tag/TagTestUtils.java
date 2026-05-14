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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

public class TagTestUtils {
	public static <T> RegistryKey<T> resourceKey(RegistryKey<? extends Registry<T>> registryRef, String name) {
		return RegistryKey.of(registryRef, Identifier.of(TagTest.MOD_ID, name));
	}

	public static <T> TagKey<T> tagKey(RegistryKey<? extends Registry<T>> registryRef, String name) {
		return TagKey.of(registryRef, Identifier.of(TagTest.MOD_ID, name));
	}

	public static RegistryKey<Block> getBlockKey(Block block) {
		return block.getRegistryEntry().registryKey();
	}

	public static RegistryKey<Item> getItemKey(Item item) {
		return item.getRegistryEntry().registryKey();
	}

	static void assertThrows(FailableRunnable<GameTestException> action, String message) {
		boolean threw = false;

		try {
			action.run();
		} catch (GameTestException err) {
			threw = true;
		}

		if (!threw) {
			throw new GameTestException(message);
		}
	}

	@SafeVarargs
	static <T> void assertInTag(TestContext context, Logger logger, String successFmtStr, RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, Function<T, RegistryKey<T>> keyExtractor, T... expected) throws GameTestException {
		assertInTag(context, logger, successFmtStr, registries, tags, Arrays.stream(expected).map(keyExtractor).collect(Collectors.toSet()));
	}

	@SafeVarargs
	static <T> void assertInTag(TestContext context, Logger logger, String successFmtStr, RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, RegistryKey<T>... expected) throws GameTestException {
		assertInTag(context, logger, successFmtStr, registries, tags, Set.of(expected));
	}

	static <T> void assertInTag(TestContext context, Logger logger, String successFmtStr, RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, Set<RegistryKey<T>> expected) throws GameTestException {
		RegistryWrapper<T> lookup = registries.getWrapperOrThrow(tags.getFirst().registry());

		for (TagKey<T> tag : tags) {
			RegistryEntryList.Named<T> registryEntryList = lookup.getOrThrow(tag);
			Set<RegistryKey<T>> actual = registryEntryList.entries
					.stream()
					.map(entry -> entry.getKey().orElseThrow())
					.collect(Collectors.toSet());

			for (RegistryKey<T> key : expected) {
				if (!actual.contains(key)) {
					throw new GameTestException(("Expected to find %s in %s, but it was not found!").formatted(
							key, tag.id()));
				}
			}
		}

		if (!successFmtStr.isBlank()) {
			logger.info(successFmtStr, tags.getFirst().registry().getValue(), expected.stream()
					.map(RegistryKey::getValue)
					.map(Identifier::toString)
					.collect(Collectors.joining(", ")));
		}
	}

	@SafeVarargs
	static <T> void assertTagContent(TestContext context, Logger logger, String successFmtStr, RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, Function<T, RegistryKey<T>> keyExtractor, T... expected) throws GameTestException {
		Set<RegistryKey<T>> keys = Arrays.stream(expected)
				.map(keyExtractor)
				.collect(Collectors.toSet());
		assertTagContent(context, logger, successFmtStr, registries, tags, keys);
	}

	@SafeVarargs
	static <T> void assertTagContent(TestContext context, Logger logger, String successFmtStr, RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, RegistryKey<T>... expected) throws GameTestException {
		assertTagContent(context, logger, successFmtStr, registries, tags, Set.of(expected));
	}

	static <T> void assertTagContent(TestContext context, Logger logger, String successFmtStr, RegistryWrapper.WrapperLookup registries, List<TagKey<T>> tags, Set<RegistryKey<T>> expected) throws GameTestException {
		RegistryWrapper<T> lookup = registries.getWrapperOrThrow(tags.getFirst().registry());

		for (TagKey<T> tag : tags) {
			RegistryEntryList.Named<T> registryEntryList = lookup.getOrThrow(tag);
			Set<RegistryKey<T>> actual = registryEntryList.entries
					.stream()
					.map(entry -> entry.getKey().orElseThrow())
					.collect(Collectors.toSet());

			if (!actual.equals(expected)) {
				throw new GameTestException(("Expected tag %s to have contents %s, but it had %s instead").formatted(
						tag, expected, actual));
			}
		}

		if (!successFmtStr.isBlank()) {
			logger.info(successFmtStr, tags.getFirst().registry().getRegistry(), tags.stream()
					.map(TagKey::id)
					.map(Identifier::toString)
					.collect(Collectors.joining(", ")));
		}
	}

	static void reloadResources(TestContext context, MinecraftServer server, Function<TestContext, GameTestException> onException) {
		server.reloadResources(server.getDataPackManager().getEnabledIds()).exceptionally((throwable) -> {
			throw onException.apply(context);
		});
	}
}
