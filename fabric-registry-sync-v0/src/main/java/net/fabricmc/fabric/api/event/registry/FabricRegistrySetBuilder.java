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

package net.fabricmc.fabric.api.event.registry;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;

public interface FabricRegistrySetBuilder {
	private RegistrySetBuilder self() {
		return (RegistrySetBuilder) this;
	}

	@SuppressWarnings("unchecked")
	private void mergeToMap(Map<ResourceKey<?>, Pair<Lifecycle, RegistrySetBuilder.RegistryBootstrap<?>>> map, ResourceKey<?> key, Lifecycle lifecycle, RegistrySetBuilder.RegistryBootstrap<?> bootstrap) {
		Pair<Lifecycle, RegistrySetBuilder.RegistryBootstrap<?>> current = map.get(key);

		if (current == null) {
			map.put(key, Pair.of(lifecycle, bootstrap));
			return;
		}

		map.put(key, Pair.of(current.getFirst().add(lifecycle), registry -> {
			((RegistrySetBuilder.RegistryBootstrap<Object>) current.getSecond()).run(registry);
			((RegistrySetBuilder.RegistryBootstrap<Object>) bootstrap).run(registry);
		}));
	}

	/// Adds all bootstraps from others to this and applies the most severe lifecycle to this.
	/// @param others {@link RegistrySetBuilder}s to source bootstrap functions from
	/// @return this builder
	@SuppressWarnings("unchecked")
	default RegistrySetBuilder withBootstrapsFrom(RegistrySetBuilder... others) {
		List<RegistrySetBuilder.RegistryStub<?>> entries = List.copyOf(self().entries);
		self().entries.clear();
		Map<ResourceKey<?>, Pair<Lifecycle, RegistrySetBuilder.RegistryBootstrap<?>>> combinedEntries = new IdentityHashMap<>();

		for (RegistrySetBuilder.RegistryStub<?> entry : entries) {
			mergeToMap(combinedEntries, entry.key(), entry.lifecycle(), entry.bootstrap());
		}

		for (RegistrySetBuilder other : others) {
			for (RegistrySetBuilder.RegistryStub<?> entry : other.entries) {
				mergeToMap(combinedEntries, entry.key(), entry.lifecycle(), entry.bootstrap());
			}
		}

		for (Map.Entry<ResourceKey<?>, Pair<Lifecycle, RegistrySetBuilder.RegistryBootstrap<?>>> entry : combinedEntries.entrySet()) {
			self().add(
					(ResourceKey<? extends Registry<Object>>) entry.getKey(),
					entry.getValue().getFirst(),
					(RegistrySetBuilder.RegistryBootstrap<Object>) entry.getValue().getSecond()
			);
		}

		return self();
	}

	/// Adds all bootstraps from {@code VanillaRegistries.BUILDER} to this and applies the most severe lifecycle to this.
	/// @return this builder
	default RegistrySetBuilder withVanillaBootstraps() {
		return withBootstrapsFrom(VanillaRegistries.BUILDER);
	}
}
