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

package net.fabricmc.fabric.test.lookup;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.fabric.test.lookup.api.Inspectable;
import net.fabricmc.fabric.test.lookup.entity.FabricEntityApiLookupTest;
import net.fabricmc.fabric.test.lookup.item.FabricItemApiLookupTest;

public class InspectorBlock extends Block {
	public InspectorBlock(Properties settings) {
		super(settings);
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
		Inspectable inspectable = FabricItemApiLookupTest.INSPECTABLE.find(stack, null);

		if (inspectable != null) {
			if (!world.isClientSide()) {
				player.displayClientMessage(inspectable.inspect(), true);
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
		if (!world.isClientSide()) {
			Inspectable inspectable = FabricEntityApiLookupTest.INSPECTABLE.find(entity, null);

			if (inspectable != null) {
				for (ServerPlayer player : world.getServer().getPlayerList().getPlayers()) {
					player.displayClientMessage(inspectable.inspect(), true);
				}
			}
		}
	}
}
