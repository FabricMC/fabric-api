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

import net.minecraft.block.BlockState;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectExtension;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin {
	@ModifyExpressionValue(method = "fall", at = @At(value = "NEW", target = "(Lnet/minecraft/particle/ParticleType;Lnet/minecraft/block/BlockState;)Lnet/minecraft/particle/BlockStateParticleEffect;"))
	private BlockStateParticleEffect modifyBlockStateParticleEffect(BlockStateParticleEffect original, double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
		((BlockStateParticleEffectExtension) original).fabric_setBlockPos(landedPosition);
		return original;
	}
}
