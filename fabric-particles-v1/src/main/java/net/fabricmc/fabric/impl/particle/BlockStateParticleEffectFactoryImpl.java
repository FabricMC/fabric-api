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

package net.fabricmc.fabric.impl.particle;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockStateParticleEffectFactoryImpl {
	private BlockStateParticleEffectFactoryImpl() {
	}

	public static BlockParticleOption create(ParticleType<BlockParticleOption> type, BlockState blockState, @Nullable BlockPos blockPos) {
		BlockParticleOption effect = new BlockParticleOption(type, blockState);
		((BlockStateParticleEffectExtension) effect).fabric_setBlockPos(blockPos);
		return effect;
	}
}
