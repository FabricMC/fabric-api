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

package net.fabricmc.fabric.test.biome;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.OverworldBiomeCreator;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.TheNetherBiomeCreator;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.EndPlacedFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;

public final class TestBiomes {
	public static final RegistryKey<Biome> EXAMPLE_BIOME = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(FabricBiomeTest.MOD_ID, "example_biome"));
	public static final RegistryKey<Biome> TEST_CRIMSON_FOREST = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(FabricBiomeTest.MOD_ID, "test_crimson_forest"));
	public static final RegistryKey<Biome> CUSTOM_PLAINS = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(FabricBiomeTest.MOD_ID, "custom_plains"));
	public static final RegistryKey<Biome> TEST_END_HIGHLANDS = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(FabricBiomeTest.MOD_ID, "test_end_highlands"));
	public static final RegistryKey<Biome> TEST_END_MIDLANDS = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(FabricBiomeTest.MOD_ID, "test_end_midlands"));
	public static final RegistryKey<Biome> TEST_END_BARRRENS = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(FabricBiomeTest.MOD_ID, "test_end_barrens"));

	private TestBiomes() {
	}

	public static void bootstrap(Registerable<Biome> biomeRegisterable) {
		RegistryEntryLookup<PlacedFeature> placedFeatures = biomeRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
		RegistryEntryLookup<ConfiguredCarver<?>> configuredCarvers = biomeRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER);

		biomeRegisterable.register(EXAMPLE_BIOME, createExample());
		biomeRegisterable.register(TEST_CRIMSON_FOREST, TheNetherBiomeCreator.createCrimsonForest(placedFeatures, configuredCarvers));
		biomeRegisterable.register(CUSTOM_PLAINS, OverworldBiomeCreator.createPlains(placedFeatures, configuredCarvers, false, false, false));
		biomeRegisterable.register(TEST_END_HIGHLANDS, createEndHighlands(placedFeatures));
		biomeRegisterable.register(TEST_END_MIDLANDS, createEndMidlands());
		biomeRegisterable.register(TEST_END_BARRRENS, createEndBarrens());
	}

	private static Biome createExample() {
		return new Biome.Builder()
				.temperature(0.8f)
				.downfall(0.4f)
				.precipitation(false)
				.setEnvironmentAttribute(EnvironmentAttributes.SKY_COLOR_VISUAL, 7907327)
				.setEnvironmentAttribute(EnvironmentAttributes.FOG_COLOR_VISUAL, 12638463)
				.setEnvironmentAttribute(EnvironmentAttributes.WATER_FOG_COLOR_VISUAL, 329011)
				.effects(
					new BiomeEffects.Builder()
						.waterColor(4159204)
						.build()
				)
				.spawnSettings(
					new SpawnSettings.Builder().build()
				)
				.generationSettings(
					new GenerationSettings.Builder().build()
				)
				.build();
	}

	private static Biome createEndHighlands(RegistryEntryLookup<PlacedFeature> placedFeatures) {
		GenerationSettings.Builder builder = new GenerationSettings.Builder()
				.feature(GenerationStep.Feature.SURFACE_STRUCTURES, placedFeatures.getOrThrow(EndPlacedFeatures.END_GATEWAY_RETURN));
		return composeEndSpawnSettings(builder);
	}

	private static Biome createEndMidlands() {
		GenerationSettings.Builder builder = (new GenerationSettings.Builder());
		return composeEndSpawnSettings(builder);
	}

	private static Biome createEndBarrens() {
		GenerationSettings.Builder builder = (new GenerationSettings.Builder());
		return composeEndSpawnSettings(builder);
	}

	private static Biome composeEndSpawnSettings(GenerationSettings.Builder builder) {
		SpawnSettings.Builder builder2 = new SpawnSettings.Builder();
		DefaultBiomeFeatures.addPlainsMobs(builder2);
		return new Biome.Builder()
				.precipitation(false)
				.temperature(0.5F)
				.downfall(0.5F)
				.setEnvironmentAttribute(EnvironmentAttributes.WATER_FOG_COLOR_VISUAL, 329011)
				.setEnvironmentAttribute(EnvironmentAttributes.FOG_COLOR_VISUAL, 10518688)
				.setEnvironmentAttribute(EnvironmentAttributes.SKY_COLOR_VISUAL, 0)
				.setEnvironmentAttribute(EnvironmentAttributes.AMBIENT_SOUNDS_AUDIO, AmbientSounds.CAVE)
				.effects(
					new BiomeEffects.Builder()
						.waterColor(4159204)
						.build()
				)
				.spawnSettings(builder2.build())
				.generationSettings(builder.build())
				.build();
	}
}
