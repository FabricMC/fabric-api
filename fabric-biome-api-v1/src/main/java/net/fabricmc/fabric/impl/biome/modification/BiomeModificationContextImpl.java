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
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;

public class BiomeModificationContextImpl implements BiomeModificationContext {
	private final RegistryAccess registries;
	private final Biome biome;
	private final WeatherContext weather;
	private final AttributesContext attributes;
	private final EffectsContext effects;
	private final GenerationSettingsContextImpl generationSettings;
	private final SpawnSettingsContextImpl spawnSettings;

	public BiomeModificationContextImpl(RegistryAccess registries, Biome biome) {
		this.registries = registries;
		this.biome = biome;
		this.weather = new WeatherContextImpl();
		this.attributes = new AttributesContextImpl();
		this.effects = new EffectsContextImpl();
		this.generationSettings = new GenerationSettingsContextImpl();
		this.spawnSettings = new SpawnSettingsContextImpl();
	}

	@Override
	public WeatherContext getWeather() {
		return weather;
	}

	@Override
	public AttributesContext getAttributes() {
		return attributes;
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
			biome.climateSettings = new Biome.ClimateSettings(hasPrecipitation, biome.climateSettings.temperature(), biome.climateSettings.temperatureModifier(), biome.climateSettings.downfall());
		}

		@Override
		public void setTemperature(float temperature) {
			biome.climateSettings = new Biome.ClimateSettings(biome.climateSettings.hasPrecipitation(), temperature, biome.climateSettings.temperatureModifier(), biome.climateSettings.downfall());
		}

		@Override
		public void setTemperatureModifier(Biome.TemperatureModifier temperatureModifier) {
			biome.climateSettings = new Biome.ClimateSettings(biome.climateSettings.hasPrecipitation(), biome.climateSettings.temperature(), Objects.requireNonNull(temperatureModifier), biome.climateSettings.downfall());
		}

		@Override
		public void setDownfall(float downfall) {
			biome.climateSettings = new Biome.ClimateSettings(biome.climateSettings.hasPrecipitation(), biome.climateSettings.temperature(), biome.climateSettings.temperatureModifier(), downfall);
		}
	}

	private class AttributesContextImpl implements AttributesContext {
		@Override
		public void addAll(EnvironmentAttributeMap map) {
			EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(biome.getAttributes());
			attributes.putAll(map);
			biome.attributes = attributes.build();
		}

		@Override
		public <T> void set(EnvironmentAttribute<T> key, T value) {
			EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(biome.getAttributes());
			attributes.set(key, value);
			biome.attributes = attributes.build();
		}

		@Override
		public <T, M> void setModifier(EnvironmentAttribute<T> key, AttributeModifier<T, M> modifier, M value) {
			EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(biome.getAttributes());
			attributes.modify(key, modifier, value);
			biome.attributes = attributes.build();
		}
	}

	private class EffectsContextImpl implements EffectsContext {
		private final BiomeSpecialEffects effects = biome.getSpecialEffects();

		@Override
		public void setFogColor(int color) {
			attributes.set(EnvironmentAttributes.FOG_COLOR, color);
		}

		@Override
		public void setWaterColor(int color) {
			effects.waterColor = color;
		}

		@Override
		public void setWaterFogColor(int color) {
			attributes.set(EnvironmentAttributes.WATER_FOG_COLOR, color);
		}

		@Override
		public void setSkyColor(int color) {
			attributes.set(EnvironmentAttributes.SKY_COLOR, color);
		}

		@Override
		public void setFoliageColor(Optional<Integer> color) {
			effects.foliageColorOverride = Objects.requireNonNull(color);
		}

		@Override
		public void setDryFoliageColor(Optional<Integer> color) {
			effects.dryFoliageColorOverride = Objects.requireNonNull(color);
		}

		@Override
		public void setGrassColor(Optional<Integer> color) {
			effects.grassColorOverride = Objects.requireNonNull(color);
		}

		@Override
		public void setGrassColorModifier(BiomeSpecialEffects.GrassColorModifier colorModifier) {
			effects.grassColorModifier = Objects.requireNonNull(colorModifier);
		}

		@Override
		public void setMusicVolume(float volume) {
			attributes.set(EnvironmentAttributes.MUSIC_VOLUME, volume);
		}
	}

	private class GenerationSettingsContextImpl implements GenerationSettingsContext {
		private final Registry<ConfiguredWorldCarver<?>> carvers = registries.lookupOrThrow(Registries.CONFIGURED_CARVER);
		private final Registry<PlacedFeature> features = registries.lookupOrThrow(Registries.PLACED_FEATURE);
		private final BiomeGenerationSettings generationSettings = biome.getGenerationSettings();

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
			generationSettings.featureSet = Suppliers.memoize(() -> {
				return generationSettings.features.stream().flatMap(HolderSet::stream).map(Holder::value).collect(Collectors.toSet());
			});
		}

		private void rebuildFlowerFeatures() {
			// Replace the supplier to force a rebuild next time its called.
			generationSettings.flowerFeatures = Suppliers.memoize(() -> {
				return generationSettings.features.stream().flatMap(HolderSet::stream).map(Holder::value).flatMap(PlacedFeature::getFeatures).filter((configuredFeature) -> {
					return configuredFeature.feature() == Feature.FLOWER;
				}).collect(ImmutableList.toImmutableList());
			});
		}

		@Override
		public boolean removeFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> placedFeatureKey) {
			PlacedFeature placedFeature = getEntry(features, placedFeatureKey).value();

			int stepIndex = step.ordinal();
			List<HolderSet<PlacedFeature>> featureSteps = generationSettings.features;

			if (stepIndex >= featureSteps.size()) {
				return false; // The step was not populated with any features yet
			}

			HolderSet<PlacedFeature> featuresInStep = featureSteps.get(stepIndex);
			List<Holder<PlacedFeature>> features = new ArrayList<>(featuresInStep.stream().toList());

			if (features.removeIf(feature -> feature.value() == placedFeature)) {
				featureSteps.set(stepIndex, HolderSet.direct(features));
				rebuildFeatures = true;

				return true;
			}

			return false;
		}

		@Override
		public void addFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> entry) {
			List<HolderSet<PlacedFeature>> featureSteps = generationSettings.features;
			int index = step.ordinal();

			// Add new empty lists for the generation steps that have no features yet
			while (index >= featureSteps.size()) {
				featureSteps.add(HolderSet.direct(Collections.emptyList()));
			}

			Holder.Reference<PlacedFeature> feature = getEntry(features, entry);

			// Don't add the feature if it's already present
			if (featureSteps.get(index).contains(feature)) {
				return;
			}

			featureSteps.set(index, plus(featureSteps.get(index), feature));

			// Ensure the list of flower features is up-to-date
			rebuildFeatures = true;
		}

		@Override
		public void addCarver(ResourceKey<ConfiguredWorldCarver<?>> entry) {
			// We do not need to delay evaluation of this since the registries are already fully built
			generationSettings.carvers = plus(generationSettings.carvers, getEntry(carvers, entry));
		}

		@Override
		public boolean removeCarver(ResourceKey<ConfiguredWorldCarver<?>> configuredCarverKey) {
			ConfiguredWorldCarver<?> carver = getEntry(carvers, configuredCarverKey).value();
			List<Holder<ConfiguredWorldCarver<?>>> genCarvers = new ArrayList<>(generationSettings.carvers.stream().toList());

			if (genCarvers.removeIf(entry -> entry.value() == carver)) {
				generationSettings.carvers = HolderSet.direct(genCarvers);
				return true;
			}

			return false;
		}

		private <T> HolderSet<T> plus(@Nullable HolderSet<T> values, Holder<T> entry) {
			if (values == null) return HolderSet.direct(entry);

			List<Holder<T>> list = new ArrayList<>(values.stream().toList());
			list.add(entry);
			return HolderSet.direct(list);
		}
	}

	/**
	 * Gets an entry from the given registry, assuming it's a registry loaded from data packs.
	 * Gives more helpful error messages if an entry is missing by checking if the modder
	 * forgot to data-gen the JSONs corresponding to their built-in objects.
	 */
	private static <T> Holder.Reference<T> getEntry(Registry<T> registry, ResourceKey<T> key) {
		Holder.Reference<T> entry = registry.get(key).orElse(null);

		if (entry == null) {
			// The key doesn't exist in the data packs
			throw new IllegalArgumentException("Couldn't find registry entry for " + key);
		}

		return entry;
	}

	private class SpawnSettingsContextImpl implements SpawnSettingsContext {
		private final MobSpawnSettings spawnSettings = biome.getMobSettings();
		private final EnumMap<MobCategory, List<Weighted<MobSpawnSettings.SpawnerData>>> fabricSpawners = new EnumMap<>(MobCategory.class);

		SpawnSettingsContextImpl() {
			unfreezeSpawners();
			unfreezeSpawnCost();
		}

		private void unfreezeSpawners() {
			fabricSpawners.clear();

			for (MobCategory spawnGroup : MobCategory.values()) {
				WeightedList<MobSpawnSettings.SpawnerData> entries = spawnSettings.spawners.get(spawnGroup);

				if (entries != null) {
					fabricSpawners.put(spawnGroup, new ArrayList<>(entries.unwrap()));
				} else {
					fabricSpawners.put(spawnGroup, new ArrayList<>());
				}
			}
		}

		private void unfreezeSpawnCost() {
			spawnSettings.mobSpawnCosts = new HashMap<>(spawnSettings.mobSpawnCosts);
		}

		public void freeze() {
			freezeSpawners();
			freezeSpawnCosts();
		}

		private void freezeSpawners() {
			Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners = new HashMap<>(spawnSettings.spawners);

			for (Map.Entry<MobCategory, List<Weighted<MobSpawnSettings.SpawnerData>>> entry : fabricSpawners.entrySet()) {
				if (entry.getValue().isEmpty()) {
					spawners.put(entry.getKey(), WeightedList.of());
				} else {
					spawners.put(entry.getKey(), WeightedList.of(entry.getValue()));
				}
			}

			spawnSettings.spawners = ImmutableMap.copyOf(spawners);
		}

		private void freezeSpawnCosts() {
			spawnSettings.mobSpawnCosts = ImmutableMap.copyOf(spawnSettings.mobSpawnCosts);
		}

		@Override
		public void setCreatureSpawnProbability(float probability) {
			spawnSettings.creatureGenerationProbability = probability;
		}

		@Override
		public @UnmodifiableView List<Weighted<MobSpawnSettings.SpawnerData>> getSpawnEntries(MobCategory spawnGroup) {
			Objects.requireNonNull(spawnGroup);

			return Collections.unmodifiableList(fabricSpawners.get(spawnGroup));
		}

		@Override
		public void addSpawn(MobCategory spawnGroup, MobSpawnSettings.SpawnerData spawnEntry, int weight) {
			Objects.requireNonNull(spawnGroup);
			Objects.requireNonNull(spawnEntry);

			fabricSpawners.get(spawnGroup).add(new Weighted<>(spawnEntry, weight));
		}

		@Override
		public boolean removeSpawns(BiPredicate<MobCategory, MobSpawnSettings.SpawnerData> predicate) {
			boolean anyRemoved = false;

			for (MobCategory group : MobCategory.values()) {
				if (fabricSpawners.get(group).removeIf(entry -> predicate.test(group, entry.value()))) {
					anyRemoved = true;
				}
			}

			return anyRemoved;
		}

		@Override
		public void setSpawnCost(EntityType<?> entityType, double mass, double gravityLimit) {
			Objects.requireNonNull(entityType);
			spawnSettings.mobSpawnCosts.put(entityType, new MobSpawnSettings.MobSpawnCost(gravityLimit, mass));
		}

		@Override
		public void clearSpawnCost(EntityType<?> entityType) {
			spawnSettings.mobSpawnCosts.remove(entityType);
		}
	}
}
