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

package net.fabricmc.fabric.impl.modprotocol.payload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundModProtocolResponsePayload(Map<Identifier, String> supported, List<Identifier> missingServer) implements CustomPacketPayload {
	private static final int MAX_ENTRIES = 2048;

	public static final Type<ServerboundModProtocolResponsePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("fabric", "mod_protocol/response/v1"));
	public static final StreamCodec<FriendlyByteBuf, ServerboundModProtocolResponsePayload> PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.map(ServerboundModProtocolResponsePayload::createMap, Identifier.STREAM_CODEC, ByteBufCodecs.STRING_UTF8, MAX_ENTRIES), ServerboundModProtocolResponsePayload::supported,
			Identifier.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_ENTRIES)), ServerboundModProtocolResponsePayload::missingServer,
			ServerboundModProtocolResponsePayload::new
	);

	private static Map<Identifier, String> createMap(int i) {
		return new HashMap<>(i);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
