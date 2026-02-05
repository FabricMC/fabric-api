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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;

public class Int2ObjectMapTracker<V, OV> implements RegistryEntryAddedCallback<V>, RegistryIdRemapCallback<V> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Int2ObjectMapTracker.class);
	private final String name;
	private final Int2ObjectMap<OV> mappers;
	private Map<Identifier, OV> removedMapperCache = new HashMap<>();

	private Int2ObjectMapTracker(String name, Int2ObjectMap<OV> mappers) {
		this.name = name;
		this.mappers = mappers;
	}

	public static <V, OV> void register(Registry<V> registry, String name, Int2ObjectMap<OV> mappers) {
		Int2ObjectMapTracker<V, OV> updater = new Int2ObjectMapTracker<>(name, mappers);
		RegistryEntryAddedCallback.event(registry).register(updater);
		RegistryIdRemapCallback.event(registry).register(updater);
	}

	@Override
	public void onEntryAdded(int rawId, Identifier id, V object) {
		if (removedMapperCache.containsKey(id)) {
			mappers.put(rawId, removedMapperCache.get(id));
		}
	}

	@Override
	public void onRemap(RemapState<V> state) {
		Int2ObjectMap<OV> oldMappers = new Int2ObjectOpenHashMap<>(mappers);
		Int2IntMap remapMap = state.getRawIdChangeMap();
		List<String> errors = null;

		mappers.clear();

		for (int i : oldMappers.keySet()) {
			int newI = remapMap.getOrDefault(i, Integer.MIN_VALUE);

			if (newI >= 0) {
				if (mappers.containsKey(newI)) {
					if (errors == null) {
						errors = new ArrayList<>();
					}

					errors.add(" - Map contained two equal IDs " + newI + " (" + state.getIdFromOld(i) + "/" + i + " -> " + state.getIdFromNew(newI) + "/" + newI + ")!");
				} else {
					mappers.put(newI, oldMappers.get(i));
				}
			} else {
				LOGGER.warn("[fabric-registry-sync] Int2ObjectMap " + name + " is dropping mapping for integer ID " + i + " (" + state.getIdFromOld(i) + ") - should not happen!");
				removedMapperCache.put(state.getIdFromOld(i), oldMappers.get(i));
			}
		}

		if (errors != null) {
			throw new RuntimeException("Errors while remapping Int2ObjectMap " + name + " found:\n" + Joiner.on('\n').join(errors));
		}
	}
}
