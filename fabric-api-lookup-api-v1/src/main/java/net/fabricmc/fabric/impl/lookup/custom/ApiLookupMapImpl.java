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

package net.fabricmc.fabric.impl.lookup.custom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap;

public final class ApiLookupMapImpl<L> implements ApiLookupMap<L> {
	private final Map<Identifier, StoredLookup<L>> lookups = new HashMap<>();
	private final LookupConstructor<L> lookupConstructor;

	public ApiLookupMapImpl(LookupConstructor<L> lookupConstructor) {
		this.lookupConstructor = lookupConstructor;
	}

	@Override
	public synchronized L getLookup(Identifier lookupId, Class<?> apiClass, Class<?> contextClass) {
		Objects.requireNonNull(lookupId, "Lookup Identifier may not be null.");
		Objects.requireNonNull(apiClass, "API class may not be null.");
		Objects.requireNonNull(contextClass, "Context class may not be null.");

		StoredLookup<L> storedLookup = lookups.computeIfAbsent(lookupId, id -> new StoredLookup<>(lookupConstructor.get(id, apiClass, contextClass), apiClass, contextClass));

		if (storedLookup.apiClass == apiClass && storedLookup.contextClass == contextClass) {
			return storedLookup.lookup;
		}

		String errorMessage = String.format(
				"Lookup with id %s is already registered with api class %s and context class %s. It can't be registered with api class %s and context class %s.",
				lookupId,
				storedLookup.apiClass.getCanonicalName(),
				storedLookup.contextClass.getCanonicalName(),
				apiClass.getCanonicalName(),
				contextClass.getCanonicalName()
		);

		throw new IllegalArgumentException(errorMessage);
	}

	@Override
	public synchronized Iterator<L> iterator() {
		return lookups.values().stream().map(storedLookup -> storedLookup.lookup).collect(Collectors.toList()).iterator();
	}

	private static final class StoredLookup<L> {
		final L lookup;
		final Class<?> apiClass;
		final Class<?> contextClass;

		StoredLookup(L lookup, Class<?> apiClass, Class<?> contextClass) {
			this.lookup = lookup;
			this.apiClass = apiClass;
			this.contextClass = contextClass;
		}
	}
}
