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
import java.util.function.IntSupplier;

import io.netty.buffer.ByteBufUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
	public static final PayloadTypeRegistryImpl<FriendlyByteBuf> SERVERBOUND_CONFIGURATION = new PayloadTypeRegistryImpl<>(ConnectionProtocol.CONFIGURATION, PacketFlow.SERVERBOUND);
	public static final PayloadTypeRegistryImpl<FriendlyByteBuf> CLIENTBOUND_CONFIGURATION = new PayloadTypeRegistryImpl<>(ConnectionProtocol.CONFIGURATION, PacketFlow.CLIENTBOUND);
	public static final PayloadTypeRegistryImpl<RegistryFriendlyByteBuf> SERVERBOUND_PLAY = new PayloadTypeRegistryImpl<>(ConnectionProtocol.PLAY, PacketFlow.SERVERBOUND);
	public static final PayloadTypeRegistryImpl<RegistryFriendlyByteBuf> CLIENTBOUND_PLAY = new PayloadTypeRegistryImpl<>(ConnectionProtocol.PLAY, PacketFlow.CLIENTBOUND);
	private final Map<Identifier, CustomPacketPayload.TypeAndCodec<B, ? extends CustomPacketPayload>> packetTypes = new HashMap<>();
	private final Object2IntMap<Identifier> maxPacketSizes = new Object2IntOpenHashMap<>();
	private final Object2ObjectMap<Identifier, IntSupplier> pendingMaxPacketSizes = new Object2ObjectOpenHashMap<>();
	private final ConnectionProtocol protocol;
	private final PacketFlow flow;
	private final int minimalSplittableSize;

	private PayloadTypeRegistryImpl(ConnectionProtocol protocol, PacketFlow flow) {
		this.protocol = protocol;
		this.flow = flow;
		this.minimalSplittableSize = flow == PacketFlow.CLIENTBOUND ? FabricPacketSplitter.SAFE_S2C_SPLIT_SIZE : FabricPacketSplitter.SAFE_C2S_SPLIT_SIZE;
	}

	@Nullable
	public static PayloadTypeRegistryImpl<?> get(ProtocolInfo<?> state) {
		return switch (state.id()) {
		case CONFIGURATION -> state.flow() == PacketFlow.CLIENTBOUND ? CLIENTBOUND_CONFIGURATION : SERVERBOUND_CONFIGURATION;
		case PLAY -> state.flow() == PacketFlow.CLIENTBOUND ? CLIENTBOUND_PLAY : SERVERBOUND_PLAY;
		default -> null;
		};
	}

	@Override
	public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> register(CustomPacketPayload.Type<T> type, StreamCodec<? super B, T> codec) {
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(codec, "codec");

		final CustomPacketPayload.TypeAndCodec<B, T> payloadType = new CustomPacketPayload.TypeAndCodec<>(type, codec.cast());

		if (packetTypes.containsKey(type.id())) {
			throw new IllegalArgumentException("Packet type " + type + " is already registered!");
		}

		packetTypes.put(type.id(), payloadType);
		return payloadType;
	}

	@Override
	public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> registerLarge(CustomPacketPayload.Type<T> type, StreamCodec<? super B, T> codec, int maxPacketSize) {
		if (maxPacketSize < 0) {
			throw new IllegalArgumentException("Provided maxPacketSize needs to be positive!");
		}

		CustomPacketPayload.TypeAndCodec<? super B, T> typeAndCodec = register(type, codec);
		padAndSetMaxPacketSize(type.id(), maxPacketSize);
		return typeAndCodec;
	}

	@Override
	public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> registerLarge(CustomPacketPayload.Type<T> type, StreamCodec<? super B, T> codec, IntSupplier maxPacketSizeSupplier) {
		Objects.requireNonNull(maxPacketSizeSupplier, "maxPacketSizeSupplier");

		CustomPacketPayload.TypeAndCodec<? super B, T> typeAndCodec = register(type, codec);
		pendingMaxPacketSizes.put(type.id(), maxPacketSizeSupplier);
		return typeAndCodec;
	}

	private void padAndSetMaxPacketSize(Identifier id, int maxSize) {
		// Defines max packet size, increased by length of packet's Identifier to cover full size of CustomPayloadX2YPackets.
		int identifierSize = ByteBufUtil.utf8MaxBytes(id.toString());
		int paddingSize = VarInt.getByteSize(identifierSize) + identifierSize + 5 * 2;
		int maxPacketSize = maxSize + paddingSize;

		// Prevent overflow
		if (maxPacketSize < 0) {
			maxPacketSize = Integer.MAX_VALUE;
		}

		// No need to enable splitting, if packet's max size is smaller than chunk
		if (maxPacketSize > this.minimalSplittableSize) {
			this.maxPacketSizes.put(id, maxPacketSize);
		}
	}

	public CustomPacketPayload.@Nullable TypeAndCodec<B, ? extends CustomPacketPayload> get(Identifier id) {
		return packetTypes.get(id);
	}

	public <T extends CustomPacketPayload> CustomPacketPayload.@Nullable TypeAndCodec<B, T> get(CustomPacketPayload.Type<T> type) {
		//noinspection unchecked
		return (CustomPacketPayload.TypeAndCodec<B, T>) packetTypes.get(type.id());
	}

	/**
	 * @return the max packet size, or -1 if the payload type does not need splitting.
	 */
	public int getMaxPacketSizeForSplitting(Identifier id) {
		IntSupplier supplier = this.pendingMaxPacketSizes.remove(id);

		if (supplier != null) {
			int maxPacketSize = supplier.getAsInt();

			if (maxPacketSize < 0) {
				throw new IllegalArgumentException("maxPacketSize supplier for packet type " + id + ": must be positive!");
			}

			padAndSetMaxPacketSize(id, maxPacketSize);
		}

		return this.maxPacketSizes.getOrDefault(id, -1);
	}

	public ConnectionProtocol getProtocol() {
		return protocol;
	}

	public PacketFlow getFlow() {
		return flow;
	}
}
