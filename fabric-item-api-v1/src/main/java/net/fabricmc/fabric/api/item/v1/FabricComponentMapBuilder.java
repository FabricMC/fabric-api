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

package net.fabricmc.fabric.api.item.v1;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.component.ComponentType;

/**
 * Fabric-provided extensions for {@link net.minecraft.component.ComponentMap.Builder}.
 *
 * <p>Note: This interface is automatically implemented on all component map builders via Mixin and interface injection.
 */
@ApiStatus.NonExtendable
public interface FabricComponentMapBuilder {
	/**
	 * Gets the current value for the component type in the builder, or creates and adds a new value if it is not present.
	 *
	 * @param type     The component type
	 * @param fallback The supplier for the default data value if the type is not in this map yet. The value given by this supplier
	 *                 may not be null.
	 * @param <T>      The type of the component data
	 * @return Returns the current value in the map builder, or the default value provided by the fallback if not present
	 * @see #getOrEmpty(ComponentType)
	 */
	default <T> T getOrCreate(ComponentType<T> type, Supplier<@NotNull T> fallback) {
		throw new AssertionError("Implemented in Mixin");
	}

	/**
	 * Gets the current value for the component type in the builder, or creates and adds a new value if it is not present.
	 *
	 * @param type         The component type
	 * @param defaultValue The default data value if the type is not in this map yet
	 * @param <T>          The type of the component data
	 * @return Returns the current value in the map builder, or the default value if not present
	 */
	default <T> T getOrDefault(ComponentType<T> type, @NotNull T defaultValue) {
		Objects.requireNonNull(defaultValue, "Cannot insert null values to component map builder");
		return getOrCreate(type, () -> defaultValue);
	}

	/**
	 * For list component types specifically, returns a mutable list of values currently held in the builder for the given
	 * component type. If the type is not registered to this builder yet, this will create and add a new empty list to the builder
	 * for the type, and return that.
	 *
	 * @param type The component type. The component must be a list-type.
	 * @param <T>  The type of the component entry data
	 * @return Returns a mutable list of values for the type.
	 */
	default <T> List<T> getOrEmpty(ComponentType<List<T>> type) {
		throw new AssertionError("Implemented in Mixin");
	}
}
