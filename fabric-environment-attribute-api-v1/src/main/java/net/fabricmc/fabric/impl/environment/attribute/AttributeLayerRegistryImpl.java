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

package net.fabricmc.fabric.impl.environment.attribute;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.environment.attribute.v1.AttributeLayerProvider;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;

public class AttributeLayerRegistryImpl {
	// Markes for each vanilla phase. Used to ensure vanilla ordering remains the same.
	private static final Map<Identifier, VanillaLayerMarker> MARKERS = Map.copyOf(Stream.of(VanillaLayerMarker.values())
			.collect(Collectors.toMap(marker -> marker.id, marker -> marker)));

	private static final Map<Identifier, AttributeLayerProvider> PROVIDER_MAP = new HashMap<>();
	private static final Set<Dependency> DEPENDENCIES = new HashSet<>();

	// Lock used to ensure thread safety.
	private static final Object LOCK = new Object();

	// As long as this is true, we skip sorting and inserting providers all together.
	// Becomes false once a modded provider is registered.
	private static volatile boolean hasOnlyVanillaMarkers;

	// As long as this is true, the ordering in the fields below is valid.
	// Becomes false once a modded provider is registered or once a depencency order is added.
	private static volatile boolean orderValid;

	// Layers that should go before vanilla providers.
	private static final List<AttributeLayerProvider> FIRST_PHASES = new ArrayList<>();

	// Layers that should go in between or after vanilla providers.
	private static final Map<VanillaLayerMarker, List<AttributeLayerProvider>> AFTER_VANILLA_PHASES = new EnumMap<>(VanillaLayerMarker.class);

	static {
		// Initialize sorted phase lists
		for (VanillaLayerMarker layer : VanillaLayerMarker.values()) {
			AFTER_VANILLA_PHASES.put(layer, new ArrayList<>());
		}

		// Register vanilla ordering
		registerLayerProvider(AttributeLayerProvider.DIMENSIONS, VanillaLayerMarker.DIMENSION);
		registerLayerProvider(AttributeLayerProvider.BIOMES, VanillaLayerMarker.BIOMES);
		registerLayerProvider(AttributeLayerProvider.TIMELINES, VanillaLayerMarker.TIMELINES);
		registerLayerProvider(AttributeLayerProvider.WEATHER, VanillaLayerMarker.WEATHER);

		addProviderOrdering(AttributeLayerProvider.DIMENSIONS, AttributeLayerProvider.BIOMES);
		addProviderOrdering(AttributeLayerProvider.BIOMES, AttributeLayerProvider.TIMELINES);
		addProviderOrdering(AttributeLayerProvider.TIMELINES, AttributeLayerProvider.WEATHER);

		// Validate cache
		hasOnlyVanillaMarkers = true; // Set to true here because registerLayerProvider used above sets it to false
		orderValid = true; // Vanilla provider are not included in sorted phase lists
	}

	public static void registerLayerProvider(Identifier id, AttributeLayerProvider provider) {
		Objects.requireNonNull(id, "The layer identifier should not be null.");
		Objects.requireNonNull(provider, "The provider should not be null.");

		if (PROVIDER_MAP.containsKey(id)) {
			throw new IllegalArgumentException("Layer with ID %s was already registered.".formatted(id));
		}

		synchronized (LOCK) {
			PROVIDER_MAP.put(id, provider);
			orderValid = false;
			hasOnlyVanillaMarkers = false;
		}
	}

	public static void addProviderOrdering(Identifier firstProvider, Identifier secondProvider) {
		Objects.requireNonNull(firstProvider, "The first provider identifier should not be null.");
		Objects.requireNonNull(secondProvider, "The second provider identifier should not be null.");

		if (firstProvider.equals(secondProvider)) {
			throw new IllegalArgumentException("Tried to make a provider depend on itself.");
		}

		synchronized (LOCK) {
			if (DEPENDENCIES.add(new Dependency(firstProvider, secondProvider))) {
				// Adding a dependency only affects order if both IDs are associated with registered providers.
				// Dependencies with missing registrations are simply ignored during sorting.

				if (PROVIDER_MAP.containsKey(firstProvider) && PROVIDER_MAP.containsKey(secondProvider)) {
					orderValid = false;
				}
			}
		}
	}

	private static void insertModdedLayers(List<AttributeLayerProvider> providers, EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		synchronized (LOCK) {
			for (AttributeLayerProvider provider : providers) {
				provider.addAttributeLayers(systemBuilder, level);
			}
		}
	}

	public static void addPreEverythingLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		if (!hasOnlyVanillaMarkers) {
			sortIfNeeded();
			insertModdedLayers(FIRST_PHASES, systemBuilder, level);
		}
	}

	public static void addPostDimensionLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		if (!hasOnlyVanillaMarkers) {
			sortIfNeeded();
			insertModdedLayers(AFTER_VANILLA_PHASES.get(VanillaLayerMarker.DIMENSION), systemBuilder, level);
		}
	}

	public static void addPostBiomesLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		if (!hasOnlyVanillaMarkers) {
			sortIfNeeded();
			insertModdedLayers(AFTER_VANILLA_PHASES.get(VanillaLayerMarker.BIOMES), systemBuilder, level);
		}
	}

	public static void addPostTimelinesLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		if (!hasOnlyVanillaMarkers) {
			sortIfNeeded();
			insertModdedLayers(AFTER_VANILLA_PHASES.get(VanillaLayerMarker.TIMELINES), systemBuilder, level);
		}
	}

	public static void addPostWeatherLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		if (!hasOnlyVanillaMarkers) {
			sortIfNeeded();
			insertModdedLayers(AFTER_VANILLA_PHASES.get(VanillaLayerMarker.WEATHER), systemBuilder, level);
		}
	}

	private static void sortIfNeeded() {
		Map<Identifier, LayerProvider> providersById;

		// Collect sorting data from registry
		synchronized (LOCK) {
			if (orderValid) {
				return;
			}

			providersById = new HashMap<>();

			for (Map.Entry<Identifier, AttributeLayerProvider> entry : PROVIDER_MAP.entrySet()) {
				providersById.put(entry.getKey(), new LayerProvider(entry.getKey(), entry.getValue()));
			}

			for (Dependency dependency : DEPENDENCIES) {
				LayerProvider firstLayerProvider = providersById.get(dependency.first());
				LayerProvider secondLayerProvider = providersById.get(dependency.second());

				if (firstLayerProvider != null && secondLayerProvider != null) {
					LayerProvider.link(firstLayerProvider, secondLayerProvider);
				}
			}
		}

		// Sort providers
		List<LayerProvider> sorted = new ArrayList<>(providersById.values());
		NodeSorting.sort(sorted, "environment attribute providers", AttributeLayerRegistryImpl::compareIds);

		// Categorize layer providers into vanilla phases
		synchronized (LOCK) {
			FIRST_PHASES.clear();
			AFTER_VANILLA_PHASES.forEach((_, list) -> list.clear());

			List<AttributeLayerProvider> phase = FIRST_PHASES;

			for (LayerProvider layerProvider : sorted) {
				AttributeLayerProvider provider = layerProvider.provider;

				if (provider instanceof VanillaLayerMarker marker) {
					phase = AFTER_VANILLA_PHASES.get(marker);
				} else {
					phase.add(provider);
				}
			}
		}
	}

	// Tiebreaker: put vanilla providers before others, and otherwise sort by lexicographic ordering
	// This also makes sure that providers that were not tied to vanilla ordering will come last in the ordering
	private static int compareIds(LayerProvider a, LayerProvider b) {
		Identifier idA = a.id;
		Identifier idB = b.id;

		VanillaLayerMarker markerA = MARKERS.get(idA);
		VanillaLayerMarker markerB = MARKERS.get(idB);

		// If both are vanilla providers, ensure they remain the same order as defined by Minecraft
		if (markerA != null && markerB != null) {
			return markerA.compareTo(markerB);
		}

		// If one of them is a vanilla layer and the other is not, then put the vanilla layer first
		if (markerA != null) {
			return -1;
		}

		if (markerB != null) {
			return 1;
		}

		// Otherwise just mess with the mod devs that like their mod IDs to start with an A
		return idA.compareTo(idB);
	}

	// Markers for vanilla providers. It's important that these enum constants stay in the order that vanilla providers should appear,
	// since this order will be used to fix dependency cycles (and we don't want a dependency cycle to mess up the order).
	private enum VanillaLayerMarker implements AttributeLayerProvider {
		DIMENSION(AttributeLayerProvider.DIMENSIONS),
		BIOMES(AttributeLayerProvider.BIOMES),
		TIMELINES(AttributeLayerProvider.TIMELINES),
		WEATHER(AttributeLayerProvider.WEATHER);

		final Identifier id;

		VanillaLayerMarker(Identifier id) {
			this.id = id;
		}

		@Override
		public void addAttributeLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
			// N/A, done through mixin
		}
	}

	private static class LayerProvider extends SortableNode<LayerProvider> {
		private final Identifier id;
		private final AttributeLayerProvider provider;

		private LayerProvider(Identifier id, AttributeLayerProvider provider) {
			this.id = id;
			this.provider = provider;
		}

		@Override
		protected String getDescription() {
			return id.toString();
		}
	}

	private record Dependency(Identifier first, Identifier second) {
	}
}
