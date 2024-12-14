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

package net.fabricmc.fabric.impl.registry.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.registry.sync.packet.DirectRegistryPacketHandler;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistryPacketHandler;

public final class RegistrySyncManager {
	public static final boolean DEBUG = Boolean.getBoolean("fabric.registry.debug");

	public static final DirectRegistryPacketHandler DIRECT_PACKET_HANDLER = new DirectRegistryPacketHandler();

	private static final Logger LOGGER = LoggerFactory.getLogger("FabricRegistrySync");
	private static final boolean DEBUG_WRITE_REGISTRY_DATA = Boolean.getBoolean("fabric.registry.debug.writeContentsAsCsv");

	//Set to true after vanilla's bootstrap has completed
	public static boolean postBootstrap = false;

	private RegistrySyncManager() { }

	public static void configureClient(ServerConfigurationNetworkHandler handler, MinecraftServer server) {
		if (!DEBUG && server.isHost(handler.getDebugProfile())) {
			// Dont send in singleplayer
			return;
		}

		if (!ServerConfigurationNetworking.canSend(handler, DIRECT_PACKET_HANDLER.getPacketId())) {
			// Don't send if the client cannot receive
			return;
		}

		final Map<Identifier, Object2IntMap<Identifier>> map = RegistrySyncManager.createAndPopulateRegistryMap();

		if (map == null) {
			// Don't send when there is nothing to map
			return;
		}

		handler.addTask(new SyncConfigurationTask(handler, map));
	}

	public record SyncConfigurationTask(
			ServerConfigurationNetworkHandler handler,
			Map<Identifier, Object2IntMap<Identifier>> map
	) implements ServerPlayerConfigurationTask {
		public static final Key KEY = new Key("fabric:registry/sync");

		@Override
		public void sendPacket(Consumer<Packet<?>> sender) {
			DIRECT_PACKET_HANDLER.sendPacket(payload -> handler.sendPacket(ServerConfigurationNetworking.createS2CPacket(payload)), map);
		}

		@Override
		public Key getKey() {
			return KEY;
		}
	}

	public static <T extends RegistryPacketHandler.RegistrySyncPayload> CompletableFuture<Boolean> receivePacket(ThreadExecutor<?> executor, RegistryPacketHandler<T> handler, T payload, boolean accept) {
		handler.receivePayload(payload);

		if (!handler.isPacketFinished()) {
			return CompletableFuture.completedFuture(false);
		}

		if (DEBUG) {
			String handlerName = handler.getClass().getSimpleName();
			LOGGER.info("{} total packet: {}", handlerName, handler.getTotalPacketReceived());
			LOGGER.info("{} raw size: {}", handlerName, handler.getRawBufSize());
			LOGGER.info("{} deflated size: {}", handlerName, handler.getDeflatedBufSize());
		}

		Map<Identifier, Object2IntMap<Identifier>> map = handler.getSyncedRegistryMap();

		if (!accept) {
			return CompletableFuture.completedFuture(true);
		}

		return executor.submit(() -> {
			if (map == null) {
				throw new CompletionException(new RemapException("Received null map in sync packet!"));
			}

			try {
				apply(map, RemappableRegistry.RemapMode.REMOTE);
				return true;
			} catch (RemapException e) {
				throw new CompletionException(e);
			}
		});
	}

	/**
	 * Creates a {@link Map} used to sync the registry ids.
	 *
	 * @return a {@link Map} to sync, null when empty
	 */
	@Nullable
	public static Map<Identifier, Object2IntMap<Identifier>> createAndPopulateRegistryMap() {
		Map<Identifier, Object2IntMap<Identifier>> map = new LinkedHashMap<>();

		for (Identifier registryId : Registries.REGISTRIES.getIds()) {
			Registry registry = Registries.REGISTRIES.get(registryId);

			if (DEBUG_WRITE_REGISTRY_DATA) {
				File location = new File(".fabric" + File.separatorChar + "debug" + File.separatorChar + "registry");
				boolean c = true;

				if (!location.exists()) {
					if (!location.mkdirs()) {
						LOGGER.warn("[fabric-registry-sync debug] Could not create " + location.getAbsolutePath() + " directory!");
						c = false;
					}
				}

				if (c && registry != null) {
					File file = new File(location, registryId.toString().replace(':', '.').replace('/', '.') + ".csv");

					try (FileOutputStream stream = new FileOutputStream(file)) {
						StringBuilder builder = new StringBuilder("Raw ID,String ID,Class Type\n");

						for (Object o : registry) {
							String classType = (o == null) ? "null" : o.getClass().getName();
							//noinspection unchecked
							Identifier id = registry.getId(o);
							if (id == null) continue;

							//noinspection unchecked
							int rawId = registry.getRawId(o);
							String stringId = id.toString();
							builder.append("\"").append(rawId).append("\",\"").append(stringId).append("\",\"").append(classType).append("\"\n");
						}

						stream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
					} catch (IOException e) {
						LOGGER.warn("[fabric-registry-sync debug] Could not write to " + file.getAbsolutePath() + "!", e);
					}
				}
			}

			RegistryAttributeHolder attributeHolder = RegistryAttributeHolder.get(registry.getKey());

			if (!attributeHolder.hasAttribute(RegistryAttribute.SYNCED)) {
				LOGGER.debug("Not syncing registry: {}", registryId);
				continue;
			}

			/*
			 * Dont do anything with vanilla registries on client sync.
			 *
			 * This will not sync IDs if a world has been previously modded, either from removed mods
			 * or a previous version of fabric registry sync.
			 */
			if (!attributeHolder.hasAttribute(RegistryAttribute.MODDED)) {
				LOGGER.debug("Skipping un-modded registry: " + registryId);
				continue;
			}

			LOGGER.debug("Syncing registry: " + registryId);

			if (registry instanceof RemappableRegistry) {
				Object2IntMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<>();
				IntSet rawIdsFound = DEBUG ? new IntOpenHashSet() : null;

				for (Object o : registry) {
					//noinspection unchecked
					Identifier id = registry.getId(o);
					if (id == null) continue;

					//noinspection unchecked
					int rawId = registry.getRawId(o);

					if (DEBUG) {
						if (registry.get(id) != o) {
							LOGGER.error("[fabric-registry-sync] Inconsistency detected in " + registryId + ": object " + o + " -> string ID " + id + " -> object " + registry.get(id) + "!");
						}

						if (registry.get(rawId) != o) {
							LOGGER.error("[fabric-registry-sync] Inconsistency detected in " + registryId + ": object " + o + " -> integer ID " + rawId + " -> object " + registry.get(rawId) + "!");
						}

						if (!rawIdsFound.add(rawId)) {
							LOGGER.error("[fabric-registry-sync] Inconsistency detected in " + registryId + ": multiple objects hold the raw ID " + rawId + " (this one is " + id + ")");
						}
					}

					idMap.put(id, rawId);
				}

				map.put(registryId, idMap);
			}
		}

		if (map.isEmpty()) {
			return null;
		}

		return map;
	}

	public static void apply(Map<Identifier, Object2IntMap<Identifier>> map, RemappableRegistry.RemapMode mode) throws RemapException {
		if (mode == RemappableRegistry.RemapMode.REMOTE) {
			checkRemoteRemap(map);
		}

		Set<Identifier> containedRegistries = Sets.newHashSet(map.keySet());

		for (Identifier registryId : Registries.REGISTRIES.getIds()) {
			if (!containedRegistries.remove(registryId)) {
				continue;
			}

			Object2IntMap<Identifier> registryMap = map.get(registryId);
			Registry<?> registry = Registries.REGISTRIES.get(registryId);

			RegistryAttributeHolder attributeHolder = RegistryAttributeHolder.get(registry.getKey());

			if (!attributeHolder.hasAttribute(RegistryAttribute.MODDED)) {
				LOGGER.debug("Not applying registry data to vanilla registry {}", registryId.toString());
				continue;
			}

			if (registry instanceof RemappableRegistry remappableRegistry) {
				remappableRegistry.remap(registryId.toString(), registryMap, mode);
			} else {
				throw new RemapException("Registry " + registryId + " is not remappable");
			}
		}

		if (!containedRegistries.isEmpty()) {
			LOGGER.warn("[fabric-registry-sync] Could not find the following registries: " + Joiner.on(", ").join(containedRegistries));
		}
	}

	@VisibleForTesting
	public static void checkRemoteRemap(Map<Identifier, Object2IntMap<Identifier>> map) throws RemapException {
		Map<Identifier, List<Identifier>> missingEntries = new HashMap<>();

		for (Map.Entry<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> entry : Registries.REGISTRIES.getEntrySet()) {
			final Registry<?> registry = entry.getValue();
			final Identifier registryId = entry.getKey().getValue();
			final Object2IntMap<Identifier> remoteRegistry = map.get(registryId);

			if (remoteRegistry == null) {
				// Registry sync does not contain data for this registry, will print a warning when applying.
				continue;
			}

			for (Identifier remoteId : remoteRegistry.keySet()) {
				if (!registry.containsId(remoteId)) {
					// Found a registry entry from the server that is
					missingEntries.computeIfAbsent(registryId, i -> new ArrayList<>()).add(remoteId);
				}
			}
		}

		if (missingEntries.isEmpty()) {
			// All good :)
			return;
		}

		// Print out details to the log
		LOGGER.error("Received unknown remote registry entries from server");

		for (Map.Entry<Identifier, List<Identifier>> entry : missingEntries.entrySet()) {
			for (Identifier identifier : entry.getValue()) {
				LOGGER.error("Registry entry ({}) is missing from local registry ({})", identifier, entry.getKey());
			}
		}

		// Create a nice user friendly error message.
		MutableText text = Text.empty();

		final int count = missingEntries.values().stream().mapToInt(List::size).sum();

		if (count == 1) {
			text = text.append(Text.translatable("fabric-registry-sync-v0.unknown-remote.title.singular"));
		} else {
			text = text.append(Text.translatable("fabric-registry-sync-v0.unknown-remote.title.plural", count));
		}

		text = text.append(Text.translatable("fabric-registry-sync-v0.unknown-remote.subtitle.1").formatted(Formatting.GREEN));
		text = text.append(Text.translatable("fabric-registry-sync-v0.unknown-remote.subtitle.2"));

		final int toDisplay = 4;
		// Get the distinct missing namespaces
		final List<String> namespaces = missingEntries.values().stream()
				.flatMap(List::stream)
				.map(Identifier::getNamespace)
				.distinct()
				.sorted()
				.toList();

		for (int i = 0; i < Math.min(namespaces.size(), toDisplay); i++) {
			text = text.append(Text.literal(namespaces.get(i)).formatted(Formatting.YELLOW));
			text = text.append(ScreenTexts.LINE_BREAK);
		}

		if (namespaces.size() > toDisplay) {
			text = text.append(Text.translatable("fabric-registry-sync-v0.unknown-remote.footer", namespaces.size() - toDisplay));
		}

		throw new RemapException(text);
	}

	public static void unmap() throws RemapException {
		for (Identifier registryId : Registries.REGISTRIES.getIds()) {
			Registry registry = Registries.REGISTRIES.get(registryId);

			if (registry instanceof RemappableRegistry) {
				((RemappableRegistry) registry).unmap(registryId.toString());
			}
		}
	}

	public static void bootstrapRegistries() {
		postBootstrap = true;
	}
}
