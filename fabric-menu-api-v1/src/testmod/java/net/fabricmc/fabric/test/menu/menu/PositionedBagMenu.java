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

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

import net.fabricmc.fabric.test.menu.MenuTest;

public class PositionedBagMenu extends BagMenu implements PositionedMenu {
	private final BlockPos pos;

	public PositionedBagMenu(int containerId, Inventory playerInventory, BagData data) {
		this(containerId, playerInventory, new SimpleContainer(9), data.pos().orElse(null));
	}

	public PositionedBagMenu(int containerId, Inventory playerInventory, Container inventory, BlockPos pos) {
		super(MenuTest.POSITIONED_BAG_MENU, containerId, playerInventory, inventory);
		this.pos = pos;
	}

	@Override
	public BlockPos getPos() {
		return pos;
	}

	public record BagData(Optional<BlockPos> pos) {
		public static final StreamCodec<RegistryFriendlyByteBuf, BagData> PACKET_CODEC = BlockPos.STREAM_CODEC.apply(ByteBufCodecs::optional).map(BagData::new, BagData::pos).cast();
	}
}
