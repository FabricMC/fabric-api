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

package net.fabricmc.fabric.impl.resource.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.IdentifiableResourceReloader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.fabricmc.loader.api.FabricLoader;

public final class ResourceLoaderImpl implements ResourceLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger("ResourceLoader");
	private static final Map<ResourceType, ResourceLoaderImpl> IMPL_MAP = new EnumMap<>(ResourceType.class);

	private static final boolean DEBUG_RELOADERS_IDENTITY = TriState.fromSystemProperty("fabric.resource_loader.debug.reloaders_identity")
			.orElse(FabricLoader.getInstance().isDevelopmentEnvironment());
	private static final boolean DEBUG_RELOADERS_ORDER = TriState.fromSystemProperty("fabric.resource_loader.debug.reloaders_order")
			.orElse(true);

	public static ResourceLoaderImpl get(ResourceType type) {
		return IMPL_MAP.computeIfAbsent(type, ResourceLoaderImpl::new);
	}

	private final Set<Identifier> addedReloaderIds = new ObjectOpenHashSet<>();
	private final Set<IdentifiableResourceReloader> addedReloaders = new LinkedHashSet<>();
	private final Set<ReloaderOrder> reloadersOrdering = new LinkedHashSet<>();
	private final ResourceType type;

	private ResourceLoaderImpl(ResourceType type) {
		this.type = type;
	}

	@Override
	public void registerReloader(IdentifiableResourceReloader reloader) {
		if (!this.addedReloaderIds.add(reloader.getFabricId())) {
			throw new IllegalStateException(
					"Tried to register resource reloader %s twice!".formatted(reloader.getFabricId())
			);
		}

		if (!this.addedReloaders.add(reloader)) {
			throw new IllegalStateException(
					"Resource reloader with previously unknown ID %s already in resource reloader set!"
							.formatted(reloader.getFabricId())
			);
		}
	}

	@Override
	public void addReloaderOrdering(@NotNull Identifier firstReloader, @NotNull Identifier secondReloader) {
		Objects.requireNonNull(firstReloader, "The first reloader identifier should not be null.");
		Objects.requireNonNull(secondReloader, "The second reloader identifier should not be null.");

		if (firstReloader.equals(secondReloader)) {
			throw new IllegalArgumentException("Tried to add a phase that depends on itself.");
		}

		this.reloadersOrdering.add(new ReloaderOrder(firstReloader, secondReloader));
	}

	public static List<ResourceReloader> sort(ResourceType type, List<ResourceReloader> listeners) {
		if (type == null) {
			return listeners;
		}

		ResourceLoaderImpl instance = get(type);

		if (instance != null) {
			var mutable = new ArrayList<>(listeners);
			instance.sort(mutable);
			return Collections.unmodifiableList(mutable);
		}

		return listeners;
	}

	/**
	 * Sorts the given resource reloaders to satisfy dependencies.
	 *
	 * @param reloaders the resource reloaders to sort
	 */
	private void sort(List<ResourceReloader> reloaders) {
		// Build the actual full list of resource reloaders to add.
		final Set<IdentifiableResourceReloader> reloadersToAdd = new LinkedHashSet<>(this.addedReloaders);

		// Locate and extract the setup marker.
		ResourceReloader setupReloader = this.extractSetupMarker(reloaders);

		// Remove any modded reloaders to sort properly.
		reloaders.removeAll(reloadersToAdd);

		// General rules:
		// - We *do not* touch the ordering of vanilla reloaders. Ever.
		//   While dependency values are provided where possible, we cannot
		//   trust them 100%. Only code doesn't lie.
		// - We add all custom reloaders after vanilla reloaders if they don't have contrary ordering. Same reasons.

		var runtimePhases = new Object2ObjectOpenHashMap<Identifier, ResourceReloaderPhaseData>();

		Iterator<ResourceReloader> itPhases = reloaders.iterator();
		// Add the virtual before Vanilla phase.
		ResourceReloaderPhaseData last = new ResourceReloaderPhaseData(ResourceReloaderKeys.BEFORE_VANILLA, null);
		last.setVanillaStatus(ResourceReloaderPhaseData.VanillaStatus.VANILLA);
		runtimePhases.put(last.id, last);

		// Add all the Vanilla reloaders.
		while (itPhases.hasNext()) {
			ResourceReloader currentReloader = itPhases.next();
			Identifier id;

			if (currentReloader instanceof IdentifiableResourceReloader identifiable) {
				id = identifiable.getFabricId();
			} else {
				id = Identifier.of("unknown",
						"private/"
								+ currentReloader.getClass().getName()
								.replace(".", "/")
								.replace("$", "_")
								.toLowerCase(Locale.ROOT)
				);

				if (DEBUG_RELOADERS_IDENTITY) {
					LOGGER.warn(
							"The resource reloader at {} does not implement IdentifiableResourceReloader "
									+ "making ordering support more difficult for other modders.",
							currentReloader.getClass().getName()
					);
				}
			}

			var current = new ResourceReloaderPhaseData(id, currentReloader);
			current.setVanillaStatus(ResourceReloaderPhaseData.VanillaStatus.VANILLA);
			runtimePhases.put(id, current);

			SortableNode.link(last, current);
			last = current;
		}

		// Add the virtual after Vanilla phase.
		var afterVanilla = new ResourceReloaderPhaseData.AfterVanilla(ResourceReloaderKeys.AFTER_VANILLA);
		runtimePhases.put(afterVanilla.id, afterVanilla);
		SortableNode.link(last, afterVanilla);

		// Add the modded reloaders.
		for (IdentifiableResourceReloader moddedReloader : reloadersToAdd) {
			var phase = new ResourceReloaderPhaseData(moddedReloader.getFabricId(), moddedReloader);
			runtimePhases.put(phase.id, phase);
		}

		// Add the ordering.
		for (ReloaderOrder order : this.reloadersOrdering) {
			ResourceReloaderPhaseData first = runtimePhases.get(order.first);

			if (first == null) continue;

			ResourceReloaderPhaseData second = runtimePhases.get(order.second);

			if (second == null) continue;

			SortableNode.link(first, second);
		}

		// Attempt to order un-ordered modded reloaders to after Vanilla to respect the rules.
		for (ResourceReloaderPhaseData putAfter : runtimePhases.values()) {
			if (putAfter == afterVanilla) continue;

			if (putAfter.vanillaStatus == ResourceReloaderPhaseData.VanillaStatus.NONE
					|| putAfter.vanillaStatus == ResourceReloaderPhaseData.VanillaStatus.AFTER) {
				SortableNode.link(afterVanilla, putAfter);
			}
		}

		// Sort the phases.
		var phases = new ArrayList<>(runtimePhases.values());
		NodeSorting.sort(phases, "resource reloaders", Comparator.comparing(data -> data.id));

		// Apply the sorting!
		reloaders.clear();

		// Inject back the setup reloader at the beginning.
		if (setupReloader != null) {
			reloaders.add(setupReloader);
		}

		for (ResourceReloaderPhaseData phase : phases) {
			if (phase.resourceReloader != null) {
				reloaders.add(phase.resourceReloader);
			}
		}

		if (DEBUG_RELOADERS_ORDER) {
			LOGGER.info("Sorted reloaders: {}", phases.stream().map(data -> {
				String str = data.id.toString();

				if (data.resourceReloader == null) {
					str += " (virtual)";
				}

				return str;
			}).collect(Collectors.joining(", ")));
		}
	}

	private ResourceReloader extractSetupMarker(List<ResourceReloader> reloaders) {
		if (type == ResourceType.CLIENT_RESOURCES) {
			// We don't need the registry for client resources.
			return null;
		}

		Iterator<ResourceReloader> it = reloaders.iterator();

		while (it.hasNext()) {
			if (it.next() instanceof SetupMarkerResourceReloader marker) {
				it.remove();
				return marker;
			}
		}

		throw new IllegalStateException("No SetupMarkerResourceReloader found in reloaders!");
	}

	// A bit of a hack to get the registry, but it works.
	public static @Nullable RegistryWrapper.WrapperLookup getWrapperLookup(List<ResourceReloader> reloaders) {
		for (ResourceReloader resourceReloader : reloaders) {
			if (resourceReloader instanceof FabricRecipeManager recipeManager) {
				return recipeManager.fabric$getRegistries();
			}
		}

		throw new IllegalStateException("No ServerRecipeManager found in reloaders!");
	}

	private record ReloaderOrder(Identifier first, Identifier second) {
	}
}
