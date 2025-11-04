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

package net.fabricmc.fabric.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

/**
 * Convenience interface for blocks which listen to "break interactions" (left-click).
 *
 * @deprecated Use {@link AttackBlockCallback} instead and check for the block.
 * This gives more control over the different cancellation outcomes.
 */
@Deprecated
public interface BlockAttackInteractionAware {
	/**
	 * @return True if the block accepted the player and it should no longer be processed.
	 */
	boolean onAttackInteraction(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, Direction direction);
}
