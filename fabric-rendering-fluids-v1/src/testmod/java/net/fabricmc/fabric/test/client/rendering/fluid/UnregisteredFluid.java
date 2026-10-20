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

package net.fabricmc.fabric.test.client.rendering.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.references.ItemIds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

public class UnregisteredFluid extends FlowingFluid {
	public UnregisteredFluid() {
		super(Properties.of().bucketItem(ItemIds.WATER_BUCKET).soundEvent(SoundEvents.BUCKET_FILL).explosionResistance(100.0F));
	}

	@Override
	protected boolean canConvertToSource(ServerLevel level) {
		return true;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
	}

	@Override
	public int getSlopeFindDistance(LevelReader level) {
		return 4;
	}

	@Override
	public BlockState createLegacyBlock(FluidState state) {
		return TestFluids.UNREGISTERED_BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
	}

	@Override
	public int getDropOff(LevelReader level) {
		return 1;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 5;
	}

	@Override
	public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, FluidState fluid, Direction direction) {
		return direction == Direction.DOWN && !fluid.is(TestFluids.NO_OVERLAY);
	}
}
