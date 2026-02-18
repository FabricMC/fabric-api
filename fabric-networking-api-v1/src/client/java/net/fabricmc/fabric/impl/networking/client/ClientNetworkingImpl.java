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

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

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
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.mixin.networking.client.accessor.ConnectScreenAccessor;
import net.fabricmc.fabric.mixin.networking.client.accessor.MinecraftAccessor;

public final class ClientNetworkingImpl {
	public static final GlobalReceiverRegistry<ClientLoginNetworking.LoginQueryRequestHandler> LOGIN = new GlobalReceiverRegistry<>(PacketFlow.CLIENTBOUND, ConnectionProtocol.LOGIN, null);
	public static final GlobalReceiverRegistry<ClientConfigurationNetworking.ConfigurationPayloadHandler<?>> CONFIGURATION = new GlobalReceiverRegistry<>(PacketFlow.CLIENTBOUND, ConnectionProtocol.CONFIGURATION, PayloadTypeRegistryImpl.CLIENTBOUND_CONFIGURATION);
	public static final GlobalReceiverRegistry<ClientPlayNetworking.PlayPayloadHandler<?>> PLAY = new GlobalReceiverRegistry<>(PacketFlow.CLIENTBOUND, ConnectionProtocol.PLAY, PayloadTypeRegistryImpl.CLIENTBOUND_PLAY);

	public static final ScopedValue<Connection> CONNECTION_SCOPED_VALUE = ScopedValue.newInstance();

	private static ClientPlayNetworkAddon currentPlayAddon;
	private static ClientConfigurationNetworkAddon currentConfigurationAddon;

	public static ClientPlayNetworkAddon getAddon(ClientPacketListener listener) {
		return (ClientPlayNetworkAddon) ((PacketListenerExtensions) listener).getAddon();
	}

	public static ClientConfigurationNetworkAddon getAddon(ClientConfigurationPacketListenerImpl listener) {
		return (ClientConfigurationNetworkAddon) ((PacketListenerExtensions) listener).getAddon();
	}

	public static ClientLoginNetworkAddon getAddon(ClientHandshakePacketListenerImpl listener) {
		return (ClientLoginNetworkAddon) ((PacketListenerExtensions) listener).getAddon();
	}

	public static Packet<ServerCommonPacketListener> createServerboundPacket(CustomPacketPayload payload) {
		Objects.requireNonNull(payload, "Payload cannot be null");
		Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + payload.getClass());

		return new ServerboundCustomPayloadPacket(payload);
	}

	/**
	 * Due to the way logging into an integrated or remote dedicated server will differ, we need to obtain the login client connection differently.
	 */
	@Nullable
	public static Connection getLoginConnection() {
		final Connection connection = ((MinecraftAccessor) Minecraft.getInstance()).getPendingConnection();

		// Check if we are connecting to an integrated server. This will set the field on Minecraft
		if (connection != null) {
			return connection;
		} else if (CONNECTION_SCOPED_VALUE.isBound()) {
			return CONNECTION_SCOPED_VALUE.get();
		} else if (Minecraft.getInstance().screen instanceof ConnectScreen) {
			return ((ConnectScreenAccessor) Minecraft.getInstance().screen).getConnection();
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
		if (Minecraft.getInstance().getConnection() != null) {
			currentPlayAddon = null; // Shouldn't need this anymore
			return getAddon(Minecraft.getInstance().getConnection());
		}

		// We haven't hit the end of onGameJoin yet, use our backing field here to access the packet listener
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
		ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> {
			currentPlayAddon = null;
		});

		ClientConfigurationConnectionEvents.DISCONNECT.register((listener, client) -> {
			currentConfigurationAddon = null;
		});

		// Version packet
		ClientConfigurationNetworking.registerGlobalReceiver(CommonVersionPayload.TYPE, (listener, context) -> {
			int negotiatedVersion = handleVersionPacket(listener, context.responseSender());
			ClientNetworkingImpl.getClientConfigurationAddon().onCommonVersionPacket(negotiatedVersion);
		});

		// Register packet
		ClientConfigurationNetworking.registerGlobalReceiver(CommonRegisterPayload.TYPE, (listener, context) -> {
			ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();

			if (CommonRegisterPayload.PLAY_PROTOCOL.equals(listener.protocol())) {
				if (listener.version() != addon.getNegotiatedVersion()) {
					throw new IllegalStateException("Negotiated common packet version: %d but received packet with version: %d".formatted(addon.getNegotiatedVersion(), listener.version()));
				}

				addon.getChannelInfoHolder().fabric_getPendingChannelsNames(ConnectionProtocol.PLAY).addAll(listener.channels());
				NetworkingImpl.LOGGER.debug("Received accepted channels from the server");
				context.responseSender().sendPacket(new CommonRegisterPayload(addon.getNegotiatedVersion(), CommonRegisterPayload.PLAY_PROTOCOL, ClientPlayNetworking.getGlobalReceivers()));
			} else {
				addon.onCommonRegisterPacket(listener);
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
