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

	private static final Map<Identifier, AttributeLayerProvider> LAYER_MAP = new HashMap<>();
	private static final Set<Dependency> DEPENDENCIES = new HashSet<>();

	// Lock used to ensure thread safety.
	private static final Object LOCK = new Object();

	// As long as this is true, we skip sorting and inserting layers all together.
	// Becomes false once a modded layer is registered.
	private static volatile boolean hasOnlyVanillaLayers;

	// As long as this is true, the ordering in the fields below is valid.
	// Becomes false once a modded layer is registered or once a depencency order is added.
	private static volatile boolean orderValid;

	// Layers that should go before vanilla layers.
	private static final List<AttributeLayerProvider> FIRST_PHASES = new ArrayList<>();

	// Layers that should go in between or after vanilla layers.
	private static final Map<VanillaLayerMarker, List<AttributeLayerProvider>> AFTER_VANILLA_PHASES = new EnumMap<>(VanillaLayerMarker.class);

	static {
		// Initialize sorted phase lists
		for (VanillaLayerMarker layer : VanillaLayerMarker.values()) {
			AFTER_VANILLA_PHASES.put(layer, new ArrayList<>());
		}

		// Register vanilla ordering
		registerLayerProvider(AttributeLayerProvider.DIMENSION, VanillaLayerMarker.DIMENSION);
		registerLayerProvider(AttributeLayerProvider.BIOMES, VanillaLayerMarker.BIOMES);
		registerLayerProvider(AttributeLayerProvider.TIMELINES, VanillaLayerMarker.TIMELINES);
		registerLayerProvider(AttributeLayerProvider.WEATHER, VanillaLayerMarker.WEATHER);

		addLayerOrdering(AttributeLayerProvider.DIMENSION, AttributeLayerProvider.BIOMES);
		addLayerOrdering(AttributeLayerProvider.BIOMES, AttributeLayerProvider.TIMELINES);
		addLayerOrdering(AttributeLayerProvider.TIMELINES, AttributeLayerProvider.WEATHER);

		// Validate cache
		hasOnlyVanillaLayers = true; // Set to true here because registerLayerProvider used above sets it to false
		orderValid = true; // Vanilla layers are not included in sorted phase lists
	}

	public static void registerLayerProvider(Identifier id, AttributeLayerProvider layer) {
		Objects.requireNonNull(id, "The layer identifier should not be null.");
		Objects.requireNonNull(layer, "The layer should not be null.");

		if (LAYER_MAP.containsKey(id)) {
			throw new IllegalArgumentException("Layer with ID %s was already registered.".formatted(id));
		}

		synchronized (LOCK) {
			LAYER_MAP.put(id, layer);
			orderValid = false;
			hasOnlyVanillaLayers = false;
		}
	}

	public static void addLayerOrdering(Identifier firstLayer, Identifier secondLayer) {
		Objects.requireNonNull(firstLayer, "The first layer identifier should not be null.");
		Objects.requireNonNull(secondLayer, "The second layer identifier should not be null.");

		if (firstLayer.equals(secondLayer)) {
			throw new IllegalArgumentException("Tried to add a layer that depends on itself.");
		}

		synchronized (LOCK) {
			if (DEPENDENCIES.add(new Dependency(firstLayer, secondLayer))) {
				// Adding a dependency only affects order if both IDs are associated with registered layers.
				// Dependencies with missing registrations are simply ignored during sorting.

				if (LAYER_MAP.containsKey(firstLayer) && LAYER_MAP.containsKey(secondLayer)) {
					orderValid = false;
				}
			}
		}
	}

	private static void addLayers(List<AttributeLayerProvider> providers, EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		synchronized (LOCK) {
			for (AttributeLayerProvider provider : providers) {
				provider.addAttributeLayers(systemBuilder, level);
			}
		}
	}

	public static void addPreEverythingLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		if (!hasOnlyVanillaLayers) {
			sortIfNeeded();
			addLayers(FIRST_PHASES, systemBuilder, level);
		}
	}

	public static void addPostDimensionLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		if (!hasOnlyVanillaLayers) {
			sortIfNeeded();
			addLayers(AFTER_VANILLA_PHASES.get(VanillaLayerMarker.DIMENSION), systemBuilder, level);
		}
	}

	public static void addPostBiomesLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		if (!hasOnlyVanillaLayers) {
			sortIfNeeded();
			addLayers(AFTER_VANILLA_PHASES.get(VanillaLayerMarker.BIOMES), systemBuilder, level);
		}
	}

	public static void addPostTimelinesLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		if (!hasOnlyVanillaLayers) {
			sortIfNeeded();
			addLayers(AFTER_VANILLA_PHASES.get(VanillaLayerMarker.TIMELINES), systemBuilder, level);
		}
	}

	public static void addPostWeatherLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		if (!hasOnlyVanillaLayers) {
			sortIfNeeded();
			addLayers(AFTER_VANILLA_PHASES.get(VanillaLayerMarker.WEATHER), systemBuilder, level);
		}
	}

	private static void sortIfNeeded() {
		Map<Identifier, Layer> layers;

		// Collect sorting data from registry
		synchronized (LOCK) {
			if (orderValid) {
				return;
			}

			layers = new HashMap<>();

			for (Map.Entry<Identifier, AttributeLayerProvider> entry : LAYER_MAP.entrySet()) {
				layers.put(entry.getKey(), new Layer(entry.getKey(), entry.getValue()));
			}

			for (Dependency dependency : DEPENDENCIES) {
				Layer firstLayer = layers.get(dependency.firstLayer());
				Layer secondLayer = layers.get(dependency.secondLayer());

				if (firstLayer != null && secondLayer != null) {
					Layer.link(firstLayer, secondLayer);
				}
			}
		}

		// Sort layers
		List<Layer> sorted = new ArrayList<>(layers.values());
		NodeSorting.sort(sorted, "environment attribute layers", AttributeLayerRegistryImpl::compareIds);

		// Categorize layer providers into vanilla phases
		synchronized (LOCK) {
			FIRST_PHASES.clear();
			AFTER_VANILLA_PHASES.forEach((_, list) -> list.clear());

			List<AttributeLayerProvider> phase = FIRST_PHASES;

			for (Layer layer : sorted) {
				AttributeLayerProvider provider = layer.provider;

				if (provider instanceof VanillaLayerMarker marker) {
					phase = AFTER_VANILLA_PHASES.get(marker);
				} else {
					phase.add(provider);
				}
			}
		}
	}

	// Tiebreaker: put vanilla layers before others, and otherwise sort by lexicographic ordering
	// This also makes sure that layers that were not tied to vanilla ordering will come last in the ordering
	private static int compareIds(Layer a, Layer b) {
		Identifier idA = a.id;
		Identifier idB = b.id;

		VanillaLayerMarker markerA = MARKERS.get(idA);
		VanillaLayerMarker markerB = MARKERS.get(idB);

		// If both are vanilla layers, ensure they remain the same order as defined by Minecraft
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

	// Markers for vanilla layers. It's important that these enum constants stay in the order that vanilla layers should appear,
	// since this order will be used to fix dependency cycles (and we don't want a dependency cycle to mess up the order).
	private enum VanillaLayerMarker implements AttributeLayerProvider {
		DIMENSION(AttributeLayerProvider.DIMENSION),
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

	private static class Layer extends SortableNode<Layer> {
		private final Identifier id;
		private final AttributeLayerProvider provider;

		private Layer(Identifier id, AttributeLayerProvider provider) {
			this.id = id;
			this.provider = provider;
		}

		@Override
		protected String getDescription() {
			return id.toString();
		}
	}

	private record Dependency(Identifier firstLayer, Identifier secondLayer) {
	}
}
