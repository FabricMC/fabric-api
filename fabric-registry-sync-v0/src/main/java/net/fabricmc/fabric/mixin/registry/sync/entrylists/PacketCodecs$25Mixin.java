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

package net.fabricmc.fabric.mixin.registry.sync.entrylists;

import java.util.HashMap;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import io.netty.buffer.ByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.registry.entrylists.CustomRegistryEntryList;
import net.fabricmc.fabric.api.event.registry.entrylists.CustomRegistryEntryListSerializer;
import net.fabricmc.fabric.impl.registry.sync.entrylists.util.ChannelUtil;

@SuppressWarnings("unchecked")
@Mixin(targets = "net.minecraft.network.codec.PacketCodecs$25")
class PacketCodecs$25Mixin<V> {
	@Unique
	private static final String COMMENT_IN_MIXIN_EXPORT = "-45 is ours. if anyone else uses it, they deserve the consequences";
	@Unique
	private static final int FABRIC_CUSTOM_REGISTRY_LIST_ID = -45;

	@Shadow
	@Final
	RegistryKey<? extends Registry<V>> field_54511;

	@Unique
	HashMap<CustomRegistryEntryListSerializer, PacketCodec<RegistryByteBuf, CustomRegistryEntryList<V>>> cache = new HashMap<>();

	private PacketCodecs$25Mixin() {
	}

	@Inject(
			method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/registry/entry/RegistryEntryList;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private <T> void encodeCustomRegistryEntryList(RegistryByteBuf registryByteBuf, RegistryEntryList<T> registryEntryList, CallbackInfo ci) {
		if (!ChannelUtil.isInstalled(registryByteBuf) || !(registryEntryList instanceof CustomRegistryEntryList<T> customRegistryEntryList)) {
			return;
		}

		VarInts.write(registryByteBuf, FABRIC_CUSTOM_REGISTRY_LIST_ID);
		Identifier.PACKET_CODEC.encode(registryByteBuf, customRegistryEntryList.getSerializer().getIdentifier());
		PacketCodec<RegistryByteBuf, CustomRegistryEntryList<V>> packetCodec = cache.computeIfAbsent(
				customRegistryEntryList.getSerializer(),
				serializer1 -> (PacketCodec<RegistryByteBuf, CustomRegistryEntryList<V>>) serializer1.createPacketCodec(field_54511)
		);
		packetCodec.encode(registryByteBuf, (CustomRegistryEntryList<V>) customRegistryEntryList);
		ci.cancel();
	}

	@WrapOperation(
			method = "decode(Lnet/minecraft/network/RegistryByteBuf;)Lnet/minecraft/registry/entry/RegistryEntryList;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/encoding/VarInts;read(Lio/netty/buffer/ByteBuf;)I"
			)
	)
	private int decodeCustomRegistryEntryList(ByteBuf buf, Operation<Integer> original, RegistryByteBuf registryByteBuf, @Cancellable CallbackInfoReturnable<RegistryEntryList<?>> cir) {
		int id = original.call(buf);

		if (!ChannelUtil.isInstalled(registryByteBuf) || id != FABRIC_CUSTOM_REGISTRY_LIST_ID || cir.isCancelled()) {
			return id;
		}

		Identifier identifier = Identifier.PACKET_CODEC.decode(buf);
		CustomRegistryEntryListSerializer serializer = CustomRegistryEntryListSerializer.getSerializer(identifier);

		if (serializer == null) {
			throw new IllegalStateException("Unknown CustomRegistryEntryListSerializer: " + identifier);
		}

		PacketCodec<RegistryByteBuf, CustomRegistryEntryList<V>> packetCodec = cache.computeIfAbsent(
				serializer,
				serializer1 -> (PacketCodec<RegistryByteBuf, CustomRegistryEntryList<V>>) serializer1.createPacketCodec(field_54511)
		);

		cir.setReturnValue(packetCodec.decode(registryByteBuf));
		return id;
	}
}
