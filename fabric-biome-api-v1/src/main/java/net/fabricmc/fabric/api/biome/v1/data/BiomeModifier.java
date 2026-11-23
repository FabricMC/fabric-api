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

package net.fabricmc.fabric.api.biome.v1.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.impl.biome.modification.data.BiomeModifiersImpl;

/// A biome modification loaded from a data pack. Custom types are registered with
/// [BiomeModifiers#registerType] during mod initialization.
///
/// Modifiers run alongside Java registrations in the existing [modification phases][ModificationPhase].
/// Within a phase, modifiers are ordered by their identifiers. Java registrations run first
/// when identifiers are equal. Changes take effect when a world is loaded, not on resource reload.
public interface BiomeModifier {
	/// A codec for encoding and decoding biome modifiers.
	Codec<BiomeModifier> CODEC = BiomeModifiersImpl.CODEC;

	/// Returns the registered codec used to encode this modifier.
	MapCodec<? extends BiomeModifier> codec();

	/// Returns the phase in which this modifier runs.
	ModificationPhase getPhase();

	/// Selects biomes after their registries and tags have loaded.
	boolean matches(BiomeSelectionContext context);

	/// Applies this modifier to a selected biome, including changes from preceding modifiers.
	void modify(BiomeSelectionContext selection, BiomeModificationContext context);
}
