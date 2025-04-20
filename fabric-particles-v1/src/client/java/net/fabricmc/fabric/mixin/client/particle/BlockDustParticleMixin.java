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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.client.particle.v1.ParticleRenderEvents;

// Implements ParticleRenderEvents.ALLOW_BLOCK_DUST_TINT
@Mixin(BlockDustParticle.class)
abstract class BlockDustParticleMixin extends SpriteBillboardParticle {
	@Shadow
	@Final
	private BlockPos blockPos;

	private BlockDustParticleMixin() {
		super(null, 0, 0, 0);
	}

	@ModifyVariable(
			method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V",
			at = @At("LOAD"),
			argsOnly = true,
			slice = @Slice(
					from = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/BlockDustParticle;blue:F", ordinal = 0),
					to = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z")
			),
			allow = 1
	)
	private BlockState removeUntintableParticles(BlockState state) {
		if (!ParticleRenderEvents.ALLOW_BLOCK_DUST_TINT.invoker().allowBlockDustTint(state, world, blockPos)) {
			// As of 1.20.1, vanilla hardcodes grass block particles to not get tinted.
			return Blocks.GRASS_BLOCK.getDefaultState();
		}

		return state;
	}

	@Redirect(method = "create", at = @At(value = "NEW", target = "(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;)Lnet/minecraft/client/particle/BlockDustParticle;"))
	private static BlockDustParticle constructBlockDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, BlockStateParticleEffect parameters, ClientWorld world1, double x1, double y1, double z1, double velocityX1, double velocityY1, double velocityZ1) {
		BlockPos blockPos = parameters.getBlockPos();

		if (blockPos != null) {
			return new BlockDustParticle(world, x, y, z, velocityX, velocityY, velocityZ, state, blockPos);
		} else {
			return new BlockDustParticle(world, x, y, z, velocityX, velocityY, velocityZ, state);
		}
	}
}
