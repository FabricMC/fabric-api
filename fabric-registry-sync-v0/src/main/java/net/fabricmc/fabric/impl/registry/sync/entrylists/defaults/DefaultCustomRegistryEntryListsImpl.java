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

package net.fabricmc.fabric.impl.registry.sync.entrylists.defaults;

import java.util.List;
import java.util.Objects;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntryList;

import net.fabricmc.fabric.impl.registry.sync.entrylists.CustomRegistryEntryListSerializerImpl;
import net.fabricmc.fabric.impl.registry.sync.entrylists.util.RegistryWrapperUtils;

public class DefaultCustomRegistryEntryListsImpl {
	private static boolean initialized = false;

	public static void register() {
		if (initialized) {
			return;
		}

		initialized = true;
		CustomRegistryEntryListSerializerImpl.registerSerializer(UnionRegistryEntryList.SERIALIZER);
		CustomRegistryEntryListSerializerImpl.registerSerializer(IntersectionRegistryEntryList.SERIALIZER);
		CustomRegistryEntryListSerializerImpl.registerSerializer(InverseRegistryEntryList.SERIALIZER);
		CustomRegistryEntryListSerializerImpl.registerSerializer(UniversalRegistryEntryList.SERIALIZER);
	}

	public static <T> RegistryEntryList<T> union(RegistryEntryList<T>[] parts) {
		return new UnionRegistryEntryList<>(List.of(validateNotNull(parts)));
	}

	public static <T> RegistryEntryList<T> intersection(RegistryEntryList<T>[] parts) {
		return new IntersectionRegistryEntryList<>(List.of(validateNotNull(parts)));
	}

	public static <T> RegistryEntryList<T> inverse(RegistryEntryLookup<T> lookup, RegistryEntryList<T> opposite) {
		Objects.requireNonNull(lookup, "lookup");
		Objects.requireNonNull(opposite, "opposite");
		return new InverseRegistryEntryList<>(RegistryWrapperUtils.castOrCreateFromEntryLookup(lookup), opposite);
	}

	public static <T> RegistryEntryList<T> universal(RegistryEntryLookup<T> lookup) {
		Objects.requireNonNull(lookup, "lookup");
		return new UniversalRegistryEntryList<>(RegistryWrapperUtils.castOrCreateFromEntryLookup(lookup));
	}

	public static <T> RegistryEntryList<T> subtraction(RegistryEntryLookup<T> lookup, RegistryEntryList<T> initial, RegistryEntryList<T> subtracted) {
		Objects.requireNonNull(lookup, "lookup");
		Objects.requireNonNull(initial, "initial");
		Objects.requireNonNull(subtracted, "subtracted");
		return new IntersectionRegistryEntryList<>(List.of(initial, inverse(lookup, subtracted)));
	}

	private static <T> RegistryEntryList<T>[] validateNotNull(RegistryEntryList<T>[] lists) {
		Objects.requireNonNull(lists, "list array");

		for (RegistryEntryList<T> list : lists) {
			Objects.requireNonNull(list, "list");
		}

		return lists;
	}
}
