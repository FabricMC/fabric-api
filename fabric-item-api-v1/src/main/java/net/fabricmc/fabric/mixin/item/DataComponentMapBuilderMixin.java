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

package net.fabricmc.fabric.mixin.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;

import net.fabricmc.fabric.api.item.v1.FabricComponentMapBuilder;

@Mixin(DataComponentMap.Builder.class)
abstract class DataComponentMapBuilderMixin implements FabricComponentMapBuilder {
	@Shadow
	@Final
	private Reference2ObjectMap<DataComponentType<?>, Object> map;

	@Shadow
	public abstract <T> DataComponentMap.Builder set(DataComponentType<T> dataComponentType, @Nullable T object);

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getOrCreate(DataComponentType<T> type, Supplier<T> fallback) {
		if (!this.map.containsKey(type)) {
			T defaultValue = fallback.get();
			Objects.requireNonNull(defaultValue, "Cannot insert null values to component map builder");
			this.set(type, defaultValue);
		}

		return (T) this.map.get(type);
	}

	@Override
	public <T> List<T> getOrEmpty(DataComponentType<List<T>> type) {
		// creating a new array list guarantees that the list in the map is mutable
		List<T> existing = new ArrayList<>(this.getOrCreate(type, Collections::emptyList));
		this.set(type, existing);
		return existing;
	}

	@Override
	public boolean contains(DataComponentType<?> type) {
		return this.map.containsKey(type);
	}
}
