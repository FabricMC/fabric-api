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

import java.util.Map;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;

public final class ApiProviderHashMap<K, V> implements ApiProviderMap<K, V> {
	private volatile Map<K, V> lookups = new Reference2ReferenceOpenHashMap<>();

	@Nullable
	@Override
	public V get(K key) {
		Objects.requireNonNull(key, "Key may not be null.");

		return lookups.get(key);
	}

	@Override
	public synchronized V putIfAbsent(K key, V provider) {
		Objects.requireNonNull(key, "Key may not be null.");
		Objects.requireNonNull(provider, "Provider may not be null.");

		// We use a copy-on-write strategy to allow any number of reads to concur with a write
		Map<K, V> lookupsCopy = new Reference2ReferenceOpenHashMap<>(lookups);
		V result = lookupsCopy.putIfAbsent(key, provider);
		lookups = lookupsCopy;

		return result;
	}
}
