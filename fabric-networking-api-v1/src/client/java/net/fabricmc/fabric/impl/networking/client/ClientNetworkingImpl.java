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

package net.fabricmc.fabric.impl.networking.client;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.CommonPacketsImpl;
import net.fabricmc.fabric.impl.networking.CommonRegisterPayload;
import net.fabricmc.fabric.impl.networking.CommonVersionPayload;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.mixin.networking.client.accessor.ConnectScreenAccessor;
import net.fabricmc.fabric.mixin.networking.client.accessor.MinecraftClientAccessor;

public final class ClientNetworkingImpl {
	public static final GlobalReceiverRegistry<ClientLoginNetworking.LoginQueryRequestHandler> LOGIN = new GlobalReceiverRegistry<>(NetworkSide.CLIENTBOUND, NetworkPhase.LOGIN, null);
	public static final GlobalReceiverRegistry<ClientConfigurationNetworking.ConfigurationPayloadHandler<?>> CONFIGURATION = new GlobalReceiverRegistry<>(NetworkSide.CLIENTBOUND, NetworkPhase.CONFIGURATION, PayloadTypeRegistryImpl.CONFIGURATION_S2C);
	public static final GlobalReceiverRegistry<ClientPlayNetworking.PlayPayloadHandler<?>> PLAY = new GlobalReceiverRegistry<>(NetworkSide.CLIENTBOUND, NetworkPhase.PLAY, PayloadTypeRegistryImpl.PLAY_S2C);

	private static ClientPlayNetworkAddon currentPlayAddon;
	private static ClientConfigurationNetworkAddon currentConfigurationAddon;

	public static ClientPlayNetworkAddon getAddon(ClientPlayNetworkHandler handler) {
		return (ClientPlayNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static ClientConfigurationNetworkAddon getAddon(ClientConfigurationNetworkHandler handler) {
		return (ClientConfigurationNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static ClientLoginNetworkAddon getAddon(ClientLoginNetworkHandler handler) {
		return (ClientLoginNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static Packet<ServerCommonPacketListener> createC2SPacket(CustomPayload payload) {
		Objects.requireNonNull(payload, "Payload cannot be null");
		Objects.requireNonNull(payload.getId(), "CustomPayload#getId() cannot return null for payload class: " + payload.getClass());

		return new CustomPayloadC2SPacket(payload);
	}

	/**
	 * Due to the way logging into an integrated or remote dedicated server will differ, we need to obtain the login client connection differently.
	 */
	@Nullable
	public static ClientConnection getLoginConnection() {
		final ClientConnection connection = ((MinecraftClientAccessor) MinecraftClient.getInstance()).getConnection();

		// Check if we are connecting to an integrated server. This will set the field on MinecraftClient
		if (connection != null) {
			return connection;
		} else {
			// We are probably connecting to a remote server.
			// Check if the ConnectScreen is the currentScreen to determine that:
			if (MinecraftClient.getInstance().currentScreen instanceof ConnectScreen) {
				return ((ConnectScreenAccessor) MinecraftClient.getInstance().currentScreen).getConnection();
			}
		}

		// We are not connected to a server at all.
		return null;
	}

	@Nullable
	public static ClientConfigurationNetworkAddon getClientConfigurationAddon() {
		return currentConfigurationAddon;
	}

	@Nullable
	public static ClientPlayNetworkAddon getClientPlayAddon() {
		// Since Minecraft can be a bit weird, we need to check for the play addon in a few ways:
		// If the client's player is set this will work
		if (MinecraftClient.getInstance().getNetworkHandler() != null) {
			currentPlayAddon = null; // Shouldn't need this anymore
			return getAddon(MinecraftClient.getInstance().getNetworkHandler());
		}

		// We haven't hit the end of onGameJoin yet, use our backing field here to access the network handler
		if (currentPlayAddon != null) {
			return currentPlayAddon;
		}

		// We are not in play stage
		return null;
	}

	public static void setClientPlayAddon(ClientPlayNetworkAddon addon) {
		if (!(addon == null || currentConfigurationAddon == null)) {
			throw new IllegalStateException();
		}

		currentPlayAddon = addon;
	}

	public static void setClientConfigurationAddon(ClientConfigurationNetworkAddon addon) {
		currentConfigurationAddon = addon;
	}

	public static void clientInit() {
		// Reference cleanup for the locally stored addon if we are disconnected
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			currentPlayAddon = null;
		});

		ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> {
			currentConfigurationAddon = null;
		});

		// Version packet
		ClientConfigurationNetworking.registerGlobalReceiver(CommonVersionPayload.ID, (payload, context) -> {
			int negotiatedVersion = handleVersionPacket(payload, context.responseSender());
			ClientNetworkingImpl.getClientConfigurationAddon().onCommonVersionPacket(negotiatedVersion);
		});

		// Register packet
		ClientConfigurationNetworking.registerGlobalReceiver(CommonRegisterPayload.ID, (payload, context) -> {
			ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();

			if (CommonRegisterPayload.PLAY_PHASE.equals(payload.phase())) {
				if (payload.version() != addon.getNegotiatedVersion()) {
					throw new IllegalStateException("Negotiated common packet version: %d but received packet with version: %d".formatted(addon.getNegotiatedVersion(), payload.version()));
				}

				addon.getChannelInfoHolder().fabric_getPendingChannelsNames(NetworkPhase.PLAY).addAll(payload.channels());
				NetworkingImpl.LOGGER.debug("Received accepted channels from the server");
				context.responseSender().sendPacket(new CommonRegisterPayload(addon.getNegotiatedVersion(), CommonRegisterPayload.PLAY_PHASE, ClientPlayNetworking.getGlobalReceivers()));
			} else {
				addon.onCommonRegisterPacket(payload);
				context.responseSender().sendPacket(addon.createRegisterPayload());
			}
		});
	}

	// Disconnect if there are no commonly supported versions.
	// Client responds with the intersection of supported versions.
	// Return the highest supported version
	private static int handleVersionPacket(CommonVersionPayload payload, PacketSender packetSender) {
		int version = CommonPacketsImpl.getHighestCommonVersion(payload.versions(), CommonPacketsImpl.SUPPORTED_COMMON_PACKET_VERSIONS);

		if (version <= 0) {
			throw new UnsupportedOperationException("Client does not support any requested versions from server");
		}

		packetSender.sendPacket(new CommonVersionPayload(new int[]{ version }));
		return version;
	}
}
