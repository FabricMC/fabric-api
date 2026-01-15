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
import java.util.concurrent.Future;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.mixin.networking.accessor.ServerLoginPacketListenerImplAccessor;

/**
 * Offers access to login stage server-side networking functionalities.
 *
 * <p>Server-side networking functionalities include receiving serverbound query responses and sending clientbound query requests.
 *
 * @see ServerPlayNetworking
 * @see ServerConfigurationNetworking
 */
public final class ServerLoginNetworking {
	/**
	 * Registers a handler to a query response channel.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterGlobalReceiver(Identifier)} to unregister the existing handler.
	 *
	 * @param channelName the id of the channel
	 * @param channelHandler the handler
	 * @return false if a handler is already registered to the channel
	 * @see ServerLoginNetworking#unregisterGlobalReceiver(Identifier)
	 * @see ServerLoginNetworking#registerReceiver(ServerLoginPacketListenerImpl, Identifier, LoginQueryResponseHandler)
	 */
	public static boolean registerGlobalReceiver(Identifier channelName, LoginQueryResponseHandler channelHandler) {
		return ServerNetworkingImpl.LOGIN.registerGlobalReceiver(channelName, channelHandler);
	}

	/**
	 * Removes the handler of a query response channel.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>The {@code channel} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the id of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel
	 * @see ServerLoginNetworking#registerGlobalReceiver(Identifier, LoginQueryResponseHandler)
	 * @see ServerLoginNetworking#unregisterReceiver(ServerLoginPacketListenerImpl, Identifier)
	 */
	public static ServerLoginNetworking.@Nullable LoginQueryResponseHandler unregisterGlobalReceiver(Identifier channelName) {
		return ServerNetworkingImpl.LOGIN.unregisterGlobalReceiver(channelName);
	}

	/**
	 * Gets all channel names which global receivers are registered for.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * @return all channel names which global receivers are registered for.
	 */
	public static Set<Identifier> getGlobalReceivers() {
		return ServerNetworkingImpl.LOGIN.getChannels();
	}

	/**
	 * Registers a handler to a query response channel.
	 *
	 * <p>If a handler is already registered to the {@code channelName}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerLoginPacketListenerImpl, Identifier)} to unregister the existing handler.
	 *
	 * @param packetListener the listener
	 * @param channelName the id of the channel
	 * @param responseHandler the handler
	 * @return false if a handler is already registered to the channel name
	 */
	public static boolean registerReceiver(ServerLoginPacketListenerImpl packetListener, Identifier channelName, LoginQueryResponseHandler responseHandler) {
		Objects.requireNonNull(packetListener, "Packet listener cannot be null");

		return ServerNetworkingImpl.getAddon(packetListener).registerChannel(channelName, responseHandler);
	}

	/**
	 * Removes the handler of a query response channel.
	 *
	 * <p>The {@code channelName} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the id of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel name
	 */
	public static ServerLoginNetworking.@Nullable LoginQueryResponseHandler unregisterReceiver(ServerLoginPacketListenerImpl packetListener, Identifier channelName) {
		Objects.requireNonNull(packetListener, "Packet listener cannot be null");

		return ServerNetworkingImpl.getAddon(packetListener).unregisterChannel(channelName);
	}

	// Helper methods

	/**
	 * Returns the <i>Minecraft</i> Server of a server login packet listener.
	 *
	 * @param listener the server login packet listener
	 */
	public static MinecraftServer getServer(ServerLoginPacketListenerImpl listener) {
		Objects.requireNonNull(listener, "Packet listener cannot be null");

		return ((ServerLoginPacketListenerImplAccessor) listener).getServer();
	}

	/**
	 * Gets the packet sender which sends packets to the connected client.
	 *
	 * @param listener the packet listener, representing the connection to the client
	 * @return the packet sender
	 */
	public static LoginPacketSender getSender(ServerLoginPacketListenerImpl listener) {
		Objects.requireNonNull(listener, "Packet listener cannot be null");
		return ServerNetworkingImpl.getAddon(listener);
	}

	private ServerLoginNetworking() {
	}

	@FunctionalInterface
	public interface LoginQueryResponseHandler {
		/**
		 * Handles an incoming query response from a client.
		 *
		 * <p>This method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain net.minecraft.util.thread.BlockableEventLoop#submit(Runnable) scheduled} using the provided Minecraft instance.
		 *
		 * <p><b>Whether the client understood the query should be checked before reading from the payload of the packet.</b>
		 * @param server the server
		 * @param listener the packet listener that received this packet, representing the player/client who sent the response
		 * @param understood whether the client understood the packet
		 * @param buf the payload of the packet
		 * @param synchronizer the synchronizer which may be used to delay log-in till a {@link Future} is completed.
		 * @param responseSender the packet sender
		 */
		void receive(MinecraftServer server, ServerLoginPacketListenerImpl listener, boolean understood, FriendlyByteBuf buf, LoginSynchronizer synchronizer, PacketSender responseSender);
	}

	/**
	 * Allows blocking client log-in until all futures passed into {@link LoginSynchronizer#waitFor(Future)} are completed.
	 */
	@FunctionalInterface
	@ApiStatus.NonExtendable
	public interface LoginSynchronizer {
		/**
		 * Allows blocking client log-in until the {@code future} is {@link Future#isDone() done}.
		 *
		 * <p>Since packet reception happens on netty's event loops, this allows handlers to
		 * perform logic on the Server Thread, etc. For instance, a handler can prepare an
		 * upcoming query request or check necessary login data on the server thread.</p>
		 *
		 * <p>Here is an example where the player log-in is blocked so that a credential check and
		 * building of a followup query request can be performed properly on the logical server
		 * thread before the player successfully logs in:
		 * <pre>{@code
		 * ServerLoginNetworking.registerGlobalReceiver(CHECK_CHANNEL, (server, listener, understood, buf, synchronizer, responseSender) -&gt; {
		 * 	if (!understood) {
		 * 		listener.disconnect(Component.literal("Only accept clients that can check!"));
		 * 		return;
		 * 	}
		 *
		 * 	String checkMessage = buf.readString(32767);
		 *
		 * 	// Just send the CompletableFuture returned by the server's submit method
		 * 	synchronizer.waitFor(server.submit(() -&gt; {
		 * 		LoginInfoChecker checker = LoginInfoChecker.get(server);
		 *
		 * 		if (!checker.check(listener.getUserName(), checkMessage)) {
		 * 			listener.disconnect(Component.literal("Invalid credentials!"));
		 * 			return;
		 * 		}
		 *
		 * 		responseSender.send(UPCOMING_CHECK, checker.buildSecondQueryPacket(listener, checkMessage));
		 * 	}));
		 * });
		 * }</pre>
		 *
		 * <p>Usually it is enough to pass the return value for {@link net.minecraft.util.thread.BlockableEventLoop#submit(Runnable)} for {@code future}.
		 *
		 * @param future the future that must be done before the player can log in
		 */
		void waitFor(Future<?> future);
	}
}
