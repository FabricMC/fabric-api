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

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.MenuType;

import net.fabricmc.fabric.test.screenhandler.ScreenHandlerTest;

public class BoxScreenHandler extends DispenserMenu implements PositionedScreenHandler {
	private final BlockPos pos;

	public BoxScreenHandler(int syncId, Inventory playerInventory, BlockPos pos) {
		super(syncId, playerInventory);
		this.pos = pos;
	}

	public BoxScreenHandler(int syncId, Inventory playerInventory, Container inventory) {
		super(syncId, playerInventory, inventory);
		this.pos = BlockPos.ZERO;
	}

	@Override
	public BlockPos getPos() {
		return pos;
	}

	@Override
	public MenuType<?> getType() {
		return ScreenHandlerTest.BOX_SCREEN_HANDLER;
	}
}
