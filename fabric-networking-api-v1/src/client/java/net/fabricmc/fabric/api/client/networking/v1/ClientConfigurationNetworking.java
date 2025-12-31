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

package net.fabricmc.fabric.api.client.networking.v1;

import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.client.ClientConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;

/// Offers access to configuration stage client-side networking functionalities.
///
/// Client-side networking functionalities include receiving clientbound packets,
/// sending serverbound packets, and events related to client-side packet listeners.
/// Packets **received** by this class must be registered to
/// [net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry#clientboundConfiguration()] on both ends.
/// Packets **sent** by this class must be registered to
/// [net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry#serverboundConfiguration()] on both ends.
/// Packets must be registered before registering any receivers.
///
/// This class should be only used on the physical client and for the logical client.
///
/// See [net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking] for information on how to use the packet
/// object-based API.
///
/// @see net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking
public final class ClientConfigurationNetworking {
	/// Registers a handler for a packet type.
	/// A global receiver is registered to all connections, in the present and future.
	///
	/// If a handler is already registered for the `type`, this method will return `false`, and no change will be made.
	/// Use [#unregisterGlobalReceiver(CustomPacketPayload.Type)] to unregister the existing handler.
	///
	/// @param type the packet type
	/// @param handler the handler
	/// @return false if a handler is already registered to the channel
	/// @throws IllegalArgumentException if the codec for `type` has not been [registered][net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry#clientboundConfiguration()] yet
	/// @see ClientConfigurationNetworking#unregisterGlobalReceiver(CustomPacketPayload.Type)
	/// @see ClientConfigurationNetworking#registerReceiver(CustomPacketPayload.Type, ConfigurationPayloadHandler)
	public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, ConfigurationPayloadHandler<T> handler) {
		return ClientNetworkingImpl.CONFIGURATION.registerGlobalReceiver(type.id(), handler);
	}

	/// Removes the handler for a packet type.
	/// A global receiver is registered to all connections, in the present and future.
	///
	/// The `type` is guaranteed not to have an associated handler after this call.
	///
	/// @param type the packet type
	/// @return the previous handler, or `null` if no handler was bound to the channel,
	/// or it was not registered using [#registerGlobalReceiver(CustomPacketPayload.Type, ConfigurationPayloadHandler)]
	/// @see ClientConfigurationNetworking#registerGlobalReceiver(CustomPacketPayload.Type, ConfigurationPayloadHandler)
	/// @see ClientConfigurationNetworking#unregisterReceiver(Identifier)
	public static ClientConfigurationNetworking.@Nullable ConfigurationPayloadHandler<?> unregisterGlobalReceiver(CustomPacketPayload.Type<?> type) {
		return ClientNetworkingImpl.CONFIGURATION.unregisterGlobalReceiver(type.id());
	}

	/// Gets all channel names which global receivers are registered for.
	/// A global receiver is registered to all connections, in the present and future.
	///
	/// @return all channel names which global receivers are registered for.
	public static Set<Identifier> getGlobalReceivers() {
		return ClientNetworkingImpl.CONFIGURATION.getChannels();
	}

	/// Registers a handler for a packet type.
	///
	/// If a handler is already registered for the `type`, this method will return `false`, and no change will be made.
	/// Use [#unregisterReceiver(Identifier)] to unregister the existing handler.
	///
	/// For example, if you only register a receiver using this method when a [ClientLoginNetworking#registerGlobalReceiver(Identifier, ClientLoginNetworking.LoginQueryRequestHandler)]
	/// login query has been received, you should use [ClientPlayConnectionEvents#INIT] to register the channel handler.
	///
	/// @param type the payload type
	/// @param handler the handler
	/// @return `false` if a handler is already registered for the type
	/// @throws IllegalArgumentException if the codec for `type` has not been [registered][net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry#clientboundConfiguration()] yet
	/// @throws IllegalStateException if the client is not connected to a server
	/// @see ClientPlayConnectionEvents#INIT
	public static <T extends CustomPacketPayload> boolean registerReceiver(CustomPacketPayload.Type<T> type, ConfigurationPayloadHandler<T> handler) {
		final ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();

		if (addon != null) {
			return addon.registerChannel(type.id(), handler);
		}

		throw new IllegalStateException("Cannot register receiver while not configuring!");
	}

	/// Removes the handler for a packet type.
	///
	/// The `type` is guaranteed not to have an associated handler after this call.
	///
	/// @param id the payload id to unregister
	/// @return the previous handler, or `null` if no handler was bound to the channel,
	/// or it was not registered using [#registerReceiver(CustomPacketPayload.Type, ConfigurationPayloadHandler)]
	/// @throws IllegalStateException if the client is not connected to a server
	public static ClientConfigurationNetworking.@Nullable ConfigurationPayloadHandler<?> unregisterReceiver(Identifier id) {
		final ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();

		if (addon != null) {
			return addon.unregisterChannel(id);
		}

		throw new IllegalStateException("Cannot unregister receiver while not configuring!");
	}

	/// Gets all the channel names that the client can receive packets on.
	///
	/// @return All the channel names that the client can receive packets on
	/// @throws IllegalStateException if the client is not connected to a server
	public static Set<Identifier> getReceived() throws IllegalStateException {
		final ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();

		if (addon != null) {
			return addon.getReceivableChannels();
		}

		throw new IllegalStateException("Cannot get a list of channels the client can receive packets on while not configuring!");
	}

	/// Gets all channel names that the connected server declared the ability to receive a packets on.
	///
	/// @return All the channel names the connected server declared the ability to receive a packets on
	/// @throws IllegalStateException if the client is not connected to a server
	public static Set<Identifier> getSendable() throws IllegalStateException {
		final ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();

		if (addon != null) {
			return addon.getSendableChannels();
		}

		throw new IllegalStateException("Cannot get a list of channels the server can receive packets on while not configuring!");
	}

	/// Checks if the connected server declared the ability to receive a packet on a specified channel name.
	///
	/// @param channelName the channel name
	/// @return `true` if the connected server has declared the ability to receive a packet on the specified channel.
	/// False if the client is not in game.
	public static boolean canSend(Identifier channelName) throws IllegalArgumentException {
		final ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();

		if (addon != null) {
			return addon.getSendableChannels().contains(channelName);
		}

		throw new IllegalStateException("Cannot get a list of channels the server can receive packets on while not configuring!");
	}

	/// Checks if the connected server declared the ability to receive a packet on a specified channel name.
	/// This returns `false` if the client is not in game.
	///
	/// @param type the packet type
	/// @return `true` if the connected server has declared the ability to receive a packet on the specified channel
	public static boolean canSend(CustomPacketPayload.Type<?> type) {
		return canSend(type.id());
	}

	/// Gets the packet sender which sends packets to the connected server.
	///
	/// @return the client's packet sender
	/// @throws IllegalStateException if the client is not connected to a server
	public static PacketSender getSender() throws IllegalStateException {
		final ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();

		if (addon != null) {
			return addon;
		}

		throw new IllegalStateException("Cannot get PacketSender while not configuring!");
	}

	/// Sends a packet to the connected server.
	///
	/// Any packets sent must be [registered][net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry#serverboundConfiguration()].
	///
	/// @param payload to be sent
	/// @throws IllegalStateException if the client is not connected to a server
	public static void send(CustomPacketPayload payload) {
		Objects.requireNonNull(payload, "Payload cannot be null");
		Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + payload.getClass());

		final ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();

		if (addon != null) {
			addon.sendPacket(payload);
			return;
		}

		throw new IllegalStateException("Cannot send packet while not configuring!");
	}

	private ClientConfigurationNetworking() {
	}

	/// A packet handler utilizing [CustomPacketPayload].
	/// @param <T> the type of the packet
	@FunctionalInterface
	public interface ConfigurationPayloadHandler<T extends CustomPacketPayload> {
		/// Handles the incoming packet.
		///
		/// Unlike [ClientPlayNetworking.PlayPayloadHandler] this method is executed on [netty's event loops][io.netty.channel.EventLoop].
		/// Modification to the game should be [scheduled][net.minecraft.util.thread.BlockableEventLoop#submit(Runnable)].
		///
		/// An example usage of this:
		///
		/// ```java
		/// // use net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry for registering the payloadClient
		/// ConfigurationNetworking.registerReceiver(OVERLAY_PACKET_TYPE, (payload, context) ->{});
		/// ```
		///
		/// @param payload the packet payload
		/// @param context the configuration networking context
		/// @see CustomPacketPayload
		void receive(T payload, Context context);
	}

	@ApiStatus.NonExtendable
	public interface Context {
		/// @return The Minecraft instance
		Minecraft client();

		/// @return The ClientConfigurationPacketListenerImpl instance
		ClientConfigurationPacketListenerImpl packetListener();

		/// @return The packet sender
		PacketSender responseSender();
	}
}
