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

package net.fabricmc.fabric.impl.registry.sync.entrylists.util;

import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.fabric.mixin.registry.sync.entrylists.RegistryKeyAccessor;

record RegistryWrapperFromRegistryEntryLookup<T>(RegistryEntryLookup<T> entryLookup) implements RegistryWrapper<T> {
	/**
	 * @param entryLookup the entry lookup backing this wrapper
	 * @see RegistryWrapperUtils#castOrCreateFromEntryLookup(RegistryEntryLookup)
	 */
	RegistryWrapperFromRegistryEntryLookup {
		if (entryLookup instanceof RegistryWrapper<T>) {
			throw new IllegalArgumentException("use RegistryWrapperFromRegistryEntryLookup#tryFromEntryLookup instead");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Stream<RegistryEntry.Reference<T>> streamEntries() {
		return RegistryKeyAccessor
				.getInstances()
				.values()
				.stream()
				.flatMap(key -> entryLookup.getOptional((RegistryKey<T>) key).stream());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Stream<RegistryEntryList.Named<T>> getTags() {
		return TagKeyCache.stream().flatMap(
				key -> entryLookup.getOptional((TagKey<T>) key).stream()
		);
	}

	@Override
	public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
		return entryLookup.getOptional(key);
	}

	@Override
	public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
		return entryLookup.getOptional(tag);
	}
}
