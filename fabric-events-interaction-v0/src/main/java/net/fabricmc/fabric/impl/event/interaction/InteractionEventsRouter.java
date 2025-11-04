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

package net.fabricmc.fabric.impl.event.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.BlockAttackInteractionAware;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class InteractionEventsRouter implements ModInitializer {
	@Override
	public void onInitialize() {
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			BlockState state = world.getBlockState(pos);

			if (state instanceof BlockAttackInteractionAware) {
				if (((BlockAttackInteractionAware) state).onAttackInteraction(state, world, pos, player, hand, direction)) {
					return InteractionResult.FAIL;
				}
			} else if (state.getBlock() instanceof BlockAttackInteractionAware) {
				if (((BlockAttackInteractionAware) state.getBlock()).onAttackInteraction(state, world, pos, player, hand, direction)) {
					return InteractionResult.FAIL;
				}
			}

			return InteractionResult.PASS;
		});

		/*
		* This code is for telling the client that the block wasn't actually broken.
		* This covers a 3x3 area due to how vanilla redstone handles updates, as it considers
		* important functions like quasi-connectivity and redstone dust logic
		 */
		PlayerBlockBreakEvents.CANCELED.register(((world, player, pos, state, blockEntity) -> {
			BlockPos cornerPos = pos.offset(-1, -1, -1);

			for (int x = 0; x < 3; x++) {
				for (int y = 0; y < 3; y++) {
					for (int z = 0; z < 3; z++) {
						((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(world, cornerPos.offset(x, y, z)));
					}
				}
			}
		}));
	}
}
