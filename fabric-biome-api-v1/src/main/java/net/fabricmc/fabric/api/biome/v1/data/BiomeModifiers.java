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

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.fabricmc.fabric.impl.biome.modification.data.AddCarversBiomeModifier;
import net.fabricmc.fabric.impl.biome.modification.data.AddFeaturesBiomeModifier;
import net.fabricmc.fabric.impl.biome.modification.data.AddSpawnsBiomeModifier;
import net.fabricmc.fabric.impl.biome.modification.data.BiomeModifiersImpl;
import net.fabricmc.fabric.impl.biome.modification.data.NoneBiomeModifier;
import net.fabricmc.fabric.impl.biome.modification.data.RemoveCarversBiomeModifier;
import net.fabricmc.fabric.impl.biome.modification.data.RemoveFeaturesBiomeModifier;
import net.fabricmc.fabric.impl.biome.modification.data.RemoveSpawnsBiomeModifier;
import net.fabricmc.fabric.impl.biome.modification.data.SetAttributesBiomeModifier;

/// Built-in data pack biome modifiers and registration of custom modifier types.
///
/// Place entries in `data/<namespace>/fabric/biome_modifier/<path>.json`.
/// Higher-priority packs replace entries with the same identifier. Use [#none()] to
/// disable a data modifier; this does not disable Java biome modification callbacks.
/// Generate entries using `FabricDynamicRegistryProvider` and [#REGISTRY_KEY].
/// Entries are loaded when a world starts and require a world restart to change.
///
/// For example, `data/modid/fabric/biome_modifier/add_ore.json`:
/// ```json
/// {
///   "type": "fabric:add_features",
///   "biomes": "#minecraft:is_overworld",
///   "features": "modid:ore_custom",
///   "step": "underground_ores"
/// }
/// ```
///
/// Registry selections accept an identifier, a list of identifiers, or a tag prefixed
/// with `#`. Use tags with optional entries to target biomes that may not exist.
/// Files also support Fabric resource conditions when the Resource Conditions API is present.
///
/// To generate the example above, add the following in a
/// `FabricDynamicRegistryProvider.configure` implementation:
/// ```java
/// entries.add(
///     ResourceKey.create(BiomeModifiers.REGISTRY_KEY, Identifier.fromNamespaceAndPath("modid", "add_ore")),
///     BiomeModifiers.addFeatures(
///         registries.lookupOrThrow(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD),
///         HolderSet.direct(registries.lookupOrThrow(Registries.PLACED_FEATURE).getOrThrow(
///             ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath("modid", "ore_custom"))
///         )),
///         GenerationStep.Decoration.UNDERGROUND_ORES
///     )
/// );
/// ```
public final class BiomeModifiers {
	/// The unsynced world registry of data pack biome modifiers.
	public static final ResourceKey<Registry<BiomeModifier>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("fabric", "biome_modifier"));

	private BiomeModifiers() {
	}

	/// Registers a modifier codec during mod initialization.
	///
	/// @param id the identifier used in the JSON `type` field
	/// @param codec the codec for the modifier's fields
	/// @return the registered codec
	/// @throws IllegalArgumentException if the identifier or codec is already registered
	public static <T extends BiomeModifier> MapCodec<T> registerType(Identifier id, MapCodec<T> codec) {
		return BiomeModifiersImpl.registerType(id, codec);
	}

	/// Creates a `fabric:none` modifier for disabling a data pack entry.
	public static BiomeModifier none() {
		return NoneBiomeModifier.INSTANCE;
	}

	/// Adds placed features at a decoration step.
	/// The JSON type is `fabric:add_features`.
	///
	/// @param biomes the target biomes
	/// @param features the placed features to add
	/// @param step the decoration step
	public static BiomeModifier addFeatures(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features, GenerationStep.Decoration step) {
		return new AddFeaturesBiomeModifier(biomes, features, step);
	}

	/// Removes placed features. Omitting `steps` in JSON selects all decoration steps.
	/// The JSON type is `fabric:remove_features`.
	///
	/// @param biomes the target biomes
	/// @param features the placed features to remove
	/// @param steps the decoration steps to remove features from
	public static BiomeModifier removeFeatures(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features, List<GenerationStep.Decoration> steps) {
		return new RemoveFeaturesBiomeModifier(biomes, features, steps);
	}

	/// Adds weighted mob spawns using vanilla spawner data, including the `count` provider.
	/// The JSON type is `fabric:add_spawns`.
	///
	/// @param biomes the target biomes
	/// @param spawners the weighted spawn entries, encoded as a JSON list
	public static BiomeModifier addSpawns(HolderSet<Biome> biomes, List<Weighted<MobSpawnSettings.SpawnerData>> spawners) {
		return new AddSpawnsBiomeModifier(biomes, spawners);
	}

	/// Removes all spawn entries for the selected entity types.
	/// The JSON type is `fabric:remove_spawns`.
	///
	/// @param biomes the target biomes
	/// @param entityTypes the entity types to remove, encoded as `entity_types`
	public static BiomeModifier removeSpawns(HolderSet<Biome> biomes, HolderSet<EntityType<?>> entityTypes) {
		return new RemoveSpawnsBiomeModifier(biomes, entityTypes);
	}

	/// Adds carvers to the selected biomes.
	/// The JSON type is `fabric:add_carvers`.
	///
	/// @param biomes the target biomes
	/// @param carvers the carvers to add
	public static BiomeModifier addCarvers(HolderSet<Biome> biomes, HolderSet<WorldCarver> carvers) {
		return new AddCarversBiomeModifier(biomes, carvers);
	}

	/// Removes carvers from the selected biomes.
	/// The JSON type is `fabric:remove_carvers`.
	///
	/// @param biomes the target biomes
	/// @param carvers the carvers to remove
	public static BiomeModifier removeCarvers(HolderSet<Biome> biomes, HolderSet<WorldCarver> carvers) {
		return new RemoveCarversBiomeModifier(biomes, carvers);
	}

	/// Replaces the specified biome attribute entries, preserving unspecified attributes.
	/// Vanilla attribute modifiers describe how these entries affect environmental values.
	/// The JSON type is `fabric:set_attributes`.
	///
	/// @param biomes the target biomes
	/// @param attributes the biome attribute entries to set
	public static BiomeModifier setAttributes(HolderSet<Biome> biomes, EnvironmentAttributeMap attributes) {
		return new SetAttributesBiomeModifier(biomes, attributes);
	}
}
