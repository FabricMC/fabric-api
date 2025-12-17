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

package net.fabricmc.fabric.impl.networking;

import java.util.Arrays;
import java.util.function.Consumer;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ConfigurationTask;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;

public class CommonPacketsImpl {
	public static final int PACKET_VERSION_1 = 1;
	public static final int[] SUPPORTED_COMMON_PACKET_VERSIONS = new int[]{ PACKET_VERSION_1 };

	public static void init() {
		PayloadTypeRegistry.serverboundConfiguration().register(CommonVersionPayload.TYPE, CommonVersionPayload.CODEC);
		PayloadTypeRegistry.clientboundConfiguration().register(CommonVersionPayload.TYPE, CommonVersionPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(CommonVersionPayload.TYPE, CommonVersionPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(CommonVersionPayload.TYPE, CommonVersionPayload.CODEC);
		PayloadTypeRegistry.serverboundConfiguration().register(CommonRegisterPayload.TYPE, CommonRegisterPayload.CODEC);
		PayloadTypeRegistry.clientboundConfiguration().register(CommonRegisterPayload.TYPE, CommonRegisterPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(CommonRegisterPayload.TYPE, CommonRegisterPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(CommonRegisterPayload.TYPE, CommonRegisterPayload.CODEC);

		ServerConfigurationNetworking.registerGlobalReceiver(CommonVersionPayload.TYPE, (payload, context) -> {
			ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(context.packetListener());
			addon.onCommonVersionPacket(getNegotiatedVersion(payload));
			context.packetListener().completeTask(CommonVersionConfigurationTask.KEY);
		});

		ServerConfigurationNetworking.registerGlobalReceiver(CommonRegisterPayload.TYPE, (payload, context) -> {
			ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(context.packetListener());

			if (CommonRegisterPayload.PLAY_PROTOCOL.equals(payload.protocol())) {
				if (payload.version() != addon.getNegotiatedVersion()) {
					throw new IllegalStateException("Negotiated common packet version: %d but received packet with version: %d".formatted(addon.getNegotiatedVersion(), payload.version()));
				}

				// Play phase hasnt started yet, add them to the pending names.
				addon.getChannelInfoHolder().fabric_getPendingChannelsNames(ConnectionProtocol.PLAY).addAll(payload.channels());
				NetworkingImpl.LOGGER.debug("Received accepted channels from the client for play phase");
			} else {
				addon.onCommonRegisterPacket(payload);
			}

			context.packetListener().completeTask(CommonRegisterConfigurationTask.KEY);
		});

		// Create a configuration task to send and receive the common packets
		ServerConfigurationConnectionEvents.CONFIGURE.register((listener, server) -> {
			final ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(listener);

			if (ServerConfigurationNetworking.canSend(listener, CommonVersionPayload.TYPE)) {
				// Tasks are processed in order.
				listener.addTask(new CommonVersionConfigurationTask(addon));

				if (ServerConfigurationNetworking.canSend(listener, CommonRegisterPayload.TYPE)) {
					listener.addTask(new CommonRegisterConfigurationTask(addon));
				}
			}
		});
	}

	// A configuration phase task to send and receive the version packets.
	private record CommonVersionConfigurationTask(ServerConfigurationNetworkAddon addon) implements ConfigurationTask {
		public static final Type KEY = new Type(CommonVersionPayload.TYPE.id().toString());

		@Override
		public void start(Consumer<Packet<?>> sender) {
			addon.sendPacket(new CommonVersionPayload(SUPPORTED_COMMON_PACKET_VERSIONS));
		}

		@Override
		public Type type() {
			return KEY;
		}
	}

	// A configuration phase task to send and receive the registration packets.
	private record CommonRegisterConfigurationTask(ServerConfigurationNetworkAddon addon) implements ConfigurationTask {
		public static final Type KEY = new Type(CommonRegisterPayload.TYPE.id().toString());

		@Override
		public void start(Consumer<Packet<?>> sender) {
			addon.sendPacket(new CommonRegisterPayload(addon.getNegotiatedVersion(), CommonRegisterPayload.PLAY_PROTOCOL, ServerPlayNetworking.getGlobalReceivers()));
		}

		@Override
		public Type type() {
			return KEY;
		}
	}

	private static int getNegotiatedVersion(CommonVersionPayload payload) {
		int version = getHighestCommonVersion(payload.versions(), SUPPORTED_COMMON_PACKET_VERSIONS);

		if (version <= 0) {
			throw new UnsupportedOperationException("server does not support any requested versions from client");
		}

		return version;
	}

	public static int getHighestCommonVersion(int[] a, int[] b) {
		int[] as = a.clone();
		int[] bs = b.clone();

		Arrays.sort(as);
		Arrays.sort(bs);

		int ap = as.length - 1;
		int bp = bs.length - 1;

		while (ap >= 0 && bp >= 0) {
			if (as[ap] == bs[bp]) {
				return as[ap];
			}

			if (as[ap] > bs[bp]) {
				ap--;
			} else {
				bp--;
			}
		}

		return -1;
	}
}
