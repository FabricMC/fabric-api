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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;

/// Offers access to play stage server-side networking functionalities.
///
/// Server-side networking functionalities include receiving serverbound packets, sending clientbound packets, and events related to server-side packet listeners.
/// Packets **received** by this class must be registered to [PayloadTypeRegistry#serverboundPlay()] on both ends.
/// Packets **sent** by this class must be registered to [PayloadTypeRegistry#clientboundPlay()] on both ends.
/// Packets must be registered before registering any receivers.
///
/// This class should be only used for the logical server.
/// ## Packet object-based API
///
/// This class provides a registration method, utilizing packet objects, [#registerGlobalReceiver(CustomPacketPayload.Type, PlayPayloadHandler)].
/// This handler executes the callback in the server thread, ensuring thread safety.
///
/// This payload object-based API involves three classes:
///
///   - A class implementing [CustomPacketPayload] that is "sent" over the network
///   - [CustomPacketPayload.TypeAndCodec] instance, which represents the packet's type (and its codec)
///   - [PlayPayloadHandler], which handles the packet (usually implemented as a functional interface)
///
///
/// See the documentation on each class for more information.
///
/// @see ServerLoginNetworking
/// @see ServerConfigurationNetworking
public final class ServerPlayNetworking {
	/// Registers a handler for a payload type.
	/// A global receiver is registered to all connections, in the present and future.
	///
	/// If a handler is already registered for the `type`, this method will return `false`, and no change will be made.
	/// Use [#unregisterGlobalReceiver(Identifier)] to unregister the existing handler.
	///
	/// @param type the packet type
	/// @param handler the handler
	/// @return `false` if a handler is already registered to the channel
	/// @throws IllegalArgumentException if the codec for `type` has not been [registered][PayloadTypeRegistry#serverboundPlay()] yet
	/// @see ServerPlayNetworking#unregisterGlobalReceiver(Identifier)
	public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) {
		return ServerNetworkingImpl.PLAY.registerGlobalReceiver(type.id(), handler);
	}

	/// Removes the handler for a payload type.
	/// A global receiver is registered to all connections, in the present and future.
	///
	/// The `id` is guaranteed not to have an associated handler after this call.
	///
	/// @param id the payload id
	/// @return the previous handler, or `null` if no handler was bound to the channel,
	/// or it was not registered using [#registerGlobalReceiver(CustomPacketPayload.Type, PlayPayloadHandler)]
	/// @see ServerPlayNetworking#registerGlobalReceiver(CustomPacketPayload.Type, PlayPayloadHandler)
	/// @see ServerPlayNetworking#unregisterReceiver(ServerGamePacketListenerImpl, Identifier)
	public static ServerPlayNetworking.@Nullable PlayPayloadHandler<?> unregisterGlobalReceiver(Identifier id) {
		return ServerNetworkingImpl.PLAY.unregisterGlobalReceiver(id);
	}

	/// Gets all channel names which global receivers are registered for.
	/// A global receiver is registered to all connections, in the present and future.
	///
	/// @return all channel names which global receivers are registered for.
	public static Set<Identifier> getGlobalReceivers() {
		return ServerNetworkingImpl.PLAY.getChannels();
	}

	/// Registers a handler for a payload type.
	/// This method differs from [ServerPlayNetworking#registerGlobalReceiver(CustomPacketPayload.Type, PlayPayloadHandler)] since
	/// the channel handler will only be applied to the player represented by the [ServerGamePacketListenerImpl].
	///
	/// For example, if you only register a receiver using this method when a [ServerLoginNetworking.LoginQueryResponseHandler)][ServerLoginNetworking#registerGlobalReceiver(Identifier,]
	/// login response has been received, you should use [ServerPlayConnectionEvents#INIT] to register the channel handler.
	///
	/// If a handler is already registered for the `type`, this method will return `false`, and no change will be made.
	/// Use [#unregisterReceiver(ServerGamePacketListenerImpl, Identifier)] to unregister the existing handler.
	///
	/// @param packetListener the packet listener
	/// @param type the packet type
	/// @param handler the handler
	/// @return `false` if a handler is already registered to the channel name
	/// @throws IllegalArgumentException if the codec for `type` has not been [registered][PayloadTypeRegistry#serverboundPlay()] yet
	/// @see ServerPlayConnectionEvents#INIT
	public static <T extends CustomPacketPayload> boolean registerReceiver(ServerGamePacketListenerImpl packetListener, CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) {
		return ServerNetworkingImpl.getAddon(packetListener).registerChannel(type.id(), handler);
	}

	/// Removes the handler for a packet type.
	///
	/// The `id` is guaranteed not to have an associated handler after this call.
	///
	/// @param id the id of the payload
	/// @return the previous handler, or `null` if no handler was bound to the channel,
	/// or it was not registered using [#registerReceiver(ServerGamePacketListenerImpl, CustomPacketPayload.Type, PlayPayloadHandler)]
	public static ServerPlayNetworking.@Nullable PlayPayloadHandler<?> unregisterReceiver(ServerGamePacketListenerImpl packetListener, Identifier id) {
		return ServerNetworkingImpl.getAddon(packetListener).unregisterChannel(id);
	}

	/// Gets all the channel names that the server can receive packets on.
	///
	/// @param player the player
	/// @return All the channel names that the server can receive packets on
	public static Set<Identifier> getReceived(ServerPlayer player) {
		Objects.requireNonNull(player, "Server player cannot be null");

		return getReceived(player.connection);
	}

	/// Gets all the channel names that the server can receive packets on.
	///
	/// @param listener the packet listener
	/// @return All the channel names that the server can receive packets on
	public static Set<Identifier> getReceived(ServerGamePacketListenerImpl listener) {
		Objects.requireNonNull(listener, "Server game packet listener cannot be null");

		return ServerNetworkingImpl.getAddon(listener).getReceivableChannels();
	}

	/// Gets all channel names that the connected client declared the ability to receive a packets on.
	///
	/// @param player the player
	/// @return All the channel names the connected client declared the ability to receive a packets on
	public static Set<Identifier> getSendable(ServerPlayer player) {
		Objects.requireNonNull(player, "Server player cannot be null");

		return getSendable(player.connection);
	}

	/// Gets all channel names that a connected client declared the ability to receive a packets on.
	///
	/// @param listener the packet listener
	/// @return `true` if the connected client has declared the ability to receive a packet on the specified channel
	public static Set<Identifier> getSendable(ServerGamePacketListenerImpl listener) {
		Objects.requireNonNull(listener, "Server game packet listener cannot be null");

		return ServerNetworkingImpl.getAddon(listener).getSendableChannels();
	}

	/// Checks if the connected client declared the ability to receive a packet on a specified channel name.
	///
	/// @param player the player
	/// @param channelName the channel name
	/// @return `true` if the connected client has declared the ability to receive a packet on the specified channel
	public static boolean canSend(ServerPlayer player, Identifier channelName) {
		Objects.requireNonNull(player, "Server player cannot be null");

		return canSend(player.connection, channelName);
	}

	/// Checks if the connected client declared the ability to receive a specific type of packet.
	///
	/// @param player the player
	/// @param type the packet type
	/// @return `true` if the connected client has declared the ability to receive a specific type of packet
	public static boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
		Objects.requireNonNull(player, "Server player cannot be null");

		return canSend(player.connection, type.id());
	}

	/// Checks if the connected client declared the ability to receive a packet on a specified channel name.
	///
	/// @param listener the packet listener
	/// @param channelName the channel name
	/// @return `true` if the connected client has declared the ability to receive a packet on the specified channel
	public static boolean canSend(ServerGamePacketListenerImpl listener, Identifier channelName) {
		Objects.requireNonNull(listener, "Server game packet listener cannot be null");
		Objects.requireNonNull(channelName, "Channel name cannot be null");

		return ServerNetworkingImpl.getAddon(listener).getSendableChannels().contains(channelName);
	}

	/// Checks if the connected client declared the ability to receive a specific type of packet.
	///
	/// @param listener the packet listener
	/// @param type the packet type
	/// @return `true` if the connected client has declared the ability to receive a specific type of packet
	public static boolean canSend(ServerGamePacketListenerImpl listener, CustomPacketPayload.Type<?> type) {
		Objects.requireNonNull(listener, "Server game packet listener cannot be null");
		Objects.requireNonNull(type, "Packet type cannot be null");

		return ServerNetworkingImpl.getAddon(listener).getSendableChannels().contains(type.id());
	}

	/// Creates a packet which may be sent to a connected client.
	///
	/// @param packet the packet
	/// @return a new packet
	public static <T extends CustomPacketPayload> Packet<ClientCommonPacketListener> createClientboundPacket(T packet) {
		return ServerNetworkingImpl.createClientboundPacket(packet);
	}

	/// Gets the packet sender which sends packets to the connected client.
	///
	/// @param player the player
	/// @return the packet sender
	public static PacketSender getSender(ServerPlayer player) {
		Objects.requireNonNull(player, "Server player cannot be null");

		return getSender(player.connection);
	}

	/// Gets the packet sender which sends packets to the connected client.
	///
	/// @param listener the packet listener, representing the connection to the player/client
	/// @return the packet sender
	public static PacketSender getSender(ServerGamePacketListenerImpl listener) {
		Objects.requireNonNull(listener, "Server game packet listener cannot be null");

		return ServerNetworkingImpl.getAddon(listener);
	}

	/// Sends a packet to a player.
	///
	/// Any packets sent must be [registered][PayloadTypeRegistry#clientboundPlay()].
	///
	/// @param player the player to send the packet to
	/// @param payload the payload to send
	public static void send(ServerPlayer player, CustomPacketPayload payload) {
		Objects.requireNonNull(player, "Server player cannot be null");
		Objects.requireNonNull(payload, "Payload cannot be null");
		Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + payload.getClass());

		player.connection.send(createClientboundPacket(payload));
	}

	/// Put the player back into configuration phase and re-run all of the configuration tasks.
	///
	/// @param player the player
	public static void reconfigure(ServerPlayer player) {
		Objects.requireNonNull(player, "Server player cannot be null");

		reconfigure(player.connection);
	}

	/// Put the player back into configuration phase and re-run all of the configuration tasks.
	///
	/// @param listener the packet listener
	public static void reconfigure(ServerGamePacketListenerImpl listener) {
		Objects.requireNonNull(listener, "Server game packet listener cannot be null");

		ServerNetworkingImpl.getAddon(listener).reconfigure();
	}

	private ServerPlayNetworking() {
	}

	/// A thread-safe packet handler utilizing [CustomPacketPayload].
	/// @param <T> the type of the packet
	@FunctionalInterface
	public interface PlayPayloadHandler<T extends CustomPacketPayload> {
		/// Handles the incoming packet. This is called on the server thread, and can safely
		/// manipulate the world.
		///
		/// An example usage of this is to create an explosion where the player is looking:
		///
		/// ```java
		/// // use PayloadTypeRegistry for registering the payload
		/// ServerPlayNetworking.registerReceiver(BoomPayload.TYPE, (payload, context) -> {
		/// 	ModPacketHandler.createExplosion(context.player(), payload.fire());
		/// });
		/// ```
		///
		/// The packet listener can be accessed via [ServerPlayer#connection].
		///
		/// @param payload the packet payload
		/// @param context the play networking context
		/// @see CustomPacketPayload
		void receive(T payload, Context context);
	}

	@ApiStatus.NonExtendable
	public interface Context {
		/// @return The MinecraftServer instance
		MinecraftServer server();

		/// @return The player that received the packet
		ServerPlayer player();

		/// @return The packet sender
		PacketSender responseSender();
	}
}
