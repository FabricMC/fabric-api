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

package net.fabricmc.fabric.mixin.client.particle;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.particle.v1.ParticleRenderEvents;

// Implements ParticleRenderEvents.ALLOW_BLOCK_DUST_TINT
@Mixin(TerrainParticle.class)
abstract class TerrainParticleMixin extends SingleQuadParticle {
	@Shadow
	@Final
	private BlockPos pos;

	private TerrainParticleMixin() {
		super(null, 0, 0, 0, null);
	}

	@ModifyVariable(
			method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
			at = @At("LOAD"),
			argsOnly = true,
			slice = @Slice(
					from = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/TerrainParticle;bCol:F", ordinal = 0),
					to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
			),
			allow = 1
	)
	private BlockState removeUntintableParticles(BlockState state, @Local(argsOnly = true) ClientLevel world, @Local(argsOnly = true) BlockPos blockPos) {
		if (!ParticleRenderEvents.ALLOW_BLOCK_DUST_TINT.invoker().allowBlockDustTint(state, world, blockPos)) {
			// As of 1.20.1, vanilla hardcodes grass block particles to not get tinted.
			return Blocks.GRASS_BLOCK.defaultBlockState();
		}

		return state;
	}

	@Redirect(method = "createTerrainParticle", at = @At(value = "NEW", target = "(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/particle/TerrainParticle;"))
	private static TerrainParticle constructBlockDustParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, BlockParticleOption parameters, ClientLevel world1, double x1, double y1, double z1, double velocityX1, double velocityY1, double velocityZ1) {
		BlockPos blockPos = parameters.getBlockPos();

		if (blockPos != null) {
			return new TerrainParticle(world, x, y, z, velocityX, velocityY, velocityZ, state, blockPos);
		} else {
			return new TerrainParticle(world, x, y, z, velocityX, velocityY, velocityZ, state);
		}
	}
}
