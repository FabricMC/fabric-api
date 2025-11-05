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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.level.LevelAccessor;

import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectExtension;

@Mixin(ParticleUtils.class)
abstract class ParticleUtilsMixin {
	@ModifyExpressionValue(method = "spawnSmashAttackParticles", at = @At(value = "NEW", target = "(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/BlockParticleOption;"))
	private static BlockParticleOption modifyBlockStateParticleEffect(BlockParticleOption original, LevelAccessor world, BlockPos pos, int count) {
		((BlockStateParticleEffectExtension) original).fabric_setBlockPos(pos);
		return original;
	}
}
