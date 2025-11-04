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

package net.fabricmc.fabric.impl.transfer.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MapMaker;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.impl.transfer.DebugMessages;

/**
 * Implementation of {@link InventoryStorage}.
 * Note on thread-safety: we assume that Inventory's are inherently single-threaded, and no attempt is made at synchronization.
 * However, the access to implementations can happen on multiple threads concurrently, which is why we use a thread-safe wrapper map.
 */
public class InventoryStorageImpl extends CombinedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> implements InventoryStorage {
	/**
	 * Global wrapper concurrent map.
	 *
	 * <p>A note on GC: weak keys alone are not suitable as the InventoryStorage slots strongly reference the Inventory keys.
	 * Weak values are suitable, but we have to ensure that the InventoryStorageImpl remains strongly reachable as long as
	 * one of the slot wrappers refers to it, hence the {@code strongRef} field in {@link InventorySlotWrapper}.
	 */
	// TODO: look into promoting the weak reference to a soft reference if building the wrappers becomes a performance bottleneck.
	// TODO: should have identity semantics?
	private static final Map<Container, InventoryStorageImpl> WRAPPERS = new MapMaker().weakValues().makeMap();

	public static InventoryStorage of(Container inventory, @Nullable Direction direction) {
		InventoryStorageImpl storage = WRAPPERS.computeIfAbsent(inventory, inv -> {
			if (inv instanceof Inventory playerInventory) {
				return new PlayerInventoryStorageImpl(playerInventory);
			} else {
				return new InventoryStorageImpl(inv);
			}
		});
		storage.resizeSlotList();
		return storage.getSidedWrapper(direction);
	}

	final Container inventory;
	/**
	 * This {@code backingList} is the real list of wrappers.
	 * The {@code parts} in the superclass is the public-facing unmodifiable sublist with exactly the right amount of slots.
	 */
	final List<InventorySlotWrapper> backingList;
	/**
	 * This participant ensures that markDirty is only called once for the entire inventory.
	 */
	final MarkDirtyParticipant markDirtyParticipant = new MarkDirtyParticipant();

	InventoryStorageImpl(Container inventory) {
		super(Collections.emptyList());
		this.inventory = inventory;
		this.backingList = new ArrayList<>();
	}

	@Override
	public List<SingleSlotStorage<ItemVariant>> getSlots() {
		return parts;
	}

	/**
	 * Resize slot list to match the current size of the inventory.
	 */
	private void resizeSlotList() {
		int inventorySize = inventory.getContainerSize();

		// If the public-facing list must change...
		if (inventorySize != parts.size()) {
			// Ensure we have enough wrappers in the backing list.
			while (backingList.size() < inventorySize) {
				backingList.add(new InventorySlotWrapper(this, backingList.size()));
			}

			// Update the public-facing list.
			parts = Collections.unmodifiableList(backingList.subList(0, inventorySize));
		}
	}

	private InventoryStorage getSidedWrapper(@Nullable Direction direction) {
		if (inventory instanceof WorldlyContainer && direction != null) {
			return new SidedInventoryStorageImpl(this, direction);
		} else {
			return this;
		}
	}

	@Override
	public String toString() {
		return "InventoryStorage[" + DebugMessages.forInventory(inventory) + "]";
	}

	// Boolean is used to prevent allocation. Null values are not allowed by SnapshotParticipant.
	class MarkDirtyParticipant extends SnapshotParticipant<Boolean> {
		@Override
		protected Boolean createSnapshot() {
			return Boolean.TRUE;
		}

		@Override
		protected void readSnapshot(Boolean snapshot) {
		}

		@Override
		protected void onFinalCommit() {
			inventory.setChanged();
		}
	}
}
