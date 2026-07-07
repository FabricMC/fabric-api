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

package net.fabricmc.fabric.api.event.client.player;

import net.minecraft.world.entity.player.Inventory;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events pertaining to using the scroll wheel in the hotbar to change the selected item.
 */
public final class ClientHotbarScrollEvents {
	/**
	 * An event that checks whether the player's scrolling will change the selected hotbar slot.
	 *
	 * <p>Returning {@code false} cancels the hotbar selection change without running anymore
	 * registered callbacks.
	 */
	public static final Event<Allow> ALLOW = EventFactory.createArrayBacked(Allow.class, listeners -> (inventory, currentSlot, newSlot, xOffset, yOffset) -> {
		for (Allow listener : listeners) {
			boolean allow = listener.allowScroll(inventory, currentSlot, newSlot, xOffset, yOffset);

			if (!allow) {
				return false;
			}
		}

		return true;
	});

	/**
	 * An event that is invoked before player scrolling changes the selected hotbar slot.
	 *
	 * <p>This event is only fired if the result of {@link #ALLOW} is {@code true}.
	 */
	public static final Event<Before> BEFORE = EventFactory.createArrayBacked(Before.class, listeners -> (inventory, currentSlot, newSlot, xOffset, yOffset) -> {
		for (Before listener : listeners) {
			listener.beforeScroll(inventory, currentSlot, newSlot, xOffset, yOffset);
		}
	});

	/**
	 * An event that is invoked after player scrolling changes the selected hotbar slot.
	 *
	 * <p>This event is only fired if the result of {@link #ALLOW} is {@code true}.
	 */
	public static final Event<After> AFTER = EventFactory.createArrayBacked(After.class, listeners -> (inventory, currentSlot, newSlot, xOffset, yOffset) -> {
		for (After listener : listeners) {
			listener.afterScroll(inventory, currentSlot, newSlot, xOffset, yOffset);
		}
	});

	@FunctionalInterface
	public interface Allow {
		/**
		 * Called before player scrolling changes the selected slot.
		 *
		 * @param inventory The player's inventory.
		 * @param currentSlot The currently selected slot before changing.
		 * @param newSlot The slot about to be selected.
		 * @param xOffset The X scroll offset.
		 * @param yOffset The Y scroll offset.
		 * @return {@code true} if the selected slot will change to {@code newSlot}, otherwise
		 * {@code false} if the slot will remain {@code currentSlot}.
		 */
		boolean allowScroll(Inventory inventory, int currentSlot, int newSlot, double xOffset, double yOffset);
	}

	@FunctionalInterface
	public interface Before {
		/**
		 * Called before player scrolling changes the selected slot.
		 *
		 * @param inventory The player's inventory.
		 * @param currentSlot The currently selected slot before changing.
		 * @param newSlot The slot about to be selected.
		 * @param xOffset The X scroll offset.
		 * @param yOffset The Y scroll offset.
		 */
		void beforeScroll(Inventory inventory, int currentSlot, int newSlot, double xOffset, double yOffset);
	}

	@FunctionalInterface
	public interface After {
		/**
		 * Called after player scrolling changes the selected slot.
		 *
		 * @param inventory The player's inventory.
		 * @param currentSlot The currently selected slot before changing.
		 * @param newSlot The slot about to be selected.
		 * @param xOffset The X scroll offset.
		 * @param yOffset The Y scroll offset.
		 */
		void afterScroll(Inventory inventory, int currentSlot, int newSlot, double xOffset, double yOffset);
	}

	private ClientHotbarScrollEvents() {
	}
}
