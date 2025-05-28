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

package net.fabricmc.fabric.api.tag.client.v1;

import java.util.Objects;
import java.util.Set;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.tag.client.ClientTagsImpl;

/**
 * Allows the use of tags by directly loading them from the installed mods.
 *
 * <p>Tags are loaded by the server, either the internal server in singleplayer or the connected server and
 * synced to the client. This can be a pain point for interoperability, as a tag that does not exist on the server
 * because it is part of a mod only present on the client will no longer be available to the client that may wish to
 * query it.
 *
 * <p>Client Tags resolve that issue by lazily reading the tag json files within the mods on the side of the caller,
 * directly, allowing for mods to query tags such as {@link net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags}
 * even when connected to a vanilla server.
 *
 * <p>Note that locally read client tags don't currently support Fabric's tag aliases. The aliasing system is only
 * implemented on servers.
 */
public final class ClientTags {
	private ClientTags() {
	}

	/**
	 * Loads a tag into the cache, recursively loading any contained tags along with it.
	 *
	 * @param tagKey the {@code TagKey} to load
	 * @return a set of {@code Identifier}s this tag contains
	 */
	public static Set<Identifier> getOrCreateLocalTag(TagKey<?> tagKey) {
		return ClientTagsImpl.getOrCreatePartiallySyncedTag(tagKey).completeIds();
	}

	/**
	 * Checks if an entry is in a tag.
	 *
	 * <p>If the synced tag does exist, it is queried. If it does not exist,
	 * the tag populated from the available mods is checked, recursively checking the
	 * synced tags and entries contained within.
	 *
	 * @param tagKey the {@code TagKey} to being checked
	 * @param entry  the entry to check
	 * @return if the entry is in the given tag
	 */
	public static <T> boolean isInWithLocalFallback(TagKey<T> tagKey, T entry) {
		Objects.requireNonNull(tagKey);
		Objects.requireNonNull(entry);

		return ClientTagsImpl.getRegistryEntry(tagKey, entry).map(re -> isInWithLocalFallback(tagKey, re)).orElse(false);
	}

	/**
	 * Checks if an entry is in a tag, for use with entries from a dynamic registry,
	 * such as {@link net.minecraft.world.biome.Biome}s.
	 *
	 * <p>If the synced tag does exist, it is queried. If it does not exist,
	 * the tag populated from the available mods is checked, recursively checking the
	 * synced tags and entries contained within.
	 *
	 * @param tagKey        the {@code TagKey} to be checked
	 * @param registryEntry the entry to check
	 * @return if the entry is in the given tag
	 */
	public static <T> boolean isInWithLocalFallback(TagKey<T> tagKey, RegistryEntry<T> registryEntry) {
		Objects.requireNonNull(tagKey);
		Objects.requireNonNull(registryEntry);
		return ClientTagsImpl.isInWithLocalFallback(tagKey, registryEntry);
	}

	/**
	 * Checks if an entry is in a tag provided by the available mods.
	 *
	 * @param tagKey      the {@code TagKey} to being checked
	 * @param registryKey the entry to check
	 * @return if the entry is in the given tag
	 */
	public static <T> boolean isInLocal(TagKey<T> tagKey, RegistryKey<T> registryKey) {
		Objects.requireNonNull(tagKey);
		Objects.requireNonNull(registryKey);

		if (tagKey.registryRef().getValue().equals(registryKey.getRegistry())) {
			// Check local tags
			Set<Identifier> ids = getOrCreateLocalTag(tagKey);
			return ids.contains(registryKey.getValue());
		}

		return false;
	}
}
