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

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectExtension;

@Mixin(Warden.class)
abstract class WardenMixin extends Monster implements VibrationSystem {
	private WardenMixin(EntityType<? extends Monster> entityType, Level world) {
		super(entityType, world);
	}

	@ModifyExpressionValue(method = "clientDiggingParticles", at = @At(value = "NEW", target = "(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/BlockParticleOption;"))
	private BlockParticleOption modifyBlockStateParticleEffect(BlockParticleOption original) {
		((BlockStateParticleEffectExtension) original).fabric_setBlockPos(getOnPos());
		return original;
	}
}
