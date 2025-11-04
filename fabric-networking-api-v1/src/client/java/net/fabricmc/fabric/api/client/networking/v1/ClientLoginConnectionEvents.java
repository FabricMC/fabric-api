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

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Offers access to events related to the connection to a server on the client while the server is processing the client's login request.
 */
public final class ClientLoginConnectionEvents {
	/**
	 * Event indicating a connection entered the LOGIN state, ready for registering query request handlers.
	 * This event may be used by mods to prepare their client side state.
	 * This event does not guarantee that a login attempt will be successful.
	 *
	 * @see ClientLoginNetworking#registerReceiver(Identifier, ClientLoginNetworking.LoginQueryRequestHandler)
	 */
	public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (handler, client) -> {
		for (Init callback : callbacks) {
			callback.onLoginStart(handler, client);
		}
	});

	/**
	 * An event for when the client has started receiving login queries.
	 * A client can only start receiving login queries when a server has sent the first login query.
	 * Vanilla servers will typically never make the client enter this login phase, but it is not a guarantee that the
	 * connected server is a vanilla server since a modded server or proxy may have no login queries to send to the client
	 * and therefore bypass the login query phase.
	 * If this event is fired then it is a sign that a server is not a vanilla server or the server is behind a proxy which
	 * is capable of handling login queries.
	 *
	 * <p>This event may be used to {@link ClientLoginNetworking.LoginQueryRequestHandler register login query handlers}
	 * which may be used to send a response to a server.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<QueryStart> QUERY_START = EventFactory.createArrayBacked(QueryStart.class, callbacks -> (handler, client) -> {
		for (QueryStart callback : callbacks) {
			callback.onLoginQueryStart(handler, client);
		}
	});

	/**
	 * An event for when the client's login process has ended due to disconnection.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (handler, client) -> {
		for (Disconnect callback : callbacks) {
			callback.onLoginDisconnect(handler, client);
		}
	});

	private ClientLoginConnectionEvents() {
	}

	/**
	 * @see ClientLoginConnectionEvents#INIT
	 */
	@FunctionalInterface
	public interface Init {
		void onLoginStart(ClientHandshakePacketListenerImpl handler, Minecraft client);
	}

	/**
	 * @see ClientLoginConnectionEvents#QUERY_START
	 */
	@FunctionalInterface
	public interface QueryStart {
		void onLoginQueryStart(ClientHandshakePacketListenerImpl handler, Minecraft client);
	}

	/**
	 * @see ClientLoginConnectionEvents#DISCONNECT
	 */
	@FunctionalInterface
	public interface Disconnect {
		void onLoginDisconnect(ClientHandshakePacketListenerImpl handler, Minecraft client);
	}
}
