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

package net.fabricmc.fabric.impl.registry.sync.trackers;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.impl.registry.sync.RemovableIdList;

public class IdListTracker<V, OV> implements RegistryEntryAddedCallback<V>, RegistryIdRemapCallback<V> {
	private final String name;
	private final IdMapper<OV> mappers;
	private Map<Identifier, OV> removedMapperCache = new HashMap<>();

	private IdListTracker(String name, IdMapper<OV> mappers) {
		this.name = name;
		this.mappers = mappers;
	}

	public static <V, OV> void register(Registry<V> registry, String name, IdMapper<OV> mappers) {
		IdListTracker<V, OV> updater = new IdListTracker<>(name, mappers);
		RegistryEntryAddedCallback.event(registry).register(updater);
		RegistryIdRemapCallback.event(registry).register(updater);
	}

	@Override
	public void onEntryAdded(int rawId, Identifier id, V object) {
		if (removedMapperCache.containsKey(id)) {
			mappers.addMapping(removedMapperCache.get(id), rawId);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onRemap(RemapState<V> state) {
		((RemovableIdList<OV>) mappers).fabric_remapIds(state.getRawIdChangeMap());
	}
}
