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

package net.fabricmc.fabric.impl.client.registry.sync;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.RemappableRegistry;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistryPacketHandler;

public final class ClientRegistrySyncHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientRegistrySyncHandler.class);

	private ClientRegistrySyncHandler() {
	}

	public static <T extends RegistryPacketHandler.RegistrySyncPayload> CompletableFuture<Boolean> receivePacket(ThreadExecutor<?> executor, RegistryPacketHandler<T> handler, T payload, boolean accept) {
		handler.receivePayload(payload);

		if (!handler.isPacketFinished()) {
			return CompletableFuture.completedFuture(false);
		}

		if (RegistrySyncManager.DEBUG) {
			String handlerName = handler.getClass().getSimpleName();
			LOGGER.info("{} total packet: {}", handlerName, handler.getTotalPacketReceived());
			LOGGER.info("{} raw size: {}", handlerName, handler.getRawBufSize());
			LOGGER.info("{} deflated size: {}", handlerName, handler.getDeflatedBufSize());
		}

		RegistryPacketHandler.SyncedPacketData data = handler.getSyncedPacketData();

		if (!accept) {
			return CompletableFuture.completedFuture(true);
		}

		return executor.submit(() -> {
			if (data == null) {
				throw new CompletionException(new RemapException("Received null map in sync packet!"));
			}

			try {
				apply(data);
				return true;
			} catch (RemapException e) {
				throw new CompletionException(e);
			}
		});
	}

	public static void apply(RegistryPacketHandler.SyncedPacketData data) throws RemapException {
		// First check that all of the data provided is valid before making any changes
		checkRemoteRemap(data);

		for (Map.Entry<Identifier, Object2IntMap<Identifier>> entry : data.idMap().entrySet()) {
			final Identifier registryId = entry.getKey();

			Registry<?> registry = Registries.REGISTRIES.get(registryId);

			// Registry was not found on the client, is it optional?
			// If so we can just ignore it.
			// Otherwise we throw an exception and disconnect.
			if (registry == null) {
				if (isRegistryOptional(registryId, data)) {
					LOGGER.info("Received registry data for unknown optional registry: {}", registryId);
					continue;
				}
			}

			if (!(registry instanceof RemappableRegistry remappableRegistry)) {
				throw new RemapException("Registry " + registryId + " is not remappable");
			}

			remappableRegistry.remap(entry.getValue(), RemappableRegistry.RemapMode.REMOTE);
		}
	}

	@VisibleForTesting
	public static void checkRemoteRemap(RegistryPacketHandler.SyncedPacketData data) throws RemapException {
		Map<Identifier, Object2IntMap<Identifier>> map = data.idMap();
		ArrayList<Identifier> missingRegistries = new ArrayList<>();
		Map<Identifier, List<Identifier>> missingEntries = new HashMap<>();

		for (Identifier registryId : map.keySet()) {
			final Object2IntMap<Identifier> remoteRegistry = map.get(registryId);
			Registry<?> registry = Registries.REGISTRIES.get(registryId);

			if (registry == null) {
				if (!isRegistryOptional(registryId, data)) {
					// Registry was not found on the client, and is not optional.
					missingRegistries.add(registryId);
				}

				continue;
			}

			for (Identifier remoteId : remoteRegistry.keySet()) {
				if (!registry.containsId(remoteId)) {
					// Found a registry entry from the server that is missing on the client
					missingEntries.computeIfAbsent(registryId, i -> new ArrayList<>()).add(remoteId);
				}
			}
		}

		if (missingRegistries.isEmpty() && missingEntries.isEmpty()) {
			// All good :)
			return;
		}

		// Print out details to the log
		if (!missingRegistries.isEmpty()) {
			LOGGER.error("Received unknown remote registries from server");

			for (Identifier registryId : missingRegistries) {
				LOGGER.error("Received unknown remote registry ({}) from server", registryId);
			}
		}

		if (!missingEntries.isEmpty()) {
			LOGGER.error("Received unknown remote registry entries from server");

			for (Map.Entry<Identifier, List<Identifier>> entry : missingEntries.entrySet()) {
				for (Identifier identifier : entry.getValue()) {
					LOGGER.error("Registry entry ({}) is missing from local registry ({})", identifier, entry.getKey());
				}
			}
		}

		if (!missingRegistries.isEmpty()) {
			throw new RemapException(missingRegistriesError(missingRegistries));
		}

		throw new RemapException(missingEntriesError(missingEntries));
	}

	private static Text missingRegistriesError(List<Identifier> missingRegistries) {
		MutableText text = Text.empty();

		final int count = missingRegistries.size();

		if (count == 1) {
			text = text.append(Text.translatable("fabric-registry-sync-v0.unknown-registry.title.singular"));
		} else {
			text = text.append(Text.translatable("fabric-registry-sync-v0.unknown-registry.title.plural", count));
		}

		text = text.append(Text.translatable("fabric-registry-sync-v0.unknown-registry.subtitle.1").formatted(Formatting.GREEN));
		text = text.append(Text.translatable("fabric-registry-sync-v0.unknown-registry.subtitle.2"));

		final int toDisplay = 4;

		for (int i = 0; i < Math.min(missingRegistries.size(), toDisplay); i++) {
			text = text.append(Text.literal(missingRegistries.get(i).toString()).formatted(Formatting.YELLOW));
			text = text.append(ScreenTexts.LINE_BREAK);
		}

		if (missingRegistries.size() > toDisplay) {
			text = text.append(Text.translatable("fabric-registry-sync-v0.unknown-registry.footer", missingRegistries.size() - toDisplay));
		}

		return text;
	}

	private static Text missingEntriesError(Map<Identifier, List<Identifier>> missingEntries) {
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

		return text;
	}

	private static boolean isRegistryOptional(Identifier registryId, RegistryPacketHandler.SyncedPacketData data) {
		EnumSet<RegistryAttribute> registryAttributes = data.attributes().get(registryId);
		return registryAttributes.contains(RegistryAttribute.OPTIONAL);
	}
}
