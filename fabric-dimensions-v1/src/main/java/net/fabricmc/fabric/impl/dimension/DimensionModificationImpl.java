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

package net.fabricmc.fabric.impl.dimension;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.dimension.DimensionType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.dimension.v1.DimensionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.mixin.dimension.DimensionTypeAccessor;
import net.fabricmc.fabric.mixin.dimension.MappedRegistryAccessor;

public class DimensionModificationImpl implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> DimensionModificationImpl.finalizeWorldGen(server.registryAccess()));
	}

	public static void finalizeWorldGen(RegistryAccess registries) {
		// Now that we apply dimension modifications when the server is starting, we should only ever do this once for a
		// dynamic registry manager. Marking the dynamic registry manager as modified ensures a crash if the
		// precondition is violated.
		DimensionModificationMarker modificationTracker = (DimensionModificationMarker) registries;
		modificationTracker.fabric_markDimensionsModified();

		Registry<DimensionType> dimensions = registries.lookupOrThrow(Registries.DIMENSION_TYPE);

		// Build a list of all dimension keys in ascending order of their raw-id to get a consistent result.
		List<ResourceKey<DimensionType>> keys = dimensions.entrySet().stream()
				.map(Map.Entry::getKey)
				.sorted(Comparator.comparingInt(key -> dimensions.getId(dimensions.getValueOrThrow(key))))
				.toList();

		for (ResourceKey<DimensionType> key : keys) {
			Holder.Reference<DimensionType> reference = dimensions.getOrThrow(key);

			if (applyChanges(reference, registries)) {
				// Re-freeze and apply certain cleanup actions
				if (dimensions instanceof MappedRegistry<DimensionType> registry) {
					Map<ResourceKey<DimensionType>, RegistrationInfo> registrationInfos = ((MappedRegistryAccessor<DimensionType>) registry).fabric_getRegistrationInfos();
					RegistrationInfo info = registrationInfos.get(key);
					RegistrationInfo newInfo = new RegistrationInfo(Optional.empty(), info.lifecycle());
					registrationInfos.put(key, newInfo);
				}
			}
		}
	}

	/**
	 * Applies the changes from the events of {@link DimensionEvents} to a dimension.
	 *
	 * @return true if the dimension was changed
	 */
	private static boolean applyChanges(Holder<DimensionType> dimension, RegistryAccess registries) {
		EnvironmentAttributeMap oldAttributes = dimension.value().attributes();
		EnvironmentAttributeMap.Builder attributeBuilder = EnvironmentAttributeMap.builder().putAll(oldAttributes);
		DimensionEvents.MODIFY_ATTRIBUTES.invoker().modifyDimensionAttributes(dimension, attributeBuilder, registries);
		EnvironmentAttributeMap newAttributes = attributeBuilder.build();
		boolean changed = !oldAttributes.equals(newAttributes);

		if (changed) {
			((DimensionTypeAccessor) (Object) dimension.value()).fabric_setAttributes(newAttributes);
		}

		return changed;
	}
}
