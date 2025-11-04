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
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Offers access to events related to the configuration connection to a server on a logical client.
 */
public final class ClientConfigurationConnectionEvents {
	/**
	 * Event indicating a connection entering the CONFIGURATION state, ready for registering channel handlers.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 *
	 * @see ClientConfigurationNetworking#registerReceiver(CustomPacketPayload.Type, ClientConfigurationNetworking.ConfigurationPayloadHandler)
	 */
	public static final Event<ClientConfigurationConnectionEvents.Init> INIT = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Init.class, callbacks -> (handler, client) -> {
		for (ClientConfigurationConnectionEvents.Init callback : callbacks) {
			callback.onConfigurationInit(handler, client);
		}
	});

	/**
	 * An event called after the connection has been initialized and is ready to start sending and receiving configuration packets.
	 *
	 * <p>Packets may be sent during this event.
	 */
	public static final Event<ClientConfigurationConnectionEvents.Start> START = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Start.class, callbacks -> (handler, client) -> {
		for (ClientConfigurationConnectionEvents.Start callback : callbacks) {
			callback.onConfigurationStart(handler, client);
		}
	});

	/**
	 * An event called after the ReadyS2CPacket has been received, just before switching to the PLAY state.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<ClientConfigurationConnectionEvents.Complete> COMPLETE = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Complete.class, callbacks -> (handler, client) -> {
		for (ClientConfigurationConnectionEvents.Complete callback : callbacks) {
			callback.onConfigurationComplete(handler, client);
		}
	});

	/**
	 * An event for the disconnection of the client configuration network handler.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<ClientConfigurationConnectionEvents.Disconnect> DISCONNECT = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Disconnect.class, callbacks -> (handler, client) -> {
		for (ClientConfigurationConnectionEvents.Disconnect callback : callbacks) {
			callback.onConfigurationDisconnect(handler, client);
		}
	});

	private ClientConfigurationConnectionEvents() {
	}

	@FunctionalInterface
	public interface Init {
		void onConfigurationInit(ClientConfigurationPacketListenerImpl handler, Minecraft client);
	}

	@FunctionalInterface
	public interface Start {
		void onConfigurationStart(ClientConfigurationPacketListenerImpl handler, Minecraft client);
	}

	@FunctionalInterface
	public interface Complete {
		void onConfigurationComplete(ClientConfigurationPacketListenerImpl handler, Minecraft client);
	}

	@FunctionalInterface
	public interface Disconnect {
		void onConfigurationDisconnect(ClientConfigurationPacketListenerImpl handler, Minecraft client);
	}

	// Deprecated:

	/**
	 * @deprecated replaced by {@link #COMPLETE}
	 */
	@Deprecated
	public static final Event<ClientConfigurationConnectionEvents.Ready> READY = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Ready.class, callbacks -> (handler, client) -> {
		for (ClientConfigurationConnectionEvents.Ready callback : callbacks) {
			callback.onConfigurationReady(handler, client);
		}
	});

	/**
	 * @deprecated replaced by {@link ClientConfigurationConnectionEvents.Complete}
	 */
	@Deprecated
	@FunctionalInterface
	public interface Ready {
		void onConfigurationReady(ClientConfigurationPacketListenerImpl handler, Minecraft client);
	}
}
