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

package net.fabricmc.fabric.api.transfer.v1.fluid;

import java.util.Objects;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.DebugMessages;

/**
 * Helper functions to work with fluid storages.
 */
public final class FluidStorageUtil {
	/**
	 * Try to make the item in a player hand "interact" with a fluid storage.
	 * This can be used when a player right-clicks a tank, for example.
	 *
	 * <p>More specifically, this function tries to find a fluid storing item in the player's hand.
	 * Then, it tries to fill that item from the storage. If that fails, it tries to fill the storage from that item.
	 *
	 * <p>Only up to one fluid variant will be moved, and the corresponding emptying/filling sound will be played.
	 * In creative mode, the original container item is not modified,
	 * and the player's inventory will additionally receive a copy of the modified container, if it doesn't have it yet.
	 *
	 * @param storage The storage that the player is interacting with.
	 * @param player The player.
	 * @param hand The hand that the player used.
	 * @return True if some fluid was moved.
	 */
	public static boolean interactWithFluidStorage(Storage<FluidVariant> storage, Player player, InteractionHand hand) {
		// Check if hand is a fluid container.
		Storage<FluidVariant> handStorage = ContainerItemContext.forPlayerInteraction(player, hand).find(FluidStorage.ITEM);
		if (handStorage == null) return false;

		// Try to fill hand first, otherwise try to empty it.
		Item handItem = player.getItemInHand(hand).getItem();

		try {
			return moveWithSound(storage, handStorage, player, true, handItem) || moveWithSound(handStorage, storage, player, false, handItem);
		} catch (Exception e) {
			CrashReport report = CrashReport.forThrowable(e, "Interacting with fluid storage");
			report.addCategory("Interaction details")
					.setDetail("Player", () -> DebugMessages.forPlayer(player))
					.setDetail("Hand", hand)
					.setDetail("Hand item", handItem::toString)
					.setDetail("Fluid storage", () -> Objects.toString(storage, null));
			throw new ReportedException(report);
		}
	}

	private static boolean moveWithSound(Storage<FluidVariant> from, Storage<FluidVariant> to, Player player, boolean fill, Item handItem) {
		for (StorageView<FluidVariant> view : from) {
			if (view.isResourceBlank()) continue;
			FluidVariant resource = view.getResource();
			long maxExtracted;

			// check how much can be extracted
			try (Transaction extractionTestTransaction = Transaction.openOuter()) {
				maxExtracted = view.extract(resource, Long.MAX_VALUE, extractionTestTransaction);
				extractionTestTransaction.abort();
			}

			try (Transaction transferTransaction = Transaction.openOuter()) {
				// check how much can be inserted
				long accepted = to.insert(resource, maxExtracted, transferTransaction);

				// extract it, or rollback if the amounts don't match
				if (accepted > 0 && view.extract(resource, accepted, transferTransaction) == accepted) {
					transferTransaction.commit();

					SoundEvent sound = fill ? FluidVariantAttributes.getFillSound(resource) : FluidVariantAttributes.getEmptySound(resource);

					// Temporary workaround to use the correct sound for water bottles.
					// TODO: Look into providing a proper item-aware fluid sound API.
					if (resource.isOf(Fluids.WATER)) {
						if (fill && handItem == Items.GLASS_BOTTLE) sound = SoundEvents.BOTTLE_FILL;
						if (!fill && handItem == Items.POTION) sound = SoundEvents.BOTTLE_EMPTY;
					}

					player.level().playSound(player, player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, 1, 1);

					return true;
				}
			}
		}

		return false;
	}

	private FluidStorageUtil() {
	}
}
