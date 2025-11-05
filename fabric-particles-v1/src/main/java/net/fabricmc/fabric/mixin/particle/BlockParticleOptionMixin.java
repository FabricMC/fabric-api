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

package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import net.fabricmc.fabric.api.particle.v1.FabricBlockStateParticleEffect;
import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectExtension;
import net.fabricmc.fabric.impl.particle.ExtendedBlockStateParticleEffectPacketCodec;

@Mixin(BlockParticleOption.class)
abstract class BlockParticleOptionMixin implements FabricBlockStateParticleEffect, BlockStateParticleEffectExtension {
	@Nullable
	@Unique
	private BlockPos blockPos;

	@Override
	@Nullable
	public BlockPos getBlockPos() {
		return blockPos;
	}

	@Override
	public void fabric_setBlockPos(@Nullable BlockPos pos) {
		blockPos = pos;
	}

	@ModifyReturnValue(method = "streamCodec", at = @At("RETURN"))
	private static StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> modifyPacketCodec(StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> codec) {
		return new ExtendedBlockStateParticleEffectPacketCodec(codec);
	}
}
