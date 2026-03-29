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

import java.lang.reflect.Array;
import java.util.Arrays;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.base.toposort.SortableNode;

/**
 * Data of an {@link ArrayBackedEvent} phase.
 */
public class EventPhaseData<T> extends SortableNode<EventPhaseData<T>> {
	final Identifier id;
	T[] listeners;

	@SuppressWarnings("unchecked")
	EventPhaseData(Identifier id, Class<?> listenerClass) {
		this.id = id;
		this.listeners = (T[]) Array.newInstance(listenerClass, 0);
	}

	void addListener(T listener) {
		int oldLength = listeners.length;
		listeners = Arrays.copyOf(listeners, oldLength + 1);
		listeners[oldLength] = listener;
	}

	public boolean removeListener(T listener) {
		int indexToRemove;

		for (indexToRemove = listeners.length - 1; indexToRemove >= 0; indexToRemove--) {
			if (listeners[indexToRemove] == listener) {
				break;
			}
		}

		if (indexToRemove == -1) {
			return false;
		}

		T[] newListeners = Arrays.copyOf(listeners, listeners.length - 1);
		System.arraycopy(listeners, indexToRemove + 1, newListeners, indexToRemove, newListeners.length - indexToRemove);
		listeners = newListeners;
		return true;
	}

	@Override
	protected String getDescription() {
		return id.toString();
	}
}
