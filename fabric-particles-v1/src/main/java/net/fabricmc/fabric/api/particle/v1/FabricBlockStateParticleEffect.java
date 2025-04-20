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

package net.fabricmc.fabric.api.particle.v1;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectFactoryImpl;

/**
 * Note: This interface is automatically implemented on {@link BlockStateParticleEffect} via Mixin and interface injection.
 */
public interface FabricBlockStateParticleEffect {
	/**
	 * Alternative for {@link BlockStateParticleEffect#BlockStateParticleEffect(ParticleType, BlockState)} that also
	 * accepts a {@link BlockPos}. This method should be used instead of the vanilla constructor when the block state
	 * is retrieved using a block pos, most commonly through {@link BlockView#getBlockState(BlockPos)}. This ensures
	 * that any particles created from this effect use an accurate pos for any client-side logic.
	 *
	 * <p>If an instance with a non-null block pos needs to be synced to the client, the block pos will only be synced
	 * if it is known that the client supports decoding it (has this Fabric API module installed); otherwise, the effect
	 * will be sent as a vanilla effect and the client will produce a null block pos.
	 *
	 * @param type the particle type
	 * @param blockState the block state
	 * @param blockPos the block pos from which the block state was retrieved
	 * @return the particle effect
	 */
	static BlockStateParticleEffect create(ParticleType<BlockStateParticleEffect> type, BlockState blockState, @Nullable BlockPos blockPos) {
		return BlockStateParticleEffectFactoryImpl.create(type, blockState, blockPos);
	}

	/**
	 * @return the block pos from which {@linkplain BlockStateParticleEffect#getBlockState() the block state} was
	 * retrieved, or {@code null} if not applicable or this instance was synced from a remote server that does not have
	 * this Fabric API module installed
	 */
	@Nullable
	default BlockPos getBlockPos() {
		throw new AssertionError("Implemented in Mixin");
	}
}
