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

package net.fabricmc.fabric.test.item;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.ItemClickBehaviorCallback;
import net.fabricmc.fabric.api.util.EventResult;

public class ItemClickBehaviorTest implements ModInitializer {
	@Override
	public void onInitialize() {
		ItemClickBehaviorCallback.EVENT.register((ItemStack hoveredItem, Slot hoveredSlot, ItemStack itemHeldByCursor, SlotAccess slotHeldByCursor, ClickAction clickAction, Player player) -> {
			if (hoveredItem.is(Items.DYED_BUNDLE.yellow())
					|| itemHeldByCursor.is(Items.DYED_BUNDLE.yellow())) {
				// Disables any special click behavior for yellow bundles, so they behave like most other items in container menus.
				return EventResult.ALLOW;
			} else if (hoveredItem.is(Items.COPPER_NUGGET) && !itemHeldByCursor.isEmpty()
					|| !hoveredItem.isEmpty() && itemHeldByCursor.is(Items.COPPER_NUGGET)) {
				// Prevents click interactions for copper nugget in container menus (without providing any special handling).
				return EventResult.DENY;
			} else {
				return EventResult.PASS;
			}
		});
	}
}
