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

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PayloadTypeRegistryTests {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		PayloadTypeRegistry.playC2S().register(C2SPlayPayload.ID, C2SPlayPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(S2CPlayPayload.ID, S2CPlayPayload.CODEC);

		PayloadTypeRegistry.configurationC2S().register(C2SConfigPayload.ID, C2SConfigPayload.CODEC);
		PayloadTypeRegistry.configurationS2C().register(S2CConfigPayload.ID, S2CConfigPayload.CODEC);
	}

	@Test
	void C2SPlay() {
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(PacketByteBufs.create(), null);

		var packetToSend = new ServerboundCustomPayloadPacket(new C2SPlayPayload("Hello"));
		ServerboundCustomPayloadPacket.STREAM_CODEC.encode(buf, packetToSend);

		ServerboundCustomPayloadPacket decodedPacket = ServerboundCustomPayloadPacket.STREAM_CODEC.decode(buf);

		if (decodedPacket.payload() instanceof C2SPlayPayload payload) {
			assertEquals("Hello", payload.value());
		} else {
			fail();
		}
	}

	@Test
	void S2CPlay() {
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(PacketByteBufs.create(), null);

		var packetToSend = new ClientboundCustomPayloadPacket(new S2CPlayPayload("Hello"));
		ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC.encode(buf, packetToSend);

		ClientboundCustomPayloadPacket decodedPacket = ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC.decode(buf);

		if (decodedPacket.payload() instanceof S2CPlayPayload payload) {
			assertEquals("Hello", payload.value());
		} else {
			fail();
		}
	}

	@Test
	void C2SConfig() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		var packetToSend = new ServerboundCustomPayloadPacket(new C2SConfigPayload("Hello"));
		ServerboundCustomPayloadPacket.STREAM_CODEC.encode(buf, packetToSend);

		ServerboundCustomPayloadPacket decodedPacket = ServerboundCustomPayloadPacket.STREAM_CODEC.decode(buf);

		if (decodedPacket.payload() instanceof C2SConfigPayload payload) {
			assertEquals("Hello", payload.value());
		} else {
			fail();
		}
	}

	@Test
	void S2CConfig() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		var packetToSend = new ClientboundCustomPayloadPacket(new S2CConfigPayload("Hello"));
		ClientboundCustomPayloadPacket.CONFIG_STREAM_CODEC.encode(buf, packetToSend);

		ClientboundCustomPayloadPacket decodedPacket = ClientboundCustomPayloadPacket.CONFIG_STREAM_CODEC.decode(buf);

		if (decodedPacket.payload() instanceof S2CConfigPayload payload) {
			assertEquals("Hello", payload.value());
		} else {
			fail();
		}
	}

	private record C2SPlayPayload(String value) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<C2SPlayPayload> ID = new Type<>(Identifier.parse("fabric:c2s_play"));
		public static final StreamCodec<RegistryFriendlyByteBuf, C2SPlayPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(C2SPlayPayload::new, C2SPlayPayload::value).cast();

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	private record S2CPlayPayload(String value) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<S2CPlayPayload> ID = new Type<>(Identifier.parse("fabric:s2c_play"));
		public static final StreamCodec<RegistryFriendlyByteBuf, S2CPlayPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(S2CPlayPayload::new, S2CPlayPayload::value).cast();

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	private record C2SConfigPayload(String value) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<C2SConfigPayload> ID = new Type<>(Identifier.parse("fabric:c2s_config"));
		public static final StreamCodec<FriendlyByteBuf, C2SConfigPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(C2SConfigPayload::new, C2SConfigPayload::value).cast();

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	private record S2CConfigPayload(String value) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<S2CConfigPayload> ID = new Type<>(Identifier.parse("fabric:s2c_config"));
		public static final StreamCodec<FriendlyByteBuf, S2CConfigPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(S2CConfigPayload::new, S2CConfigPayload::value).cast();

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}
}
