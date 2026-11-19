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

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Contains events triggered by players interact with item.
 */
public interface ItemEvents {
	/**
	 * Callback that runs when {@link Item#useOn(UseOnContext)} is called within {@link ItemStack#useOn(UseOnContext)}.
	 */
	Event<UseOnCallback> USE_ON = EventFactory.createArrayBacked(UseOnCallback.class,
			(listeners) -> (useOnContext) -> {
				for (UseOnCallback event : listeners) {
					InteractionResult result = event.useOn(useOnContext);

					if (result != null) {
						return result;
					}
				}

				return null;
			}
	);

	/**
	 * Callback that runs when {@link Item#use(Level, Player, InteractionHand)} is called within {@link ItemStack#use(Level, Player, InteractionHand)}.
	 */
	Event<UseCallback> USE = EventFactory.createArrayBacked(UseCallback.class,
			(listeners) -> (level, player, interactionHand) -> {
				for (UseCallback event : listeners) {
					InteractionResult result = event.use(level, player, interactionHand);

					if (result != null) {
						return result;
					}
				}

				return null;
			}
	);

	@FunctionalInterface
	interface UseOnCallback {
		/**
		 * Handles item-side interaction, caused by interacting with block.
		 *
		 * @param useOnContext the context of interaction.
		 * @return any {@link InteractionResult} to indicate that interaction was handler or null to pass it forward to other listeners and vanilla
		 */
		@Nullable
		InteractionResult useOn(UseOnContext useOnContext);
	}

	@FunctionalInterface
	interface UseCallback {
		/**
		 * Handles item interaction.
		 *
		 * @param level the level in which block resides
		 * @param player the player causing the interaction
		 * @param interactionHand player's hand used in interaction
		 * @return any {@link InteractionResult} to indicate that interaction was handler or null to pass it forward to other listeners and vanilla
		 */
		@Nullable
		InteractionResult use(Level level, Player player, InteractionHand interactionHand);
	}
}
