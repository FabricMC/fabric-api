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

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.fabricmc.fabric.impl.biome.modification.BiomeModificationImpl;

/**
 * Provides an API to modify Biomes after they have been loaded and before they are used in the Level.
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
	public static void addFeature(Predicate<BiomeSelectionContext> biomeSelector, GenerationStep.Decoration step, ResourceKey<PlacedFeature> placedFeatureKey) {
		create(placedFeatureKey.identifier()).add(ModificationPhase.ADDITIONS, biomeSelector, context -> {
			context.getGenerationSettings().addFeature(step, placedFeatureKey);
		});
	}

	/**
	 * Convenience method to add a carver to one or more biomes.
	 *
	 * @see BiomeSelectors
	 */
	public static void addCarver(Predicate<BiomeSelectionContext> biomeSelector, ResourceKey<WorldCarver> carverKey) {
		create(carverKey.identifier()).add(ModificationPhase.ADDITIONS, biomeSelector, context -> {
			context.getGenerationSettings().addCarver(carverKey);
		});
	}

	/**
	 * Convenience method to add an entity spawn to one or more biomes.
	 *
	 * @see BiomeSelectors
	 * @see net.minecraft.world.level.biome.MobSpawnSettings.Builder#addSpawn(EntityType, MobCategory, int, IntProvider)
	 */
	public static void addSpawn(Predicate<BiomeSelectionContext> biomeSelector,
								MobCategory category, EntityType<?> entityType,
								int weight, int minGroupSize, int maxGroupSize) {
		// See constructor of SpawnSettings.SpawnEntry for context
		Preconditions.checkArgument(entityType.getCategory() != MobCategory.MISC,
				"Cannot add spawns for entities with category=MISC since they'd be replaced by pigs.");

		// We need the entity type to be registered, or we cannot deduce an ID otherwise
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
		Preconditions.checkState(BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).isPresent(), "Unregistered entity type: %s", entityType);

		create(id).add(ModificationPhase.ADDITIONS, biomeSelector, context -> {
			IntProvider count = minGroupSize == maxGroupSize ? ConstantInt.of(minGroupSize) : UniformInt.of(minGroupSize, maxGroupSize);
			context.getMobSpawnSettings().addSpawn(category, new MobSpawnSettings.SpawnerData(entityType, count), weight);
		});
	}

	/**
	 * Creates a copy of a biome with every biome modification applied to it, in the order a server applies them,
	 * leaving the biome in the registries as it is.
	 *
	 * <p>Biome modifications are applied as a server starts. Before then, such as on the world creation screen,
	 * this shows what a biome will be like in the world, for example which features it will place.
	 *
	 * <p>The registries must include the world's dimensions, since modifications can select biomes by the dimensions
	 * they generate in, such as {@link BiomeSelectors#foundInOverworld()}. On the world creation screen, the
	 * dimensions are kept apart from the registries, so add the ones the player has chosen:
	 *
	 * <pre>{@code
	 * RegistryAccess registries = context.worldgenRegistries()
	 *         .replaceFrom(RegistryLayer.DIMENSIONS, context.selectedDimensions().bake(context.datapackDimensions()).dimensionsRegistryAccess())
	 *         .compositeAccess();
	 * }</pre>
	 *
	 * <p>Copying a biome is costly, so don't call this while the game is running.
	 *
	 * @param registries registries a server hasn't modified yet, including the world's dimensions
	 * @param biome      the key of the biome to copy
	 * @return a copy of the biome, modified
	 * @throws IllegalStateException    if the registries are a server's, whose biomes are already modified
	 * @throws IllegalArgumentException if the registries don't include dimensions
	 */
	public static Biome createModifiedCopy(RegistryAccess registries, ResourceKey<Biome> biome) {
		return BiomeModificationImpl.INSTANCE.createModifiedCopy(registries, biome);
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
