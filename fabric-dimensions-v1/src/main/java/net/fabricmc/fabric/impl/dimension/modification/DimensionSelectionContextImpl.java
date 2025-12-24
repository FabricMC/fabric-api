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
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;

import net.fabricmc.fabric.api.dimension.v1.DimensionSelectionContext;

public class DimensionSelectionContextImpl implements DimensionSelectionContext {
	private final RegistryAccess dynamicRegistries;
	private final ResourceKey<DimensionType> key;
	private final DimensionType dimension;
	private final Holder<DimensionType> entry;

	public DimensionSelectionContextImpl(RegistryAccess dynamicRegistries, ResourceKey<DimensionType> key, DimensionType dimension) {
		this.dynamicRegistries = dynamicRegistries;
		this.key = key;
		this.dimension = dimension;
		this.entry = dynamicRegistries.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(this.key);
	}

	@Override
	public RegistryAccess getDynamicRegistries() {
		return dynamicRegistries;
	}

	@Override
	public ResourceKey<DimensionType> getDimensionKey() {
		return key;
	}

	@Override
	public DimensionType getDimension() {
		return dimension;
	}

	@Override
	public Holder<DimensionType> getDimensionRegistryEntry() {
		return entry;
	}

	@Override
	public boolean hasTag(TagKey<DimensionType> tag) {
		Registry<DimensionType> dimensionRegistry = dynamicRegistries.lookupOrThrow(Registries.DIMENSION_TYPE);
		return dimensionRegistry.getOrThrow(getDimensionKey()).is(tag);
	}
}
