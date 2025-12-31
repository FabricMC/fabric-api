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

package net.fabricmc.fabric.api.transfer.v1.storage;

import java.util.List;

import org.jetbrains.annotations.UnmodifiableView;

import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

/// A [Storage] implementation made of indexed slots.
///
/// Please note that some storages may not implement this interface.
/// It is up to the storage implementation to decide whether to implement this interface or not.
/// Checking whether a storage is slotted can be done using `instanceof`.
///
/// @param <T> The type of the stored resources.
public interface SlottedStorage<T> extends Storage<T> {
	/// Retrieve the number of slots in this storage.
	int getSlotCount();

	/// Retrieve a specific slot of this storage.
	///
	/// @throws IndexOutOfBoundsException If the slot index is out of bounds.
	SingleSlotStorage<T> getSlot(int slot);

	/// Retrieve a list containing all the slots of this storage. **The list must not be modified.**
	///
	/// This function can be used to interface with code that requires a slot list,
	/// for example [StorageUtil#insertStacking] or [net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext#getAdditionalSlots()].
	///
	/// It is guaranteed that calling this function is fast.
	/// The default implementation returns a view over the storage that delegates to [#getSlotCount] and [#getSlot].
	@UnmodifiableView
	default List<SingleSlotStorage<T>> getSlots() {
		return TransferApiImpl.makeListView(this);
	}
}
