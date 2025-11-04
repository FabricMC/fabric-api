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

package net.fabricmc.fabric.impl.tag.client;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public class ClientTagsImpl {
	private static final Map<TagKey<?>, ClientTagsLoader.LoadedTag> LOCAL_TAG_HIERARCHY = new ConcurrentHashMap<>();

	public static <T> boolean isInWithLocalFallback(TagKey<T> tagKey, Holder<T> registryEntry) {
		return isInWithLocalFallback(tagKey, registryEntry, new HashSet<>());
	}

	@SuppressWarnings("unchecked")
	private static <T> boolean isInWithLocalFallback(TagKey<T> tagKey, Holder<T> registryEntry, Set<TagKey<T>> checked) {
		if (checked.contains(tagKey)) {
			return false;
		}

		checked.add(tagKey);

		// Check if the tag exists in the dynamic registry first
		Optional<? extends Registry<T>> maybeRegistry = ClientTagsImpl.getRegistry(tagKey);

		if (maybeRegistry.isPresent()) {
			// Check the synced tag exists and use that
			if (maybeRegistry.get().get(tagKey).isPresent()) {
				return registryEntry.is(tagKey);
			}
		}

		if (registryEntry.unwrapKey().isEmpty()) {
			// No key?
			return false;
		}

		// Recursively search the entries contained with the tag
		ClientTagsLoader.LoadedTag wt = ClientTagsImpl.getOrCreatePartiallySyncedTag(tagKey);

		if (wt.immediateChildIds().contains(registryEntry.unwrapKey().get().identifier())) {
			return true;
		}

		for (TagKey<?> key : wt.immediateChildTags()) {
			if (isInWithLocalFallback((TagKey<T>) key, registryEntry, checked)) {
				return true;
			}

			checked.add((TagKey<T>) key);
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<? extends Registry<T>> getRegistry(TagKey<T> tagKey) {
		Objects.requireNonNull(tagKey);

		// Check if the tag represents a dynamic registry
		if (Minecraft.getInstance() != null) {
			if (Minecraft.getInstance().level != null) {
				if (Minecraft.getInstance().level.registryAccess() != null) {
					Optional<? extends Registry<T>> maybeRegistry = Minecraft.getInstance().level
							.registryAccess().lookup(tagKey.registry());
					if (maybeRegistry.isPresent()) return maybeRegistry;
				}
			}
		}

		return (Optional<? extends Registry<T>>) BuiltInRegistries.REGISTRY.getOptional(tagKey.registry().identifier());
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<Holder<T>> getRegistryEntry(TagKey<T> tagKey, T entry) {
		Optional<? extends Registry<?>> maybeRegistry = getRegistry(tagKey);

		if (maybeRegistry.isEmpty() || !tagKey.isFor(maybeRegistry.get().key())) {
			return Optional.empty();
		}

		Registry<T> registry = (Registry<T>) maybeRegistry.get();

		Optional<ResourceKey<T>> maybeKey = registry.getResourceKey(entry);

		return maybeKey.map(registry::getOrThrow);
	}

	public static ClientTagsLoader.LoadedTag getOrCreatePartiallySyncedTag(TagKey<?> tagKey) {
		ClientTagsLoader.LoadedTag loadedTag = LOCAL_TAG_HIERARCHY.get(tagKey);

		if (loadedTag == null) {
			loadedTag = ClientTagsLoader.loadTag(tagKey);
			LOCAL_TAG_HIERARCHY.put(tagKey, loadedTag);
		}

		return loadedTag;
	}
}
