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

package net.fabricmc.fabric.impl.networking.splitter;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.jspecify.annotations.Nullable;

import net.minecraft.network.PacketDecoder;
import net.minecraft.network.VarInt;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.networking.GenericPayloadAccessor;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.impl.networking.VanillaPacketTypes;
import net.fabricmc.fabric.mixin.networking.accessor.PacketDecoderAccessor;

public class FabricPacketMerger extends MessageToMessageDecoder<Packet<?>> {
	private final PacketDecoder<?> decoderHandler;
	private final PayloadTypeRegistryImpl<?> payloadTypeRegistry;
	private final VanillaPacketTypes vanillaPacketTypes;
	@Nullable
	private Merger packetMerger;

	public FabricPacketMerger(PacketDecoder<?> decoderHandler, PayloadTypeRegistryImpl<?> payloadTypeRegistry, VanillaPacketTypes vanillaPacketTypes) {
		this.decoderHandler = decoderHandler;
		this.payloadTypeRegistry = payloadTypeRegistry;
		this.vanillaPacketTypes = vanillaPacketTypes;
	}

	protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
		if (this.packetMerger != null) {
			ensureNotTransitioning(packet);

			CustomPacketPayload payload = packet instanceof GenericPayloadAccessor accessor ? accessor.fabric_payload() : null;

			if (payload == null) {
				throw new DecoderException("Received '" + packet.type().id() + "' packet, while expecting 'minecraft:custom_payload'!");
			}

			if (!(payload instanceof FabricSplitPacketPayload splitPacketPayload)) {
				throw new DecoderException("Expected '" + FabricSplitPacketPayload.ID.id() +"' payload packet, but received '" + payload.type().id() + "'!");
			}

			if (this.packetMerger.add(channelHandlerContext, splitPacketPayload, list)) {
				this.packetMerger = null;
			}
		} else if (packet instanceof GenericPayloadAccessor accessor && accessor.fabric_payload() instanceof FabricSplitPacketPayload payload) {
			ensureNotTransitioning(packet);
			ByteBuf buf = payload.byteBuf();
			int packetSize = VarInt.read(buf);
			int readerIndex = buf.readerIndex();

			PacketType<?> packetType = this.vanillaPacketTypes.get(VarInt.read(buf));

			if (packetType != packet.type()) {
				throw new DecoderException("Received unsupported split packet type! Expected '" + packet.type().id() + " got '" + (packetType != null ? packetType.id() : "<NULL>") + "'!");
			}

			Identifier payloadId = Identifier.STREAM_CODEC.decode(payload.byteBuf());

			buf.readerIndex(readerIndex);
			int maxSize = payloadTypeRegistry.getMaxPacketSize(payloadId);

			if (maxSize == -1) {
				throw new DecoderException("Received '" + payloadId + "' packet doesn't support splitting, but received split data!");
			} else if (maxSize < packetSize) {
				throw new DecoderException("Received '" + payloadId + "' packet is larger than max allowed size! Got " + packetSize + " bytes, expected " + maxSize + " bytes!");
			}

			this.packetMerger = new Merger(this.decoderHandler, payloadId, packetSize);

			if (this.packetMerger.add(channelHandlerContext, payload, list)) {
				throw new DecoderException("Received '" + payloadId + "' as a split packet, but it wasn't actually split!");
			}
		} else {
			list.add(packet);

			if (packet.isTerminal()) {
				channelHandlerContext.pipeline().remove(channelHandlerContext.name());
			}
		}
	}

	private static void ensureNotTransitioning(Packet<?> packet) {
		if (packet.isTerminal()) {
			throw new DecoderException("Terminal message received in bundle");
		}
	}

	private static class Merger {
		private final PacketDecoderAccessor decoderHandler;
		private final Identifier packetId;
		private final int finalSize;

		private final ByteBuf byteBuf;

		Merger(PacketDecoder<?> decoderHandler, Identifier identifier, int finalSize) {
			this.decoderHandler = (PacketDecoderAccessor) decoderHandler;
			this.packetId = identifier;
			this.byteBuf = Unpooled.buffer(finalSize);
			this.finalSize = finalSize;
		}

		boolean add(ChannelHandlerContext channelHandlerContext, FabricSplitPacketPayload payload, List<Object> objects) throws Exception {
			int newSize = this.byteBuf.readableBytes() + payload.byteBuf().readableBytes();

			if (this.finalSize < newSize) {
				throw new DecoderException("Received too much data for packet '" + this.packetId + "'! Expected " + this.finalSize + " bytes, received " + newSize + " bytes!");
			}

			this.byteBuf.writeBytes(payload.byteBuf());

			if (this.byteBuf.readableBytes() == this.finalSize) {
				this.decoderHandler.fabric_decode(channelHandlerContext, byteBuf, objects);
				return true;
			}

			return false;
		}
	}
}
