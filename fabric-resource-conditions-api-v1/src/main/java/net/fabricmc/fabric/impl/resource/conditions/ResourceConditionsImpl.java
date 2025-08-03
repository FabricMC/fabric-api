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
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.tag.TagKey;
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
		ResourceConditions.register(DefaultResourceConditionTypes.NOT);
		ResourceConditions.register(DefaultResourceConditionTypes.AND);
		ResourceConditions.register(DefaultResourceConditionTypes.OR);
		ResourceConditions.register(DefaultResourceConditionTypes.ALL_MODS_LOADED);
		ResourceConditions.register(DefaultResourceConditionTypes.ANY_MODS_LOADED);
		ResourceConditions.register(DefaultResourceConditionTypes.TAGS_POPULATED);
		ResourceConditions.register(DefaultResourceConditionTypes.FEATURES_ENABLED);
		ResourceConditions.register(DefaultResourceConditionTypes.REGISTRY_CONTAINS);
	}

	public static boolean applyResourceConditions(JsonObject obj, String dataType, Identifier key, @Nullable RegistryOps.RegistryInfoGetter registryInfo) {
		boolean debugLogEnabled = ResourceConditionsImpl.LOGGER.isDebugEnabled();

		if (obj.has(ResourceConditions.CONDITIONS_KEY)) {
			DataResult<ResourceCondition> conditions = ResourceCondition.CONDITION_CODEC.parse(JsonOps.INSTANCE, obj.get(ResourceConditions.CONDITIONS_KEY));

			if (conditions.isSuccess()) {
				boolean matched = conditions.getOrThrow().test(registryInfo);

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

	public static boolean conditionsMet(List<ResourceCondition> conditions, @Nullable RegistryOps.RegistryInfoGetter registryInfo, boolean and) {
		for (ResourceCondition condition : conditions) {
			if (condition.test(registryInfo) != and) {
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

	public static boolean tagsPopulated(@Nullable RegistryOps.RegistryInfoGetter infoGetter, Identifier registryId, List<Identifier> tags) {
		if (infoGetter == null) {
			LOGGER.warn("Can't retrieve registry {}, failing tags_populated resource condition check", registryId);
			return false;
		}

		RegistryKey<? extends Registry<Object>> registryKey = RegistryKey.ofRegistry(registryId);
		Optional<RegistryOps.RegistryInfo<Object>> optionalInfo = infoGetter.getRegistryInfo(registryKey);

		if (optionalInfo.isPresent()) {
			RegistryEntryLookup<Object> lookup = optionalInfo.get().entryLookup();

			for (Identifier id : tags) {
				if (lookup.getOptional(TagKey.of(registryKey, id)).isEmpty()) {
					return false;
				}
			}

			return true;
		} else {
			return tags.isEmpty();
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

	public static boolean registryContains(@Nullable RegistryOps.RegistryInfoGetter infoGetter, Identifier registryId, List<Identifier> entries) {
		if (infoGetter == null) {
			LOGGER.warn("Can't retrieve registry {}, failing registry_contains resource condition check", registryId);
			return false;
		}

		RegistryKey<? extends Registry<Object>> registryKey = RegistryKey.ofRegistry(registryId);
		Optional<RegistryOps.RegistryInfo<Object>> optionalInfo = infoGetter.getRegistryInfo(registryKey);

		if (optionalInfo.isPresent()) {
			RegistryEntryLookup<Object> lookup = optionalInfo.get().entryLookup();

			for (Identifier id : entries) {
				if (lookup.getOptional(RegistryKey.of(registryKey, id)).isEmpty()) {
					return false;
				}
			}

			return true;
		} else {
			return entries.isEmpty();
		}
	}
}
