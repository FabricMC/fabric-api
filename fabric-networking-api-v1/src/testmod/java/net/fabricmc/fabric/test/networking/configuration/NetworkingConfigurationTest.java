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

import net.minecraft.dialog.type.Dialog;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.DebugConfigCommand;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.CustomClickActionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.test.networking.NetworkingTestmods;

/**
 * Also see NetworkingConfigurationClientTest.
 */
public class NetworkingConfigurationTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkingConfigurationTest.class);
	private static final RegistryKey<Dialog> CONFIGURATION_TEST_DIALOG = RegistryKey.of(RegistryKeys.DIALOG, NetworkingTestmods.id("configuration_custom_click_event"));

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.configurationS2C().register(ConfigurationPacket.ID, ConfigurationPacket.CODEC);
		PayloadTypeRegistry.configurationC2S().register(ConfigurationCompletePacket.ID, ConfigurationCompletePacket.CODEC);
		PayloadTypeRegistry.configurationC2S().register(ConfigurationStartPacket.ID, ConfigurationStartPacket.CODEC);
		PayloadTypeRegistry.configurationS2C().register(MockShowDialogPacket.ID, MockShowDialogPacket.CODEC);

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (ServerConfigurationNetworking.isReconfiguring(handler)) {
				LOGGER.info("Reconfiguring client");
			}

			// You must check to see if the client can handle your config task
			if (ServerConfigurationNetworking.canSend(handler, ConfigurationPacket.ID)) {
				handler.addTask(new TestConfigurationTask("Example data"));
			} else {
				// You can opt to disconnect the client if it cannot handle the configuration task
				handler.disconnect(Text.literal("Network test configuration not supported by client"));
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

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			// open a dialog screen on the client
			// note: normally you would actually use ShowDialogS2CPacket, a mock packet is used here for testing purposes
			// important: use a task to prevent this dialog from being quickly skipped over
			handler.addTask(new TestDialogConfigurationTask(CONFIGURATION_TEST_DIALOG));
		});

		CustomClickActionEvents.configurationClickActionEvent(NetworkingTestmods.id("configuration_event")).register(
				context -> {
					NbtElement payload = context.payload();
					String payloadString = payload != null ? payload.toString() : "null";
					NetworkingTestmods.LOGGER.info("Received configuration event with payload: {}", payloadString);

					// important: make sure to complete the task to continue to the game
					context.handler().completeTask(TestDialogConfigurationTask.KEY);
				}
		);
	}

	public record TestConfigurationTask(String data) implements ServerPlayerConfigurationTask {
		public static final Key KEY = new Key(Identifier.of(NetworkingTestmods.ID, "configure").toString());

		@Override
		public void sendPacket(Consumer<Packet<?>> sender) {
			var packet = new ConfigurationPacket(data);
			sender.accept(ServerConfigurationNetworking.createS2CPacket(packet));
		}

		@Override
		public Key getKey() {
			return KEY;
		}
	}

	public record TestDialogConfigurationTask(RegistryKey<Dialog> dialog) implements ServerPlayerConfigurationTask {
		public static final Key KEY = new Key(Identifier.of(NetworkingTestmods.ID, "configure_dialog").toString());

		@Override
		public void sendPacket(Consumer<Packet<?>> sender) {
			var packet = new MockShowDialogPacket(dialog);
			sender.accept(ServerConfigurationNetworking.createS2CPacket(packet));
		}

		@Override
		public Key getKey() {
			return KEY;
		}
	}

	public record ConfigurationPacket(String data) implements CustomPayload {
		public static final CustomPayload.Id<ConfigurationPacket> ID = new Id<>(Identifier.of(NetworkingTestmods.ID, "configure"));
		public static final PacketCodec<PacketByteBuf, ConfigurationPacket> CODEC = CustomPayload.codecOf(ConfigurationPacket::write, ConfigurationPacket::new);

		public ConfigurationPacket(PacketByteBuf buf) {
			this(buf.readString());
		}

		public void write(PacketByteBuf buf) {
			buf.writeString(data);
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public record MockShowDialogPacket(RegistryKey<Dialog> dialog) implements CustomPayload {
		public static final CustomPayload.Id<MockShowDialogPacket> ID = new Id<>(Identifier.of(NetworkingTestmods.ID, "mock_show_dialog"));
		public static final PacketCodec<PacketByteBuf, MockShowDialogPacket> CODEC = CustomPayload.codecOf(MockShowDialogPacket::write, MockShowDialogPacket::new);

		public MockShowDialogPacket(PacketByteBuf buf) {
			this(buf.readRegistryKey(RegistryKeys.DIALOG));
		}

		public void write(PacketByteBuf buf) {
			buf.writeRegistryKey(dialog);
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public static class ConfigurationCompletePacket implements CustomPayload {
		public static final ConfigurationCompletePacket INSTANCE = new ConfigurationCompletePacket();
		public static final CustomPayload.Id<ConfigurationCompletePacket> ID = new Id<>(Identifier.of(NetworkingTestmods.ID, "configure_complete"));
		public static final PacketCodec<PacketByteBuf, ConfigurationCompletePacket> CODEC = PacketCodec.unit(INSTANCE);

		private ConfigurationCompletePacket() {
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public static class ConfigurationStartPacket implements CustomPayload {
		public static final ConfigurationStartPacket INSTANCE = new ConfigurationStartPacket();
		public static final CustomPayload.Id<ConfigurationStartPacket> ID = new Id<>(Identifier.of(NetworkingTestmods.ID, "configure_start"));
		public static final PacketCodec<PacketByteBuf, ConfigurationStartPacket> CODEC = PacketCodec.unit(INSTANCE);

		private ConfigurationStartPacket() {
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}
