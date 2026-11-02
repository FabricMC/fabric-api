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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifier;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifierType;

public record RemoveCarversBiomeModifier(HolderSet<Biome> biomes, HolderSet<WorldCarver> carvers) implements BiomeModifier {
	static final MapCodec<RemoveCarversBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			RegistryCodecs.holderSet(Registries.BIOME).fieldOf("biomes").forGetter(RemoveCarversBiomeModifier::biomes),
			RegistryCodecs.holderSet(Registries.CARVER).fieldOf("carvers").forGetter(RemoveCarversBiomeModifier::carvers)
	).apply(instance, RemoveCarversBiomeModifier::new));

	@Override
	public BiomeModifierType<?> getType() {
		return BiomeModifiersImpl.REMOVE_CARVERS;
	}

	@Override
	public ModificationPhase getPhase() {
		return ModificationPhase.REMOVALS;
	}

	@Override
	public boolean matches(BiomeSelectionContext context) {
		return biomes.contains(context.getBiomeHolder());
	}

	@Override
	public void modify(BiomeSelectionContext selection, BiomeModificationContext context) {
		for (Holder<WorldCarver> carver : carvers) {
			context.getGenerationSettings().removeCarver(carver.unwrapKey().orElseThrow());
		}
	}
}
