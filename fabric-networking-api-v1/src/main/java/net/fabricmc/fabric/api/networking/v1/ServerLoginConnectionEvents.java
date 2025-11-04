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

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Offers access to events related to the connection to a client on a logical server while a client is logging in.
 */
public final class ServerLoginConnectionEvents {
	/**
	 * Event indicating a connection entered the LOGIN state, ready for registering query response handlers.
	 *
	 * @see ServerLoginNetworking#registerReceiver(ServerLoginPacketListenerImpl, Identifier, ServerLoginNetworking.LoginQueryResponseHandler)
	 */
	public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (handler, server) -> {
		for (Init callback : callbacks) {
			callback.onLoginInit(handler, server);
		}
	});

	/**
	 * An event for the start of login queries of the server login network handler.
	 * This event may be used to register {@link ServerLoginNetworking.LoginQueryResponseHandler login query response handlers}
	 * using {@link ServerLoginNetworking#registerReceiver(ServerLoginPacketListenerImpl, Identifier, ServerLoginNetworking.LoginQueryResponseHandler)}
	 * since this event is fired just before the first login query response is processed.
	 *
	 * <p>You may send login queries to the connected client using the provided {@link LoginPacketSender}.
	 */
	public static final Event<QueryStart> QUERY_START = EventFactory.createArrayBacked(QueryStart.class, callbacks -> (handler, server, sender, synchronizer) -> {
		for (QueryStart callback : callbacks) {
			callback.onLoginStart(handler, server, sender, synchronizer);
		}
	});

	/**
	 * An event for the disconnection of the server login network handler.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (handler, server) -> {
		for (Disconnect callback : callbacks) {
			callback.onLoginDisconnect(handler, server);
		}
	});

	private ServerLoginConnectionEvents() {
	}

	/**
	 * @see ServerLoginConnectionEvents#INIT
	 */
	@FunctionalInterface
	public interface Init {
		void onLoginInit(ServerLoginPacketListenerImpl handler, MinecraftServer server);
	}

	/**
	 * @see ServerLoginConnectionEvents#QUERY_START
	 */
	@FunctionalInterface
	public interface QueryStart {
		void onLoginStart(ServerLoginPacketListenerImpl handler, MinecraftServer server, LoginPacketSender sender, ServerLoginNetworking.LoginSynchronizer synchronizer);
	}

	/**
	 * @see ServerLoginConnectionEvents#DISCONNECT
	 */
	@FunctionalInterface
	public interface Disconnect {
		void onLoginDisconnect(ServerLoginPacketListenerImpl handler, MinecraftServer server);
	}
}
