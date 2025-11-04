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

import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

/**
 * A view providing access to the registries that are currently being loaded. This is passed to
 * the {@link DynamicRegistrySetupCallback} event.
 *
 * @apiNote This might not contain all the registry, as the event is invoked for each layer of
 * the combined registry manager, and each layer holds different registries. For example, the biome
 * registry is not loaded in the {@link net.minecraft.server.RegistryLayer#DIMENSIONS}
 * layer.
 */
@ApiStatus.NonExtendable
public interface DynamicRegistryView {
	/**
	 * @return an {@link RegistryAccess} instance representing the registry view
	 */
	RegistryAccess asDynamicRegistryManager();

	/**
	 * @return the stream of registries that are currently being loaded
	 */
	Stream<Registry<?>> stream();

	/**
	 * Returns the registry identified by the registry key. This returns an empty optional if
	 * the key does not refer to a registry, or if the current combined registry layer being loaded
	 * does not contain the registry.
	 *
	 * @param registryRef the registry key of the registry to get
	 * @return the registry, or {@link Optional#empty()} if the registry is not currently being loaded
	 */
	<T> Optional<Registry<T>> getOptional(ResourceKey<? extends Registry<? extends T>> registryRef);

	/**
	 * A shortcut to register {@link RegistryEntryAddedCallback}.
	 * @param registryRef the registry key of the registry to register the event to
	 * @param callback the callback of the event
	 */
	<T> void registerEntryAdded(ResourceKey<? extends Registry<? extends T>> registryRef, RegistryEntryAddedCallback<T> callback);
}
