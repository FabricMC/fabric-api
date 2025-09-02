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

package net.fabricmc.fabric.mixin.renderer.client.block.particle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockMarkerParticle;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(BlockMarkerParticle.class)
abstract class BlockMarkerParticleMixin {
	@Redirect(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/block/BlockState;)V", at = @At(value = "INVOKE", target = "net/minecraft/client/render/block/BlockModels.getModelParticleSprite(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/texture/Sprite;"))
	private static Sprite getModelParticleSpriteProxy(BlockModels models, BlockState state, ClientWorld world, double x, double y, double z, BlockState state1) {
		return models.getModelParticleSprite(state, world, BlockPos.ofFloored(x, y, z));
	}
}
