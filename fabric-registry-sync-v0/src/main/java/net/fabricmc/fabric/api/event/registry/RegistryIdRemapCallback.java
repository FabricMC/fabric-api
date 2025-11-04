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

import it.unimi.dsi.fastutil.ints.Int2IntMap;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.registry.sync.ListenableRegistry;

/**
 * The remapping process functions as follows:
 *
 * <ul><li>RegistryEntryRemovedCallbacks are called to remove any objects culled in the process, with the old numeric ID.
 * <li>RegistryIdRemapCallback is emitted to allow remapping the IDs of objects still present.
 * <li>RegistryEntryAddedCallbacks are called to add any objects added in the process, with the new numeric ID.</ul>
 *
 * <p>RegistryIdRemapCallback is called on every remapping operation, if you want to do your own processing in one swoop
 * (say, rebuild the ID map from scratch).
 *
 * <p>Generally speaking, a remap can only cause object *removals*; object *additions* are necessary to reverse remaps.
 *
 * @param <T> The registry type.
 */
@FunctionalInterface
public interface RegistryIdRemapCallback<T> {
	void onRemap(RemapState<T> state);

	interface RemapState<T> {
		Int2IntMap getRawIdChangeMap();
		Identifier getIdFromOld(int oldRawId);
		Identifier getIdFromNew(int newRawId);
	}

	static <T> Event<RegistryIdRemapCallback<T>> event(Registry<T> registry) {
		return ListenableRegistry.get(registry).fabric_getRemapEvent();
	}
}
