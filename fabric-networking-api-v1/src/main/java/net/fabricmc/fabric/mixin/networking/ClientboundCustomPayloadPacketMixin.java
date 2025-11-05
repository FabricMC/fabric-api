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

package net.fabricmc.fabric.mixin.networking;

import java.util.List;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.fabricmc.fabric.impl.networking.FabricCustomPayloadPacketCodec;
import net.fabricmc.fabric.impl.networking.GenericPayloadAccessor;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.impl.networking.splitter.FabricPacketSplitter;
import net.fabricmc.fabric.impl.networking.splitter.SplittablePacket;

@Mixin(ClientboundCustomPayloadPacket.class)
public class ClientboundCustomPayloadPacketMixin implements SplittablePacket, GenericPayloadAccessor {
	@Shadow
	@Final
	private CustomPacketPayload payload;

	@WrapOperation(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;",
					ordinal = 0
			)
	)
	private static StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload> wrapPlayCodec(CustomPacketPayload.FallbackProvider<RegistryFriendlyByteBuf> unknownCodecFactory, List<CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, ?>> types, Operation<StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>> original) {
		StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload> codec = original.call(unknownCodecFactory, types);
		FabricCustomPayloadPacketCodec<RegistryFriendlyByteBuf> fabricCodec = (FabricCustomPayloadPacketCodec<RegistryFriendlyByteBuf>) codec;
		fabricCodec.fabric_setPacketCodecProvider((packetByteBuf, identifier) -> PayloadTypeRegistryImpl.PLAY_S2C.get(identifier));
		return codec;
	}

	@WrapOperation(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;",
					ordinal = 1
			)
	)
	private static StreamCodec<FriendlyByteBuf, CustomPacketPayload> wrapConfigCodec(CustomPacketPayload.FallbackProvider<FriendlyByteBuf> unknownCodecFactory, List<CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, ?>> types, Operation<StreamCodec<FriendlyByteBuf, CustomPacketPayload>> original) {
		StreamCodec<FriendlyByteBuf, CustomPacketPayload> codec = original.call(unknownCodecFactory, types);
		FabricCustomPayloadPacketCodec<FriendlyByteBuf> fabricCodec = (FabricCustomPayloadPacketCodec<FriendlyByteBuf>) codec;
		fabricCodec.fabric_setPacketCodecProvider((packetByteBuf, identifier) -> PayloadTypeRegistryImpl.CONFIGURATION_S2C.get(identifier));
		return codec;
	}

	@Override
	public void fabric_split(PayloadTypeRegistryImpl<?> payloadTypeRegistry, ChannelHandlerContext channelHandlerContext, PacketEncoder<?> encoder, Packet<?> packet, Consumer<Packet<?>> consumer) throws Exception {
		int size = payloadTypeRegistry.getMaxPacketSize(this.payload.type().id());

		if (size == -1) {
			consumer.accept((Packet<?>) this);
			return;
		}

		FabricPacketSplitter.genericPacketSplitter(this.payload.type().id(), channelHandlerContext, encoder, packet, ClientboundCustomPayloadPacket::new, consumer, FabricPacketSplitter.SAFE_S2C_SPLIT_SIZE, size);
	}

	@Override
	public CustomPacketPayload fabric_payload() {
		return this.payload;
	}
}
