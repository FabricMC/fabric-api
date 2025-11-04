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

package net.fabricmc.fabric.api.biome.v1;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import net.fabricmc.fabric.impl.biome.NetherBiomeData;

/**
 * API that exposes the internals of Minecraft's nether biome code.
 */
public final class NetherBiomes {
	private NetherBiomes() {
	}

	/**
	 * Adds a biome to the Nether generator.
	 *
	 * @param biome           The biome to add. Must not be null.
	 * @param mixedNoisePoint data about the given {@link Biome}'s spawning information in the nether.
	 * @see Climate.TargetPoint
	 */
	public static void addNetherBiome(ResourceKey<Biome> biome, Climate.TargetPoint mixedNoisePoint) {
		NetherBiomeData.addNetherBiome(biome, Climate.parameters(
				mixedNoisePoint.temperature(),
				mixedNoisePoint.humidity(),
				mixedNoisePoint.continentalness(),
				mixedNoisePoint.erosion(),
				mixedNoisePoint.depth(),
				mixedNoisePoint.weirdness(),
				0
		));
	}

	/**
	 * Adds a biome to the Nether generator.
	 *
	 * @param biome           The biome to add. Must not be null.
	 * @param mixedNoisePoint data about the given {@link Biome}'s spawning information in the nether.
	 * @see Climate.ParameterPoint
	 */
	public static void addNetherBiome(ResourceKey<Biome> biome, Climate.ParameterPoint mixedNoisePoint) {
		NetherBiomeData.addNetherBiome(biome, mixedNoisePoint);
	}

	/**
	 * Returns true if the given biome can generate in the nether, considering the Vanilla nether biomes,
	 * and any biomes added to the Nether by mods.
	 */
	public static boolean canGenerateInNether(ResourceKey<Biome> biome) {
		return NetherBiomeData.canGenerateInNether(biome);
	}
}
