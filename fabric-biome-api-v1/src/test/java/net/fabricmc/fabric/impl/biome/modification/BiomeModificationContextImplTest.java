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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.ARGB;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.MobSpawnSettingsModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;

class BiomeModificationContextImplTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void genericSpawnSettingsUpdateBeforeSpecializedUpdate() {
		for (GenericSpawnSettingsUpdate update : GenericSpawnSettingsUpdate.values()) {
			Biome biome = createBiome();
			BiomeModificationContextImpl context = createContext(biome);
			List<Weighted<MobSpawnSettings.SpawnerData>> creatureView = context.getMobSpawnSettings().getMobs(MobCategory.CREATURE);

			update.apply(context.getAttributes(), cowSpawns());
			assertEquals(List.of(EntityTypes.COW), getSpawnTypes(creatureView), update.name());

			context.getMobSpawnSettings().addSpawn(EntityTypes.ZOMBIE, 1, ConstantInt.of(1));
			context.freeze();

			MobSpawnSettings spawnSettings = resolveSpawnSettings(biome, baseSpawns());
			assertEquals(List.of(EntityTypes.COW), getSpawnTypes(spawnSettings, MobCategory.CREATURE), update.name());
			assertEquals(List.of(EntityTypes.ZOMBIE), getSpawnTypes(spawnSettings, MobCategory.MONSTER), update.name());
			assertEquals(
					update == GenericSpawnSettingsUpdate.SET_MODIFIER ? List.of(EntityTypes.BAT) : List.of(),
					getSpawnTypes(spawnSettings, MobCategory.AMBIENT),
					update.name()
			);
			assertEquals(update == GenericSpawnSettingsUpdate.SET_MODIFIER, spawnSettings.allSpawnCosts().containsKey(EntityTypes.PHANTOM), update.name());
		}
	}

	@Test
	void genericSpawnSettingsUpdateAfterSpecializedUpdate() {
		for (GenericSpawnSettingsUpdate update : GenericSpawnSettingsUpdate.values()) {
			Biome biome = createBiome();
			BiomeModificationContextImpl context = createContext(biome);

			context.getMobSpawnSettings().addSpawn(EntityTypes.ZOMBIE, 1, ConstantInt.of(1));
			update.apply(context.getAttributes(), cowSpawns());
			context.freeze();

			assertEquals(List.of(EntityTypes.COW), getSpawnTypes(biome, MobCategory.CREATURE), update.name());
			assertEquals(List.of(), getSpawnTypes(biome, MobCategory.MONSTER), update.name());
		}
	}

	@Test
	void readMethodsReflectLiveState() {
		RegistryAccess registries = mock(RegistryAccess.class);
		Registry<WorldCarver> carvers = mock(Registry.class);
		Registry<PlacedFeature> placedFeatures = mock(Registry.class);
		when(registries.lookupOrThrow(Registries.CARVER)).thenReturn(carvers);
		when(registries.lookupOrThrow(Registries.PLACED_FEATURE)).thenReturn(placedFeatures);

		ResourceKey<PlacedFeature> featureKey = ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath("fabric", "test_feature"));
		ResourceKey<WorldCarver> carverKey = ResourceKey.create(Registries.CARVER, Identifier.fromNamespaceAndPath("fabric", "test_carver"));
		@SuppressWarnings("unchecked")
		Holder.Reference<PlacedFeature> feature = mock(Holder.Reference.class);
		@SuppressWarnings("unchecked")
		Holder.Reference<WorldCarver> carver = mock(Holder.Reference.class);
		when(placedFeatures.get(featureKey)).thenReturn(Optional.of(feature));
		when(carvers.get(carverKey)).thenReturn(Optional.of(carver));

		Biome biome = createBiome();
		BiomeModificationContextImpl context = new BiomeModificationContextImpl(registries, biome);

		assertSame(registries, context.getRegistryAccess());

		BiomeModificationContext.WeatherContext weather = context.getWeather();
		weather.setPrecipitation(true);
		weather.setTemperature(1.5F);
		weather.setTemperatureModifier(Biome.TemperatureModifier.FROZEN);
		weather.setDownfall(2.5F);
		assertTrue(weather.hasPrecipitation());
		assertEquals(1.5F, weather.getTemperature());
		assertEquals(Biome.TemperatureModifier.FROZEN, weather.getTemperatureModifier());
		assertEquals(2.5F, weather.getDownfall());

		BiomeModificationContext.AttributesContext attributes = context.getAttributes();
		attributes.set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x112233));
		assertTrue(attributes.contains(EnvironmentAttributes.SKY_COLOR));
		assertEquals(ARGB.vector3fFromRGB24(0x112233), attributes.getValue(EnvironmentAttributes.SKY_COLOR));

		BiomeModificationContext.EffectsContext effects = context.getEffects();
		effects.setWaterColor(0x112233);
		effects.setFoliageColorOverride(0x445566);
		effects.setGrassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP);
		assertEquals(0x112233, effects.getWaterColor());
		assertEquals(Optional.of(0x445566), effects.getFoliageColorOverride());
		assertEquals(Optional.empty(), effects.getDryFoliageColorOverride());
		assertEquals(Optional.empty(), effects.getGrassColorOverride());
		assertEquals(BiomeSpecialEffects.GrassColorModifier.SWAMP, effects.getGrassColorModifier());

		BiomeModificationContext.MobSpawnSettingsContext spawnSettings = context.getMobSpawnSettings();
		assertNull(spawnSettings.getMobCharge(EntityTypes.ZOMBIE));
		spawnSettings.addMobCharge(EntityTypes.ZOMBIE, 1.0, 2.0);
		assertEquals(new MobSpawnSettings.MobSpawnCost(2.0, 1.0), spawnSettings.getMobCharge(EntityTypes.ZOMBIE));
		assertTrue(spawnSettings.getMobCharges().containsKey(EntityTypes.ZOMBIE));
		spawnSettings.clearMobCharge(EntityTypes.ZOMBIE);
		assertNull(spawnSettings.getMobCharge(EntityTypes.ZOMBIE));

		spawnSettings.addSpawn(EntityTypes.COW, 1, 1, 2);
		assertTrue(spawnSettings.getMobCategories().contains(MobCategory.CREATURE));
		assertTrue(spawnSettings.getMobs().containsKey(MobCategory.CREATURE));

		BiomeModificationContext.GenerationSettingsContext generationSettings = context.getGenerationSettings();
		generationSettings.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, featureKey);
		assertTrue(generationSettings.hasFeature(GenerationStep.Decoration.VEGETAL_DECORATION, feature));
		assertTrue(generationSettings.hasFeature(feature));
		assertEquals(List.of(feature), generationSettings.getFeatures(GenerationStep.Decoration.VEGETAL_DECORATION));
		generationSettings.removeFeature(GenerationStep.Decoration.VEGETAL_DECORATION, featureKey);
		assertFalse(generationSettings.hasFeature(feature));

		generationSettings.addCarver(carverKey);
		assertTrue(generationSettings.hasCarver(carver));
		assertEquals(List.of(carver), generationSettings.getCarvers());
	}

	private static BiomeModificationContextImpl createContext(Biome biome) {
		RegistryAccess registries = mock(RegistryAccess.class);
		Registry<WorldCarver> carvers = mock(Registry.class);
		Registry<PlacedFeature> placedFeatures = mock(Registry.class);
		when(registries.lookupOrThrow(Registries.CARVER)).thenReturn(carvers);
		when(registries.lookupOrThrow(Registries.PLACED_FEATURE)).thenReturn(placedFeatures);
		return new BiomeModificationContextImpl(registries, biome);
	}

	private static Biome createBiome() {
		return new Biome.BiomeBuilder()
				.hasPrecipitation(false)
				.temperature(0.5F)
				.downfall(0.5F)
				.specialEffects(new BiomeSpecialEffects.Builder().waterColor(0).build())
				.generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
				.build();
	}

	private static MobSpawnSettings cowSpawns() {
		return new MobSpawnSettings.Builder()
				.addSpawn(EntityTypes.COW, 1, ConstantInt.of(1))
				.build();
	}

	private static MobSpawnSettings baseSpawns() {
		return new MobSpawnSettings.Builder()
				.addSpawn(EntityTypes.BAT, 1, ConstantInt.of(1))
				.addMobSpawnCost(EntityTypes.PHANTOM, 1, 1)
				.build();
	}

	private static List<EntityType<?>> getSpawnTypes(Biome biome, MobCategory category) {
		return getSpawnTypes(resolveSpawnSettings(biome, EnvironmentAttributes.NATURAL_MOB_SPAWNS.defaultValue()), category);
	}

	private static MobSpawnSettings resolveSpawnSettings(Biome biome, MobSpawnSettings base) {
		return biome.getAttributes().applyModifier(
				EnvironmentAttributes.NATURAL_MOB_SPAWNS,
				base
		);
	}

	private static List<EntityType<?>> getSpawnTypes(MobSpawnSettings spawnSettings, MobCategory category) {
		return getSpawnTypes(spawnSettings.getMobsToSpawn(category).unwrap());
	}

	private static List<EntityType<?>> getSpawnTypes(List<Weighted<MobSpawnSettings.SpawnerData>> spawns) {
		return spawns.stream().map(Weighted::value).map(MobSpawnSettings.SpawnerData::type).toList();
	}

	private enum GenericSpawnSettingsUpdate {
		SET((attributes, spawnSettings) -> attributes.set(EnvironmentAttributes.NATURAL_MOB_SPAWNS, spawnSettings)),
		ADD_ALL((attributes, spawnSettings) -> attributes.addAll(
				EnvironmentAttributeMap.builder().set(EnvironmentAttributes.NATURAL_MOB_SPAWNS, spawnSettings)
		)),
		SET_MODIFIER((attributes, spawnSettings) -> attributes.setModifier(
				EnvironmentAttributes.NATURAL_MOB_SPAWNS,
				MobSpawnSettingsModifier.overlay(),
				spawnSettings
		));

		private final BiConsumer<BiomeModificationContext.AttributesContext, MobSpawnSettings> operation;

		GenericSpawnSettingsUpdate(BiConsumer<BiomeModificationContext.AttributesContext, MobSpawnSettings> operation) {
			this.operation = operation;
		}

		void apply(BiomeModificationContext.AttributesContext attributes, MobSpawnSettings spawnSettings) {
			operation.accept(attributes, spawnSettings);
		}
	}
}
