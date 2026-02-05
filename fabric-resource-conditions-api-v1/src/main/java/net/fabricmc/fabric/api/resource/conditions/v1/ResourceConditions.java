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

package net.fabricmc.fabric.api.resource.conditions.v1;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlag;

import net.fabricmc.fabric.impl.resource.conditions.conditions.AllModsLoadedResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.AndResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.AnyModsLoadedResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.FeaturesEnabledResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.NotResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.OrResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.RegistryContainsResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.TagsPopulatedResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.TrueResourceCondition;

/**
 * Contains default resource conditions and the condition registry.
 */
public final class ResourceConditions {
	private static final Map<Identifier, ResourceConditionType<?>> REGISTERED_CONDITIONS = new ConcurrentHashMap<>();

	/**
	 * The JSON key for resource conditions, {@value #CONDITIONS_KEY}.
	 */
	public static final String CONDITIONS_KEY = "fabric:load_conditions";

	/**
	 * The JSON key for conditional overlays in pack.mcmeta files.
	 */
	public static final String OVERLAYS_KEY = "fabric:overlays";

	private ResourceConditions() {
	}

	/**
	 * Registers {@code condition}.
	 * @param condition the condition to register
	 * @throws IllegalArgumentException if {@code condition} is already registered
	 */
	public static void register(ResourceConditionType<?> condition) {
		Objects.requireNonNull(condition, "Condition may not be null.");

		if (REGISTERED_CONDITIONS.put(condition.id(), condition) != null) {
			throw new IllegalArgumentException("Duplicate resource condition registered with id " + condition.id());
		}
	}

	/**
	 * @return the condition with ID {@code id}, or {@code null} if there is no such condition
	 */
	public static ResourceConditionType<?> getConditionType(Identifier id) {
		return REGISTERED_CONDITIONS.get(id);
	}

	/**
	 * A condition that always passes. Has ID {@code fabric:true}.
	 */
	public static ResourceCondition alwaysTrue() {
		return new TrueResourceCondition();
	}

	/**
	 * A condition that passes if {@code condition} does not pass. Has ID {@code fabric:not} and
	 * takes one field, {@code value}, which is a resource condition.
	 */
	public static ResourceCondition not(ResourceCondition condition) {
		return new NotResourceCondition(condition);
	}

	/**
	 * A condition that passes if each of the {@code conditions} passes. Has ID {@code fabric:and}
	 * and takes one field, {@code values}, which is a list of resource conditions.
	 * If there are no conditions to check, it always passes.
	 */
	public static ResourceCondition and(ResourceCondition... conditions) {
		return new AndResourceCondition(List.of(conditions));
	}

	/**
	 * A condition that passes if any of the {@code conditions} passes. Has ID {@code fabric:or}
	 * and takes one field, {@code values}, which is a list of resource conditions.
	 * If there are no conditions to check, it always fails.
	 */
	public static ResourceCondition or(ResourceCondition... conditions) {
		return new OrResourceCondition(List.of(conditions));
	}

	/**
	 * A condition that passes if each of the specified {@code modIds} are loaded. Has ID
	 * {@code all_mods_loaded} and takes one field, {@code values}, which is a list of strings indicating
	 * mod IDs. If there are no IDs to check, it always passes.
	 */
	public static ResourceCondition allModsLoaded(String... modIds) {
		return new AllModsLoadedResourceCondition(List.of(modIds));
	}

	/**
	 * A condition that passes if any of the specified {@code modIds} are loaded. Has ID
	 * {@code any_mods_loaded} and takes one field, {@code values}, which is a list of strings indicating
	 * mod IDs. If there are no IDs to check, it always fails.
	 */
	public static ResourceCondition anyModsLoaded(String... modIds) {
		return new AnyModsLoadedResourceCondition(List.of(modIds));
	}

	/**
	 * A condition that passes if each of the {@code tags} exist. This does not check if those tags have
	 * any entries. Has ID {@code tags_populated} and takes two fields: {@code registry}, which is the ID
	 * of the registry the tag is for, and {@code values}, which is a list of the tag IDs to check.
	 * If there are no IDs to check, it always passes, including in cases where a nonexistent registry is
	 * specified.
	 *
	 * @implNote Because tags are loaded after loot tables (and predicates/item modifiers), these resources
	 * do not support this condition. In those cases, this condition logs a warning and always fails.
	 * @param <T> the type of the tag values
	 */
	@SafeVarargs
	public static <T> ResourceCondition tagsPopulated(TagKey<T>... tags) {
		return new TagsPopulatedResourceCondition(tags);
	}

	/**
	 * @see #tagsPopulated(TagKey[])
	 * @param <T> the type of the tag values
	 */
	@SafeVarargs
	public static <T> ResourceCondition tagsPopulated(ResourceKey<? extends Registry<T>> registry, TagKey<T>... tags) {
		return new TagsPopulatedResourceCondition(registry.identifier(), tags);
	}

	/**
	 * A condition that passes if each of the {@code features} are enabled. Has ID {@code features_enabled}
	 * and takes one field, {@code features}, which is a list of the feature IDs. If there are no IDs to
	 * check, it always passes. If an unknown feature is specified, it always fails.
	 */
	public static ResourceCondition featuresEnabled(Identifier... features) {
		return new FeaturesEnabledResourceCondition(features);
	}

	/**
	 * @see #featuresEnabled(Identifier...)
	 */
	public static ResourceCondition featuresEnabled(FeatureFlag... features) {
		return new FeaturesEnabledResourceCondition(features);
	}

	/**
	 * A condition that passes if each of the {@code entries} exist. The entries may be from static or
	 * dynamic registries. Has ID {@code registry_contains} and takes two fields: {@code registry}, which
	 * is the ID of the registry, and {@code values}, which is a list of IDs to check.
	 * If there are no IDs to check, it always passes, including in cases where a nonexistent registry is
	 * specified.
	 *
	 * @param <T> the type of the tag values
	 */
	@SafeVarargs
	public static <T> ResourceCondition registryContains(ResourceKey<T>... entries) {
		return new RegistryContainsResourceCondition(entries);
	}

	/**
	 * @see #registryContains(ResourceKey[])
	 * @param <T> the type of the tag values
	 */
	public static <T> ResourceCondition registryContains(ResourceKey<? extends Registry<T>> registry, Identifier... entries) {
		return new RegistryContainsResourceCondition(registry.identifier(), entries);
	}
}
