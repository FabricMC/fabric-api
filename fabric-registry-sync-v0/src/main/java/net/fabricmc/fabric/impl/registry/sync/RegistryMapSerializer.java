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

import java.util.LinkedHashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class RegistryMapSerializer {
	public static final int VERSION = 1;

	public static Map<Identifier, Object2IntMap<Identifier>> fromNbt(NbtCompound nbt) {
		NbtCompound mainNbt = nbt.getCompound("registries").orElseThrow();
		Map<Identifier, Object2IntMap<Identifier>> map = new LinkedHashMap<>();

		for (String registryId : mainNbt.getKeys()) {
			Object2IntMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<>();
			NbtCompound idNbt = mainNbt.getCompound(registryId).orElseThrow();

			for (String id : idNbt.getKeys()) {
				idMap.put(Identifier.of(id), idNbt.getInt(id, 0));
			}

			map.put(Identifier.of(registryId), idMap);
		}

		return map;
	}

	public static NbtCompound toNbt(Map<Identifier, Object2IntMap<Identifier>> map) {
		NbtCompound mainNbt = new NbtCompound();

		map.forEach((registryId, idMap) -> {
			NbtCompound registryNbt = new NbtCompound();

			for (Object2IntMap.Entry<Identifier> idPair : idMap.object2IntEntrySet()) {
				registryNbt.putInt(idPair.getKey().toString(), idPair.getIntValue());
			}

			mainNbt.put(registryId.toString(), registryNbt);
		});

		NbtCompound nbt = new NbtCompound();
		nbt.putInt("version", VERSION);
		nbt.put("registries", mainNbt);
		return nbt;
	}
}
