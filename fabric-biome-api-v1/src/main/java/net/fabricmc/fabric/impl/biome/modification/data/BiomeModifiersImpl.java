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

import java.util.Objects;
import java.util.function.Function;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifier;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifiers;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class BiomeModifiersImpl implements ModInitializer {
	private static final BiMap<Identifier, MapCodec<? extends BiomeModifier>> TYPES = HashBiMap.create();

	private static final Codec<MapCodec<? extends BiomeModifier>> TYPE_CODEC = ExtraCodecs.idResolverCodec(
			Identifier.CODEC, TYPES::get, TYPES.inverse()::get
	);

	public static final Codec<BiomeModifier> CODEC = TYPE_CODEC.dispatch(BiomeModifier::codec, Function.identity());

	public static <T extends BiomeModifier> MapCodec<T> registerType(Identifier id, MapCodec<T> codec) {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(codec, "codec");

		if (TYPES.putIfAbsent(id, codec) != null) {
			throw new IllegalArgumentException("Biome modifier type already registered: " + id);
		}

		return codec;
	}

	@Override
	public void onInitialize() {
		BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "add_features"), AddFeaturesBiomeModifier.CODEC);
		BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "remove_features"), RemoveFeaturesBiomeModifier.CODEC);
		BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "add_spawns"), AddSpawnsBiomeModifier.CODEC);
		BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "remove_spawns"), RemoveSpawnsBiomeModifier.CODEC);
		BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "add_carvers"), AddCarversBiomeModifier.CODEC);
		BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "remove_carvers"), RemoveCarversBiomeModifier.CODEC);
		BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "set_attributes"), SetAttributesBiomeModifier.CODEC);
		BiomeModifiers.registerType(Identifier.fromNamespaceAndPath("fabric", "none"), NoneBiomeModifier.CODEC);

		DynamicRegistries.register(BiomeModifiers.REGISTRY_KEY, BiomeModifier.CODEC);
	}
}
