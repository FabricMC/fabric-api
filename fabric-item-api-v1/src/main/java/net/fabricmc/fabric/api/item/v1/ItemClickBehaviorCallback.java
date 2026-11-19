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

package net.fabricmc.fabric.api.item.v1;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.EventResult;

/// A single event that allows for overriding item behavior otherwise implemented via
/// [ItemStack#overrideStackedOnOther(Slot, ClickAction, Player)] and
/// [ItemStack#overrideOtherStackedOnMe(ItemStack, Slot, ClickAction, Player, SlotAccess)] on a
/// per-item basis.
///
/// The event runs whenever a slot in an [net.minecraft.world.inventory.AbstractContainerMenu] is
/// clicked and provides the item in the slot and the item currently carried by the cursor. Either
/// item can be empty.
///
/// This behavior runs on both the client and server side except the creative mode inventory menu,
/// which is only ever handled client-side.
@FunctionalInterface
public interface ItemClickBehaviorCallback {
	/// Callback that runs in
	/// [net.minecraft.world.inventory.AbstractContainerMenu#tryItemClickBehaviourOverride(Player,
	/// ClickAction, Slot, ItemStack, ItemStack)].
	Event<ItemClickBehaviorCallback> EVENT = EventFactory.createArrayBacked(
			ItemClickBehaviorCallback.class,
			callbacks -> (ItemStack hoveredItem, Slot hoveredSlot, ItemStack itemHeldByCursor, SlotAccess slotHeldByCursor, ClickAction clickAction, Player player) -> {
				for (ItemClickBehaviorCallback callback : callbacks) {
					EventResult result = callback.onItemClickBehavior(hoveredItem,
							hoveredSlot,
							itemHeldByCursor,
							slotHeldByCursor,
							clickAction,
							player);
					if (result != EventResult.PASS) {
						return result;
					}
				}

				return EventResult.PASS;
			});

	/// Handles menu interactions when clicking items on top of each other in a container menu.
	///
	/// @param hoveredItem      the item in the slot hovered by the mouse cursor
	/// @param hoveredSlot      the slot hovered by the mouse cursor
	/// @param itemHeldByCursor the item carried by the cursor
	/// @param slotHeldByCursor the slot abstraction for the cursor
	/// @param clickAction      the mouse button that was used in the click
	/// @param player           the player
	/// @return [EventResult#ALLOW] to allow normal container menu click behavior to run,
	/// 		[EventResult#DENY] to prevent normal click behavior, which allows for implementing a
	/// 		custom interaction as vanilla does for bundles, [EventResult#PASS] to fall back to other
	/// 		callbacks and eventually resolve [ItemStack#overrideStackedOnOther(Slot, ClickAction,
	/// 		Player)] and [ItemStack#overrideOtherStackedOnMe(ItemStack, Slot, ClickAction, Player,
	/// 		SlotAccess)]
	EventResult onItemClickBehavior(ItemStack hoveredItem, Slot hoveredSlot, ItemStack itemHeldByCursor, SlotAccess slotHeldByCursor, ClickAction clickAction, Player player);
}
