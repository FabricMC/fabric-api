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
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

/**
 * Offers access to events related to the connection to a server on a logical client.
 */
public final class ClientPlayConnectionEvents {
	/**
	 * Event indicating a connection entered the PLAY state, ready for registering channel handlers.
	 *
	 * @see ClientPlayNetworking#registerReceiver(CustomPacketPayload.Type, ClientPlayNetworking.PlayPayloadHandler)
	 */
	public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (handler, client) -> {
		for (Init callback : callbacks) {
			callback.onPlayInit(handler, client);
		}
	});

	/**
	 * An event for notification when the client play network handler is ready to send packets to the server.
	 *
	 * <p>At this stage, the network handler is ready to send packets to the server.
	 * Since the client's local state has been set up.
	 */
	public static final Event<Join> JOIN = EventFactory.createArrayBacked(Join.class, callbacks -> (handler, sender, client) -> {
		for (Join callback : callbacks) {
			callback.onPlayReady(handler, sender, client);
		}
	});

	/**
	 * An event for the disconnection of the client play network handler.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (handler, client) -> {
		for (Disconnect callback : callbacks) {
			callback.onPlayDisconnect(handler, client);
		}
	});

	private ClientPlayConnectionEvents() {
	}

	@FunctionalInterface
	public interface Init {
		void onPlayInit(ClientPacketListener handler, Minecraft client);
	}

	@FunctionalInterface
	public interface Join {
		void onPlayReady(ClientPacketListener handler, PacketSender sender, Minecraft client);
	}

	@FunctionalInterface
	public interface Disconnect {
		void onPlayDisconnect(ClientPacketListener handler, Minecraft client);
	}
}
