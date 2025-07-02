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

package net.fabricmc.fabric.api.event.registry.entrylists;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntryList;

import net.fabricmc.fabric.impl.registry.sync.entrylists.defaults.DefaultCustomRegistryEntryListsImpl;

/**
 * convenience implementations for {@link CustomRegistryEntryList}.
 */
public class DefaultCustomRegistryEntryLists {
	/**
	 * creates a {@link RegistryEntryList} that contains anything contained by any element of {@code parts}.
	 *
	 * @param parts the component parts to OR
	 * @param <T>   the type of elements contained in all the lists
	 * @return the custom list
	 */
	@SafeVarargs
	public static <T> RegistryEntryList<T> union(RegistryEntryList<T>... parts) {
		return DefaultCustomRegistryEntryListsImpl.union(parts);
	}

	/**
	 * creates a {@link RegistryEntryList} that contains anything contained by all element of {@code parts}.
	 *
	 * @param parts the component parts to AND
	 * @param <T>   the type of elements contained in all the lists
	 * @return the custom list
	 */
	@SafeVarargs
	public static <T> RegistryEntryList<T> intersection(RegistryEntryList<T>... parts) {
		return DefaultCustomRegistryEntryListsImpl.intersection(parts);
	}

	/**
	 * creates a {@link RegistryEntryList} that contains anything not contained by its opposite.
	 *
	 * @param lookup   the source registry for this list
	 * @param opposite the list that contains everything the returned list will not
	 * @param <T>      the type of elements contained in all the lists
	 * @return the custom list
	 * @implNote {@link net.minecraft.item.Items#AIR} and {@link net.minecraft.block.Blocks#AIR} are included in the returned list, provided that they are not in opposite. for certain use cases, such as {@link net.minecraft.recipe.Ingredient}s, the caller must ensure that air is not in the end result, or else the ingredient will fail to serialize
	 */
	public static <T> RegistryEntryList<T> inverse(RegistryEntryLookup<T> lookup, RegistryEntryList<T> opposite) {
		return DefaultCustomRegistryEntryListsImpl.inverse(lookup, opposite);
	}

	/**
	 * creates a {@link RegistryEntryList} that contains everything in the source lookup.
	 *
	 * @param lookup   the source registry for this list
	 * @param <T>      the type of elements contained in all the lists
	 * @return the custom list
	 * @implNote {@link net.minecraft.item.Items#AIR} and {@link net.minecraft.block.Blocks#AIR} are included in the returned list. for certain use cases, such as {@link net.minecraft.recipe.Ingredient}s, the caller must ensure that air is not in the end result, or else the ingredient will fail to serialize
	 */
	public static <T> RegistryEntryList<T> universal(RegistryEntryLookup<T> lookup) {
		return DefaultCustomRegistryEntryListsImpl.universal(lookup);
	}

	/**
	 * creates a {@link RegistryEntryList} that contains everything in {@code initial} that is not also in {@code subtracted}.
	 *
	 * @param lookup   the source registry for this list
	 * @param initial the beginning list
	 * @param subtracted the list to subtract from initial
	 * @param <T>      the type of elements contained in all the lists
	 * @return the custom list
	 */
	public static <T> RegistryEntryList<T> subtraction(RegistryEntryLookup<T> lookup, RegistryEntryList<T> initial, RegistryEntryList<T> subtracted) {
		return DefaultCustomRegistryEntryListsImpl.subtraction(lookup, initial, subtracted);
	}
}
