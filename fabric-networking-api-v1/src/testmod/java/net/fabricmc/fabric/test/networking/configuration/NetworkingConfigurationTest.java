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

package net.fabricmc.fabric.test.networking.configuration;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.DebugConfigCommand;
import net.minecraft.server.network.ConfigurationTask;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.test.networking.NetworkingTestmods;

/**
 * Also see NetworkingConfigurationClientTest.
 */
public class NetworkingConfigurationTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkingConfigurationTest.class);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.configurationS2C().register(ConfigurationPacket.ID, ConfigurationPacket.CODEC);
		PayloadTypeRegistry.configurationC2S().register(ConfigurationCompletePacket.ID, ConfigurationCompletePacket.CODEC);
		PayloadTypeRegistry.configurationC2S().register(ConfigurationStartPacket.ID, ConfigurationStartPacket.CODEC);

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (ServerConfigurationNetworking.isReconfiguring(handler)) {
				LOGGER.info("Reconfiguring client");
			}

			// You must check to see if the client can handle your config task
			if (ServerConfigurationNetworking.canSend(handler, ConfigurationPacket.ID)) {
				handler.addTask(new TestConfigurationTask("Example data"));
			} else {
				// You can opt to disconnect the client if it cannot handle the configuration task
				handler.disconnect(Component.literal("Network test configuration not supported by client"));
			}
		});

		ServerConfigurationNetworking.registerGlobalReceiver(ConfigurationCompletePacket.ID, (packet, context) -> {
			context.networkHandler().completeTask(TestConfigurationTask.KEY);
		});

		ServerConfigurationNetworking.registerGlobalReceiver(ConfigurationStartPacket.ID, (packet, context) -> {
			LOGGER.info("Received configuration start packet from client");
		});

		// Enable the vanilla debugconfig command
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> DebugConfigCommand.register(dispatcher, registryAccess));
	}

	public record TestConfigurationTask(String data) implements ConfigurationTask {
		public static final Type KEY = new Type(Identifier.fromNamespaceAndPath(NetworkingTestmods.ID, "configure").toString());

		@Override
		public void start(Consumer<Packet<?>> sender) {
			var packet = new ConfigurationPacket(data);
			sender.accept(ServerConfigurationNetworking.createS2CPacket(packet));
		}

		@Override
		public Type type() {
			return KEY;
		}
	}

	public record ConfigurationPacket(String data) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ConfigurationPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(NetworkingTestmods.ID, "configure"));
		public static final StreamCodec<FriendlyByteBuf, ConfigurationPacket> CODEC = CustomPacketPayload.codec(ConfigurationPacket::write, ConfigurationPacket::new);

		public ConfigurationPacket(FriendlyByteBuf buf) {
			this(buf.readUtf());
		}

		public void write(FriendlyByteBuf buf) {
			buf.writeUtf(data);
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	public static class ConfigurationCompletePacket implements CustomPacketPayload {
		public static final ConfigurationCompletePacket INSTANCE = new ConfigurationCompletePacket();
		public static final CustomPacketPayload.Type<ConfigurationCompletePacket> ID = new Type<>(Identifier.fromNamespaceAndPath(NetworkingTestmods.ID, "configure_complete"));
		public static final StreamCodec<FriendlyByteBuf, ConfigurationCompletePacket> CODEC = StreamCodec.unit(INSTANCE);

		private ConfigurationCompletePacket() {
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	public static class ConfigurationStartPacket implements CustomPacketPayload {
		public static final ConfigurationStartPacket INSTANCE = new ConfigurationStartPacket();
		public static final CustomPacketPayload.Type<ConfigurationStartPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(NetworkingTestmods.ID, "configure_start"));
		public static final StreamCodec<FriendlyByteBuf, ConfigurationStartPacket> CODEC = StreamCodec.unit(INSTANCE);

		private ConfigurationStartPacket() {
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}
}
