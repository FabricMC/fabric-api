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

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import net.fabricmc.fabric.impl.biome.MultiNoiseSamplerHooks;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin {
	@Unique
	private long seed;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(int horizontalSize, RandomState noiseConfig, int i, int j, NoiseSettings generationShapeConfig, DensityFunctions.BeardifierOrMarker arg, NoiseGeneratorSettings chunkGeneratorSettings, Aquifer.FluidPicker fluidLevelSampler, Blender blender, CallbackInfo ci) {
		seed = ((MultiNoiseSamplerHooks) (Object) noiseConfig.sampler()).fabric_getSeed();
	}

	@Inject(method = "cachedClimateSampler", at = @At("RETURN"))
	private void createMultiNoiseSampler(NoiseRouter noiseRouter, List<Climate.ParameterPoint> list, CallbackInfoReturnable<Climate.Sampler> cir) {
		((MultiNoiseSamplerHooks) (Object) cir.getReturnValue()).fabric_setSeed(seed);
	}
}
