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

package net.fabricmc.fabric.mixin.registry.sync;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.IdMapper;

import net.fabricmc.fabric.impl.registry.sync.RemovableIdList;

@Mixin(IdMapper.class)
public class IdMapperMixin<T> implements RemovableIdList<T> {
	@Shadow
	private int nextId;
	@Final
	@Shadow
	private Reference2IntMap<T> tToId;
	@Final
	@Shadow
	private List<T> idToT;

	@Override
	public void fabric_clear() {
		nextId = 0;
		tToId.clear();
		idToT.clear();
	}

	@Unique
	private void fabric_removeInner(T o) {
		int value = tToId.removeInt(o);
		idToT.set(value, null);

		while (nextId > 1 && idToT.get(nextId - 1) == null) {
			nextId--;
		}
	}

	@Override
	public void fabric_remove(T o) {
		if (tToId.containsKey(o)) {
			fabric_removeInner(o);
		}
	}

	@Override
	public void fabric_removeId(int i) {
		List<T> removals = new ArrayList<>();

		for (T o : tToId.keySet()) {
			int j = tToId.getInt(o);

			if (i == j) {
				removals.add(o);
			}
		}

		removals.forEach(this::fabric_removeInner);
	}

	@Override
	public void fabric_remapId(int from, int to) {
		fabric_remapIds(Int2IntMaps.singleton(from, to));
	}

	@Override
	public void fabric_remapIds(Int2IntMap map) {
		// remap idMap
		tToId.replaceAll((a, b) -> map.get((int) b));

		// remap list
		nextId = 0;
		List<T> oldList = new ArrayList<>(idToT);
		idToT.clear();

		for (int k = 0; k < oldList.size(); k++) {
			T o = oldList.get(k);

			if (o != null) {
				int i = map.getOrDefault(k, k);

				while (idToT.size() <= i) {
					idToT.add(null);
				}

				idToT.set(i, o);

				if (nextId <= i) {
					nextId = i + 1;
				}
			}
		}
	}
}
