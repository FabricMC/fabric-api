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

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Contains events triggered by players interact with blocks.
 */
public interface BlockEvents {
	/**
	 * Callback that runs when {@link BlockState#useItemOn(ItemStack, Level, Player, InteractionHand, BlockHitResult)} is called.
	 */
	Event<UseItemOnCallback> USE_ITEM_ON = EventFactory.createArrayBacked(UseItemOnCallback.class,
			(listeners) -> (itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult) -> {
				for (UseItemOnCallback event : listeners) {
					InteractionResult result = event.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);

					if (result != null) {
						return result;
					}
				}

				return null;
			}
	);

	/**
	 * Callback that runs when {@link BlockState#useWithoutItem(Level, Player, BlockHitResult)} is called.
	 */
	Event<UseWithoutItemCallback> USE_WITHOUT_ITEM = EventFactory.createArrayBacked(UseWithoutItemCallback.class,
			(listeners) -> (blockState, level, blockPos, player, blockHitResult) -> {
				for (UseWithoutItemCallback event : listeners) {
					InteractionResult result = event.useWithoutItem(blockState, level, blockPos, player, blockHitResult);

					if (result != null) {
						return result;
					}
				}

				return null;
			}
	);

	@FunctionalInterface
	interface UseItemOnCallback {
		/**
		 * Handles block-side interaction, which uses item stack as a context.
		 *
		 * @param itemStack the stack used for interaction
		 * @param blockState the interacted block state
		 * @param level the level in which block resides
		 * @param blockPos the position of the block
		 * @param player the player causing the interaction
		 * @param interactionHand player's hand used in interaction
		 * @param blockHitResult the hit-result pointing at interacted block
		 * @return any {@link InteractionResult} to indicate that interaction was handler or null to pass it forward to other listeners and vanilla
		 */
		@Nullable
		InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult);
	}

	@FunctionalInterface
	interface UseWithoutItemCallback {
		/**
		 * Handles block-side interaction, which doesn't require any held item stack.
		 *
		 * @param blockState the interacted block state
		 * @param level the level in which block resides
		 * @param blockPos the position of the block
		 * @param player the player causing the interaction
		 * @param blockHitResult the hit-result pointing at interacted block
		 * @return any {@link InteractionResult} to indicate that interaction was handler or null to pass it forward to other listeners and vanilla
		 */
		@Nullable
		InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult);
	}
}
