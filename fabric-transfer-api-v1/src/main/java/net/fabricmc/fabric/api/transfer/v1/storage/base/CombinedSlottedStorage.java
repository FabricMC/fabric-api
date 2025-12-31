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

package net.fabricmc.fabric.api.transfer.v1.storage.base;

import java.util.List;
import java.util.StringJoiner;

import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;

/// A [net.fabricmc.fabric.api.transfer.v1.storage.Storage] wrapping multiple slotted storages.
/// Same as [CombinedStorage], but for [SlottedStorage]s.
///
/// @param <T> The type of the stored resources.
/// @param <S> The class of every part. `? extends net.fabricmc.fabric.api.transfer.v1.storage.Storage<T>` can be used if the parts are of different types.
public class CombinedSlottedStorage<T, S extends SlottedStorage<T>> extends CombinedStorage<T, S> implements SlottedStorage<T> {
	public CombinedSlottedStorage(List<S> parts) {
		super(parts);
	}

	@Override
	public int getSlotCount() {
		int count = 0;

		for (S part : parts) {
			count += part.getSlotCount();
		}

		return count;
	}

	@Override
	public SingleSlotStorage<T> getSlot(int slot) {
		int updatedSlot = slot;

		for (SlottedStorage<T> part : parts) {
			if (updatedSlot < part.getSlotCount()) {
				return part.getSlot(updatedSlot);
			}

			updatedSlot -= part.getSlotCount();
		}

		throw new IndexOutOfBoundsException("Slot " + slot + " is out of bounds. This storage has size " + getSlotCount());
	}

	@Override
	public String toString() {
		StringJoiner partNames = new StringJoiner(", ");

		for (S part : parts) {
			partNames.add(part.toString());
		}

		return "CombinedSlottedStorage[" + partNames + "]";
	}
}
