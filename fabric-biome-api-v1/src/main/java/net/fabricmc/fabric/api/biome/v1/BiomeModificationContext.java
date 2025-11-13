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

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Allows {@link Biome} properties to be modified.
 */
public interface BiomeModificationContext {
	/**
	 * Returns the modification context for the biomes weather properties.
	 */
	WeatherContext getWeather();

	/**
	 * Returns the modification context for the biomes environment attributes.
	 */
	AttributesContext getAttributes();

	/**
	 * Returns the modification context for the biomes effects.
	 */
	EffectsContext getEffects();

	/**
	 * Returns the modification context for the biomes generation settings.
	 */
	GenerationSettingsContext getGenerationSettings();

	/**
	 * Returns the modification context for the biomes spawn settings.
	 */
	SpawnSettingsContext getSpawnSettings();

	interface WeatherContext {
		/**
		 * @see Biome#hasPrecipitation()
		 * @see Biome.BiomeBuilder#hasPrecipitation(boolean)
		 */
		void setPrecipitation(boolean hasPrecipitation);

		/**
		 * @see Biome#getBaseTemperature()
		 * @see Biome.BiomeBuilder#temperature(float)
		 */
		void setTemperature(float temperature);

		/**
		 * @see Biome.BiomeBuilder#temperatureAdjustment(Biome.TemperatureModifier)
		 */
		void setTemperatureModifier(Biome.TemperatureModifier temperatureModifier);

		/**
		 * @see Biome.ClimateSettings#downfall()
		 * @see Biome.BiomeBuilder#downfall(float)
		 */
		void setDownfall(float downfall);
	}

	interface AttributesContext {
		/**
		 * @see Biome.BiomeBuilder#putAttributes(EnvironmentAttributeMap)
		 */
		void addAll(EnvironmentAttributeMap map);

		/**
		 * @see Biome.BiomeBuilder#putAttributes(EnvironmentAttributeMap.Builder)
		 */
		default void addAll(EnvironmentAttributeMap.Builder map) {
			this.addAll(map.build());
		}

		/**
		 * @see Biome.BiomeBuilder#setAttribute(EnvironmentAttribute, Object)
		 */
		<T> void set(EnvironmentAttribute<T> key, T value);

		/**
		 * @see Biome.BiomeBuilder#modifyAttribute(EnvironmentAttribute, AttributeModifier, Object)
		 */
		<T, M> void setModifier(EnvironmentAttribute<T> key, AttributeModifier<T, M> modifier, M value);
	}

	interface EffectsContext {
		/**
		 * @deprecated Set the fog color using environment attributes instead
		 * @see BiomeModificationContext#getAttributes()
		 * @see EnvironmentAttributes#FOG_COLOR
		 */
		@Deprecated
		void setFogColor(int color);

		/**
		 * @see BiomeSpecialEffects#waterColor()
		 * @see BiomeSpecialEffects.Builder#waterColor(int)
		 */
		void setWaterColor(int color);

		/**
		 * @deprecated Set the water fog color using environment attributes instead
		 * @see BiomeModificationContext#getAttributes()
		 * @see EnvironmentAttributes#WATER_FOG_COLOR
		 */
		@Deprecated
		void setWaterFogColor(int color);

		/**
		 * @deprecated Set the sky color using environment attributes instead
		 * @see BiomeModificationContext#getAttributes()
		 * @see EnvironmentAttributes#SKY_COLOR
		 */
		@Deprecated
		void setSkyColor(int color);

		/**
		 * @see BiomeSpecialEffects#foliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#foliageColorOverride(int)
		 */
		void setFoliageColor(Optional<Integer> color);

		/**
		 * @see BiomeSpecialEffects#foliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#foliageColorOverride(int)
		 */
		default void setFoliageColor(int color) {
			setFoliageColor(Optional.of(color));
		}

		/**
		 * @see BiomeSpecialEffects#foliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#foliageColorOverride(int)
		 */
		default void setFoliageColor(OptionalInt color) {
			color.ifPresentOrElse(this::setFoliageColor, this::clearFoliageColor);
		}

		/**
		 * @see BiomeSpecialEffects#foliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#foliageColorOverride(int)
		 */
		default void clearFoliageColor() {
			setFoliageColor(Optional.empty());
		}

		/**
		 * @see BiomeSpecialEffects#dryFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#dryFoliageColorOverride(int)
		 */
		void setDryFoliageColor(Optional<Integer> color);

		/**
		 * @see BiomeSpecialEffects#dryFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#dryFoliageColorOverride(int)
		 */
		default void setDryFoliageColor(int color) {
			setDryFoliageColor(Optional.of(color));
		}

		/**
		 * @see BiomeSpecialEffects#dryFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#dryFoliageColorOverride(int)
		 */
		default void setDryFoliageColor(OptionalInt color) {
			color.ifPresentOrElse(this::setDryFoliageColor, this::clearDryFoliageColor);
		}

		/**
		 * @see BiomeSpecialEffects#dryFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#dryFoliageColorOverride(int)
		 */
		default void clearDryFoliageColor() {
			setDryFoliageColor(Optional.empty());
		}

		/**
		 * @see BiomeSpecialEffects#grassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		void setGrassColor(Optional<Integer> color);

		/**
		 * @see BiomeSpecialEffects#grassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		default void setGrassColor(int color) {
			setGrassColor(Optional.of(color));
		}

		/**
		 * @see BiomeSpecialEffects#grassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		default void setGrassColor(OptionalInt color) {
			color.ifPresentOrElse(this::setGrassColor, this::clearGrassColor);
		}

		/**
		 * @see BiomeSpecialEffects#grassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		default void clearGrassColor() {
			setGrassColor(Optional.empty());
		}

		/**
		 * @see BiomeSpecialEffects#grassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorModifier(BiomeSpecialEffects.GrassColorModifier)
		 */
		void setGrassColorModifier(BiomeSpecialEffects.GrassColorModifier colorModifier);

		/**
		 * @deprecated Set the music volume using environment attributes instead
		 * @see BiomeModificationContext#getAttributes()
		 * @see EnvironmentAttributes#MUSIC_VOLUME
		 */
		@Deprecated
		void setMusicVolume(float volume);
	}

	interface GenerationSettingsContext {
		/**
		 * Removes a feature from one of this biomes generation steps, and returns if any features were removed.
		 */
		boolean removeFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> placedFeatureKey);

		/**
		 * Removes a feature from all of this biomes generation steps, and returns if any features were removed.
		 */
		default boolean removeFeature(ResourceKey<PlacedFeature> placedFeatureKey) {
			boolean anyFound = false;

			for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
				if (removeFeature(step, placedFeatureKey)) {
					anyFound = true;
				}
			}

			return anyFound;
		}

		/**
		 * Adds a feature to one of this biomes generation steps, identified by the placed feature's registry key.
		 */
		void addFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> placedFeatureKey);

		/**
		 * Adds a configured carver to this biome.
		 */
		void addCarver(ResourceKey<ConfiguredWorldCarver<?>> carverKey);

		/**
		 * Removes all carvers with the given key from this biome.
		 *
		 * @return True if any carvers were removed.
		 */
		boolean removeCarver(ResourceKey<ConfiguredWorldCarver<?>> configuredCarverKey);
	}

	interface SpawnSettingsContext {
		/**
		 * Associated JSON property: <code>creature_spawn_probability</code>.
		 *
		 * @see MobSpawnSettings#getCreatureProbability()
		 * @see MobSpawnSettings.Builder#creatureGenerationProbability(float)
		 */
		void setCreatureSpawnProbability(float probability);

		/**
		 * Provides a view of all spawns of the given spawn group.
		 *
		 * <p>Associated JSON property: <code>spawners</code>.
		 *
		 * @see MobSpawnSettings#getMobs(MobCategory)
		 */
		@UnmodifiableView List<Weighted<MobSpawnSettings.SpawnerData>> getSpawnEntries(MobCategory spawnGroup);

		/**
		 * Associated JSON property: <code>spawners</code>.
		 *
		 * @see MobSpawnSettings#getMobs(MobCategory)
		 * @see MobSpawnSettings.Builder#addSpawn(MobCategory, int, MobSpawnSettings.SpawnerData)
		 */
		void addSpawn(MobCategory spawnGroup, MobSpawnSettings.SpawnerData spawnEntry, int weight);

		/**
		 * Removes any spawns matching the given predicate from this biome, and returns true if any matched.
		 *
		 * <p>Associated JSON property: <code>spawners</code>.
		 */
		boolean removeSpawns(BiPredicate<MobCategory, MobSpawnSettings.SpawnerData> predicate);

		/**
		 * Removes all spawns of the given entity type.
		 *
		 * <p>Associated JSON property: <code>spawners</code>.
		 *
		 * @return True if any spawns were removed.
		 */
		default boolean removeSpawnsOfEntityType(EntityType<?> entityType) {
			return removeSpawns((spawnGroup, spawnEntry) -> spawnEntry.type() == entityType);
		}

		/**
		 * Removes all spawns of the given spawn group.
		 *
		 * <p>Associated JSON property: <code>spawners</code>.
		 */
		default void clearSpawns(MobCategory group) {
			removeSpawns((spawnGroup, spawnEntry) -> spawnGroup == group);
		}

		/**
		 * Removes all spawns.
		 *
		 * <p>Associated JSON property: <code>spawners</code>.
		 */
		default void clearSpawns() {
			removeSpawns((spawnGroup, spawnEntry) -> true);
		}

		/**
		 * Associated JSON property: <code>spawn_costs</code>.
		 *
		 * @see MobSpawnSettings#getMobSpawnCost(EntityType)
		 * @see MobSpawnSettings.Builder#addMobCharge(EntityType, double, double)
		 */
		void setSpawnCost(EntityType<?> entityType, double mass, double gravityLimit);

		/**
		 * Removes a spawn cost entry for a given entity type.
		 *
		 * <p>Associated JSON property: <code>spawn_costs</code>.
		 */
		void clearSpawnCost(EntityType<?> entityType);
	}
}
