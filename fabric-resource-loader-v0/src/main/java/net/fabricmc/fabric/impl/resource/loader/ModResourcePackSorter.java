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

package net.fabricmc.fabric.impl.resource.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;

public class ModResourcePackSorter {
	private final Object lock = new Object();
	private ModResourcePack[] packs;
	/**
	 * Registered load phases.
	 */
	private final Map<String, LoadPhaseData> phases = new LinkedHashMap<>();
	/**
	 * Phases sorted in the correct dependency order.
	 */
	private final List<LoadPhaseData> sortedPhases = new ArrayList<>();

	ModResourcePackSorter() {
		this.packs = new ModResourcePack[0];
	}

	public List<ModResourcePack> getPacks() {
		return Collections.unmodifiableList(Arrays.asList(this.packs));
	}

	public void addPack(ModResourcePack pack) {
		Objects.requireNonNull(pack, "Can't register a null pack");

		String modId = pack.packId();
		Objects.requireNonNull(modId, "Can't register a pack without a mod id");

		synchronized (lock) {
			getOrCreatePhase(modId, true).addPack(pack);
			rebuildPackList(packs.length + 1);
		}
	}

	private LoadPhaseData getOrCreatePhase(String id, boolean sortIfCreate) {
		LoadPhaseData phase = phases.get(id);

		if (phase == null) {
			phase = new LoadPhaseData(id);
			phases.put(id, phase);
			sortedPhases.add(phase);

			if (sortIfCreate) {
				NodeSorting.sort(sortedPhases, "mod resource packs", Comparator.comparing(data -> data.modId));
			}
		}

		return phase;
	}

	private void rebuildPackList(int newLength) {
		// Rebuild pack list.
		if (sortedPhases.size() == 1) {
			// Special case with a single phase: use the array of the phase directly.
			packs = sortedPhases.getFirst().packs;
		} else {
			ModResourcePack[] newHandlers = new ModResourcePack[newLength];
			int newHandlersIndex = 0;

			for (LoadPhaseData existingPhase : sortedPhases) {
				int length = existingPhase.packs.length;
				System.arraycopy(existingPhase.packs, 0, newHandlers, newHandlersIndex, length);
				newHandlersIndex += length;
			}

			packs = newHandlers;
		}
	}

	public void addLoadOrdering(String firstPhase, String secondPhase, ModResourcePackUtil.Order order) {
		Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
		Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");
		if (firstPhase.equals(secondPhase)) throw new IllegalArgumentException("Tried to add a phase that depends on itself.");

		synchronized (lock) {
			LoadPhaseData first = getOrCreatePhase(firstPhase, false);
			LoadPhaseData second = getOrCreatePhase(secondPhase, false);

			switch (order) {
			case BEFORE -> LoadPhaseData.link(first, second);
			case AFTER -> LoadPhaseData.link(second, first);
			}

			NodeSorting.sort(this.sortedPhases, "event phases", Comparator.comparing(data -> data.modId));
			rebuildPackList(packs.length);
		}
	}

	public static class LoadPhaseData extends SortableNode<LoadPhaseData> {
		final String modId;
		ModResourcePack[] packs;

		LoadPhaseData(String modId) {
			this.modId = modId;
			this.packs = new ModResourcePack[0];
		}

		void addPack(ModResourcePack pack) {
			int oldLength = packs.length;
			packs = Arrays.copyOf(packs, oldLength + 1);
			packs[oldLength] = pack;
		}

		@Override
		protected String getDescription() {
			return modId;
		}
	}
}
