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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.particle.v1.FabricBlockStateParticleEffect;
import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectExtension;
import net.fabricmc.fabric.impl.particle.ExtendedBlockStateParticleEffectPacketCodec;

@Mixin(BlockStateParticleEffect.class)
abstract class BlockStateParticleEffectMixin implements FabricBlockStateParticleEffect, BlockStateParticleEffectExtension {
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

	@ModifyReturnValue(method = "createPacketCodec", at = @At("RETURN"))
	private static PacketCodec<? super RegistryByteBuf, BlockStateParticleEffect> modifyPacketCodec(PacketCodec<? super RegistryByteBuf, BlockStateParticleEffect> codec) {
		return new ExtendedBlockStateParticleEffectPacketCodec(codec);
	}
}
