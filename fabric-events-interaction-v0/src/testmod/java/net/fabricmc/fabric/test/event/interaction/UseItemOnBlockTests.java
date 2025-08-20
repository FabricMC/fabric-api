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

package net.fabricmc.fabric.test.event.interaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemOnBlockEvents;

public class UseItemOnBlockTests implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(UseItemOnBlockTests.class);

	/*
	 * Expected behavior:
	 *  - ITEM: When sneaking, right-clicking with a moss block should change any stone bricks into mossy stone bricks
	 * 	- BLOCK: When sneaking, right-clicking with an axe on mossy stone bricks should 'shear' the moss and change them into stone bricks
	 */

	@Override
	public void onInitialize() {
		UseItemOnBlockEvents.ITEM.register((stack, context) -> {
			LOGGER.info("Invoked an UseItemOnBlock Event - item side");

			if (context.getStack().isOf(Items.MOSS_BLOCK)) {
				World world = context.getWorld();
				BlockPos pos = context.getBlockPos();
				BlockState state = world.getBlockState(pos);
				PlayerEntity playerEntity = context.getPlayer();

				if (playerEntity != null && playerEntity.isSneaking() && state.getBlock() == Blocks.STONE_BRICKS) {
					playerEntity.swingHand(context.getHand());
					context.getStack().decrementUnlessCreative(1, playerEntity);

					world.setBlockState(pos, Blocks.MOSSY_STONE_BRICKS.getStateWithProperties(state));
					return ActionResult.SUCCESS;
				}
			}

			return null;
		});

		UseItemOnBlockEvents.BLOCK.register((stack, state, world, pos, player, hand, hit) -> {
			LOGGER.info("Invoked an UseItemOnBlock Event - block side");

			if (state.getBlock() == Blocks.MOSSY_STONE_BRICKS && stack.getItem() instanceof AxeItem) {
				world.setBlockState(pos, Blocks.STONE_BRICKS.getStateWithProperties(state));
				Block.dropStack(world, pos, new ItemStack(Items.MOSS_BLOCK));

				return ActionResult.SUCCESS;
			}

			return null;
		});
	}
}
