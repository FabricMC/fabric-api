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

package net.fabricmc.fabric.impl.biome.modification.data;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifier;

public record AddSpawnsBiomeModifier(HolderSet<Biome> biomes, List<Weighted<MobSpawnSettings.SpawnerData>> spawners) implements BiomeModifier {
	static final MapCodec<AddSpawnsBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			RegistryCodecs.holderSet(Registries.BIOME).fieldOf("biomes").forGetter(AddSpawnsBiomeModifier::biomes),
			Weighted.codec(MobSpawnSettings.SpawnerData.CODEC).listOf().fieldOf("spawners").forGetter(AddSpawnsBiomeModifier::spawners)
	).apply(instance, AddSpawnsBiomeModifier::new));

	public AddSpawnsBiomeModifier {
		spawners = List.copyOf(spawners);
	}

	@Override
	public MapCodec<? extends BiomeModifier> codec() {
		return CODEC;
	}

	@Override
	public ModificationPhase getPhase() {
		return ModificationPhase.ADDITIONS;
	}

	@Override
	public boolean matches(BiomeSelectionContext context) {
		return biomes.contains(context.getBiomeHolder());
	}

	@Override
	public void modify(BiomeSelectionContext selection, BiomeModificationContext context) {
		for (Weighted<MobSpawnSettings.SpawnerData> spawner : spawners) {
			MobSpawnSettings.SpawnerData data = spawner.value();
			context.getMobSpawnSettings().addSpawn(data.type(), spawner.weight(), data.count());
		}
	}
}
