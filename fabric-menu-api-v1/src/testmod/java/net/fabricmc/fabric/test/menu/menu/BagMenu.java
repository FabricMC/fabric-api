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

package net.fabricmc.fabric.test.menu.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.test.menu.MenuTest;
import net.fabricmc.fabric.test.menu.item.BagItem;

public class BagMenu extends DispenserMenu {
	private final MenuType<?> type;

	public BagMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new SimpleContainer(9));
	}

	public BagMenu(int containerId, Inventory playerInventory, Container inventory) {
		this(MenuTest.BAG_MENU, containerId, playerInventory, inventory);
	}

	protected BagMenu(MenuType<?> type, int containerId, Inventory playerInventory, Container inventory) {
		super(containerId, playerInventory, inventory);
		this.type = type;
	}

	@Override
	public MenuType<?> getType() {
		return type;
	}

	@Override
	public void clicked(int slotId, int clickData, ContainerInput containerInput, Player player) {
		if (slotId >= 0) { // slotId < 0 are used for networking internals
			ItemStack stack = getSlot(slotId).getItem();

			if (stack.getItem() instanceof BagItem) {
				// Prevent moving bags around
				return;
			}
		}

		super.clicked(slotId, clickData, containerInput, player);
	}
}
