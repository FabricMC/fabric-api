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

import com.google.common.base.Preconditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

import net.fabricmc.fabric.impl.biome.MultiNoiseSamplerHooks;

@Mixin(Climate.Sampler.class)
public class ClimateSamplerMixin implements MultiNoiseSamplerHooks {
	@Unique
	private Long seed = null;

	@Unique
	private ImprovedNoise endBiomesSampler = null;

	@Override
	public void fabric_setSeed(long seed) {
		this.seed = seed;
	}

	@Override
	public long fabric_getSeed() {
		return this.seed;
	}

	@Override
	public ImprovedNoise fabric_getEndBiomesSampler() {
		if (endBiomesSampler == null) {
			Preconditions.checkState(seed != null, "MultiNoiseSampler doesn't have a seed set, created using different method?");
			endBiomesSampler = new ImprovedNoise(new WorldgenRandom(new LegacyRandomSource(seed)));
		}

		return endBiomesSampler;
	}
}
