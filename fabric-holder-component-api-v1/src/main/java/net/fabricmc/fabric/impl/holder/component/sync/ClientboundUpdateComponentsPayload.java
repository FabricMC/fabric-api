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

package net.fabricmc.fabric.impl.holder.component.sync;

import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public record ClientboundUpdateComponentsPayload(
		// We don't decode any component yet as registries aren't available during configuration.
		// Each ByteBuf encodes a registry ResourceKey and a Map<Holder, DataComponentMap>
		List<ByteBuf> registryToComponents
) implements CustomPacketPayload {
	public static final Type<ClientboundUpdateComponentsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("fabric", "update_holder_components"));
	public static final StreamCodec<ByteBuf, ClientboundUpdateComponentsPayload> STREAM_CODEC = HolderComponentSynchronization.BYTE_BUF_SLICE_CODEC
			.apply(ByteBufCodecs.list())
			.map(ClientboundUpdateComponentsPayload::new, ClientboundUpdateComponentsPayload::registryToComponents);

	@Override
	public Type<ClientboundUpdateComponentsPayload> type() {
		return TYPE;
	}

	public static boolean shouldSend(ServerPlayer player) {
		return ServerPlayNetworking.canSend(player, ClientboundUpdateComponentsPayload.TYPE)
				&& !player.getPacketContext().orElseThrow(PacketContext.CONNECTION).isMemoryConnection();
	}
}
