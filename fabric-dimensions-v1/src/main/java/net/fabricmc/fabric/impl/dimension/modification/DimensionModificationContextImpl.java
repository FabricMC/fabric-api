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

package net.fabricmc.fabric.impl.dimension.modification;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.level.dimension.DimensionType;

import net.fabricmc.fabric.api.dimension.v1.DimensionModificationContext;

public class DimensionModificationContextImpl implements DimensionModificationContext {
	private final DimensionType dimensionType;
	private final AttributesContext attributes;

	public DimensionModificationContextImpl(DimensionType dimensionType) {
		this.dimensionType = dimensionType;
		this.attributes = new AttributesContextImpl();
	}

	@Override
	public AttributesContext getAttributes() {
		return attributes;
	}

	private class AttributesContextImpl implements AttributesContext {
		@Override
		public void addAll(EnvironmentAttributeMap map) {
			EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(dimensionType.attributes());
			attributes.putAll(map);
			dimensionType.attributes = attributes.build();
		}

		@Override
		public <T> void set(EnvironmentAttribute<T> key, T value) {
			EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(dimensionType.attributes());
			attributes.set(key, value);
			dimensionType.attributes = attributes.build();
		}

		@Override
		public <T, M> void setModifier(EnvironmentAttribute<T> key, AttributeModifier<T, M> modifier, M value) {
			EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(dimensionType.attributes());
			attributes.modify(key, modifier, value);
			dimensionType.attributes = attributes.build();
		}
	}

	/**
	 * Gets an entry from the given registry, assuming it's a registry loaded from data packs.
	 * Gives more helpful error messages if an entry is missing by checking if the modder
	 * forgot to data-gen the JSONs corresponding to their built-in objects.
	 */
	private static <T> Holder.Reference<T> getEntry(Registry<T> registry, ResourceKey<T> key) {
		Holder.Reference<T> entry = registry.get(key).orElse(null);

		if (entry == null) {
			// The key doesn't exist in the data packs
			throw new IllegalArgumentException("Couldn't find registry entry for " + key);
		}

		return entry;
	}
}
