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

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;

import net.fabricmc.fabric.test.screenhandler.ScreenHandlerTest;
import net.fabricmc.fabric.test.screenhandler.item.BagItem;

public class BagScreenHandler extends Generic3x3ContainerScreenHandler {
	private final ScreenHandlerType<?> type;

	public BagScreenHandler(int syncId, PlayerInventory playerInventory, List<Integer> list) {
		this(syncId, playerInventory, new SimpleInventory(9));
	}

	public BagScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
		this(ScreenHandlerTest.BAG_SCREEN_HANDLER, syncId, playerInventory, inventory);
	}

	protected BagScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory) {
		super(syncId, playerInventory, inventory);
		this.type = type;
	}

	@Override
	public ScreenHandlerType<?> getType() {
		return type;
	}

	@Override
	public void onSlotClick(int slotId, int clickData, SlotActionType actionType, PlayerEntity playerEntity) {
		if (slotId >= 0) { // slotId < 0 are used for networking internals
			ItemStack stack = getSlot(slotId).getStack();

			if (stack.getItem() instanceof BagItem) {
				// Prevent moving bags around
				return;
			}
		}

		super.onSlotClick(slotId, clickData, actionType, playerEntity);
	}
}
