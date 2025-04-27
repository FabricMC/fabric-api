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

package net.fabricmc.fabric.impl.object.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Int2ObjectBiMap;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.mixin.object.builder.TrackedDataHandlerRegistryAccessor;

public class FabricTrackedDataRegistryImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(FabricTrackedDataRegistryImpl.class);

	private static final Identifier HANDLER_REGISTRY_ID = Identifier.of("fabric-object-builder-api-v1", "tracked_data_handler");
	private static final RegistryKey<Registry<TrackedDataHandler<?>>> HANDLER_REGISTRY_KEY = RegistryKey.ofRegistry(HANDLER_REGISTRY_ID);

	private static List<TrackedDataHandler<?>> VANILLA_HANDLERS = new ArrayList<>();
	private static @Nullable Registry<TrackedDataHandler<?>> HANDLER_REGISTRY = null;
	private static List<TrackedDataHandler<?>> EXTERNAL_MODDED_HANDLERS = new ArrayList<>();

	private FabricTrackedDataRegistryImpl() {
	}

	public static void storeVanillaHandlers() {
		Int2ObjectBiMap<TrackedDataHandler<?>> dataHandlers = TrackedDataHandlerRegistryAccessor.fabric_getDataHandlers();

		for (TrackedDataHandler<?> handler : dataHandlers) {
			VANILLA_HANDLERS.add(handler);
		}

		LOGGER.debug("Stored {} vanilla handlers", VANILLA_HANDLERS.size());
	}

	/**
	 * Reorders handlers in {@link net.minecraft.entity.data.TrackedDataHandlerRegistry#DATA_HANDLERS} to have a consistent order between client and server.
	 *
	 * <p>The order used is the following:
	 *
	 * <ul>
	 *   <li>Vanilla handlers</li>
	 *   <li>Handlers in the Fabric API registry (sorted by ID)</li>
	 *   <li>External modded handlers</li>
	 * </ul>
	*/
	private static void reorderHandlers() {
		Int2ObjectBiMap<TrackedDataHandler<?>> dataHandlers = TrackedDataHandlerRegistryAccessor.fabric_getDataHandlers();
		LOGGER.debug("Reordering tracked data handlers containing {} entries", dataHandlers.size());

		// Store external modded handlers
		for (TrackedDataHandler<?> handler : dataHandlers) {
			if (VANILLA_HANDLERS.contains(handler)) continue;
			if (HANDLER_REGISTRY != null && HANDLER_REGISTRY.getId(handler) != null) continue;
			if (EXTERNAL_MODDED_HANDLERS.contains(handler)) continue;

			EXTERNAL_MODDED_HANDLERS.add(handler);
			LOGGER.warn("Tracked data handler {} is not managed by vanilla or Fabric API; it may be prone to desynchronization!", handler);
		}

		// Reset the map so that handlers can be added back in a new order
		dataHandlers.clear();

		// Add handlers back to map
		for (TrackedDataHandler<?> handler : VANILLA_HANDLERS) {
			dataHandlers.add(handler);
		}

		if (HANDLER_REGISTRY != null) {
			for (TrackedDataHandler<?> handler : HANDLER_REGISTRY) {
				dataHandlers.add(handler);
			}
		}

		for (TrackedDataHandler<?> handler : EXTERNAL_MODDED_HANDLERS) {
			dataHandlers.add(handler);
		}

		LOGGER.debug("Finished reordering tracked data handlers containing {} entries", dataHandlers.size());
	}

	public static void registerHandler(Identifier id, TrackedDataHandler<?> handler) {
		Objects.requireNonNull(id, "Tracked data handler ID cannot be null!");
		Objects.requireNonNull(handler, "Tracked data handler cannot be null!");

		if (VANILLA_HANDLERS.contains(handler) || EXTERNAL_MODDED_HANDLERS.contains(handler)) {
			throw new IllegalArgumentException("Cannot register tracked data handler added via TrackedDataHandlerRegistry.register");
		}

		if (HANDLER_REGISTRY == null) {
			HANDLER_REGISTRY = FabricRegistryBuilder
					.createSimple(HANDLER_REGISTRY_KEY)
					.attribute(RegistryAttribute.SYNCED)
					.buildAndRegister();

			RegistryIdRemapCallback.event(HANDLER_REGISTRY).register(state -> reorderHandlers());
		}

		Registry.register(HANDLER_REGISTRY, id, handler);
		reorderHandlers();
	}
}
