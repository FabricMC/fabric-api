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

package net.fabricmc.fabric.test.renderer;

import org.jspecify.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;

// Need to implement FabricBlock manually because this is a testmod for another Fabric module, otherwise it would be injected.
public class FrameBlock extends Block implements BlockEntityProvider, FabricBlock {
	public FrameBlock(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult onUseWithItem(ItemStack stack, BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		if (world.getBlockEntity(pos) instanceof FrameBlockEntity frame) {
			@Nullable
			Block currentBlock = frame.getBlock();

			if (stack.isEmpty()) {
				// Try to remove if the stack in hand is empty
				if (currentBlock != null) {
					if (!world.isClient()) {
						player.getInventory().offerOrDrop(new ItemStack(currentBlock));
						frame.setBlock(null);
					}

					return ActionResult.SUCCESS;
				} else {
					return ActionResult.PASS;
				}
			}

			Block handBlock = Block.getBlockFromItem(stack.getItem());

			// getBlockFromItem will return air if we do not have a block item in hand
			if (handBlock == Blocks.AIR) {
				return ActionResult.PASS;
			}

			// Do not allow blocks that may have a block entity
			if (handBlock instanceof BlockEntityProvider) {
				return ActionResult.PASS;
			}

			if (currentBlock == handBlock) {
				return ActionResult.PASS;
			}

			if (!world.isClient()) {
				stack.decrementUnlessCreative(1, player);

				if (currentBlock != null) {
					player.getInventory().offerOrDrop(new ItemStack(currentBlock));
				}

				frame.setBlock(handBlock);
			}

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new FrameBlockEntity(pos, state);
	}

	// The frames don't look exactly like the block they are mimicking,
	// but the goal here is just to test the behavior with the pillar's connected textures. ;-)
	@Override
	public BlockState getAppearance(BlockState state, BlockRenderView renderView, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
		// For this specific block, the render data works on both the client and the server, so let's use that.
		if (((FabricBlockView) renderView).getBlockEntityRenderData(pos) instanceof Block mimickedBlock) {
			return mimickedBlock.getDefaultState();
		}

		return state;
	}
}
