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

import java.util.function.Predicate;

import com.google.common.base.Preconditions;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * Provides an API to modify Biomes after they have been loaded and before they are used in the World.
 *
 * <p>Any modifications made to biomes will not be available for use in the demo level.
 */
public final class BiomeModifications {
	private BiomeModifications() {
	}

	/**
	 * Convenience method to add a feature to one or more biomes.
	 *
	 * @see BiomeSelectors
	 */
	public static void addFeature(Predicate<BiomeSelectionContext> biomeSelector, GenerationStep.Feature step, RegistryKey<PlacedFeature> placedFeatureRegistryKey) {
		create(placedFeatureRegistryKey.getValue()).add(ModificationPhase.ADDITIONS, biomeSelector, context -> {
			context.getGenerationSettings().addFeature(step, placedFeatureRegistryKey);
		});
	}

	/**
	 * Convenience method to add a carver to one or more biomes.
	 *
	 * @see BiomeSelectors
	 */
	public static void addCarver(Predicate<BiomeSelectionContext> biomeSelector, RegistryKey<ConfiguredCarver<?>> configuredCarverKey) {
		create(configuredCarverKey.getValue()).add(ModificationPhase.ADDITIONS, biomeSelector, context -> {
			context.getGenerationSettings().addCarver(configuredCarverKey);
		});
	}

	/**
	 * Convenience method to add an entity spawn to one or more biomes.
	 *
	 * @see BiomeSelectors
	 * @see net.minecraft.world.biome.SpawnSettings.Builder#spawn(SpawnGroup, int, SpawnSettings.SpawnEntry)
	 */
	public static void addSpawn(Predicate<BiomeSelectionContext> biomeSelector,
								SpawnGroup spawnGroup, EntityType<?> entityType,
								int weight, int minGroupSize, int maxGroupSize) {
		// See constructor of SpawnSettings.SpawnEntry for context
		Preconditions.checkArgument(entityType.getSpawnGroup() != SpawnGroup.MISC,
				"Cannot add spawns for entities with spawnGroup=MISC since they'd be replaced by pigs.");

		// We need the entity type to be registered, or we cannot deduce an ID otherwise
		Identifier id = Registries.ENTITY_TYPE.getId(entityType);
		Preconditions.checkState(Registries.ENTITY_TYPE.getKey(entityType).isPresent(), "Unregistered entity type: %s", entityType);

		create(id).add(ModificationPhase.ADDITIONS, biomeSelector, context -> {
			context.getSpawnSettings().addSpawn(spawnGroup, new SpawnSettings.SpawnEntry(entityType, minGroupSize, maxGroupSize), weight);
		});
	}

	/**
	 * Creates a new biome modification which will be applied whenever biomes are loaded from data packs.
	 *
	 * @param id An identifier for the new set of biome modifications that is returned. Is used for
	 *           guaranteeing consistent ordering between the biome modifications added by different mods
	 *           (assuming they otherwise have the same phase).
	 */
	public static BiomeModification create(Identifier id) {
		return new BiomeModification(id);
	}
}
