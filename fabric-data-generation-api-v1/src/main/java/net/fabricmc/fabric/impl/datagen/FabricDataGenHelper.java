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

package net.fabricmc.fabric.impl.datagen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

public final class FabricDataGenHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(FabricDataGenHelper.class);

	/**
	 * When enabled the dedicated server startup will be hijacked to run the data generators and then quit.
	 */
	public static final boolean ENABLED = System.getProperty("fabric-api.datagen") != null;

	/**
	 * Sets the output directory for the generated data.
	 */
	private static final String OUTPUT_DIR = System.getProperty("fabric-api.datagen.output-dir");

	/**
	 * When enabled providers can enable extra validation, such as ensuring all registry entries have data generated for them.
	 */
	private static final boolean STRICT_VALIDATION = System.getProperty("fabric-api.datagen.strict-validation") != null;

	/**
	 * Filter to a specific mod ID with this property, useful if dependencies also have data generators.
	 */
	@Nullable
	private static final String MOD_ID_FILTER = System.getProperty("fabric-api.datagen.modid");

	/**
	 * Entrypoint key to register classes implementing {@link DataGeneratorEntrypoint}.
	 */
	private static final String ENTRYPOINT_KEY = "fabric-datagen";

	private FabricDataGenHelper() {
	}

	public static void run() {
		try {
			runInternal();
		} catch (Throwable t) {
			LOGGER.error(LogUtils.FATAL_MARKER, "Failed to run data generation", t);

			// Ensure we exit with a none zero exit code.
			System.exit(-1);
		}
	}

	private static void runInternal() {
		Path outputDir = Paths.get(Objects.requireNonNull(OUTPUT_DIR, "No output dir provided with the 'fabric-api.datagen.output-dir' property"));
		List<EntrypointContainer<DataGeneratorEntrypoint>> dataGeneratorInitializers = FabricLoader.getInstance()
				.getEntrypointContainers(ENTRYPOINT_KEY, DataGeneratorEntrypoint.class);
		if (dataGeneratorInitializers.isEmpty()) {
			LOGGER.warn("No data generator entrypoints are defined. Implement {} and add your class to the '{}' entrypoint key in your fabric.mod.json.",
					DataGeneratorEntrypoint.class.getName(), ENTRYPOINT_KEY);
		}

		// Ensure that the DataGeneratorEntrypoint is constructed on the main thread.
		final List<DataGeneratorEntrypoint> entrypoints = dataGeneratorInitializers.stream()
				.map(EntrypointContainer::getEntrypoint)
				.toList();
		CompletableFuture<HolderLookup.Provider> worldRegistriesFuture = CompletableFuture.supplyAsync(() -> createWorldLookupProvider(entrypoints), Util.backgroundExecutor());
		CompletableFuture<HolderLookup.Provider> registriesFuture = worldRegistriesFuture.thenApplyAsync(provider -> createReloadableLookupProvider(entrypoints, provider), Util.backgroundExecutor());

		List<EntrypointContainer<DataGeneratorEntrypoint>> filteredDataGeneratorInitializers = getFilteredInitializers(dataGeneratorInitializers);
		Object2IntOpenHashMap<String> jsonKeySortOrders = (Object2IntOpenHashMap<String>) DataProvider.FIXED_ORDER_FIELDS;
		FabricDataGenerator dataGenerator = new FabricDataGenerator(outputDir, STRICT_VALIDATION, worldRegistriesFuture, registriesFuture);

		for (EntrypointContainer<DataGeneratorEntrypoint> initializer : filteredDataGeneratorInitializers) {
			DataGeneratorEntrypoint entrypoint = initializer.getEntrypoint();
			ModContainer modContainer = initializer.getProvider();
			String effectiveModId = entrypoint.getEffectiveModId();

			if (effectiveModId != null) {
				modContainer = FabricLoader.getInstance()
						.getModContainer(effectiveModId)
						.orElseThrow(() -> new RuntimeException("Failed to find effective mod container for mod id (%s)".formatted(effectiveModId)));
			}

			entrypoint.addJsonKeySortOrders((key, value) -> {
				Objects.requireNonNull(key, "Tried to register a priority for a null key");
				jsonKeySortOrders.put(key, value);
			});
			dataGenerator.markModContainerAsActive(modContainer);
			entrypoint.onInitializeDataGenerator(dataGenerator);
		}

		dataGenerator.markModContainerAsActive(null);
		LOGGER.info(
				"Running data generator for {}",
				filteredDataGeneratorInitializers.stream()
						.map(entrypoint -> entrypoint.getProvider()
								.getMetadata()
								.getId()
						)
						.distinct()
						.toList()
		);

		try {
			dataGenerator.run();
		} catch (Throwable t) {
			throw new RuntimeException("Failed to run data generator", t);
		}
	}

	private static List<EntrypointContainer<DataGeneratorEntrypoint>> getFilteredInitializers(List<EntrypointContainer<DataGeneratorEntrypoint>> initializers) {
		if (MOD_ID_FILTER == null) {
			return initializers;
		}

		return initializers.stream()
				.filter(entrypoint -> entrypoint.getProvider().getMetadata().getId().equals(MOD_ID_FILTER))
				.toList();
	}

	private static HolderLookup.Provider createWorldLookupProvider(List<DataGeneratorEntrypoint> dataGeneratorInitializers) {
		RegistrySetBuilder registryBuilder = new RegistrySetBuilder();
		HashSet<ResourceKey<? extends Registry<?>>> vanillaWorldRegistries = new HashSet<>();
		VanillaRegistries.WORLD_BUILDER.entries.stream()
				.flatMap(RegistrySetBuilder.RegistryStub::requiredRegistries)
				.forEach(vanillaWorldRegistries::add);

		for (RegistryDataLoader.RegistryData<?> registry : DynamicRegistries.getWorldRegistries()) {
			if (!vanillaWorldRegistries.contains(registry.key())) {
				addEmptyRegistry(registryBuilder, registry.key());
			}
		}

		registryBuilder.entries.addAll(VanillaRegistries.WORLD_BUILDER.entries);

		for (DataGeneratorEntrypoint entrypoint : dataGeneratorInitializers) {
			entrypoint.buildRegistry(registryBuilder);
		}

		RegistryAccess.Frozen staticRegistries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		HolderLookup.Provider registryLookup = registryBuilder.build(staticRegistries);
		VanillaRegistries.validateThatAllBiomeFeaturesHaveBiomeFilter(registryLookup);
		return registryLookup;
	}

	private static HolderLookup.Provider createReloadableLookupProvider(List<DataGeneratorEntrypoint> dataGeneratorInitializers, HolderLookup.Provider registryLookup) {
		RegistrySetBuilder registryBuilder = new RegistrySetBuilder();
		HashSet<ResourceKey<? extends Registry<?>>> vanillaReloadableRegistries = new HashSet<>();
		VanillaRegistries.RELOADABLE_BUILDER.entries.stream()
				.flatMap(RegistrySetBuilder.RegistryStub::requiredRegistries)
				.forEach(vanillaReloadableRegistries::add);

		for (RegistryDataLoader.RegistryData<?> registry : DynamicRegistries.getReloadableRegistries()) {
			if (!vanillaReloadableRegistries.contains(registry.key())) {
				addEmptyRegistry(registryBuilder, registry.key());
			}
		}

		registryBuilder.entries.addAll(VanillaRegistries.RELOADABLE_BUILDER.entries);

		for (DataGeneratorEntrypoint entrypoint : dataGeneratorInitializers) {
			entrypoint.buildReloadableRegistry(registryBuilder);
		}

		return registryBuilder.build(registryLookup);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void addEmptyRegistry(RegistrySetBuilder registryBuilder, ResourceKey<? extends Registry<?>> key) {
		registryBuilder.add((ResourceKey) key, context -> { });
	}

	/**
	 * Used to keep track of conditions associated to generated objects.
	 */
	private static final Map<Object, ResourceCondition[]> CONDITIONS_MAP = new IdentityHashMap<>();

	public static void addConditions(Object object, ResourceCondition[] conditions) {
		CONDITIONS_MAP.merge(object, conditions, ArrayUtils::addAll);
	}

	@Nullable
	public static ResourceCondition[] consumeConditions(Object object) {
		return CONDITIONS_MAP.remove(object);
	}

	/**
	 * Adds {@code conditions} to {@code baseObject}.
	 * @param baseObject the base JSON object to which the conditions are inserted
	 * @param conditions the conditions to insert
	 * @throws IllegalArgumentException if the object already has conditions
	 */
	public static void addConditions(JsonObject baseObject, ResourceCondition... conditions) {
		if (baseObject.has(ResourceConditions.CONDITIONS_KEY)) {
			throw new IllegalArgumentException("Object already has a condition entry: " + baseObject);
		} else if (conditions == null || conditions.length == 0) {
			// Datagen might pass null conditions.
			return;
		}

		baseObject.add(ResourceConditions.CONDITIONS_KEY, ResourceCondition.LIST_CODEC.encodeStart(JsonOps.INSTANCE, Arrays.asList(conditions)).getOrThrow());
	}
}
