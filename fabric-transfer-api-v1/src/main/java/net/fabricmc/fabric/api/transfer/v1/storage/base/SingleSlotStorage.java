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

import java.util.Iterator;

import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

/// A storage that is also its only storage view.
/// It can be used in APIs for storages that are wrappers around a single "slot", or for slightly more convenient implementation.
///
/// @param <T> The type of the stored resource.
public interface SingleSlotStorage<T> extends SlottedStorage<T>, StorageView<T> {
	@Override
	default Iterator<StorageView<T>> iterator() {
		return TransferApiImpl.singletonIterator(this);
	}

	@Override
	default int getSlotCount() {
		return 1;
	}

	@Override
	default SingleSlotStorage<T> getSlot(int slot) {
		if (slot != 0) {
			throw new IndexOutOfBoundsException("Slot " + slot + " does not exist in a single-slot storage.");
		}

		return this;
	}
}
