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

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
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
import net.minecraft.world.level.levelgen.carver.WorldCarver;
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
	 * Returns the modification context for the biomes mob spawn settings.
	 */
	MobSpawnSettingsContext getMobSpawnSettings();

	/**
	 * Returns the {@link RegistryAccess} of the level the biomes are being modified for.
	 *
	 * @see BiomeSelectionContext#getRegistryAccess()
	 */
	RegistryAccess getRegistryAccess();

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
		 * @see Biome.BiomeBuilder#downfall(float)
		 */
		void setDownfall(float downfall);

		/**
		 * @see Biome.ClimateSettings#hasPrecipitation()
		 * @see Biome#hasPrecipitation()
		 */
		boolean hasPrecipitation();

		/**
		 * @see Biome.ClimateSettings#temperature()
		 * @see Biome#getBaseTemperature()
		 */
		float getTemperature();

		/**
		 * @see Biome.ClimateSettings#temperatureModifier()
		 */
		Biome.TemperatureModifier getTemperatureModifier();

		/**
		 * @see Biome.ClimateSettings#downfall()
		 */
		float getDownfall();
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

		/**
		 * Returns the attribute entry for the given attribute, or {@code null} if it is not present.
		 *
		 * <p>This reflects any {@link #addAll(EnvironmentAttributeMap)}, {@link #set(EnvironmentAttribute, Object)}
		 * or {@link #setModifier(EnvironmentAttribute, AttributeModifier, Object)} call performed earlier in
		 * the current modification pass.
		 *
		 * @see EnvironmentAttributeMap#get(EnvironmentAttribute)
		 */
		<T> EnvironmentAttributeMap.@Nullable Entry<T, ?> get(EnvironmentAttribute<T> attribute);

		/**
		 * Returns whether the given attribute is currently present.
		 *
		 * @see EnvironmentAttributeMap#contains(EnvironmentAttribute)
		 */
		default boolean contains(EnvironmentAttribute<?> attribute) {
			return this.get(attribute) != null;
		}

		/**
		 * Returns the current value of the given attribute, resolved using the attribute's default value
		 * when the attribute is not present.
		 *
		 * @see EnvironmentAttributeMap#applyModifier(EnvironmentAttribute, Object)
		 * @see EnvironmentAttribute#defaultValue()
		 */
		default <T> T getValue(EnvironmentAttribute<T> attribute) {
			return this.applyModifier(attribute, attribute.defaultValue());
		}

		/**
		 * Applies the current modifier of the given attribute to {@code value}, or returns {@code value}
		 * unchanged when the attribute is not present.
		 *
		 * @see EnvironmentAttributeMap#applyModifier(EnvironmentAttribute, Object)
		 */
		<T> T applyModifier(EnvironmentAttribute<T> attribute, T value);
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
		void setFoliageColorOverride(Optional<Integer> color);

		/**
		 * @see BiomeSpecialEffects#foliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#foliageColorOverride(int)
		 */
		default void setFoliageColorOverride(int color) {
			setFoliageColorOverride(Optional.of(color));
		}

		/**
		 * @see BiomeSpecialEffects#foliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#foliageColorOverride(int)
		 */
		default void setFoliageColorOverride(OptionalInt color) {
			color.ifPresentOrElse(this::setFoliageColorOverride, this::clearFoliageColorOverride);
		}

		/**
		 * @see BiomeSpecialEffects#foliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#foliageColorOverride(int)
		 */
		default void clearFoliageColorOverride() {
			setFoliageColorOverride(Optional.empty());
		}

		/**
		 * @see BiomeSpecialEffects#dryFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#dryFoliageColorOverride(int)
		 */
		void setDryFoliageColorOverride(Optional<Integer> color);

		/**
		 * @see BiomeSpecialEffects#dryFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#dryFoliageColorOverride(int)
		 */
		default void setDryFoliageColorOverride(int color) {
			setDryFoliageColorOverride(Optional.of(color));
		}

		/**
		 * @see BiomeSpecialEffects#dryFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#dryFoliageColorOverride(int)
		 */
		default void setDryFoliageColorOverride(OptionalInt color) {
			color.ifPresentOrElse(this::setDryFoliageColorOverride, this::clearDryFoliageColorOverride);
		}

		/**
		 * @see BiomeSpecialEffects#dryFoliageColorOverride()
		 * @see BiomeSpecialEffects.Builder#dryFoliageColorOverride(int)
		 */
		default void clearDryFoliageColorOverride() {
			setDryFoliageColorOverride(Optional.empty());
		}

		/**
		 * @see BiomeSpecialEffects#grassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		void setGrassColorOverride(Optional<Integer> color);

		/**
		 * @see BiomeSpecialEffects#grassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		default void setGrassColorOverride(int color) {
			setGrassColorOverride(Optional.of(color));
		}

		/**
		 * @see BiomeSpecialEffects#grassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		default void setGrassColorOverride(OptionalInt color) {
			color.ifPresentOrElse(this::setGrassColorOverride, this::clearGrassColorOverride);
		}

		/**
		 * @see BiomeSpecialEffects#grassColorOverride()
		 * @see BiomeSpecialEffects.Builder#grassColorOverride(int)
		 */
		default void clearGrassColorOverride() {
			setGrassColorOverride(Optional.empty());
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

		/**
		 * @see BiomeSpecialEffects#waterColor()
		 */
		int getWaterColor();

		/**
		 * @see BiomeSpecialEffects#foliageColorOverride()
		 */
		Optional<Integer> getFoliageColorOverride();

		/**
		 * @see BiomeSpecialEffects#dryFoliageColorOverride()
		 */
		Optional<Integer> getDryFoliageColorOverride();

		/**
		 * @see BiomeSpecialEffects#grassColorOverride()
		 */
		Optional<Integer> getGrassColorOverride();

		/**
		 * @see BiomeSpecialEffects#grassColorModifier()
		 */
		BiomeSpecialEffects.GrassColorModifier getGrassColorModifier();
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
		 * Adds a feature to one of this biomes generation steps, identified by the placed feature's resource key.
		 */
		void addFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> placedFeatureKey);

		/**
		 * Adds a world carver to this biome.
		 */
		void addCarver(ResourceKey<WorldCarver> carverKey);

		/**
		 * Removes all carvers with the given key from this biome.
		 *
		 * @return True if any carvers were removed.
		 */
		boolean removeCarver(ResourceKey<WorldCarver> carverKey);

		/**
		 * Returns an unmodifiable view of the placed features in the given generation step.
		 *
		 * <p>This reflects any {@link #addFeature(GenerationStep.Decoration, ResourceKey)} or
		 * {@link #removeFeature(GenerationStep.Decoration, ResourceKey)} call performed earlier in the
		 * current modification pass.
		 */
		@UnmodifiableView
		List<Holder<PlacedFeature>> getFeatures(GenerationStep.Decoration step);

		/**
		 * Returns whether the given feature is present in the given generation step.
		 */
		boolean hasFeature(GenerationStep.Decoration step, Holder<PlacedFeature> feature);

		/**
		 * Returns whether the given feature is present in any generation step.
		 */
		default boolean hasFeature(Holder<PlacedFeature> feature) {
			for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
				if (this.hasFeature(step, feature)) {
					return true;
				}
			}

			return false;
		}

		/**
		 * Returns an unmodifiable view of the carvers of this biome.
		 */
		@UnmodifiableView
		List<Holder<WorldCarver>> getCarvers();

		/**
		 * Returns whether the given carver is present in this biome.
		 */
		boolean hasCarver(Holder<WorldCarver> carver);
	}

	interface MobSpawnSettingsContext {
		/**
		 * Associated environment attribute: {@link EnvironmentAttributes#CREATURE_WORLD_GEN_SPAWN_PROBABILITY}.
		 */
		void setCreatureGenerationProbability(float probability);

		/**
		 * Provides a view of all spawns of the given category.
		 *
		 * <p>Associated environment attribute: {@link EnvironmentAttributes#NATURAL_MOB_SPAWNS}.
		 *
		 * @see MobSpawnSettings#getMobsToSpawn(MobCategory)
		 */
		@UnmodifiableView List<Weighted<MobSpawnSettings.SpawnerData>> getMobs(MobCategory category);

		/**
		 * Associated environment attribute: {@link EnvironmentAttributes#NATURAL_MOB_SPAWNS}.
		 *
		 * @see MobSpawnSettings#getMobsToSpawn(MobCategory)
		 * @see MobSpawnSettings.Builder
		 */
		void addSpawn(MobCategory category, MobSpawnSettings.SpawnerData data, int weight);

		/**
		 * Removes any spawns matching the given predicate from this biome, and returns true if any matched.
		 *
		 * <p>Associated environment attribute: {@link EnvironmentAttributes#NATURAL_MOB_SPAWNS}.
		 */
		boolean removeSpawns(BiPredicate<MobCategory, MobSpawnSettings.SpawnerData> predicate);

		/**
		 * Removes all spawns of the given entity type.
		 *
		 * <p>Associated environment attribute: {@link EnvironmentAttributes#NATURAL_MOB_SPAWNS}.
		 *
		 * @return True if any spawns were removed.
		 */
		default boolean removeSpawnsOfEntityType(EntityType<?> entityType) {
			return removeSpawns((category, spawnEntry) -> spawnEntry.type() == entityType);
		}

		/**
		 * Removes all spawns of the given category.
		 *
		 * <p>Associated environment attribute: {@link EnvironmentAttributes#NATURAL_MOB_SPAWNS}.
		 */
		default void clearSpawns(MobCategory category) {
			removeSpawns((mobCategory, spawnEntry) -> mobCategory == category);
		}

		/**
		 * Removes all spawns.
		 *
		 * <p>Associated environment attribute: {@link EnvironmentAttributes#NATURAL_MOB_SPAWNS}.
		 */
		default void clearSpawns() {
			removeSpawns((mobCategory, spawnEntry) -> true);
		}

		/**
		 * Associated environment attribute: {@link EnvironmentAttributes#NATURAL_MOB_SPAWNS}.
		 *
		 * @see MobSpawnSettings#getMobSpawnCost(EntityType)
		 * @see MobSpawnSettings.Builder#addMobSpawnCost(EntityType, double, double)
		 */
		void addMobCharge(EntityType<?> entityType, double charge, double energyBudget);

		/**
		 * Removes a spawn cost entry for a given entity type.
		 *
		 * <p>Associated environment attribute: {@link EnvironmentAttributes#NATURAL_MOB_SPAWNS}.
		 */
		void clearMobCharge(EntityType<?> entityType);

		/**
		 * Returns the spawn cost currently set for the given entity type, or {@code null} if none is set.
		 *
		 * <p>This reflects any {@link #addMobCharge(EntityType, double, double)} or
		 * {@link #clearMobCharge(EntityType)} call performed earlier in the current modification pass.
		 *
		 * <p>Associated environment attribute: {@link EnvironmentAttributes#NATURAL_MOB_SPAWNS}.
		 *
		 * @see MobSpawnSettings#getMobSpawnCost(EntityType)
		 */
		MobSpawnSettings.@Nullable MobSpawnCost getMobCharge(EntityType<?> entityType);

		/**
		 * Returns an unmodifiable view of all spawn costs currently set for this biome, keyed by entity type.
		 *
		 * <p>This reflects any {@link #addMobCharge(EntityType, double, double)} or
		 * {@link #clearMobCharge(EntityType)} call performed earlier in the current modification pass.
		 *
		 * <p>Associated environment attribute: {@link EnvironmentAttributes#NATURAL_MOB_SPAWNS}.
		 *
		 * @see MobSpawnSettings#allSpawnCosts()
		 */
		@UnmodifiableView
		Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> getMobCharges();

		/**
		 * Returns an unmodifiable view of all mob categories that currently have at least one spawn.
		 *
		 * @see MobSpawnSettings#getMobsInCategory(MobCategory)
		 */
		@UnmodifiableView
		default Set<MobCategory> getMobCategories() {
			Set<MobCategory> categories = EnumSet.noneOf(MobCategory.class);

			for (MobCategory category : MobCategory.values()) {
				if (!this.getMobs(category).isEmpty()) {
					categories.add(category);
				}
			}

			return Collections.unmodifiableSet(categories);
		}

		/**
		 * Returns an unmodifiable view of all spawns of this biome, grouped by mob category. Categories
		 * without spawns are omitted.
		 *
		 * @see MobSpawnSettings#getMobsInCategory(MobCategory)
		 */
		@UnmodifiableView
		default Map<MobCategory, List<Weighted<MobSpawnSettings.SpawnerData>>> getMobs() {
			Map<MobCategory, List<Weighted<MobSpawnSettings.SpawnerData>>> mobs = new EnumMap<>(MobCategory.class);

			for (MobCategory category : this.getMobCategories()) {
				mobs.put(category, this.getMobs(category));
			}

			return Collections.unmodifiableMap(mobs);
		}
	}
}
