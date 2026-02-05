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

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;

public class RemapStateImpl<T> implements RegistryIdRemapCallback.RemapState<T> {
	private final Int2IntMap rawIdChangeMap;
	private final Int2ObjectMap<Identifier> oldIdMap;
	private final Int2ObjectMap<Identifier> newIdMap;

	public RemapStateImpl(Registry<T> registry, Int2ObjectMap<Identifier> oldIdMap, Int2IntMap rawIdChangeMap) {
		this.rawIdChangeMap = rawIdChangeMap;
		this.oldIdMap = oldIdMap;
		this.newIdMap = new Int2ObjectOpenHashMap<>();

		for (Int2IntMap.Entry entry : rawIdChangeMap.int2IntEntrySet()) {
			Identifier id = registry.getKey(registry.byId(entry.getIntValue()));
			newIdMap.put(entry.getIntValue(), id);
		}
	}

	@Override
	public Int2IntMap getRawIdChangeMap() {
		return rawIdChangeMap;
	}

	@Override
	public Identifier getIdFromOld(int oldRawId) {
		return oldIdMap.get(oldRawId);
	}

	@Override
	public Identifier getIdFromNew(int newRawId) {
		return newIdMap.get(newRawId);
	}
}
