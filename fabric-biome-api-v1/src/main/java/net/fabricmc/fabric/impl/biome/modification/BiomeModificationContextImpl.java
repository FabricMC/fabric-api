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

package net.fabricmc.fabric.impl.biome.modification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.collection.Pool;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeParticleConfig;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;

public class BiomeModificationContextImpl implements BiomeModificationContext {
	private final DynamicRegistryManager registries;
	private final Biome biome;
	private final WeatherContext weather;
	private final EffectsContext effects;
	private final GenerationSettingsContextImpl generationSettings;
	private final SpawnSettingsContextImpl spawnSettings;

	public BiomeModificationContextImpl(DynamicRegistryManager registries, Biome biome) {
		this.registries = registries;
		this.biome = biome;
		this.weather = new WeatherContextImpl();
		this.effects = new EffectsContextImpl();
		this.generationSettings = new GenerationSettingsContextImpl();
		this.spawnSettings = new SpawnSettingsContextImpl();
	}

	@Override
	public WeatherContext getWeather() {
		return weather;
	}

	@Override
	public EffectsContext getEffects() {
		return effects;
	}

	@Override
	public GenerationSettingsContext getGenerationSettings() {
		return generationSettings;
	}

	@Override
	public SpawnSettingsContext getSpawnSettings() {
		return spawnSettings;
	}

	/**
	 * Re-freeze any immutable lists and perform general post-modification cleanup.
	 */
	void freeze() {
		generationSettings.freeze();
		spawnSettings.freeze();
	}

	boolean shouldRebuildFeatures() {
		return generationSettings.rebuildFeatures;
	}

	private class WeatherContextImpl implements WeatherContext {
		@Override
		public void setPrecipitation(boolean hasPrecipitation) {
			biome.weather = new Biome.Weather(hasPrecipitation, biome.weather.temperature(), biome.weather.temperatureModifier(), biome.weather.downfall());
		}

		@Override
		public void setTemperature(float temperature) {
			biome.weather = new Biome.Weather(biome.weather.hasPrecipitation(), temperature, biome.weather.temperatureModifier(), biome.weather.downfall());
		}

		@Override
		public void setTemperatureModifier(Biome.TemperatureModifier temperatureModifier) {
			biome.weather = new Biome.Weather(biome.weather.hasPrecipitation(), biome.weather.temperature(), Objects.requireNonNull(temperatureModifier), biome.weather.downfall());
		}

		@Override
		public void setDownfall(float downfall) {
			biome.weather = new Biome.Weather(biome.weather.hasPrecipitation(), biome.weather.temperature(), biome.weather.temperatureModifier(), downfall);
		}
	}

	private class EffectsContextImpl implements EffectsContext {
		private final BiomeEffects effects = biome.getEffects();

		@Override
		public void setFogColor(int color) {
			effects.fogColor = color;
		}

		@Override
		public void setWaterColor(int color) {
			effects.waterColor = color;
		}

		@Override
		public void setWaterFogColor(int color) {
			effects.waterFogColor = color;
		}

		@Override
		public void setSkyColor(int color) {
			effects.skyColor = color;
		}

		@Override
		public void setFoliageColor(Optional<Integer> color) {
			effects.foliageColor = Objects.requireNonNull(color);
		}

		@Override
		public void setGrassColor(Optional<Integer> color) {
			effects.grassColor = Objects.requireNonNull(color);
		}

		@Override
		public void setGrassColorModifier(@NotNull BiomeEffects.GrassColorModifier colorModifier) {
			effects.grassColorModifier = Objects.requireNonNull(colorModifier);
		}

		@Override
		public void setParticleConfig(Optional<BiomeParticleConfig> particleConfig) {
			effects.particleConfig = Objects.requireNonNull(particleConfig);
		}

		@Override
		public void setAmbientSound(Optional<RegistryEntry<SoundEvent>> sound) {
			effects.loopSound = Objects.requireNonNull(sound);
		}

		@Override
		public void setMoodSound(Optional<BiomeMoodSound> sound) {
			effects.moodSound = Objects.requireNonNull(sound);
		}

		@Override
		public void setAdditionsSound(Optional<BiomeAdditionsSound> sound) {
			effects.additionsSound = Objects.requireNonNull(sound);
		}

		@Override
		public void setMusic(Optional<DataPool<MusicSound>> sound) {
			effects.music = Objects.requireNonNull(sound);
		}

		@Override
		public void setMusicVolume(float volume) {
			effects.field_55050 = volume;
		}
	}

	private class GenerationSettingsContextImpl implements GenerationSettingsContext {
		private final Registry<ConfiguredCarver<?>> carvers = registries.getOrThrow(RegistryKeys.CONFIGURED_CARVER);
		private final Registry<PlacedFeature> features = registries.getOrThrow(RegistryKeys.PLACED_FEATURE);
		private final GenerationSettings generationSettings = biome.getGenerationSettings();

		boolean rebuildFeatures;

		/**
		 * Unfreeze the immutable lists found in the generation settings, and make sure they're filled up to every
		 * possible step if they're dense lists.
		 */
		GenerationSettingsContextImpl() {
			unfreezeFeatures();

			rebuildFeatures = false;
		}

		private void unfreezeFeatures() {
			generationSettings.features = new ArrayList<>(generationSettings.features);
		}

		/**
		 * Re-freeze the lists in the generation settings to immutable variants, also fixes the flower features.
		 */
		public void freeze() {
			freezeFeatures();

			if (rebuildFeatures) {
				rebuildFlowerFeatures();
			}
		}

		private void freezeFeatures() {
			generationSettings.features = ImmutableList.copyOf(generationSettings.features);
			// Replace the supplier to force a rebuild next time its called.
			generationSettings.allowedFeatures = Suppliers.memoize(() -> {
				return generationSettings.features.stream().flatMap(RegistryEntryList::stream).map(RegistryEntry::value).collect(Collectors.toSet());
			});
		}

		private void rebuildFlowerFeatures() {
			// Replace the supplier to force a rebuild next time its called.
			generationSettings.flowerFeatures = Suppliers.memoize(() -> {
				return generationSettings.features.stream().flatMap(RegistryEntryList::stream).map(RegistryEntry::value).flatMap(PlacedFeature::getDecoratedFeatures).filter((configuredFeature) -> {
					return configuredFeature.feature() == Feature.FLOWER;
				}).collect(ImmutableList.toImmutableList());
			});
		}

		@Override
		public boolean removeFeature(GenerationStep.Feature step, RegistryKey<PlacedFeature> placedFeatureKey) {
			PlacedFeature placedFeature = getEntry(features, placedFeatureKey).value();

			int stepIndex = step.ordinal();
			List<RegistryEntryList<PlacedFeature>> featureSteps = generationSettings.features;

			if (stepIndex >= featureSteps.size()) {
				return false; // The step was not populated with any features yet
			}

			RegistryEntryList<PlacedFeature> featuresInStep = featureSteps.get(stepIndex);
			List<RegistryEntry<PlacedFeature>> features = new ArrayList<>(featuresInStep.stream().toList());

			if (features.removeIf(feature -> feature.value() == placedFeature)) {
				featureSteps.set(stepIndex, RegistryEntryList.of(features));
				rebuildFeatures = true;

				return true;
			}

			return false;
		}

		@Override
		public void addFeature(GenerationStep.Feature step, RegistryKey<PlacedFeature> entry) {
			List<RegistryEntryList<PlacedFeature>> featureSteps = generationSettings.features;
			int index = step.ordinal();

			// Add new empty lists for the generation steps that have no features yet
			while (index >= featureSteps.size()) {
				featureSteps.add(RegistryEntryList.of(Collections.emptyList()));
			}

			featureSteps.set(index, plus(featureSteps.get(index), getEntry(features, entry)));

			// Ensure the list of flower features is up-to-date
			rebuildFeatures = true;
		}

		@Override
		public void addCarver(RegistryKey<ConfiguredCarver<?>> entry) {
			// We do not need to delay evaluation of this since the registries are already fully built
			generationSettings.carvers = plus(generationSettings.carvers, getEntry(carvers, entry));
		}

		@Override
		public boolean removeCarver(RegistryKey<ConfiguredCarver<?>> configuredCarverKey) {
			ConfiguredCarver<?> carver = getEntry(carvers, configuredCarverKey).value();
			List<RegistryEntry<ConfiguredCarver<?>>> genCarvers = new ArrayList<>(generationSettings.carvers.stream().toList());

			if (genCarvers.removeIf(entry -> entry.value() == carver)) {
				generationSettings.carvers = RegistryEntryList.of(genCarvers);
				return true;
			}

			return false;
		}

		private <T> RegistryEntryList<T> plus(@Nullable RegistryEntryList<T> values, RegistryEntry<T> entry) {
			if (values == null) return RegistryEntryList.of(entry);

			List<RegistryEntry<T>> list = new ArrayList<>(values.stream().toList());
			list.add(entry);
			return RegistryEntryList.of(list);
		}
	}

	/**
	 * Gets an entry from the given registry, assuming it's a registry loaded from data packs.
	 * Gives more helpful error messages if an entry is missing by checking if the modder
	 * forgot to data-gen the JSONs corresponding to their built-in objects.
	 */
	private static <T> RegistryEntry.Reference<T> getEntry(Registry<T> registry, RegistryKey<T> key) {
		RegistryEntry.Reference<T> entry = registry.getOptional(key).orElse(null);

		if (entry == null) {
			// The key doesn't exist in the data packs
			throw new IllegalArgumentException("Couldn't find registry entry for " + key);
		}

		return entry;
	}

	private class SpawnSettingsContextImpl implements SpawnSettingsContext {
		private final SpawnSettings spawnSettings = biome.getSpawnSettings();
		private final EnumMap<SpawnGroup, List<SpawnSettings.SpawnEntry>> fabricSpawners = new EnumMap<>(SpawnGroup.class);

		SpawnSettingsContextImpl() {
			unfreezeSpawners();
			unfreezeSpawnCost();
		}

		private void unfreezeSpawners() {
			fabricSpawners.clear();

			for (SpawnGroup spawnGroup : SpawnGroup.values()) {
				Pool<SpawnSettings.SpawnEntry> entries = spawnSettings.spawners.get(spawnGroup);

				if (entries != null) {
					fabricSpawners.put(spawnGroup, new ArrayList<>(entries.getEntries()));
				} else {
					fabricSpawners.put(spawnGroup, new ArrayList<>());
				}
			}
		}

		private void unfreezeSpawnCost() {
			spawnSettings.spawnCosts = new HashMap<>(spawnSettings.spawnCosts);
		}

		public void freeze() {
			freezeSpawners();
			freezeSpawnCosts();
		}

		private void freezeSpawners() {
			Map<SpawnGroup, Pool<SpawnSettings.SpawnEntry>> spawners = new HashMap<>(spawnSettings.spawners);

			for (Map.Entry<SpawnGroup, List<SpawnSettings.SpawnEntry>> entry : fabricSpawners.entrySet()) {
				if (entry.getValue().isEmpty()) {
					spawners.put(entry.getKey(), Pool.empty());
				} else {
					spawners.put(entry.getKey(), Pool.of(entry.getValue()));
				}
			}

			spawnSettings.spawners = ImmutableMap.copyOf(spawners);
		}

		private void freezeSpawnCosts() {
			spawnSettings.spawnCosts = ImmutableMap.copyOf(spawnSettings.spawnCosts);
		}

		@Override
		public void setCreatureSpawnProbability(float probability) {
			spawnSettings.creatureSpawnProbability = probability;
		}

		@Override
		public void addSpawn(SpawnGroup spawnGroup, SpawnSettings.SpawnEntry spawnEntry) {
			Objects.requireNonNull(spawnGroup);
			Objects.requireNonNull(spawnEntry);

			fabricSpawners.get(spawnGroup).add(spawnEntry);
		}

		@Override
		public boolean removeSpawns(BiPredicate<SpawnGroup, SpawnSettings.SpawnEntry> predicate) {
			boolean anyRemoved = false;

			for (SpawnGroup group : SpawnGroup.values()) {
				if (fabricSpawners.get(group).removeIf(entry -> predicate.test(group, entry))) {
					anyRemoved = true;
				}
			}

			return anyRemoved;
		}

		@Override
		public void setSpawnCost(EntityType<?> entityType, double mass, double gravityLimit) {
			Objects.requireNonNull(entityType);
			spawnSettings.spawnCosts.put(entityType, new SpawnSettings.SpawnDensity(gravityLimit, mass));
		}

		@Override
		public void clearSpawnCost(EntityType<?> entityType) {
			spawnSettings.spawnCosts.remove(entityType);
		}
	}
}
