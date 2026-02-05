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

package net.fabricmc.fabric.impl.transfer.context;

import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class CreativeInteractionContainerItemContext extends ConstantContainerItemContext {
	private final PlayerInventoryStorage playerInventory;

	public CreativeInteractionContainerItemContext(ItemVariant initialVariant, long initialAmount, Player player) {
		super(initialVariant, initialAmount);

		this.playerInventory = PlayerInventoryStorage.of(player);
	}

	@Override
	public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
		StoragePreconditions.notBlankNotNegative(itemVariant, maxAmount);

		if (maxAmount > 0) {
			// Only add the item to the player inventory if it's not already in the inventory.
			boolean hasItem = false;

			for (SingleSlotStorage<ItemVariant> slot : playerInventory.getSlots()) {
				if (slot.getResource().equals(itemVariant) && slot.getAmount() > 0) {
					hasItem = true;
					break;
				}
			}

			if (!hasItem) {
				playerInventory.offer(itemVariant, 1, transactionContext);
			}
		}

		// Insertion always succeeds from the POV of the context user.
		return maxAmount;
	}

	@Override
	public String toString() {
		return "CreativeInteractionContainerItemContext[%d %s]"
				.formatted(getMainSlot().getAmount(), getMainSlot().getResource());
	}
}
