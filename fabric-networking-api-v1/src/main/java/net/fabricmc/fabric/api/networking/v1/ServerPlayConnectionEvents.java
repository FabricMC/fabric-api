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

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Offers access to events related to the connection to a client on a logical server while a client is in game.
 */
public final class ServerPlayConnectionEvents {
	/**
	 * Event indicating a connection entered the PLAY state, ready for registering channel handlers.
	 *
	 * @see ServerPlayNetworking#registerReceiver(ServerGamePacketListenerImpl, CustomPacketPayload.Type, ServerPlayNetworking.PlayPayloadHandler)
	 */
	public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (handler, server) -> {
		for (Init callback : callbacks) {
			callback.onPlayInit(handler, server);
		}
	});

	/**
	 * An event for notification when the server play network handler is ready to send packets to the client.
	 *
	 * <p>At this stage, the network handler is ready to send packets to the client.
	 */
	public static final Event<Join> JOIN = EventFactory.createArrayBacked(Join.class, callbacks -> (handler, sender, server) -> {
		for (Join callback : callbacks) {
			callback.onPlayReady(handler, sender, server);
		}
	});

	/**
	 * An event for the disconnection of the server play network handler.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (handler, server) -> {
		for (Disconnect callback : callbacks) {
			callback.onPlayDisconnect(handler, server);
		}
	});

	private ServerPlayConnectionEvents() {
	}

	@FunctionalInterface
	public interface Init {
		void onPlayInit(ServerGamePacketListenerImpl handler, MinecraftServer server);
	}

	@FunctionalInterface
	public interface Join {
		void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server);
	}

	@FunctionalInterface
	public interface Disconnect {
		void onPlayDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server);
	}
}
