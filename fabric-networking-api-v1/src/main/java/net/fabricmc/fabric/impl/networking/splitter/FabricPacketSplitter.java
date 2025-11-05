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
import java.util.function.Consumer;
import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;

import net.minecraft.network.PacketEncoder;
import net.minecraft.network.VarInt;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.mixin.networking.accessor.PacketEncoderAccessor;

public class FabricPacketSplitter extends MessageToMessageEncoder<Packet<?>> {
	public static final int SAFE_S2C_SPLIT_SIZE = ClientboundCustomPayloadPacket.MAX_PAYLOAD_SIZE;
	public static final int SAFE_C2S_SPLIT_SIZE = ServerboundCustomPayloadPacket.MAX_PAYLOAD_SIZE;
	private final PacketEncoder<?> encoder;
	private final PayloadTypeRegistryImpl<?> payloadTypeRegistry;

	public FabricPacketSplitter(PacketEncoder<?> encoderHandler, PayloadTypeRegistryImpl<?> payloadTypeRegistry) {
		this.encoder = encoderHandler;
		this.payloadTypeRegistry = payloadTypeRegistry;
	}

	protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
		if (packet instanceof SplittablePacket splittablePacket) {
			splittablePacket.fabric_split(this.payloadTypeRegistry, channelHandlerContext, this.encoder, packet, list::add);
		} else {
			list.add(packet);
		}

		if (packet.isTerminal()) {
			channelHandlerContext.pipeline().remove(channelHandlerContext.name());
		}
	}

	public static void genericPacketSplitter(Identifier packetId, ChannelHandlerContext channelHandlerContext, PacketEncoder<?> encoder, Packet<?> packet,
											Function<CustomPacketPayload, Packet<?>> packetConstructor, Consumer<Packet<?>> consumer, int maxChunkSize, int maxPacketSize) throws Exception {
		ByteBuf buf = Unpooled.buffer();
		((PacketEncoderAccessor) encoder).fabric_encode(channelHandlerContext, packet, buf);

		if (buf.readableBytes() < maxChunkSize) {
			consumer.accept(new PassthroughPacket(buf));
			return;
		}

		if (buf.readableBytes() > maxPacketSize) {
			throw new EncoderException("Packet '" + packetId + "' may not be larger than " + maxPacketSize + " bytes!");
		}

		// First packet split with added packet size
		ByteBuf firstSplit = Unpooled.buffer(maxChunkSize);
		VarInt.write(firstSplit, buf.readableBytes());
		// First slice needs to be slightly smaller to accommodate the header (by the already written data amount)
		firstSplit.writeBytes(buf.readSlice(maxChunkSize - firstSplit.readableBytes()));

		consumer.accept(packetConstructor.apply(new FabricSplitPacketPayload(firstSplit)));

		// Remaining packets, as needed to send everything
		while (buf.isReadable()) {
			consumer.accept(packetConstructor.apply(new FabricSplitPacketPayload(buf.readSlice(Math.min(buf.readableBytes(), maxChunkSize)))));
		}
	}
}
