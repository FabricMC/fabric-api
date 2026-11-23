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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.impl.biome.modification.data.BiomeModifiersImpl;
import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;

/**
 * Each kind of selector adds a feature that the biomes checked here don't place in vanilla. The copies show those ores before a server has
 * modified the biomes, and match what the server's own pass makes.
 */
class ModifiedCopyTest {
	private static final ResourceKey<PlacedFeature> BY_KEY = placed("ore_emerald");
	private static final ResourceKey<PlacedFeature> IN_OVERWORLD = placed("ice_spike");
	private static final ResourceKey<PlacedFeature> IN_NETHER = placed("blue_ice");
	private static final ResourceKey<PlacedFeature> IN_END = placed("chorus_plant");
	private static final ResourceKey<PlacedFeature> BY_TAG = placed("basalt_pillar");
	private static final ResourceKey<PlacedFeature> AFTER_BY_KEY = placed("ore_gold_nether");
	private static final ResourceKey<PlacedFeature> AFTER_BY_HOLDER = placed("ore_magma");
	private static final float WARMED = 1.25F;

	private RegistryAccess registries;
	private RegistryAccess withoutDimensions;

	private static ResourceKey<PlacedFeature> placed(String path) {
		return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.withDefaultNamespace(path));
	}

	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		// What the module's initializer does in a game: the data pack registry of biome modifiers.
		new BiomeModifiersImpl().onInitialize();

		BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.PLAINS), GenerationStep.Decoration.UNDERGROUND_ORES, BY_KEY);
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, IN_OVERWORLD);
		BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Decoration.UNDERGROUND_ORES, IN_NETHER);
		BiomeModifications.addFeature(BiomeSelectors.foundInTheEnd(), GenerationStep.Decoration.UNDERGROUND_ORES, IN_END);
		BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_FOREST), GenerationStep.Decoration.UNDERGROUND_ORES, BY_TAG);
		// A later phase selects by what an earlier one added, so the selection must see the copy being modified.
		BiomeModifications.create(Identifier.fromNamespaceAndPath("fabric", "modified_copy_test"))
				.add(ModificationPhase.POST_PROCESSING, context -> context.hasPlacedFeature(BY_KEY),
						context -> context.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, AFTER_BY_KEY))
				// The same through the biome's holder, which on a server holds the biome being modified.
				.add(ModificationPhase.ADDITIONS, BiomeSelectors.includeByKey(Biomes.SNOWY_PLAINS),
						context -> context.getWeather().setTemperature(WARMED))
				.add(ModificationPhase.POST_PROCESSING, context -> context.getBiomeHolder().value().getBaseTemperature() == WARMED,
						context -> context.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, AFTER_BY_HOLDER));

		// A game start freezes the built-in registries before binding their tags; a unit test leaves them open.
		for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
			if (registry instanceof MappedRegistry<?> mapped) {
				try {
					mapped.bindAllTagsToEmpty();
					mapped.freeze();
				} catch (IllegalStateException alreadyFrozen) {
					// Frozen by bootstrap.
				}
			}
		}
	}

	// Fresh registries for each test, loaded from the vanilla data pack as a world being created has them.
	@BeforeEach
	void loadRegistries() {
		try (var manager = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(ServerPacksSource.createVanillaPackSource().fullResources()))) {
			TagLoader.loadTagsForExistingRegistries(manager, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY))
					.forEach(Registry.PendingTags::apply);
			LayeredRegistryAccess<RegistryLayer> layers = RegistryLayer.createRegistryAccess();
			List<HolderLookup.RegistryLookup<?>> context = TagLoader.buildUpdatedLookups(layers.getAccessForLoading(RegistryLayer.WORLD), List.of());
			RegistryAccess.Frozen world = RegistryDataLoader.load(manager, context, DynamicRegistriesImpl.getWorldRegistries(), Runnable::run).join();
			List<HolderLookup.RegistryLookup<?>> withWorld = Stream.concat(context.stream(), world.listRegistries()).toList();
			RegistryAccess.Frozen datapackDimensions = RegistryDataLoader.load(manager, withWorld, RegistryDataLoader.DIMENSION_REGISTRIES, Runnable::run).join();

			// As on the world creation screen: the registries without dimensions, and the chosen preset's dimensions
			// added as the API's documentation shows.
			LayeredRegistryAccess<RegistryLayer> worldgen = layers.replaceFrom(RegistryLayer.WORLD, world);
			withoutDimensions = worldgen.compositeAccess();
			WorldDimensions normal = withoutDimensions.lookupOrThrow(Registries.WORLD_PRESET).getValueOrThrow(WorldPresets.NORMAL).createWorldDimensions();
			registries = worldgen.replaceFrom(RegistryLayer.DIMENSIONS, normal.bake(datapackDimensions.lookupOrThrow(Registries.LEVEL_STEM)).dimensionsRegistryAccess())
					.compositeAccess();
		}
	}

	private Biome biome(ResourceKey<Biome> key) {
		return registries.lookupOrThrow(Registries.BIOME).getValueOrThrow(key);
	}

	private Biome copy(ResourceKey<Biome> key) {
		return BiomeModifications.createModifiedCopy(registries, key);
	}

	private static boolean places(Biome biome, ResourceKey<PlacedFeature> feature) {
		return biome.getGenerationSettings().features().stream().flatMap(step -> step.stream()).anyMatch(placed -> placed.is(feature));
	}

	private JsonElement json(Biome biome) {
		return Biome.DIRECT_CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), biome).getOrThrow();
	}

	@Test
	void theCopyHasTheModification() {
		assertFalse(places(biome(Biomes.PLAINS), BY_KEY), "vanilla plains have no emerald ore");
		assertTrue(places(copy(Biomes.PLAINS), BY_KEY));
	}

	@Test
	void theBiomeInTheRegistriesIsLeftAlone() {
		JsonElement before = json(biome(Biomes.PLAINS));
		copy(Biomes.PLAINS);
		assertEquals(before, json(biome(Biomes.PLAINS)));
	}

	@Test
	void unselectedBiomesAreCopiedUnchanged() {
		// The void generates in none of the normal world's dimensions, so no selector here picks it.
		assertEquals(json(biome(Biomes.THE_VOID)), json(copy(Biomes.THE_VOID)));
	}

	@Test
	void biomesAreSelectedByTheDimensionsTheyGenerateIn() {
		Biome plains = copy(Biomes.PLAINS);
		Biome netherWastes = copy(Biomes.NETHER_WASTES);
		Biome theEnd = copy(Biomes.THE_END);

		assertTrue(places(plains, IN_OVERWORLD));
		assertFalse(places(plains, IN_NETHER));
		assertFalse(places(plains, IN_END));

		assertTrue(places(netherWastes, IN_NETHER));
		assertFalse(places(netherWastes, IN_OVERWORLD));

		assertTrue(places(theEnd, IN_END));
		assertFalse(places(theEnd, IN_OVERWORLD));
	}

	@Test
	void biomesAreSelectedByTag() {
		assertTrue(places(copy(Biomes.FOREST), BY_TAG));
		assertFalse(places(copy(Biomes.PLAINS), BY_TAG));
	}

	@Test
	void laterPhasesSeeWhatEarlierOnesAdded() {
		assertTrue(places(copy(Biomes.PLAINS), AFTER_BY_KEY));
		assertFalse(places(copy(Biomes.DESERT), AFTER_BY_KEY));
	}

	@Test
	void laterPhasesSeeEarlierOnesThroughTheHolder() {
		assertTrue(places(copy(Biomes.SNOWY_PLAINS), AFTER_BY_HOLDER));
		assertFalse(places(copy(Biomes.PLAINS), AFTER_BY_HOLDER));
	}

	@Test
	void theCopiesMatchWhatTheServerMakes() {
		Map<ResourceKey<Biome>, JsonElement> copies = new HashMap<>();

		for (ResourceKey<Biome> key : registries.lookupOrThrow(Registries.BIOME).registryKeySet()) {
			copies.put(key, json(copy(key)));
		}

		// Making copies doesn't count as modifying the registries, so a server can still start from them.
		BiomeModificationImpl.INSTANCE.finalizeWorldGen(registries);
		copies.forEach((key, copy) -> assertEquals(json(biome(key)), copy, key.identifier().toString()));
	}

	@Test
	void modifiedRegistriesAreRefused() {
		BiomeModificationImpl.INSTANCE.finalizeWorldGen(registries);
		assertThrows(IllegalStateException.class, () -> copy(Biomes.PLAINS));
	}

	@Test
	void registriesWithoutDimensionsAreRefused() {
		assertThrows(IllegalArgumentException.class, () -> BiomeModifications.createModifiedCopy(withoutDimensions, Biomes.PLAINS));
	}
}
