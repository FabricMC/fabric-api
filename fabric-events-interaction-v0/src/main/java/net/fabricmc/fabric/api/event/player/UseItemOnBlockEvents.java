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

package net.fabricmc.fabric.api.event.player;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Contains events triggered when using an item on a block. Fires both on client and server side.
 */
public final class UseItemOnBlockEvents {
	/**
	 * Called when {@link AbstractBlockState#onUseWithItem} is triggered.
	 *
	 * <p>Any other return value than {@code null} cancels any further processing and default block interaction.
	 */
	public static final Event<Block> BLOCK = EventFactory.createArrayBacked(Block.class,
			listeners -> ((stack, state, world, pos, player, hand, hit) -> {
				for (Block event : listeners) {
					return event.onUseWithItem(stack, state, world, pos, player, hand, hit);
				}

				return null;
			}));

	/**
	 * Called when {@link net.minecraft.item.Item#useOnBlock Item.useOnBlock} is triggered.
	 *
	 * <p>Any other return value than {@code null} cancels any further processing and default block interaction (e.g., placing of blocks in {@link BlockItem#useOnBlock}).
	 */
	public static final Event<Item> ITEM = EventFactory.createArrayBacked(Item.class,
			listeners -> ((stack, context) -> {
				for (Item event : listeners) {
					return event.useItemOnBlock(stack, context);
				}

				return null;
			}));

	@FunctionalInterface
	public interface Block {
		/**
		 * Called on both client and server side when interacting with a block via an item.
		 *
		 * @param stack the item stack in the player's interacting hand
		 * @param state the block state of the block <b>before</b> interacting
		 * @param world the {@link World} in which the interaction has happened
		 * @param pos the position of the block that's been interacted with
		 * @param player the player which interacted with the block
		 * @param hand the player's hand
		 * @param hit the BlockHitResult with all the information about hitting the block
		 * @return {@code null} to further process other actions, any other value cancels other actions
		 */
		@Nullable ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit);
	}

	@FunctionalInterface
	public interface Item {
		/**
		 * Called on both client and server side when right-clicking ("using") an item on a block.
		 *
		 * @param stack the stack containing the used item
		 * @param context the context used to implement custom logic
		 * @return {@code null} to further process other actions, any other value cancels other actions
		 */
		@Nullable ActionResult useItemOnBlock(ItemStack stack, ItemUsageContext context);
	}
}
