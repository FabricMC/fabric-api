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

import java.util.function.Consumer;

import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.registry.sync.ListenableRegistry;

/**
 * An event for when an entry is added to a registry.
 *
 * @param <T> the type of the entry within the registry
 */
@FunctionalInterface
public interface RegistryEntryAddedCallback<T> {
	/**
	 * Called when a new entry is added to the registry.
	 *
	 * @param rawId the raw id of the entry
	 * @param id the identifier of the entry
	 * @param object the object that was added
	 */
	void onEntryAdded(int rawId, Identifier id, T object);

	/**
	 * Get the {@link Event} for the {@link RegistryEntryAddedCallback} for the given registry.
	 *
	 * @param registry the registry to get the event for
	 * @return the event
	 */
	static <T> Event<RegistryEntryAddedCallback<T>> event(Registry<T> registry) {
		return ListenableRegistry.get(registry).fabric_getAddObjectEvent();
	}

	/**
	 * Register a callback for all present and future entries in the registry.
	 *
	 * <p>Note: The callback is recursive and will be invoked for anything registered within the callback itself.
	 *
	 * @param registry the registry to listen to
	 * @param consumer the callback that accepts a {@link RegistryEntry.Reference}
	 */
	static <T> void allEntries(Registry<T> registry, Consumer<RegistryEntry.Reference<T>> consumer) {
		event(registry).register((rawId, id, object) -> consumer.accept(registry.getEntry(id).orElseThrow()));
		// Call the consumer for all existing entries, after registering the callback.
		// This way if the callback registers a new entry, it will also be called for that entry.
		// It is also important to take a copy of the registry with .toList() to avoid concurrent modification exceptions if the callback modifies the registry.
		registry.streamEntries().toList().forEach(consumer);
	}
}
