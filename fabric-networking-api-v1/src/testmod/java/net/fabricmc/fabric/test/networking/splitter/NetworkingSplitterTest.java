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

package net.fabricmc.fabric.test.networking.splitter;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.RandomSource;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.test.networking.NetworkingTestmods;

public class NetworkingSplitterTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkingSplitterTest.class);

	private static final int DATA_SIZE = 20 * 1024 * 1024;

	// 20 MB of random data source
	private static final int[][] RANDOM_DATA = {
			IntStream.generate(RandomSource.create(24534)::nextInt).limit(20).toArray(),
			IntStream.generate(RandomSource.create(24533)::nextInt).limit(DATA_SIZE / 4).toArray()
	};

	@Override
	public void onInitialize() {
		// Register the payload on both sides for play and configuration
		PayloadTypeRegistry.playS2C().registerLarge(LargePayload.ID, LargePayload.CODEC, DATA_SIZE + 14);
		PayloadTypeRegistry.playC2S().registerLarge(LargePayload.ID, LargePayload.CODEC, DATA_SIZE + 14);

		// When the client joins, send a packet expecting it to be validated and echoed back
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sender.sendPacket(new LargePayload(0, RANDOM_DATA[0])));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sender.sendPacket(new LargePayload(1, RANDOM_DATA[1])));

		// Validate received packet
		ServerPlayNetworking.registerGlobalReceiver(LargePayload.ID, (payload, context) -> {
			validateLargePacketData(payload.index(), payload.data(), "server");
		});
	}

	public static void validateLargePacketData(int index, int[] data, String side) {
		if (Arrays.equals(RANDOM_DATA[index], data)) {
			NetworkingTestmods.LOGGER.info("Successfully received large packet [" + index + "] on " + side);
			return;
		}

		throw new IllegalStateException("Received large packet [" + index + "] doesn't match sent one on the " + side + " side!");
	}

	// A payload registered on both sides
	// This tests that the server can send a large packet to the client, and then receive a response from the client
	public record LargePayload(int index, int[] data) implements CustomPacketPayload {
		public static final Type<LargePayload> ID = new Type<>(NetworkingTestmods.id("large_packet"));
		public static final StreamCodec<FriendlyByteBuf, LargePayload> CODEC = StreamCodec.ofMember(LargePayload::write, LargePayload::read);

		private static LargePayload read(FriendlyByteBuf buf) {
			int index = buf.readVarInt();
			var data = new int[buf.readVarInt()];

			for (int i = 0; i < data.length; i++) {
				data[i] = buf.readInt();
			}

			return new LargePayload(index, data);
		}

		private void write(FriendlyByteBuf buf) {
			buf.writeVarInt(this.index);
			buf.writeVarInt(this.data.length);

			for (int i = 0; i < this.data.length; i++) {
				buf.writeInt(this.data[i]);
			}
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}
}
