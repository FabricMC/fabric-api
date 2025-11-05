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

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.mixin.object.builder.EntityDataSerializersAccessor;

public final class FabricTrackedDataRegistryImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(FabricTrackedDataRegistryImpl.class);

	private static final Identifier HANDLER_REGISTRY_ID = Identifier.fromNamespaceAndPath("fabric-object-builder-api-v1", "tracked_data_handler");
	private static final ResourceKey<Registry<EntityDataSerializer<?>>> HANDLER_REGISTRY_KEY = ResourceKey.createRegistryKey(HANDLER_REGISTRY_ID);

	private static final List<EntityDataSerializer<?>> VANILLA_HANDLERS = new ArrayList<>();
	@Nullable
	private static Registry<EntityDataSerializer<?>> handlerRegistry = null;
	private static final List<EntityDataSerializer<?>> EXTERNAL_MODDED_HANDLERS = new ArrayList<>();

	private FabricTrackedDataRegistryImpl() {
	}

	public static boolean hasStoredVanillaHandlers() {
		return !VANILLA_HANDLERS.isEmpty();
	}

	public static void storeVanillaHandlers() {
		if (hasStoredVanillaHandlers()) {
			throw new IllegalStateException("Already stored vanilla handlers!");
		}

		CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> dataHandlers = EntityDataSerializersAccessor.fabric_getDataHandlers();

		for (EntityDataSerializer<?> handler : dataHandlers) {
			VANILLA_HANDLERS.add(handler);
		}

		LOGGER.debug("Stored {} vanilla handlers", VANILLA_HANDLERS.size());
	}

	private static void storeExternalHandlers() {
		CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> dataHandlers = EntityDataSerializersAccessor.fabric_getDataHandlers();

		for (EntityDataSerializer<?> handler : dataHandlers) {
			if (VANILLA_HANDLERS.contains(handler)) continue;
			if (handlerRegistry != null && handlerRegistry.getKey(handler) != null) continue;
			if (EXTERNAL_MODDED_HANDLERS.contains(handler)) continue;

			EXTERNAL_MODDED_HANDLERS.add(handler);
			LOGGER.warn("Tracked data handler {} is not managed by vanilla or Fabric API; it may be prone to desynchronization!", handler);
		}
	}

	/**
	 * Reorders handlers in {@code TrackedDataHandlerRegistry#DATA_HANDLERS} to have a consistent order between client and server.
	 *
	 * <p>The order used is as follows:
	 *
	 * <ul>
	 *   <li>Vanilla handlers</li>
	 *   <li>Handlers in the Fabric API registry (sorted by ID)</li>
	 *   <li>External modded handlers</li>
	 * </ul>
	*/
	private static void reorderHandlers() {
		CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> dataHandlers = EntityDataSerializersAccessor.fabric_getDataHandlers();
		LOGGER.debug("Reordering tracked data handlers containing {} entries", dataHandlers.size());

		// Reset the map so that handlers can be added back in a new order
		dataHandlers.clear();

		// Add handlers back to map
		for (EntityDataSerializer<?> handler : VANILLA_HANDLERS) {
			dataHandlers.add(handler);
		}

		if (handlerRegistry != null) {
			for (EntityDataSerializer<?> handler : handlerRegistry) {
				dataHandlers.add(handler);
			}
		}

		for (EntityDataSerializer<?> handler : EXTERNAL_MODDED_HANDLERS) {
			dataHandlers.add(handler);
		}

		LOGGER.debug("Finished reordering tracked data handlers containing {} entries", dataHandlers.size());
	}

	public static void register(Identifier id, EntityDataSerializer<?> handler) {
		Objects.requireNonNull(id, "Tracked data handler ID cannot be null!");
		Objects.requireNonNull(handler, "Tracked data handler cannot be null!");

		storeExternalHandlers();

		if (VANILLA_HANDLERS.contains(handler) || EXTERNAL_MODDED_HANDLERS.contains(handler)) {
			throw new IllegalArgumentException("Cannot register tracked data handler previously added via TrackedDataHandlerRegistry.register");
		}

		if (handlerRegistry == null) {
			handlerRegistry = FabricRegistryBuilder
					.createSimple(HANDLER_REGISTRY_KEY)
					.attribute(RegistryAttribute.SYNCED)
					.buildAndRegister();

			RegistryIdRemapCallback.event(handlerRegistry).register(state -> {
				storeExternalHandlers();
				reorderHandlers();
			});
		}

		Registry.register(handlerRegistry, id, handler);
		reorderHandlers();
	}

	@Nullable
	public static EntityDataSerializer<?> get(Identifier id) {
		Objects.requireNonNull(id, "Tracked data handler ID cannot be null!");

		if (handlerRegistry == null) {
			return null;
		}

		return handlerRegistry.getValue(id);
	}

	@Nullable
	public static Identifier getId(EntityDataSerializer<?> handler) {
		Objects.requireNonNull(handler, "Tracked data handler cannot be null!");

		if (handlerRegistry == null) {
			return null;
		}

		return handlerRegistry.getKey(handler);
	}
}
