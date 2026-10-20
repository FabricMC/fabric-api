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

package net.fabricmc.fabric.mixin.biome;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

import net.fabricmc.fabric.impl.biome.MultiNoiseSamplerHooks;

@Mixin(NoiseBasedChunkGenerator.class)
class NoiseBasedChunkGeneratorMixin {
	@ModifyExpressionValue(method = "buildTerrain(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/server/level/WorldGenRegion;Ljava/util/Set;Lnet/minecraft/world/level/levelgen/NoiseSettings;)Lnet/minecraft/world/level/chunk/ChunkAccess;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseRouter;createClimateSampler(Lnet/minecraft/world/level/levelgen/densityfunction/DensitySamplerSet;)Lnet/minecraft/world/level/biome/Climate$Sampler;"))
	private Climate.Sampler setSeed(Climate.Sampler sampler, @Local(argsOnly = true) RandomState randomState) {
		((MultiNoiseSamplerHooks) (Object) sampler).fabric_setSeed(randomState.seed());
		return sampler;
	}
}
