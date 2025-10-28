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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.fabricmc.loader.api.FabricLoader;

public sealed class ResourceLoaderImpl implements ResourceLoader permits DataResourceLoaderImpl {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<ResourceType, ResourceLoaderImpl> IMPL_MAP = new EnumMap<>(ResourceType.class);

	private static final boolean DEBUG_RELOADERS_IDENTITY = TriState.fromSystemProperty("fabric.resource_loader.debug.reloaders_identity")
			.orElse(FabricLoader.getInstance().isDevelopmentEnvironment());
	public static final boolean DEBUG_PROFILE_RESOURCE_RELOADERS = Boolean.getBoolean("fabric.resource_loader.debug.profile_resource_reloaders");
	private static final boolean DEBUG_RELOADERS_ORDER = Boolean.getBoolean("fabric.resource_loader.debug.reloaders_order");

	public static ResourceLoaderImpl get(ResourceType type) {
		return IMPL_MAP.computeIfAbsent(type, target ->
				target == ResourceType.SERVER_DATA ? DataResourceLoaderImpl.INSTANCE : new ResourceLoaderImpl(type)
		);
	}

	private final Map<Identifier, ResourceReloader> addedReloaders = new LinkedHashMap<>();
	private final Set<ReloaderOrder> reloadersOrdering = new LinkedHashSet<>();
	private final ResourceType type;

	ResourceLoaderImpl(ResourceType type) {
		this.type = type;
	}

	protected boolean hasResourceReloader(Identifier id) {
		return this.addedReloaders.containsKey(id);
	}

	protected final void checkUniqueResourceReloader(Identifier id) {
		if (this.hasResourceReloader(id)) {
			throw new IllegalStateException(
					"Tried to register resource reloader %s twice!".formatted(id)
			);
		}
	}

	@Override
	public void registerReloader(Identifier id, ResourceReloader reloader) {
		Objects.requireNonNull(id, "The reloader identifier should not be null.");
		Objects.requireNonNull(reloader, "The reloader should not be null.");
		this.checkUniqueResourceReloader(id);

		for (Map.Entry<Identifier, ResourceReloader> entry : this.addedReloaders.entrySet()) {
			if (entry.getValue() == reloader) {
				throw new IllegalStateException(
						"Resource reloader with ID %s already in resource reloader set with ID %s!"
								.formatted(id, entry.getKey())
				);
			}
		}

		this.addedReloaders.put(id, reloader);
	}

	@Override
	public void addReloaderOrdering(Identifier firstReloader, Identifier secondReloader) {
		Objects.requireNonNull(firstReloader, "The first reloader identifier should not be null.");
		Objects.requireNonNull(secondReloader, "The second reloader identifier should not be null.");

		if (firstReloader.equals(secondReloader)) {
			throw new IllegalArgumentException("Tried to add a phase that depends on itself.");
		}

		this.reloadersOrdering.add(new ReloaderOrder(firstReloader, secondReloader));
	}

	private Identifier getResourceReloaderIdForSorting(ResourceReloader reloader) {
		if (reloader instanceof FabricResourceReloader identifiable) {
			return identifiable.fabric$getId();
		} else {
			if (DEBUG_RELOADERS_IDENTITY) {
				LOGGER.warn(
						"The resource reloader at {} does not use identifiable registration "
								+ "making ordering support more difficult for other modders.",
						reloader.getClass().getName()
				);
			}

			return Identifier.of("unknown",
					"private/"
							+ reloader.getClass().getName()
							.replace(".", "/")
							.replace("$", "_")
							.toLowerCase(Locale.ROOT)
			);
		}
	}

	public static List<ResourceReloader> sort(ResourceType type, List<ResourceReloader> listeners) {
		if (type == null) {
			return listeners;
		}

		ResourceLoaderImpl instance = get(type);

		var mutable = new ArrayList<>(listeners);
		instance.sort(mutable);
		return Collections.unmodifiableList(mutable);
	}

	protected Set<Map.Entry<Identifier, ResourceReloader>> collectReloadersToAdd(
			@Nullable SetupMarkerResourceReloader setupMarker
	) {
		return new LinkedHashSet<>(this.addedReloaders.entrySet());
	}

	/**
	 * Sorts the given resource reloaders to satisfy dependencies.
	 *
	 * @param reloaders the resource reloaders to sort
	 */
	private void sort(List<ResourceReloader> reloaders) {
		// Locate and extract the setup marker.
		SetupMarkerResourceReloader setupReloader = this.extractSetupMarker(reloaders);

		// Build the actual full list of resource reloaders to add.
		final Set<Map.Entry<Identifier, ResourceReloader>> reloadersToAdd = this.collectReloadersToAdd(setupReloader);

		// Remove any modded reloaders to sort properly.
		reloadersToAdd.stream().map(Map.Entry::getValue).forEach(reloaders::remove);

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
			Identifier id = this.getResourceReloaderIdForSorting(currentReloader);

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
		for (Map.Entry<Identifier, ResourceReloader> moddedReloader : reloadersToAdd) {
			var phase = new ResourceReloaderPhaseData(moddedReloader.getKey(), moddedReloader.getValue());
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

	private @Nullable SetupMarkerResourceReloader extractSetupMarker(List<ResourceReloader> reloaders) {
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

	private record ReloaderOrder(Identifier first, Identifier second) {
	}
}
