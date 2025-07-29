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

package net.fabricmc.fabric.test.entity.event;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TestBedBlock extends Block {
	private static final VoxelShape SHAPE = createCuboidShape(0, 0, 0, 16, 8, 16);
	public static final BooleanProperty OCCUPIED = Properties.OCCUPIED;

	public TestBedBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(OCCUPIED, false));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (state.get(OCCUPIED)) {
			player.sendMessage(Text.translatable("block.minecraft.bed.occupied"), true);
			return ActionResult.CONSUME;
		}

		if (world.getDimension().bedWorks()) {
			if (!world.isClient()) {
				player.trySleep(pos).ifLeft(sleepFailureReason -> {
					Text message = sleepFailureReason.getMessage();

					if (message != null) {
						player.sendMessage(message, true);
					}
				});
			}

			return ActionResult.CONSUME;
		}

		return ActionResult.PASS;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(OCCUPIED);
	}
}
