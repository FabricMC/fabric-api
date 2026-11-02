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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifier;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifierType;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifiers;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class BiomeModifiersImpl implements ModInitializer {
	private static final Map<Identifier, BiomeModifierType<?>> TYPES = new HashMap<>();

	public static final Codec<BiomeModifierType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(id -> {
		BiomeModifierType<?> type = TYPES.get(id);
		return type == null ? DataResult.error(() -> "Unknown biome modifier type: " + id) : DataResult.success(type);
	}, BiomeModifierType::id);

	static final BiomeModifierType<AddFeaturesBiomeModifier> ADD_FEATURES = BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "add_features"), AddFeaturesBiomeModifier.CODEC);
	static final BiomeModifierType<RemoveFeaturesBiomeModifier> REMOVE_FEATURES = BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "remove_features"), RemoveFeaturesBiomeModifier.CODEC);
	static final BiomeModifierType<AddSpawnsBiomeModifier> ADD_SPAWNS = BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "add_spawns"), AddSpawnsBiomeModifier.CODEC);
	static final BiomeModifierType<RemoveSpawnsBiomeModifier> REMOVE_SPAWNS = BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "remove_spawns"), RemoveSpawnsBiomeModifier.CODEC);
	static final BiomeModifierType<AddCarversBiomeModifier> ADD_CARVERS = BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "add_carvers"), AddCarversBiomeModifier.CODEC);
	static final BiomeModifierType<RemoveCarversBiomeModifier> REMOVE_CARVERS = BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "remove_carvers"), RemoveCarversBiomeModifier.CODEC);
	static final BiomeModifierType<SetAttributesBiomeModifier> SET_ATTRIBUTES = BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "set_attributes"), SetAttributesBiomeModifier.CODEC);
	static final BiomeModifierType<NoneBiomeModifier> NONE = BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "none"), MapCodec.unit(NoneBiomeModifier.INSTANCE));

	public static <T extends BiomeModifier> BiomeModifierType<T> registerType(Identifier id, MapCodec<T> codec) {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(codec, "codec");
		BiomeModifierType<T> type = new BiomeModifierTypeImpl<>(id, codec);

		if (TYPES.putIfAbsent(id, type) != null) {
			throw new IllegalArgumentException("Biome modifier type already registered: " + id);
		}

		return type;
	}

	@Override
	public void onInitialize() {
		DynamicRegistries.register(BiomeModifiers.REGISTRY_KEY, BiomeModifier.CODEC);
	}
}
