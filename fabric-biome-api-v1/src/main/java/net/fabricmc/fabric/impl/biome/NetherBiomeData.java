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

package net.fabricmc.fabric.impl.biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

/**
 * Internal data for modding Vanilla's {@link MultiNoiseBiomeSourceParameterList.Preset#NETHER}.
 */
public final class NetherBiomeData {
	// Cached sets of the biomes that would generate from Vanilla's default biome source without consideration
	// for data packs (as those would be distinct biome sources).
	private static final Set<ResourceKey<Biome>> NETHER_BIOMES = new HashSet<>();

	private static final Map<ResourceKey<Biome>, Climate.ParameterPoint> NETHER_BIOME_NOISE_POINTS = new HashMap<>();

	private NetherBiomeData() {
	}

	public static void addNetherBiome(ResourceKey<Biome> biome, Climate.ParameterPoint spawnNoisePoint) {
		Preconditions.checkArgument(biome != null, "Biome is null");
		Preconditions.checkArgument(spawnNoisePoint != null, "MultiNoiseUtil.NoiseValuePoint is null");
		NETHER_BIOME_NOISE_POINTS.put(biome, spawnNoisePoint);
		clearBiomeSourceCache();
	}

	public static boolean canGenerateInNether(ResourceKey<Biome> biome) {
		return MultiNoiseBiomeSourceParameterList.Preset.NETHER.usedBiomes().anyMatch(input -> input.equals(biome));
	}

	private static void clearBiomeSourceCache() {
		NETHER_BIOMES.clear(); // Clear cached biome source data
	}

	public static <T> Climate.ParameterList<T> withModdedBiomeEntries(Climate.ParameterList<T> entries, Function<ResourceKey<Biome>, T> biomes) {
		if (NETHER_BIOME_NOISE_POINTS.isEmpty()) {
			return entries;
		}

		ArrayList<Pair<Climate.ParameterPoint, T>> entryList = new ArrayList<>(entries.values());

		for (Map.Entry<ResourceKey<Biome>, Climate.ParameterPoint> entry : NETHER_BIOME_NOISE_POINTS.entrySet()) {
			entryList.add(Pair.of(entry.getValue(), biomes.apply(entry.getKey())));
		}

		return new Climate.ParameterList<>(Collections.unmodifiableList(entryList));
	}
}
