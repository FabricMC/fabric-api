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

import net.minecraft.core.Registry;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;

public interface ListenableRegistry<T> {
	Event<RegistryEntryAddedCallback<T>> fabric_getAddObjectEvent();
	Event<RegistryIdRemapCallback<T>> fabric_getRemapEvent();
	@SuppressWarnings("unchecked")
	static <T> ListenableRegistry<T> get(Registry<T> registry) {
		if (!(registry instanceof ListenableRegistry)) {
			throw new IllegalArgumentException("Unsupported registry: " + registry.key().identifier());
		}

		// Safe cast: this is implemented via Mixin and T will always match the T in Registry<T>
		return (ListenableRegistry<T>) registry;
	}
}
