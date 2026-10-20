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

package net.fabricmc.fabric.test.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.references.ItemIds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;

public class TestFluid extends LavaFluid {
	public TestFluid() {
		super(Properties.of()
				.bucketItem(ItemIds.WATER_BUCKET)
				.soundEvent(SoundEvents.BUCKET_FILL_LAVA)
				.explosionResistance(100.0F)
				.dripParticle(ParticleTypes.DRIPPING_LAVA)
				.randomTicking()
				.drag(0.5F)
		);
	}

	@Override
	public BlockState createLegacyBlock(FluidState state) {
		return BlockTest.TEST_FLUID_BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
	}

	@Override
	public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, FluidState fluid, Direction direction) {
		return direction == Direction.DOWN;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 5;
	}
}
