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

package net.fabricmc.fabric.impl.base.event;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.base.toposort.SortableNode;

/**
 * Data of an {@link ArrayBackedEvent} phase.
 *
 * <p>The listeners themselves live in the event's single handler array,
 * a phase only remembers how many of them it owns.
 */
class EventPhaseData<T> extends SortableNode<EventPhaseData<T>> {
	final Identifier id;
	/**
	 * Number of listeners registered in this phase.
	 */
	int size;
	/**
	 * Scratch value: start index of this phase in the handler array, only valid during a reorder.
	 */
	int start;

	EventPhaseData(Identifier id) {
		this.id = id;
	}

	/**
	 * Adds an ordering constraint, ignoring it if it already exists.
	 *
	 * @return {@code true} if a new edge was added.
	 */
	boolean linkTo(EventPhaseData<T> second) {
		if (subsequentNodes.contains(second)) {
			return false;
		}

		link(this, second);
		return true;
	}

	@Override
	protected String getDescription() {
		return id.toString();
	}
}
