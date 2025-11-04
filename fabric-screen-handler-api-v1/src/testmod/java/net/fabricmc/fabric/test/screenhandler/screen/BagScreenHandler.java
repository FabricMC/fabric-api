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

package net.fabricmc.fabric.test.screenhandler.screen;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.test.screenhandler.ScreenHandlerTest;
import net.fabricmc.fabric.test.screenhandler.item.BagItem;

public class BagScreenHandler extends DispenserMenu {
	private final MenuType<?> type;

	public BagScreenHandler(int syncId, Inventory playerInventory) {
		this(syncId, playerInventory, new SimpleContainer(9));
	}

	public BagScreenHandler(int syncId, Inventory playerInventory, Container inventory) {
		this(ScreenHandlerTest.BAG_SCREEN_HANDLER, syncId, playerInventory, inventory);
	}

	protected BagScreenHandler(MenuType<?> type, int syncId, Inventory playerInventory, Container inventory) {
		super(syncId, playerInventory, inventory);
		this.type = type;
	}

	@Override
	public MenuType<?> getType() {
		return type;
	}

	@Override
	public void clicked(int slotId, int clickData, ClickType actionType, Player playerEntity) {
		if (slotId >= 0) { // slotId < 0 are used for networking internals
			ItemStack stack = getSlot(slotId).getItem();

			if (stack.getItem() instanceof BagItem) {
				// Prevent moving bags around
				return;
			}
		}

		super.clicked(slotId, clickData, actionType, playerEntity);
	}
}
