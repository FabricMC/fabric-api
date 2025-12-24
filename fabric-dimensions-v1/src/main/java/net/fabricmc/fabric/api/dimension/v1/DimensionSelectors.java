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

package net.fabricmc.fabric.api.dimension.v1;

import java.util.Collection;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;

import net.fabricmc.fabric.impl.dimension.modification.BuiltInRegistryKeys;

/**
 * Provides several convenient dimension selectors that can be used with {@link DimensionModifications}.
 */
public final class DimensionSelectors {
	private DimensionSelectors() {
	}

	/**
	 * Matches all Dimensions. Use a more specific selector if possible.
	 */
	public static Predicate<DimensionSelectionContext> all() {
		return context -> true;
	}

	/**
	 * Returns a dimension selector that will match all dimensions from the minecraft namespace.
	 */
	public static Predicate<DimensionSelectionContext> vanilla() {
		return context -> {
			// In addition to the namespace, we also check that it exists in the vanilla registries
			return context.getDimensionKey().identifier().getNamespace().equals("minecraft")
					&& BuiltInRegistryKeys.isBuiltinDimension(context.getDimensionKey());
		};
	}

	/**
	 * Returns a dimension selector that will match all dimensions in the given tag.
	 */
	public static Predicate<DimensionSelectionContext> tag(TagKey<DimensionType> tag) {
		return context -> context.hasTag(tag);
	}

	/**
	 * @see #excludeByKey(Collection)
	 */
	@SafeVarargs
	public static Predicate<DimensionSelectionContext> excludeByKey(ResourceKey<DimensionType>... keys) {
		return excludeByKey(ImmutableSet.copyOf(keys));
	}

	/**
	 * Returns a selector that will reject any dimension whose key is in the given collection of keys.
	 *
	 * <p>This is useful for allowing a list of dimensions to be defined in the config file, where
	 * a certain feature should not spawn.
	 */
	public static Predicate<DimensionSelectionContext> excludeByKey(Collection<ResourceKey<DimensionType>> keys) {
		return context -> !keys.contains(context.getDimensionKey());
	}

	/**
	 * @see #includeByKey(Collection)
	 */
	@SafeVarargs
	public static Predicate<DimensionSelectionContext> includeByKey(ResourceKey<DimensionType>... keys) {
		return includeByKey(ImmutableSet.copyOf(keys));
	}

	/**
	 * Returns a selector that will accept only dimensions whose keys are in the given collection of keys.
	 *
	 * <p>This is useful for allowing a list of dimensions to be defined in the config file, where
	 * a certain feature should spawn exclusively.
	 */
	public static Predicate<DimensionSelectionContext> includeByKey(Collection<ResourceKey<DimensionType>> keys) {
		return context -> keys.contains(context.getDimensionKey());
	}
}
