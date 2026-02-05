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

package net.fabricmc.fabric.impl.networking.server;

import java.util.Objects;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;

public final class ServerNetworkingImpl {
	public static final GlobalReceiverRegistry<ServerLoginNetworking.LoginQueryResponseHandler> LOGIN = new GlobalReceiverRegistry<>(PacketFlow.SERVERBOUND, ConnectionProtocol.LOGIN, null);
	public static final GlobalReceiverRegistry<ServerConfigurationNetworking.ConfigurationPacketHandler<?>> CONFIGURATION = new GlobalReceiverRegistry<>(PacketFlow.SERVERBOUND, ConnectionProtocol.CONFIGURATION, PayloadTypeRegistryImpl.SERVERBOUND_CONFIGURATION);
	public static final GlobalReceiverRegistry<ServerPlayNetworking.PlayPayloadHandler<?>> PLAY = new GlobalReceiverRegistry<>(PacketFlow.SERVERBOUND, ConnectionProtocol.PLAY, PayloadTypeRegistryImpl.SERVERBOUND_PLAY);

	public static ServerPlayNetworkAddon getAddon(ServerGamePacketListenerImpl listener) {
		return (ServerPlayNetworkAddon) ((PacketListenerExtensions) listener).getAddon();
	}

	public static ServerLoginNetworkAddon getAddon(ServerLoginPacketListenerImpl listener) {
		return (ServerLoginNetworkAddon) ((PacketListenerExtensions) listener).getAddon();
	}

	public static ServerConfigurationNetworkAddon getAddon(ServerConfigurationPacketListenerImpl listener) {
		return (ServerConfigurationNetworkAddon) ((PacketListenerExtensions) listener).getAddon();
	}

	public static Packet<ClientCommonPacketListener> createClientboundPacket(CustomPacketPayload payload) {
		Objects.requireNonNull(payload, "Payload cannot be null");
		Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + payload.getClass());

		return new ClientboundCustomPayloadPacket(payload);
	}
}
