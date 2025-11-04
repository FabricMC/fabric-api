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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.netty.buffer.ByteBufUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jspecify.annotations.Nullable;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.splitter.FabricPacketSplitter;

public class PayloadTypeRegistryImpl<B extends FriendlyByteBuf> implements PayloadTypeRegistry<B> {
	public static final PayloadTypeRegistryImpl<FriendlyByteBuf> CONFIGURATION_C2S = new PayloadTypeRegistryImpl<>(ConnectionProtocol.CONFIGURATION, PacketFlow.SERVERBOUND);
	public static final PayloadTypeRegistryImpl<FriendlyByteBuf> CONFIGURATION_S2C = new PayloadTypeRegistryImpl<>(ConnectionProtocol.CONFIGURATION, PacketFlow.CLIENTBOUND);
	public static final PayloadTypeRegistryImpl<RegistryFriendlyByteBuf> PLAY_C2S = new PayloadTypeRegistryImpl<>(ConnectionProtocol.PLAY, PacketFlow.SERVERBOUND);
	public static final PayloadTypeRegistryImpl<RegistryFriendlyByteBuf> PLAY_S2C = new PayloadTypeRegistryImpl<>(ConnectionProtocol.PLAY, PacketFlow.CLIENTBOUND);
	private final Map<Identifier, CustomPacketPayload.TypeAndCodec<B, ? extends CustomPacketPayload>> packetTypes = new HashMap<>();
	private final Object2IntMap<Identifier> maxPacketSize = new Object2IntOpenHashMap<>();
	private final ConnectionProtocol state;
	private final PacketFlow side;
	private final int minimalSplittableSize;

	private PayloadTypeRegistryImpl(ConnectionProtocol state, PacketFlow side) {
		this.state = state;
		this.side = side;
		this.minimalSplittableSize = side == PacketFlow.CLIENTBOUND ? FabricPacketSplitter.SAFE_S2C_SPLIT_SIZE : FabricPacketSplitter.SAFE_C2S_SPLIT_SIZE;
	}

	@Nullable
	public static PayloadTypeRegistryImpl<?> get(ProtocolInfo<?> state) {
		return switch (state.id()) {
		case CONFIGURATION -> state.flow() == PacketFlow.CLIENTBOUND ? CONFIGURATION_S2C : CONFIGURATION_C2S;
		case PLAY -> state.flow() == PacketFlow.CLIENTBOUND ? PLAY_S2C : PLAY_C2S;
		default -> null;
		};
	}

	@Override
	public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> register(CustomPacketPayload.Type<T> id, StreamCodec<? super B, T> codec) {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(codec, "codec");

		final CustomPacketPayload.TypeAndCodec<B, T> payloadType = new CustomPacketPayload.TypeAndCodec<>(id, codec.cast());

		if (packetTypes.containsKey(id.id())) {
			throw new IllegalArgumentException("Packet type " + id + " is already registered!");
		}

		packetTypes.put(id.id(), payloadType);
		return payloadType;
	}

	@Override
	public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> registerLarge(CustomPacketPayload.Type<T> id, StreamCodec<? super B, T> codec, int maxPayloadSize) {
		if (maxPayloadSize < 0) {
			throw new IllegalArgumentException("Provided maxPayloadSize needs to be positive!");
		}

		CustomPacketPayload.TypeAndCodec<? super B, T> type = register(id, codec);
		// Defines max packet size, increased by length of packet's Identifier to cover full size of CustomPayloadX2YPackets.
		int identifierSize = ByteBufUtil.utf8MaxBytes(id.id().toString());
		int maxPacketSize = maxPayloadSize + VarInt.getByteSize(identifierSize) + identifierSize + 5 * 2;

		// Prevent overflow
		if (maxPacketSize < 0) {
			maxPacketSize = Integer.MAX_VALUE;
		}

		// No need to enable splitting, if packet's max size is smaller than chunk
		if (maxPacketSize > this.minimalSplittableSize) {
			this.maxPacketSize.put(id.id(), maxPacketSize);
		}

		return type;
	}

	public CustomPacketPayload.@Nullable TypeAndCodec<B, ? extends CustomPacketPayload> get(Identifier id) {
		return packetTypes.get(id);
	}

	public <T extends CustomPacketPayload> CustomPacketPayload.@Nullable TypeAndCodec<B, T> get(CustomPacketPayload.Type<T> id) {
		//noinspection unchecked
		return (CustomPacketPayload.TypeAndCodec<B, T>) packetTypes.get(id.id());
	}

	public int getMaxPacketSize(Identifier id) {
		return this.maxPacketSize.getOrDefault(id, -1);
	}

	public ConnectionProtocol getPhase() {
		return state;
	}

	public PacketFlow getSide() {
		return side;
	}
}
