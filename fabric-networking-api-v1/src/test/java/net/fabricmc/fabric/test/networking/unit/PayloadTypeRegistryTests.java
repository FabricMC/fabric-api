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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;

import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PayloadTypeRegistryTests {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		PayloadTypeRegistry.serverboundPlay().register(ServerboundPlayPayload.TYPE, ServerboundPlayPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ClientboundPlayPayload.TYPE, ClientboundPlayPayload.CODEC);

		PayloadTypeRegistry.serverboundConfiguration().register(ServerboundConfigPayload.ID, ServerboundConfigPayload.CODEC);
		PayloadTypeRegistry.clientboundConfiguration().register(ClientboundConfigPayload.ID, ClientboundConfigPayload.CODEC);
	}

	@Test
	void serverboundPlay() {
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(FriendlyByteBufs.create(), null);

		var packetToSend = new ServerboundCustomPayloadPacket(new ServerboundPlayPayload("Hello"));
		ServerboundCustomPayloadPacket.STREAM_CODEC.encode(buf, packetToSend);

		ServerboundCustomPayloadPacket decodedPacket = ServerboundCustomPayloadPacket.STREAM_CODEC.decode(buf);

		if (decodedPacket.payload() instanceof ServerboundPlayPayload payload) {
			assertEquals("Hello", payload.value());
		} else {
			fail();
		}
	}

	@Test
	void clientboundPlay() {
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(FriendlyByteBufs.create(), null);

		var packetToSend = new ClientboundCustomPayloadPacket(new ClientboundPlayPayload("Hello"));
		ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC.encode(buf, packetToSend);

		ClientboundCustomPayloadPacket decodedPacket = ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC.decode(buf);

		if (decodedPacket.payload() instanceof ClientboundPlayPayload payload) {
			assertEquals("Hello", payload.value());
		} else {
			fail();
		}
	}

	@Test
	void serverboundConfig() {
		FriendlyByteBuf buf = FriendlyByteBufs.create();

		var packetToSend = new ServerboundCustomPayloadPacket(new ServerboundConfigPayload("Hello"));
		ServerboundCustomPayloadPacket.STREAM_CODEC.encode(buf, packetToSend);

		ServerboundCustomPayloadPacket decodedPacket = ServerboundCustomPayloadPacket.STREAM_CODEC.decode(buf);

		if (decodedPacket.payload() instanceof ServerboundConfigPayload payload) {
			assertEquals("Hello", payload.value());
		} else {
			fail();
		}
	}

	@Test
	void clientboundConfig() {
		FriendlyByteBuf buf = FriendlyByteBufs.create();

		var packetToSend = new ClientboundCustomPayloadPacket(new ClientboundConfigPayload("Hello"));
		ClientboundCustomPayloadPacket.CONFIG_STREAM_CODEC.encode(buf, packetToSend);

		ClientboundCustomPayloadPacket decodedPacket = ClientboundCustomPayloadPacket.CONFIG_STREAM_CODEC.decode(buf);

		if (decodedPacket.payload() instanceof ClientboundConfigPayload payload) {
			assertEquals("Hello", payload.value());
		} else {
			fail();
		}
	}

	private record ServerboundPlayPayload(String value) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ServerboundPlayPayload> TYPE = new Type<>(Identifier.parse("fabric:c2s_play"));
		public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPlayPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(ServerboundPlayPayload::new, ServerboundPlayPayload::value).cast();

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	private record ClientboundPlayPayload(String value) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ClientboundPlayPayload> TYPE = new Type<>(Identifier.parse("fabric:s2c_play"));
		public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlayPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(ClientboundPlayPayload::new, ClientboundPlayPayload::value).cast();

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	private record ServerboundConfigPayload(String value) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ServerboundConfigPayload> ID = new Type<>(Identifier.parse("fabric:c2s_config"));
		public static final StreamCodec<FriendlyByteBuf, ServerboundConfigPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(ServerboundConfigPayload::new, ServerboundConfigPayload::value).cast();

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	private record ClientboundConfigPayload(String value) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ClientboundConfigPayload> ID = new Type<>(Identifier.parse("fabric:s2c_config"));
		public static final StreamCodec<FriendlyByteBuf, ClientboundConfigPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(ClientboundConfigPayload::new, ClientboundConfigPayload::value).cast();

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}
}
