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

package net.fabricmc.fabric.api.event.client.player;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Contains client side events triggered by block breaking.
 *
 * <p>For preventing block breaking client side and other purposes, see {@link net.fabricmc.fabric.api.event.player.AttackBlockCallback}.
 * For server side block break events, see {@link net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents}.
 */
public final class ClientPlayerBlockBreakEvents {
	private ClientPlayerBlockBreakEvents() {
	}

	/**
	 * Callback after a block is broken client side.
	 *
	 * <p>Only called client side. For server side see {@link net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents#AFTER}
	 */
	public static final Event<After> AFTER = EventFactory.createArrayBacked(After.class,
			(listeners) -> (world, player, pos, state) -> {
				for (After event : listeners) {
					event.afterBlockBreak(world, player, pos, state);
				}
			}
	);

	@FunctionalInterface
	public interface After {
		/**
		 * Called after a block is successfully broken.
		 *
		 * @param world  the world where the block was broken
		 * @param player the player who broke the block
		 * @param pos    the position where the block was broken
		 * @param state  the block state <strong>before</strong> the block was broken
		 */
		void afterBlockBreak(ClientLevel world, LocalPlayer player, BlockPos pos, BlockState state);
	}
}
