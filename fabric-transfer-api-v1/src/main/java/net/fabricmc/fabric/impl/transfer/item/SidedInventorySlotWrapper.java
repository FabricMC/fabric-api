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

import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.DebugMessages;

/**
 * Wrapper around an {@link InventorySlotWrapper}, with additional canInsert and canExtract checks.
 */
class SidedInventorySlotWrapper implements SingleSlotStorage<ItemVariant> {
	private final InventorySlotWrapper slotWrapper;
	private final SidedInventory sidedInventory;
	private final Direction direction;

	SidedInventorySlotWrapper(InventorySlotWrapper slotWrapper, SidedInventory sidedInventory, Direction direction) {
		this.slotWrapper = slotWrapper;
		this.sidedInventory = sidedInventory;
		this.direction = direction;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!sidedInventory.canInsert(slotWrapper.slot, ((ItemVariantImpl) resource).getCachedStack(), direction)) {
			return 0;
		} else {
			return slotWrapper.insert(resource, maxAmount, transaction);
		}
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!sidedInventory.canExtract(slotWrapper.slot, ((ItemVariantImpl) resource).getCachedStack(), direction)) {
			return 0;
		} else {
			return slotWrapper.extract(resource, maxAmount, transaction);
		}
	}

	@Override
	public boolean isResourceBlank() {
		return slotWrapper.isResourceBlank();
	}

	@Override
	public ItemVariant getResource() {
		return slotWrapper.getResource();
	}

	@Override
	public long getAmount() {
		return slotWrapper.getAmount();
	}

	@Override
	public long getCapacity() {
		return slotWrapper.getCapacity();
	}

	@Override
	public StorageView<ItemVariant> getUnderlyingView() {
		return slotWrapper.getUnderlyingView();
	}

	@Override
	public String toString() {
		return "SidedInventorySlotWrapper[%s#%d/%s]".formatted(DebugMessages.forInventory(sidedInventory), slotWrapper.slot, direction.name());
	}
}
