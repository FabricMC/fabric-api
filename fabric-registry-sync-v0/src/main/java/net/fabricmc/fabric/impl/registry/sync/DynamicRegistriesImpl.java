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

package net.fabricmc.fabric.impl.registry.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryValidator;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class DynamicRegistriesImpl {
	private static final List<RegistryDataLoader.RegistryData<?>> WORLD_REGISTRIES;
	private static final List<RegistryDataLoader.RegistryData<?>> BOOTSTRAPPING_REGISTRIES = new ArrayList<>(RegistryDataLoader.WORLDGEN_REGISTRIES);

	private static final Set<ResourceKey<? extends Registry<?>>> VANILLA_DYNAMIC_REGISTRY_KEYS;
	public static final Set<ResourceKey<? extends Registry<?>>> FABRIC_DYNAMIC_REGISTRY_KEYS = new HashSet<>();

	public static final Set<ResourceKey<? extends Registry<?>>> SKIP_EMPTY_SYNC_REGISTRIES = new HashSet<>();

	static {
		WORLD_REGISTRIES = new ArrayList<>(RegistryDataLoader.WORLDGEN_REGISTRIES);
		WORLD_REGISTRIES.addAll(RegistryDataLoader.DIMENSION_REGISTRIES);
		Set<ResourceKey<? extends Registry<?>>> vanillaDynamicRegistryKeys = new HashSet<>();

		for (RegistryDataLoader.RegistryData<?> worldgenEntry : RegistryDataLoader.WORLDGEN_REGISTRIES) {
			vanillaDynamicRegistryKeys.add(worldgenEntry.key());
		}

		for (RegistryDataLoader.RegistryData<?> dimensionEntry : RegistryDataLoader.DIMENSION_REGISTRIES) {
			vanillaDynamicRegistryKeys.add(dimensionEntry.key());
		}

		VANILLA_DYNAMIC_REGISTRY_KEYS = Collections.unmodifiableSet(vanillaDynamicRegistryKeys);
	}

	private DynamicRegistriesImpl() {
	}

	public static @Unmodifiable List<RegistryDataLoader.RegistryData<?>> getWorldRegistries() {
		return List.copyOf(WORLD_REGISTRIES);
	}

	public static @Unmodifiable List<RegistryDataLoader.RegistryData<?>> getBootstrappingRegistries() {
		return List.copyOf(BOOTSTRAPPING_REGISTRIES);
	}

	private static void addDynamicRegistryData(ResourceKey<? extends Registry<?>> key, RegistryDataLoader.RegistryData<?> data) {
		FABRIC_DYNAMIC_REGISTRY_KEYS.add(key);
		BOOTSTRAPPING_REGISTRIES.add(data);
		WORLD_REGISTRIES.add(data);
	}

	public static <T> RegistryDataLoader.RegistryData<T> register(ResourceKey<? extends Registry<T>> key, Codec<T> serverCodec) {
		Objects.requireNonNull(key, "Registry key cannot be null");
		Objects.requireNonNull(serverCodec, "Server codec cannot be null");

		if (VANILLA_DYNAMIC_REGISTRY_KEYS.contains(key) || FABRIC_DYNAMIC_REGISTRY_KEYS.contains(key)) {
			throw new IllegalArgumentException("Dynamic registry " + key + " has already been registered!");
		}

		var entry = new RegistryDataLoader.RegistryData<>(key, serverCodec, RegistryValidator.none());
		addDynamicRegistryData(key, entry);
		return entry;
	}

	public static <T> void addSyncedRegistry(ResourceKey<? extends Registry<T>> key, Codec<T> clientCodec, DynamicRegistries.SyncOption... options) {
		Objects.requireNonNull(key, "Registry key cannot be null");
		Objects.requireNonNull(clientCodec, "Client codec cannot be null");
		Objects.requireNonNull(options, "Options cannot be null");

		if (!(RegistryDataLoader.SYNCHRONIZED_REGISTRIES instanceof ArrayList<RegistryDataLoader.RegistryData<?>>)) {
			RegistryDataLoader.SYNCHRONIZED_REGISTRIES = new ArrayList<>(RegistryDataLoader.SYNCHRONIZED_REGISTRIES);
		}

		RegistryDataLoader.SYNCHRONIZED_REGISTRIES.add(new RegistryDataLoader.RegistryData<>(key, clientCodec, RegistryValidator.none()));

		if (!(RegistrySynchronization.NETWORKABLE_REGISTRIES instanceof HashSet<ResourceKey<? extends Registry<?>>>)) {
			RegistrySynchronization.NETWORKABLE_REGISTRIES = new HashSet<>(RegistrySynchronization.NETWORKABLE_REGISTRIES);
		}

		RegistrySynchronization.NETWORKABLE_REGISTRIES.add(key);

		for (DynamicRegistries.SyncOption option : options) {
			if (option == DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY) {
				SKIP_EMPTY_SYNC_REGISTRIES.add(key);
			}
		}
	}
}
