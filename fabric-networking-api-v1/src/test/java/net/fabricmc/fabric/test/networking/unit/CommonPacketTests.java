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

package net.fabricmc.fabric.test.networking.unit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.CommonPacketHandler;
import net.fabricmc.fabric.impl.networking.CommonPacketsImpl;
import net.fabricmc.fabric.impl.networking.CommonRegisterPayload;
import net.fabricmc.fabric.impl.networking.CommonVersionPayload;
import net.fabricmc.fabric.impl.networking.client.ClientConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;

public class CommonPacketTests {
	private static final CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, CommonVersionPayload> VERSION_PAYLOAD_TYPE = new CustomPacketPayload.TypeAndCodec<>(CommonVersionPayload.ID, CommonVersionPayload.CODEC);
	private static final CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, CommonRegisterPayload> REGISTER_PAYLOAD_TYPE = new CustomPacketPayload.TypeAndCodec<>(CommonRegisterPayload.ID, CommonRegisterPayload.CODEC);

	private PacketSender packetSender;
	private ChannelInfoHolder channelInfoHolder;

	private ClientConfigurationPacketListenerImpl clientNetworkHandler;
	private ClientConfigurationNetworkAddon clientAddon;

	private ServerConfigurationPacketListenerImpl serverNetworkHandler;
	private ServerConfigurationNetworkAddon serverAddon;

	private ClientConfigurationNetworking.Context clientContext;
	private ServerConfigurationNetworking.Context serverContext;

	@BeforeAll
	static void beforeAll() {
		CommonPacketsImpl.init();
		ClientNetworkingImpl.clientInit();

		// Register the packet codec on both sides
		PayloadTypeRegistry.playS2C().register(TestPayload.ID, TestPayload.CODEC);

		// Listen for the payload on the client
		ClientPlayNetworking.registerGlobalReceiver(TestPayload.ID, (payload, context) -> {
			System.out.println(payload.data());
		});
	}

	private record TestPayload(String data) implements CustomPacketPayload {
		static final CustomPacketPayload.Type<TestPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("fabric", "global_client"));
		static final StreamCodec<RegistryFriendlyByteBuf, TestPayload> CODEC = CustomPacketPayload.codec(TestPayload::write, TestPayload::new);

		TestPayload(RegistryFriendlyByteBuf buf) {
			this(buf.readUtf());
		}

		private void write(RegistryFriendlyByteBuf buf) {
			buf.writeUtf(data);
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	@BeforeEach
	void setUp() {
		packetSender = mock(PacketSender.class);
		channelInfoHolder = new MockChannelInfoHolder();

		clientNetworkHandler = mock(ClientConfigurationPacketListenerImpl.class);
		clientAddon = mock(ClientConfigurationNetworkAddon.class);
		when(ClientNetworkingImpl.getAddon(clientNetworkHandler)).thenReturn(clientAddon);
		when(clientAddon.getChannelInfoHolder()).thenReturn(channelInfoHolder);

		serverNetworkHandler = mock(ServerConfigurationPacketListenerImpl.class);
		serverAddon = mock(ServerConfigurationNetworkAddon.class);
		when(ServerNetworkingImpl.getAddon(serverNetworkHandler)).thenReturn(serverAddon);
		when(serverAddon.getChannelInfoHolder()).thenReturn(channelInfoHolder);

		ClientNetworkingImpl.setClientConfigurationAddon(clientAddon);

		clientContext = new ClientConfigurationNetworking.Context() {
			@Override
			public Minecraft client() {
				return null;
			}

			@Override
			public ClientConfigurationPacketListenerImpl networkHandler() {
				return clientNetworkHandler;
			}

			@Override
			public PacketSender responseSender() {
				return packetSender;
			}
		};
		serverContext = new ServerConfigurationNetworking.Context() {
			@Override
			public MinecraftServer server() {
				return null;
			}

			@Override
			public ServerConfigurationPacketListenerImpl networkHandler() {
				return serverNetworkHandler;
			}

			@Override
			public PacketSender responseSender() {
				return packetSender;
			}
		};
	}

	// Test handling the version packet on the client
	@Test
	void handleVersionPacketClient() {
		ClientConfigurationNetworking.ConfigurationPayloadHandler<CommonVersionPayload> packetHandler = (ClientConfigurationNetworking.ConfigurationPayloadHandler<CommonVersionPayload>) ClientNetworkingImpl.CONFIGURATION.getHandler(CommonVersionPayload.ID.id());
		assertNotNull(packetHandler);

		// Receive a packet from the server
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarIntArray(new int[]{1, 2, 3});

		CommonVersionPayload payload = CommonVersionPayload.CODEC.decode(buf);
		packetHandler.receive(payload, clientContext);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());

		// Check the response we are sending back to the server
		FriendlyByteBuf response = readResponse(packetSender, VERSION_PAYLOAD_TYPE);
		assertArrayEquals(new int[]{1}, response.readVarIntArray());
		assertEquals(0, response.readableBytes());

		assertEquals(1, getNegotiatedVersion(clientAddon));
	}

	// Test handling the version packet on the client, when the server sends unsupported versions
	@Test
	void handleVersionPacketClientUnsupported() {
		ClientConfigurationNetworking.ConfigurationPayloadHandler<CommonVersionPayload> packetHandler = (ClientConfigurationNetworking.ConfigurationPayloadHandler<CommonVersionPayload>) ClientNetworkingImpl.CONFIGURATION.getHandler(CommonVersionPayload.ID.id());
		assertNotNull(packetHandler);

		// Receive a packet from the server
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarIntArray(new int[]{2, 3}); // We only support version 1

		assertThrows(UnsupportedOperationException.class, () -> {
			CommonVersionPayload payload = CommonVersionPayload.CODEC.decode(buf);
			packetHandler.receive(payload, clientContext);
		});

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
	}

	// Test handling the version packet on the server
	@Test
	void handleVersionPacketServer() {
		ServerConfigurationNetworking.ConfigurationPacketHandler<CommonVersionPayload> packetHandler = (ServerConfigurationNetworking.ConfigurationPacketHandler<CommonVersionPayload>) ServerNetworkingImpl.CONFIGURATION.getHandler(CommonVersionPayload.ID.id());
		assertNotNull(packetHandler);

		// Receive a packet from the client
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarIntArray(new int[]{1, 2, 3});

		CommonVersionPayload payload = CommonVersionPayload.CODEC.decode(buf);
		packetHandler.receive(payload, serverContext);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
		assertEquals(1, getNegotiatedVersion(serverAddon));
	}

	// Test handling the version packet on the server unsupported version
	@Test
	void handleVersionPacketServerUnsupported() {
		ServerConfigurationNetworking.ConfigurationPacketHandler<CommonVersionPayload> packetHandler = (ServerConfigurationNetworking.ConfigurationPacketHandler<CommonVersionPayload>) ServerNetworkingImpl.CONFIGURATION.getHandler(CommonVersionPayload.ID.id());
		assertNotNull(packetHandler);

		// Receive a packet from the client
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarIntArray(new int[]{3}); // Server only supports version 1

		assertThrows(UnsupportedOperationException.class, () -> {
			CommonVersionPayload payload = CommonVersionPayload.CODEC.decode(buf);
			packetHandler.receive(payload, serverContext);
		});

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
	}

	// Test handing the play registry packet on the client configuration handler
	@Test
	void handlePlayRegistryClient() {
		ClientConfigurationNetworking.ConfigurationPayloadHandler<CommonRegisterPayload> packetHandler = (ClientConfigurationNetworking.ConfigurationPayloadHandler<CommonRegisterPayload>) ClientNetworkingImpl.CONFIGURATION.getHandler(CommonRegisterPayload.ID.id());
		assertNotNull(packetHandler);

		when(clientAddon.getNegotiatedVersion()).thenReturn(1);

		// Receive a packet from the server
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(1); // Version
		buf.writeUtf("play"); // Target phase
		buf.writeCollection(List.of(Identifier.fromNamespaceAndPath("fabric", "test")), FriendlyByteBuf::writeIdentifier);

		CommonRegisterPayload payload = CommonRegisterPayload.CODEC.decode(buf);
		packetHandler.receive(payload, clientContext);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
		assertIterableEquals(List.of(Identifier.fromNamespaceAndPath("fabric", "test")), channelInfoHolder.fabric_getPendingChannelsNames(ConnectionProtocol.PLAY));

		// Check the response we are sending back to the server
		FriendlyByteBuf response = readResponse(packetSender, REGISTER_PAYLOAD_TYPE);
		assertEquals(1, response.readVarInt());
		assertEquals("play", response.readUtf());
		assertIterableEquals(List.of(Identifier.fromNamespaceAndPath("fabric", "global_client")), response.readCollection(HashSet::new, FriendlyByteBuf::readIdentifier));
		assertEquals(0, response.readableBytes());
	}

	// Test handling the configuration registry packet on the client configuration handler
	@Test
	void handleConfigurationRegistryClient() {
		ClientConfigurationNetworking.ConfigurationPayloadHandler<CommonRegisterPayload> packetHandler = (ClientConfigurationNetworking.ConfigurationPayloadHandler<CommonRegisterPayload>) ClientNetworkingImpl.CONFIGURATION.getHandler(CommonRegisterPayload.ID.id());
		assertNotNull(packetHandler);

		when(clientAddon.getNegotiatedVersion()).thenReturn(1);
		when(clientAddon.createRegisterPayload()).thenAnswer(i -> new CommonRegisterPayload(1, "configuration", Set.of(Identifier.fromNamespaceAndPath("fabric", "global_configuration_client"))));

		// Receive a packet from the server
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(1); // Version
		buf.writeUtf("configuration"); // Target phase
		buf.writeCollection(List.of(Identifier.fromNamespaceAndPath("fabric", "test")), FriendlyByteBuf::writeIdentifier);

		CommonRegisterPayload payload = CommonRegisterPayload.CODEC.decode(buf);
		packetHandler.receive(payload, clientContext);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
		verify(clientAddon, times(1)).onCommonRegisterPacket(any());

		// Check the response we are sending back to the server
		FriendlyByteBuf response = readResponse(packetSender, REGISTER_PAYLOAD_TYPE);
		assertEquals(1, response.readVarInt());
		assertEquals("configuration", response.readUtf());
		assertIterableEquals(List.of(Identifier.fromNamespaceAndPath("fabric", "global_configuration_client")), response.readCollection(HashSet::new, FriendlyByteBuf::readIdentifier));
		assertEquals(0, response.readableBytes());
	}

	// Test handing the play registry packet on the server configuration handler
	@Test
	void handlePlayRegistryServer() {
		ServerConfigurationNetworking.ConfigurationPacketHandler<CommonRegisterPayload> packetHandler = (ServerConfigurationNetworking.ConfigurationPacketHandler<CommonRegisterPayload>) ServerNetworkingImpl.CONFIGURATION.getHandler(CommonRegisterPayload.ID.id());
		assertNotNull(packetHandler);

		when(serverAddon.getNegotiatedVersion()).thenReturn(1);

		// Receive a packet from the client
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(1); // Version
		buf.writeUtf("play"); // Target phase
		buf.writeCollection(List.of(Identifier.fromNamespaceAndPath("fabric", "test")), FriendlyByteBuf::writeIdentifier);

		CommonRegisterPayload payload = CommonRegisterPayload.CODEC.decode(buf);
		packetHandler.receive(payload, serverContext);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
		assertIterableEquals(List.of(Identifier.fromNamespaceAndPath("fabric", "test")), channelInfoHolder.fabric_getPendingChannelsNames(ConnectionProtocol.PLAY));
	}

	// Test handing the configuration registry packet on the server configuration handler
	@Test
	void handleConfigurationRegistryServer() {
		ServerConfigurationNetworking.ConfigurationPacketHandler<CommonRegisterPayload> packetHandler = (ServerConfigurationNetworking.ConfigurationPacketHandler<CommonRegisterPayload>) ServerNetworkingImpl.CONFIGURATION.getHandler(CommonRegisterPayload.ID.id());
		assertNotNull(packetHandler);

		when(serverAddon.getNegotiatedVersion()).thenReturn(1);

		// Receive a packet from the client
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(1); // Version
		buf.writeUtf("configuration"); // Target phase
		buf.writeCollection(List.of(Identifier.fromNamespaceAndPath("fabric", "test")), FriendlyByteBuf::writeIdentifier);

		CommonRegisterPayload payload = CommonRegisterPayload.CODEC.decode(buf);
		packetHandler.receive(payload, serverContext);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
		verify(serverAddon, times(1)).onCommonRegisterPacket(any());
	}

	@Test
	public void testHighestCommonVersionWithCommonElement() {
		int[] a = {1, 2, 3};
		int[] b = {1, 2};
		assertEquals(2, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithoutCommonElement() {
		int[] a = {1, 3, 5};
		int[] b = {2, 4, 6};
		assertEquals(-1, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithOneEmptyArray() {
		int[] a = {1, 3, 5};
		int[] b = {};
		assertEquals(-1, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithBothEmptyArrays() {
		int[] a = {};
		int[] b = {};
		assertEquals(-1, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithIdenticalArrays() {
		int[] a = {1, 2, 3};
		int[] b = {1, 2, 3};
		assertEquals(3, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	private static <T extends CustomPacketPayload> FriendlyByteBuf readResponse(PacketSender packetSender, CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, T> type) {
		ArgumentCaptor<CustomPacketPayload> responseCaptor = ArgumentCaptor.forClass(CustomPacketPayload.class);
		verify(packetSender, times(1)).sendPacket(responseCaptor.capture());

		final T payload = (T) responseCaptor.getValue();
		final FriendlyByteBuf buf = PacketByteBufs.create();
		type.codec().encode(buf, payload);

		return buf;
	}

	private static int getNegotiatedVersion(CommonPacketHandler packetHandler) {
		ArgumentCaptor<Integer> responseCaptor = ArgumentCaptor.forClass(Integer.class);
		verify(packetHandler, times(1)).onCommonVersionPacket(responseCaptor.capture());
		return responseCaptor.getValue();
	}

	private static class MockChannelInfoHolder implements ChannelInfoHolder {
		private final Map<ConnectionProtocol, Collection<Identifier>> playChannels = new ConcurrentHashMap<>();

		@Override
		public Collection<Identifier> fabric_getPendingChannelsNames(ConnectionProtocol state) {
			return this.playChannels.computeIfAbsent(state, (key) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
		}
	}
}
