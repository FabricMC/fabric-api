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

package net.fabricmc.fabric.api.tag.convention.v2;

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

/**
 * A Helper class for checking whether a {@link TagKey} contains some entry.
 * This can be useful for {@link TagKey}s whose type has no easy way of querying if they are in a tag, such as {@link net.minecraft.world.item.enchantment.Enchantment}s.
 *
 * <p>For dynamic registry entries, use {@link #isIn(RegistryAccess, TagKey, Object)} with a non-null dynamic registry manager.
 * For non-dynamic registry entries, the simpler {@link #isIn(TagKey, Object)} can be used.
 */
public final class TagUtil {
	public static final String C_TAG_NAMESPACE = "c";
	public static final String FABRIC_TAG_NAMESPACE = "fabric";

	private TagUtil() {
	}

	/**
	 * See {@link TagUtil#isIn(RegistryAccess, TagKey, Object)} to check tags that refer to entries in dynamic
	 * registries, such as {@link net.minecraft.world.level.biome.Biome}s.
	 * @return if the entry is in the provided tag.
	 */
	public static <T> boolean isIn(TagKey<T> tagKey, T entry) {
		return isIn(null, tagKey, entry);
	}

	/**
	 * @param registryManager the registry manager instance of the client or server. If the tag refers to entries
	 *                        within a dynamic registry, such as {@link net.minecraft.world.level.biome.Biome}s,
	 *                        this must be passed to correctly evaluate the tag. Otherwise, the registry is found by
	 *                        looking in {@link BuiltInRegistries#REGISTRY}.
	 * @return if the entry is in the provided tag.
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean isIn(@Nullable RegistryAccess registryManager, TagKey<T> tagKey, T entry) {
		Optional<? extends Registry<?>> maybeRegistry;
		Objects.requireNonNull(tagKey);
		Objects.requireNonNull(entry);

		if (registryManager != null) {
			maybeRegistry = registryManager.lookup(tagKey.registry());
		} else {
			maybeRegistry = BuiltInRegistries.REGISTRY.getOptional(tagKey.registry().identifier());
		}

		if (maybeRegistry.isPresent()) {
			if (tagKey.isFor(maybeRegistry.get().key())) {
				Registry<T> registry = (Registry<T>) maybeRegistry.get();

				Optional<ResourceKey<T>> maybeKey = registry.getResourceKey(entry);

				// Check synced tag
				if (maybeKey.isPresent()) {
					return registry.getOrThrow(maybeKey.get()).is(tagKey);
				}
			}
		}

		return false;
	}
}
