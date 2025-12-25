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

package net.fabricmc.fabric.test.menu.item;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.test.menu.menu.PositionedBagMenu;

public class PositionedBagItem extends BagItem {
	public PositionedBagItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player user, InteractionHand hand) {
		ItemStack stack = user.getItemInHand(hand);
		user.openMenu(createMenuProvider(stack, null));
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player user = context.getPlayer();
		ItemStack stack = user.getItemInHand(context.getHand());
		BlockPos pos = context.getClickedPos();
		user.openMenu(createMenuProvider(stack, pos));
		return InteractionResult.SUCCESS;
	}

	private ExtendedMenuProvider<PositionedBagMenu.BagData> createMenuProvider(ItemStack stack, BlockPos pos) {
		return new ExtendedMenuProvider<>() {
			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
				return new PositionedBagMenu(containerId, inventory, new BagInventory(stack), pos);
			}

			@Override
			public Component getDisplayName() {
				return stack.getHoverName();
			}

			@Override
			public PositionedBagMenu.BagData getScreenOpeningData(ServerPlayer player) {
				return new PositionedBagMenu.BagData(Optional.of(pos));
			}
		};
	}
}
