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

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.function.FailableRunnable;
import org.slf4j.Logger;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;

import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.fabricmc.fabric.impl.tag.client.ClientTagsImpl;

public class ClientTagTestUtils {
	@SafeVarargs
	static <T> void assertInWithLocalFallback(Logger logger, String successFmtStr, TagKey<T> tag, Function<T, ResourceKey<T>> keyExtractor, T... expected) {
		assertInWithLocalFallback(logger, successFmtStr, tag, Arrays.stream(expected)
				.map(value -> ClientTagsImpl.getHolder(tag, value).orElseThrow())
				.collect(Collectors.toSet()));
	}

	@SafeVarargs
	static <T> void assertInWithLocalFallback(Logger logger, String successFmtStr, TagKey<T> tag, ResourceKey<T>... expected) {
		assertInWithLocalFallback(logger, successFmtStr, tag, Arrays.stream(expected)
				.map(key -> {
					Registry<T> registry = ClientTagsImpl.getRegistry(tag).orElseThrow();
					return registry.getOrThrow(key);
				}).collect(Collectors.toSet()));
	}

	static <T> void assertInWithLocalFallback(Logger logger, String successFmtStr, TagKey<T> tag, Set<Holder<T>> expected) {
		for (Holder<T> holder : expected) {
			if (!ClientTags.isInWithLocalFallback(tag, holder)) {
				throw new AssertionError("Expected to find %s in %s, but it was not found!"
						.formatted(holder.unwrapKey().orElseThrow().identifier(), tag.location()));
			}
		}

		if (!successFmtStr.isBlank()) {
			logger.info(successFmtStr, tag, expected.stream()
					.map(Holder::unwrapKey)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.map(ResourceKey::identifier)
					.map(Identifier::toString)
					.collect(Collectors.joining(", ")));
		}
	}

	@SafeVarargs
	static <T> void assertInLocal(Logger logger, String successFmtStr, TagKey<T> tag, ResourceKey<T>... expected) {
		assertInLocal(logger, successFmtStr, tag, Set.of(expected));
	}

	static <T> void assertInLocal(Logger logger, String successFmtStr, TagKey<T> tag, Set<ResourceKey<T>> expected) {
		for (ResourceKey<T> key : expected) {
			if (!ClientTags.isInLocal(tag, key)) {
				throw new AssertionError("Expected to find %s in %s, but it was not found!"
						.formatted(key.identifier(), tag.location()));
			}
		}

		if (!successFmtStr.isBlank()) {
			logger.info(successFmtStr, tag, expected.stream()
					.map(ResourceKey::identifier)
					.map(Identifier::toString)
					.collect(Collectors.joining(", ")));
		}
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

	static void reloadResources(MinecraftServer server, Supplier<AssertionError> onException) {
		server.reloadResources(server.getPackRepository().getSelectedIds()).exceptionally((throwable) -> {
			throw onException.get();
		});
	}
}
