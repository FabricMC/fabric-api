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

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import net.fabricmc.fabric.impl.biome.TheEndBiomeData;

/**
 * API that exposes some internals of the minecraft default biome source for The End.
 *
 */
public final class TheEndBiomes {
	private TheEndBiomes() {
	}

	/**
	 * Adds the biome as a main end island biome with the specified weight; note that this includes the main island
	 * and some of the land encircling the empty space. Note that adding a biome to this region could potentially mess
	 * with the generation of the center island and cause it to generate incorrectly; this method only exists for
	 * consistency.
	 *
	 * @param biome  the biome to be added
	 * @param weight the weight of the entry. The weight in this method corresponds to its selection likelihood, with
	 *               heavier biomes being more likely to be selected and lighter biomes being selected with less likelihood.
	 *               Vanilla biomes have a weight of 1.0
	 */
	public static void addMainIslandBiome(RegistryKey<Biome> biome, double weight) {
		TheEndBiomeData.addEndBiomeReplacement(BiomeKeys.THE_END, biome, weight);
	}

	/**
	 * Adds the biome as an end highlands biome with the specified weight. End Highlands biomes make up the
	 * center region of the large outer islands in The End.
	 *
	 * @param biome  the biome to be added
	 * @param weight the weight of the entry. The weight in this method corresponds to its selection likelihood, with
	 *               heavier biomes being more likely to be selected and lighter biomes being selected with less likelihood.
	 *               The vanilla biome has a weight of 1.0.
	 */
	public static void addHighlandsBiome(RegistryKey<Biome> biome, double weight) {
		TheEndBiomeData.addEndBiomeReplacement(BiomeKeys.END_HIGHLANDS, biome, weight);
	}

	/**
	 * Adds a custom biome as a small end islands biome with the specified weight; small end island biomes
	 * make up the smaller islands in between the larger islands of the end.
	 *
	 * @param biome  the biome to be added
	 * @param weight the weight of the entry. The weight in this method corresponds to its selection likelihood, with
	 *               heavier biomes being more likely to be selected and lighter biomes being selected with less likelihood.
	 *               The vanilla biome has a weight of 1.0.
	 */
	public static void addSmallIslandsBiome(RegistryKey<Biome> biome, double weight) {
		TheEndBiomeData.addEndBiomeReplacement(BiomeKeys.SMALL_END_ISLANDS, biome, weight);
	}

	/**
	 * Adds the biome as an end midlands of the parent end highlands biome. End Midlands make up the area on
	 * the large outer islands between the highlands and the barrens and are similar to edge biomes in the
	 * overworld. If you don't call this method, the vanilla biome will be used by default.
	 *
	 * @param highlands The highlands biome to where the midlands biome is added
	 * @param midlands  the biome to be added as a midlands biome
	 * @param weight    the weight of the entry. The weight in this method corresponds to its selection likelihood, with
	 *                  heavier biomes being more likely to be selected and lighter biomes being selected with less likelihood.
	 *                  The vanilla biome has a weight of 1.0.
	 */
	public static void addMidlandsBiome(RegistryKey<Biome> highlands, RegistryKey<Biome> midlands, double weight) {
		TheEndBiomeData.addEndMidlandsReplacement(highlands, midlands, weight);
	}

	/**
	 * Adds the biome as an end barrens of the parent end highlands biome. End Midlands make up the area on
	 * the edge of the large outer islands and are similar to edge biomes in the overworld. If you don't call
	 * this method, the vanilla biome will be used by default.
	 *
	 * @param highlands The highlands biome to where the barrens biome is added
	 * @param barrens   the biome to be added as a barrens biome
	 * @param weight    the weight of the entry. The weight in this method corresponds to its selection likelihood, with
	 *                  heavier biomes being more likely to be selected and lighter biomes being selected with less likelihood.
	 *                  The vanilla biome has a weight of 1.0.
	 */
	public static void addBarrensBiome(RegistryKey<Biome> highlands, RegistryKey<Biome> barrens, double weight) {
		TheEndBiomeData.addEndBarrensReplacement(highlands, barrens, weight);
	}
}
