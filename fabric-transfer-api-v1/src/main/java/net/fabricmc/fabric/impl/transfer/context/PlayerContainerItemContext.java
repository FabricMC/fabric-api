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

import java.util.List;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class PlayerContainerItemContext implements ContainerItemContext {
	private final PlayerInventoryStorage playerWrapper;
	private final SingleSlotStorage<ItemVariant> slot;

	public PlayerContainerItemContext(Player player, InteractionHand hand) {
		this.playerWrapper = PlayerInventoryStorage.of(player);
		this.slot = playerWrapper.getHandSlot(hand);
	}

	public PlayerContainerItemContext(Player player, SingleSlotStorage<ItemVariant> slot) {
		this.playerWrapper = PlayerInventoryStorage.of(player);
		this.slot = slot;
	}

	@Override
	public SingleSlotStorage<ItemVariant> getMainSlot() {
		return slot;
	}

	@Override
	public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
		playerWrapper.offerOrDrop(itemVariant, maxAmount, transactionContext);
		return maxAmount;
	}

	@Override
	public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
		return playerWrapper.getSlots();
	}

	@Override
	public String toString() {
		return "PlayerContainerItemContext[%d %s %s/%s]"
				.formatted(slot.getAmount(), slot.getResource(), playerWrapper, slot);
	}
}
