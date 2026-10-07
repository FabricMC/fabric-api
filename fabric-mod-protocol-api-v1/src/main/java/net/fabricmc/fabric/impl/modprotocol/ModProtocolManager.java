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

package net.fabricmc.fabric.impl.modprotocol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.modprotocol.payload.ClientboundModProtocolRequestPayload;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.loader.api.ModContainer;

public final class ModProtocolManager {
	public static final List<String> NAMESPACE_PRIORITY = new ArrayList<>(List.of("special", "mod", "feature"));
	public static final Comparator<RemoteModProtocol> MOD_PROTOCOL_COMPARATOR = Comparator.<RemoteModProtocol>comparingInt(x -> {
		int out = NAMESPACE_PRIORITY.indexOf(x.id().getNamespace());
		return out == -1 ? NAMESPACE_PRIORITY.size() : out;
	}).thenComparing(RemoteModProtocol::id);

	public static Component MISSING = LocalizedComponents.translatable("text.fabric-mod-protocol-v1.missing").withStyle(ChatFormatting.DARK_RED);

	public static final int INLINE_VERSION_COUNT = 5;

	public static final Map<Identifier, ModProtocolImpl> LOCAL_MOD_PROTOCOLS_BY_ID = new HashMap<>();
	public static final List<ModProtocolImpl> LOCAL_MOD_PROTOCOLS = new ArrayList<>();

	public static final List<RemoteModProtocol> SYNCED_PROTOCOLS = new ArrayList<>();
	public static final List<RemoteModProtocol> REQUIRED_ON_CLIENT = new ArrayList<>();
	public static final List<RemoteModProtocol> REQUIRED_ON_SERVER = new ArrayList<>();

	public static final PacketContext.Key<Map<Identifier, String>> REMOTE_MOD_VERSIONS_KEY = PacketContext.key(Identifier.fromNamespaceAndPath("fabric-mod-protocol-v1", "remote_mod_versions"));

	public static void setupClient(ServerConfigurationPacketListenerImpl handler, MinecraftServer server) {
		if (!ServerConfigurationNetworking.canSend(handler, ClientboundModProtocolRequestPayload.TYPE)) {
			if (REQUIRED_ON_CLIENT.isEmpty()) {
				return;
			} else {
				handler.disconnect(getIncompatibleClientMessage(ServerNetworkingImpl.getAddon(handler).getClientBrand(), new ArrayList<>(REQUIRED_ON_CLIENT), Map.of()));
			}
		}

		handler.addTask(new SyncConfigurationTask());
	}

	public static Component getIncompatibleClientMessage(@Nullable String brand, List<RemoteModProtocol> missingProtocols, Map<Identifier, ModProtocolImpl> localProtocols) {
		MutableComponent brandText = switch (brand) {
			case "fabric" -> LocalizedComponents.translatable("text.fabric-mod-protocol-v1.fabric_api");
			case null, default -> LocalizedComponents.translatable("text.fabric-mod-protocol-v1.loader_and_fabric_api");
		};

		MutableComponent text = Component.empty();
		text.append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.vanilla_client.title", brandText.withColor(TextColor.GREEN))).append(CommonComponents.NEW_LINE);
		text.append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.vanilla_client.desc")).append("\n\n");
		text.append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.entries.title").withStyle(ChatFormatting.RED)).append(CommonComponents.NEW_LINE);
		appendComponentEntries(missingProtocols, localProtocols, INLINE_VERSION_COUNT, text::append, false);

		text.append("\n\n").append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.contact").withColor(TextColor.GOLD));
		return text;
	}

	public static Component getIncompatibleServerMessage(@Nullable String brand, List<RemoteModProtocol> missingProtocols, Map<Identifier, ModProtocolImpl> localProtocols) {
		MutableComponent text = Component.empty();
		text.append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.vanilla_server.title")).append(CommonComponents.NEW_LINE);
		text.append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.vanilla_server.desc")).append("\n\n");
		text.append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.entries.title").withStyle(ChatFormatting.RED)).append(CommonComponents.NEW_LINE);
		appendComponentEntries(missingProtocols, localProtocols, INLINE_VERSION_COUNT, text::append, true);

		text.append("\n\n").append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.contact").withColor(TextColor.GOLD));
		return text;
	}

	public static Component getMismatchedVersionsMessage(List<RemoteModProtocol> missingProtocols, Map<Identifier, ModProtocolImpl> localProtocols) {
		MutableComponent text = Component.empty();
		text.append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.title")).append(CommonComponents.NEW_LINE);
		text.append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.desc")).append("\n\n");
		text.append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.entries.title").withStyle(ChatFormatting.RED)).append(CommonComponents.NEW_LINE);

		appendComponentEntries(missingProtocols, localProtocols, INLINE_VERSION_COUNT, text::append, false);

		text.append("\n\n").append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.mismatched.contact").withColor(TextColor.GOLD));
		return text;
	}

	public static void appendComponentEntries(List<RemoteModProtocol> missingProtocols, Map<Identifier, ModProtocolImpl> localProtocols, int limit, Consumer<Component> consumer, boolean remoteMissing) {
		missingProtocols.sort(MOD_PROTOCOL_COMPARATOR);

		if (limit == -1) {
			limit = missingProtocols.size();
		}

		int size = Math.min(limit, missingProtocols.size());

		for (int i = 0; i < size; i++) {
			RemoteModProtocol protocol = missingProtocols.get(i);
			ModProtocolImpl local = localProtocols.get(protocol.id());
			Component localVersion = local == null ? ModProtocolManager.MISSING
					: Component.literal(local.version()).withStyle(ChatFormatting.YELLOW);
			Component remoteVersion = protocol.version().isEmpty() || remoteMissing ? ModProtocolManager.MISSING
					: Component.literal(protocol.version()).withStyle(ChatFormatting.YELLOW);

			MutableComponent text = LocalizedComponents.translatable("text.fabric-mod-protocol-v1.entry",
					Component.literal(protocol.name()).withStyle(ChatFormatting.WHITE), localVersion, remoteVersion).withStyle(ChatFormatting.GRAY);

			if (i + 1 < size) {
				text.append(CommonComponents.NEW_LINE);
			}

			consumer.accept(text);
		}

		if (limit < missingProtocols.size()) {
			consumer.accept(CommonComponents.NEW_LINE.copy().append(LocalizedComponents.translatable("text.fabric-mod-protocol-v1.and_x_more", missingProtocols.size() - size).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
		}
	}

	public static ValidationResult validateClient(List<RemoteModProtocol> received) {
		var receivedById = new HashMap<Identifier, RemoteModProtocol>(received.size());

		for (RemoteModProtocol protocol : received) {
			receivedById.put(protocol.id(), protocol);
		}

		return validate(LOCAL_MOD_PROTOCOLS_BY_ID, receivedById, REQUIRED_ON_SERVER);
	}

	public static ValidationResult validate(Map<Identifier, ModProtocolImpl> localById, Map<Identifier, RemoteModProtocol> received, List<RemoteModProtocol> requiredRemote) {
		var supported = new HashMap<Identifier, String>();
		var alreadyFailed = new HashSet<Identifier>();
		var missingLocal = new ArrayList<RemoteModProtocol>();
		var missingRemote = new ArrayList<RemoteModProtocol>();

		for (RemoteModProtocol modProtocol : received.values()) {
			ModProtocolImpl local = localById.get(modProtocol.id());

			if (local != null && modProtocol.matches(local.version())) {
				supported.put(modProtocol.id(), local.version());
			} else if (modProtocol.require().isPresent()) {
				missingLocal.add(modProtocol);
				alreadyFailed.add(modProtocol.id());
			}
		}

		for (RemoteModProtocol modProtocol : requiredRemote) {
			if (alreadyFailed.contains(modProtocol.id())) {
				continue;
			}

			RemoteModProtocol remote = received.get(modProtocol.id());

			if (remote == null) {
				missingRemote.add(new RemoteModProtocol(modProtocol.id(), modProtocol.name(), "", Optional.empty()));
			} else if (!modProtocol.matches(remote.version())) {
				missingRemote.add(new RemoteModProtocol(modProtocol.id(), modProtocol.name(), remote.version(), Optional.empty()));
			}
		}

		return new ValidationResult(supported, missingLocal, missingRemote, false);
	}

	public static void collectModProtocols() {
		ModProtocolLocator.provide(ModProtocolManager::add);
	}

	public static ModProtocolImpl add(@Nullable ModContainer container, ModProtocolImpl protocol) {
		if (LOCAL_MOD_PROTOCOLS_BY_ID.containsKey(protocol.id())) {
			if (container != null) {
				ModProtocolInit.LOGGER.warn("Found duplicate protocol id '{}' provided by mod '{}'", protocol.id(), container.getMetadata().getId());
			} else {
				ModProtocolInit.LOGGER.warn("Found duplicate protocol id '{}' registered by a mod!'", protocol.id(), new RuntimeException());
			}

			return LOCAL_MOD_PROTOCOLS_BY_ID.get(protocol.id());
		}

		LOCAL_MOD_PROTOCOLS_BY_ID.put(protocol.id(), protocol);
		LOCAL_MOD_PROTOCOLS.add(protocol);

		RemoteModProtocol clientboundProtocol = protocol.asClientbound();
		SYNCED_PROTOCOLS.add(clientboundProtocol);

		if (clientboundProtocol.require().isPresent()) {
			REQUIRED_ON_CLIENT.add(clientboundProtocol);
		}

		if (protocol.requireOnServer().isPresent()) {
			REQUIRED_ON_SERVER.add(protocol.asServerbound());
		}

		return protocol;
	}

	@SuppressWarnings("ConstantValue")
	public static boolean registerOrder(String firstNamespace, String secondNamespace) {
		if (firstNamespace.equals(secondNamespace)) {
			return false;
		}

		int firstIndex = NAMESPACE_PRIORITY.indexOf(firstNamespace);
		int secondIndex = NAMESPACE_PRIORITY.indexOf(secondNamespace);

		if (firstIndex != -1 && secondIndex != -1) {
			if (firstIndex > secondIndex) {
				ModProtocolInit.LOGGER.warn("Protocol '{}' is already set to display after '{}'!", firstNamespace, secondNamespace);
				return false;
			}

			return true;
		} else if (firstIndex == -1) {
			NAMESPACE_PRIORITY.add(secondIndex, firstNamespace);
		} else if (secondIndex == -1) {
			NAMESPACE_PRIORITY.add(firstIndex + 1, secondNamespace);
		} else {
			NAMESPACE_PRIORITY.add(firstNamespace);
			NAMESPACE_PRIORITY.add(secondNamespace);
		}

		return true;
	}

	public static class SyncConfigurationTask implements ConfigurationTask {
		public static final Type TYPE = new Type("fabric:mod_protocol_sync");

		@Override
		public void start(Consumer<Packet<?>> connection) {
			connection.accept(new ClientboundCustomPayloadPacket(new ClientboundModProtocolRequestPayload(SYNCED_PROTOCOLS, true)));
		}

		@Override
		public Type type() {
			return TYPE;
		}
	}

	public record ValidationResult(Map<Identifier, String> supportedProtocols, List<RemoteModProtocol> missingLocal, List<RemoteModProtocol> missingRemote, boolean forceNoRemote) {
		public boolean isSuccess() {
			return missingLocal.isEmpty() && missingRemote.isEmpty();
		}

		public List<RemoteModProtocol> missing() {
			var arr = new ArrayList<RemoteModProtocol>();
			arr.addAll(missingLocal);
			arr.addAll(missingRemote);
			return arr;
		}

		public int missingCount() {
			return this.missingLocal.size() + this.missingRemote.size();
		}
	}
}
