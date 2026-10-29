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

package net.fabricmc.fabric.impl.resource.conditions;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;

public final class ResourceConditionsImpl implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Fabric Resource Conditions");
	public static FeatureSet currentFeatures = null;

	@Override
	public void onInitialize() {
		ResourceConditions.register(DefaultResourceConditionTypes.TRUE);
		ResourceConditions.register(DefaultResourceConditionTypes.FALSE);
		ResourceConditions.register(DefaultResourceConditionTypes.NOT);
		ResourceConditions.register(DefaultResourceConditionTypes.AND);
		ResourceConditions.register(DefaultResourceConditionTypes.OR);
		ResourceConditions.register(DefaultResourceConditionTypes.ALL_MODS_LOADED);
		ResourceConditions.register(DefaultResourceConditionTypes.ANY_MODS_LOADED);
		ResourceConditions.register(DefaultResourceConditionTypes.TAGS_POPULATED);
		ResourceConditions.register(DefaultResourceConditionTypes.FEATURES_ENABLED);
		ResourceConditions.register(DefaultResourceConditionTypes.REGISTRY_CONTAINS);
	}

	public static boolean applyResourceConditions(JsonObject obj, String dataType, Identifier key, @Nullable RegistryWrapper.WrapperLookup registryLookup) {
		boolean debugLogEnabled = ResourceConditionsImpl.LOGGER.isDebugEnabled();

		if (obj.has(ResourceConditions.CONDITIONS_KEY)) {
			DataResult<ResourceCondition> conditions = ResourceCondition.CONDITION_CODEC.parse(JsonOps.INSTANCE, obj.get(ResourceConditions.CONDITIONS_KEY));

			if (conditions.isSuccess()) {
				boolean matched = conditions.getOrThrow().test(registryLookup);

				if (debugLogEnabled) {
					String verdict = matched ? "Allowed" : "Rejected";
					ResourceConditionsImpl.LOGGER.debug("{} resource of type {} with id {}", verdict, dataType, key);
				}

				return matched;
			} else {
				ResourceConditionsImpl.LOGGER.error("Failed to parse resource conditions for file of type {} with id {}, skipping: {}", dataType, key, conditions.error().get().message());
			}
		}

		return true;
	}

	// Condition implementations

	public static boolean conditionsMet(List<ResourceCondition> conditions, @Nullable RegistryWrapper.WrapperLookup registryLookup, boolean and) {
		for (ResourceCondition condition : conditions) {
			if (condition.test(registryLookup) != and) {
				return !and;
			}
		}

		return and;
	}

	public static boolean modsLoaded(List<String> modIds, boolean and) {
		for (String modId : modIds) {
			if (FabricLoader.getInstance().isModLoaded(modId) != and) {
				return !and;
			}
		}

		return and;
	}

	/**
	 * Stores the tags deserialized by {@link TagManagerLoader} before they are bound, to use them in the tags_populated conditions.
	 * The tags are set at the end of the "apply" phase in {@link TagManagerLoader}, and cleared in {@link net.minecraft.server.DataPackContents#refresh}.
	 * If the resource reload fails, the thread local is not cleared and:
	 * - the map will remain in memory until the next reload;
	 * - any call to {@link #tagsPopulated} will check the tags from the failed reload instead of failing directly.
	 * This is probably acceptable.
	 */
	public static final ThreadLocal<Map<RegistryKey<?>, Set<Identifier>>> LOADED_TAGS = new ThreadLocal<>();

	public static void setTags(List<TagManagerLoader.RegistryTags<?>> tags) {
		Map<RegistryKey<?>, Set<Identifier>> tagMap = new IdentityHashMap<>();

		for (TagManagerLoader.RegistryTags<?> registryTags : tags) {
			tagMap.put(registryTags.key(), registryTags.tags().keySet());
		}

		LOADED_TAGS.set(tagMap);
	}

	// Cannot use registry because tags are not loaded to the registry at this stage yet.
	public static boolean tagsPopulated(Identifier registryId, List<Identifier> tags) {
		Map<RegistryKey<?>, Set<Identifier>> tagMap = LOADED_TAGS.get();

		if (tagMap == null) {
			LOGGER.warn("Can't retrieve registry {}, failing tags_populated resource condition check", registryId);
			return false;
		}

		Set<Identifier> tagSet = tagMap.get(RegistryKey.ofRegistry(registryId));

		if (tagSet == null) {
			return tags.isEmpty();
		} else {
			return tagSet.containsAll(tags);
		}
	}

	public static boolean featuresEnabled(Collection<Identifier> features) {
		MutableBoolean foundUnknown = new MutableBoolean();
		FeatureSet set = FeatureFlags.FEATURE_MANAGER.featureSetOf(features, (id) -> {
			LOGGER.info("Found unknown feature {}, treating it as failure", id);
			foundUnknown.setTrue();
		});

		if (foundUnknown.booleanValue()) {
			return false;
		}

		if (currentFeatures == null) {
			LOGGER.warn("Can't retrieve current features, failing features_enabled resource condition check.");
			return false;
		}

		return set.isSubsetOf(currentFeatures);
	}

	public static boolean registryContains(@Nullable RegistryWrapper.WrapperLookup registryLookup, Identifier registryId, List<Identifier> entries) {
		RegistryKey<? extends Registry<Object>> registryKey = RegistryKey.ofRegistry(registryId);

		if (registryLookup == null) {
			LOGGER.warn("Can't retrieve registry {}, failing registry_contains resource condition check", registryId);
			return false;
		}

		Optional<RegistryWrapper.Impl<Object>> wrapper = registryLookup.getOptionalWrapper(registryKey);

		if (wrapper.isPresent()) {
			for (Identifier id : entries) {
				if (wrapper.get().getOptional(RegistryKey.of(registryKey, id)).isEmpty()) {
					return false;
				}
			}

			return true;
		} else {
			return entries.isEmpty();
		}
	}
}
