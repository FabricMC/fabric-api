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

package net.fabricmc.fabric.test.screenhandler.item;

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

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.test.screenhandler.screen.PositionedBagScreenHandler;

public class PositionedBagItem extends BagItem {
	public PositionedBagItem(Properties settings) {
		super(settings);
	}

	@Override
	public InteractionResult use(Level world, Player user, InteractionHand hand) {
		ItemStack stack = user.getItemInHand(hand);
		user.openMenu(createScreenHandlerFactory(stack, null));
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player user = context.getPlayer();
		ItemStack stack = user.getItemInHand(context.getHand());
		BlockPos pos = context.getClickedPos();
		user.openMenu(createScreenHandlerFactory(stack, pos));
		return InteractionResult.SUCCESS;
	}

	private ExtendedScreenHandlerFactory<PositionedBagScreenHandler.BagData> createScreenHandlerFactory(ItemStack stack, BlockPos pos) {
		return new ExtendedScreenHandlerFactory<>() {
			@Override
			public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
				return new PositionedBagScreenHandler(syncId, inventory, new BagInventory(stack), pos);
			}

			@Override
			public Component getDisplayName() {
				return stack.getHoverName();
			}

			@Override
			public PositionedBagScreenHandler.BagData getScreenOpeningData(ServerPlayer player) {
				return new PositionedBagScreenHandler.BagData(Optional.of(pos));
			}
		};
	}
}
