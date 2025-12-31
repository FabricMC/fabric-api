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

package net.fabricmc.fabric.api.networking.v1;

import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.mixin.networking.accessor.ServerCommonPacketListenerImplAccessor;

/// Offers access to configuration stage server-side networking functionalities.
///
/// Server-side networking functionalities include receiving serverbound packets, sending clientbound packets, and events related to server-side packet listeners.
/// Packets **received** by this class must be registered to [PayloadTypeRegistry#serverboundConfiguration()] on both ends.
/// Packets **sent** by this class must be registered to [PayloadTypeRegistry#clientboundConfiguration()] on both ends.
/// Packets must be registered before registering any receivers.
///
/// This class should be only used for the logical server.
///
/// See [ServerPlayNetworking] for information on sending and receiving play phase packets.
///
/// See the documentation on each class for more information.
///
/// @see ServerLoginNetworking
/// @see ServerConfigurationNetworking
public final class ServerConfigurationNetworking {
	/// Registers a handler for a payload type.
	/// A global receiver is registered to all connections, in the present and future.
	///
	/// If a handler is already registered for the `type`, this method will return `false`, and no change will be made.
	/// Use [#unregisterReceiver(ServerConfigurationPacketListenerImpl, Identifier)] to unregister the existing handler.
	///
	/// @param type the packet type
	/// @param handler the handler
	/// @return `false` if a handler is already registered to the channel
	/// @throws IllegalArgumentException if the codec for `type` has not been [registered][PayloadTypeRegistry#serverboundConfiguration()] yet
	/// @see ServerConfigurationNetworking#unregisterGlobalReceiver(Identifier)
	/// @see ServerConfigurationNetworking#registerReceiver(ServerConfigurationPacketListenerImpl, CustomPacketPayload.Type, ConfigurationPacketHandler)
	public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, ConfigurationPacketHandler<T> handler) {
		return ServerNetworkingImpl.CONFIGURATION.registerGlobalReceiver(type.id(), handler);
	}

	/// Removes the handler for a payload type.
	/// A global receiver is registered to all connections, in the present and future.
	///
	/// The `type` is guaranteed not to have an associated handler after this call.
	///
	/// @param id the packet payload id
	/// @return the previous handler, or `null` if no handler was bound to the channel,
	/// or it was not registered using [#registerGlobalReceiver(CustomPacketPayload.Type, ConfigurationPacketHandler)]
	/// @see ServerConfigurationNetworking#registerGlobalReceiver(CustomPacketPayload.Type, ConfigurationPacketHandler)
	/// @see ServerConfigurationNetworking#unregisterReceiver(ServerConfigurationPacketListenerImpl, Identifier)
	public static ServerConfigurationNetworking.@Nullable ConfigurationPacketHandler<?> unregisterGlobalReceiver(Identifier id) {
		return ServerNetworkingImpl.CONFIGURATION.unregisterGlobalReceiver(id);
	}

	/// Gets all channel names which global receivers are registered for.
	/// A global receiver is registered to all connections, in the present and future.
	///
	/// @return all channel names which global receivers are registered for.
	public static Set<Identifier> getGlobalReceivers() {
		return ServerNetworkingImpl.CONFIGURATION.getChannels();
	}

	/// Registers a handler for a payload type.
	/// This method differs from [ServerConfigurationNetworking#registerGlobalReceiver(CustomPacketPayload.Type, ConfigurationPacketHandler)] since
	/// the channel handler will only be applied to the client represented by the [ServerConfigurationPacketListenerImpl].
	///
	/// If a handler is already registered for the `type`, this method will return `false`, and no change will be made.
	/// Use [#unregisterReceiver(ServerConfigurationPacketListenerImpl, Identifier)] to unregister the existing handler.
	///
	/// @param packetListener the packet listener
	/// @param type the packet type
	/// @param handler the handler
	/// @return `false` if a handler is already registered to the channel name
	/// @throws IllegalArgumentException if the codec for `type` has not been [registered][PayloadTypeRegistry#serverboundConfiguration()] yet
	/// @see ServerPlayConnectionEvents#INIT
	public static <T extends CustomPacketPayload> boolean registerReceiver(ServerConfigurationPacketListenerImpl packetListener, CustomPacketPayload.Type<T> type, ConfigurationPacketHandler<T> handler) {
		return ServerNetworkingImpl.getAddon(packetListener).registerChannel(type.id(), handler);
	}

	/// Removes the handler for a payload type.
	///
	/// The `type` is guaranteed not to have an associated handler after this call.
	///
	/// @param id the id of the payload
	/// @return the previous handler, or `null` if no handler was bound to the channel,
	/// or it was not registered using [#registerReceiver(ServerConfigurationPacketListenerImpl, CustomPacketPayload.Type, ConfigurationPacketHandler)]
	public static ServerConfigurationNetworking.@Nullable ConfigurationPacketHandler<?> unregisterReceiver(ServerConfigurationPacketListenerImpl packetListener, Identifier id) {
		return ServerNetworkingImpl.getAddon(packetListener).unregisterChannel(id);
	}

	/// Gets all the channel names that the server can receive packets on.
	///
	/// @param listener the packet listener
	/// @return All the channel names that the server can receive packets on
	public static Set<Identifier> getReceived(ServerConfigurationPacketListenerImpl listener) {
		Objects.requireNonNull(listener, "Server configuration packet listener cannot be null");

		return ServerNetworkingImpl.getAddon(listener).getReceivableChannels();
	}

	/// Gets all channel names that a connected client declared the ability to receive a packets on.
	///
	/// @param listener the packet listener
	/// @return `true` if the connected client has declared the ability to receive a packet on the specified channel
	public static Set<Identifier> getSendable(ServerConfigurationPacketListenerImpl listener) {
		Objects.requireNonNull(listener, "Server configuration packet listener cannot be null");

		return ServerNetworkingImpl.getAddon(listener).getSendableChannels();
	}

	/// Checks if the connected client declared the ability to receive a packet on a specified channel name.
	///
	/// @param listener the packet listener
	/// @param channelName the channel name
	/// @return `true` if the connected client has declared the ability to receive a packet on the specified channel
	public static boolean canSend(ServerConfigurationPacketListenerImpl listener, Identifier channelName) {
		Objects.requireNonNull(listener, "Server configuration packet listener cannot be null");
		Objects.requireNonNull(channelName, "Channel name cannot be null");

		return ServerNetworkingImpl.getAddon(listener).getSendableChannels().contains(channelName);
	}

	/// Checks if the connected client declared the ability to receive a specific type of packet.
	///
	/// @param packetListener the packet listener
	/// @param id the payload id
	/// @return `true` if the connected client has declared the ability to receive a specific type of packet
	public static boolean canSend(ServerConfigurationPacketListenerImpl packetListener, CustomPacketPayload.Type<?> id) {
		Objects.requireNonNull(packetListener, "Server configuration packet listener cannot be null");
		Objects.requireNonNull(id, "Payload id cannot be null");

		return ServerNetworkingImpl.getAddon(packetListener).getSendableChannels().contains(id.id());
	}

	/// Creates a packet which may be sent to a connected client.
	///
	/// @param payload the payload
	/// @return a new packet
	public static Packet<ClientCommonPacketListener> createClientboundPacket(CustomPacketPayload payload) {
		Objects.requireNonNull(payload, "Payload cannot be null");
		Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + payload.getClass());

		return ServerNetworkingImpl.createClientboundPacket(payload);
	}

	/// Gets the packet sender which sends packets to the connected client.
	///
	/// @param listener the packet listener, representing the connection to the player/client
	/// @return the packet sender
	public static PacketSender getSender(ServerConfigurationPacketListenerImpl listener) {
		Objects.requireNonNull(listener, "Server configuration packet listener cannot be null");

		return ServerNetworkingImpl.getAddon(listener);
	}

	/// Sends a packet to a configuring player.
	///
	/// Any packets sent must be [registered][PayloadTypeRegistry#clientboundConfiguration()].
	///
	/// @param listener the packet listener to send the packet to
	/// @param payload to be sent
	public static void send(ServerConfigurationPacketListenerImpl listener, CustomPacketPayload payload) {
		Objects.requireNonNull(listener, "Server configuration listener cannot be null");
		Objects.requireNonNull(payload, "Payload cannot be null");
		Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + payload.getClass());

		listener.send(createClientboundPacket(payload));
	}

	// Helper methods

	/// Returns the _Minecraft_ Server of a server configuration packet listener.
	///
	/// @param listener the server configuration packet listener
	public static MinecraftServer getServer(ServerConfigurationPacketListenerImpl listener) {
		Objects.requireNonNull(listener, "Packet listener cannot be null");

		return ((ServerCommonPacketListenerImplAccessor) listener).getServer();
	}

	/// Returns true if the client has previously completed configuration, and has re-entered the configuration phase.
	///
	/// @param listener the server configuration packet listener
	/// @return `true` if the client is reconfiguring
	public static boolean isReconfiguring(ServerConfigurationPacketListenerImpl listener) {
		Objects.requireNonNull(listener, "Server configuration packet listener cannot be null");

		return ServerNetworkingImpl.getAddon(listener).isReconfiguring();
	}

	private ServerConfigurationNetworking() {
	}

	/// A packet handler utilizing [CustomPacketPayload].
	/// @param <T> the type of the packet
	@FunctionalInterface
	public interface ConfigurationPacketHandler<T extends CustomPacketPayload> {
		/// Handles an incoming packet.
		///
		/// Unlike [ServerPlayNetworking.PlayPayloadHandler] this method is executed on [netty's event loops][io.netty.channel.EventLoop].
		/// Modification to the game should be [scheduled][net.minecraft.util.thread.BlockableEventLoop#submit(Runnable)] using the Minecraft server instance from [ServerConfigurationNetworking#getServer(ServerConfigurationPacketListenerImpl)].
		///
		/// An example usage of this:
		///
		/// ```java
		/// // use PayloadTypeRegistry for registering the payload
		/// ServerConfigurationNetworking.registerReceiver(BOOM_PACKET_TYPE, (payload, context) -> {
		/// });
		/// ```
		///
		/// @param payload the packet payload
		/// @param context the configuration networking context
		/// @see CustomPacketPayload
		void receive(T payload, Context context);
	}

	@ApiStatus.NonExtendable
	public interface Context {
		/// @return The MinecraftServer instance
		MinecraftServer server();

		/// @return The ServerConfigurationPacketListenerImpl instance
		ServerConfigurationPacketListenerImpl packetListener();

		/// @return The packet sender
		PacketSender responseSender();
	}
}
