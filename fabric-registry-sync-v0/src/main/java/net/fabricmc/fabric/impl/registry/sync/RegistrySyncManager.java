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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.players.NameAndId;

import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistrySyncPayload;

public final class RegistrySyncManager {
	public static final boolean DEBUG = Boolean.getBoolean("fabric.registry.debug");

	private static final Logger LOGGER = LoggerFactory.getLogger("FabricRegistrySync");
	private static final boolean DEBUG_WRITE_REGISTRY_DATA = Boolean.getBoolean("fabric.registry.debug.writeContentsAsCsv");

	//Set to true after vanilla's bootstrap has completed
	public static boolean postBootstrap = false;

	private RegistrySyncManager() { }

	public static void configureClient(ServerConfigurationPacketListenerImpl handler, MinecraftServer server) {
		if (!DEBUG && server.isSingleplayerOwner(new NameAndId(handler.getOwner()))) {
			// Dont send in singleplayer
			return;
		}

		final Map<Identifier, Object2IntMap<Identifier>> map = RegistrySyncManager.createAndPopulateRegistryMap();

		if (map == null) {
			// Don't send when there is nothing to map
			return;
		}

		if (!ServerConfigurationNetworking.canSend(handler, RegistrySyncPayload.ID)) {
			if (areAllRegistriesOptional(map)) {
				// Allow the client to connect if all of the registries we want to sync are optional
				return;
			}

			// Disconnect incompatible clients
			Component message = getIncompatibleClientText(ServerNetworkingImpl.getAddon(handler).getClientBrand(), map);
			handler.disconnect(message);
			return;
		}

		handler.addTask(new SyncConfigurationTask(handler, map));
	}

	private static Component getIncompatibleClientText(@Nullable String brand, Map<Identifier, Object2IntMap<Identifier>> map) {
		String brandText = switch (brand) {
		case "fabric" -> "Fabric API";
		case null, default -> "Fabric Loader and Fabric API";
		};

		final int toDisplay = 4;

		List<String> namespaces = map.values().stream()
				.map(Object2IntMap::keySet)
				.flatMap(Set::stream)
				.map(Identifier::getNamespace)
				.filter(s -> !s.equals(Identifier.DEFAULT_NAMESPACE))
				.distinct()
				.sorted()
				.toList();

		MutableComponent text = Component.literal("The following registry entry namespaces may be related:\n\n");

		for (int i = 0; i < Math.min(namespaces.size(), toDisplay); i++) {
			text = text.append(Component.literal(namespaces.get(i)).withStyle(ChatFormatting.YELLOW));
			text = text.append(CommonComponents.NEW_LINE);
		}

		if (namespaces.size() > toDisplay) {
			text = text.append(Component.literal("And %d more...".formatted(namespaces.size() - toDisplay)));
		}

		return Component.literal("This server requires ").append(Component.literal(brandText).withStyle(ChatFormatting.GREEN)).append(" installed on your client!")
				.append(CommonComponents.NEW_LINE).append(text)
				.append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.literal("Contact the server's administrator for more information!").withStyle(ChatFormatting.GOLD));
	}

	private static boolean areAllRegistriesOptional(Map<Identifier, Object2IntMap<Identifier>> map) {
		return map.keySet().stream()
				.map(BuiltInRegistries.REGISTRY::getValue)
				.filter(Objects::nonNull)
				.map(RegistryAttributeHolder::get)
				.allMatch(attributes -> attributes.hasAttribute(RegistryAttribute.OPTIONAL));
	}

	public record SyncConfigurationTask(
			ServerConfigurationPacketListenerImpl handler,
			Map<Identifier, Object2IntMap<Identifier>> map
	) implements ConfigurationTask {
		public static final Type KEY = new Type("fabric:registry/sync");

		@Override
		public void start(Consumer<Packet<?>> sender) {
			sender.accept(ServerConfigurationNetworking.createS2CPacket(new RegistrySyncPayload(map)));
		}

		@Override
		public Type type() {
			return KEY;
		}
	}

	/**
	 * Creates a {@link Map} used to sync the registry ids.
	 *
	 * @return a {@link Map} to sync, null when empty
	 */
	@Nullable
	public static Map<Identifier, Object2IntMap<Identifier>> createAndPopulateRegistryMap() {
		Map<Identifier, Object2IntMap<Identifier>> map = new LinkedHashMap<>();

		for (Identifier registryId : BuiltInRegistries.REGISTRY.keySet()) {
			Registry registry = BuiltInRegistries.REGISTRY.getValue(registryId);

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
							Identifier id = registry.getKey(o);
							if (id == null) continue;

							//noinspection unchecked
							int rawId = registry.getId(o);
							String stringId = id.toString();
							builder.append("\"").append(rawId).append("\",\"").append(stringId).append("\",\"").append(classType).append("\"\n");
						}

						stream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
					} catch (IOException e) {
						LOGGER.warn("[fabric-registry-sync debug] Could not write to " + file.getAbsolutePath() + "!", e);
					}
				}
			}

			RegistryAttributeHolder attributeHolder = RegistryAttributeHolder.get(registry.key());

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
					Identifier id = registry.getKey(o);
					if (id == null) continue;

					//noinspection unchecked
					int rawId = registry.getId(o);

					if (DEBUG) {
						if (registry.getValue(id) != o) {
							LOGGER.error("[fabric-registry-sync] Inconsistency detected in " + registryId + ": object " + o + " -> string ID " + id + " -> object " + registry.getValue(id) + "!");
						}

						if (registry.byId(rawId) != o) {
							LOGGER.error("[fabric-registry-sync] Inconsistency detected in " + registryId + ": object " + o + " -> integer ID " + rawId + " -> object " + registry.byId(rawId) + "!");
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

	public static void bootstrapRegistries() {
		postBootstrap = true;
	}
}
