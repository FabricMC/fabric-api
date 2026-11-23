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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.netty.handler.codec.DecoderException;
import io.netty.util.AsciiString;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RegistrationPayload(Type<RegistrationPayload> type, List<Identifier> channels) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<RegistrationPayload> REGISTER = new CustomPacketPayload.Type<>(NetworkingImpl.REGISTER_CHANNEL);
	public static final CustomPacketPayload.Type<RegistrationPayload> UNREGISTER = new CustomPacketPayload.Type<>(NetworkingImpl.UNREGISTER_CHANNEL);
	public static final StreamCodec<FriendlyByteBuf, RegistrationPayload> REGISTER_CODEC = codec(REGISTER);
	public static final StreamCodec<FriendlyByteBuf, RegistrationPayload> UNREGISTER_CODEC = codec(UNREGISTER);

	private RegistrationPayload(Type<RegistrationPayload> id, FriendlyByteBuf buf) {
		this(id, read(buf));
	}

	private void write(FriendlyByteBuf buf) {
		boolean first = true;

		for (Identifier channel : channels) {
			if (first) {
				first = false;
			} else {
				buf.writeByte(0);
			}

			buf.writeBytes(channel.toString().getBytes(StandardCharsets.US_ASCII));
		}
	}

	private static List<Identifier> read(FriendlyByteBuf buf) {
		long maxPayloadSize = (long) AbstractChanneledNetworkAddon.MAX_CHANNELS * (AbstractChanneledNetworkAddon.MAX_CHANNEL_NAME_LENGTH + 1L);

		if (buf.readableBytes() > maxPayloadSize) {
			throw new DecoderException("Channel registration payload is too large");
		}

		List<Identifier> ids = new ArrayList<>();
		StringBuilder active = new StringBuilder();
		int channelCount = 0;

		while (buf.isReadable()) {
			byte b = buf.readByte();

			if (b != 0) {
				if (active.length() >= AbstractChanneledNetworkAddon.MAX_CHANNEL_NAME_LENGTH) {
					throw new DecoderException("Channel identifier is too long");
				}

				active.append(AsciiString.b2c(b));
			} else {
				channelCount++;

				if (channelCount > AbstractChanneledNetworkAddon.MAX_CHANNELS) {
					throw new DecoderException("Too many channel identifiers");
				}

				addId(ids, active.toString());
				active.setLength(0);
			}
		}

		if (!active.isEmpty() && channelCount >= AbstractChanneledNetworkAddon.MAX_CHANNELS) {
			throw new DecoderException("Too many channel identifiers");
		}

		addId(ids, active.toString());

		return Collections.unmodifiableList(ids);
	}

	private static void addId(List<Identifier> ids, String identifier) {
		if (identifier.isEmpty()) {
			return;
		}

		Identifier id = Identifier.tryParse(identifier);

		if (id == null) {
			return;
		}

		if (id.toString().length() > AbstractChanneledNetworkAddon.MAX_CHANNEL_NAME_LENGTH) {
			throw new DecoderException("Channel identifier is too long");
		}

		ids.add(id);
	}

	private static StreamCodec<FriendlyByteBuf, RegistrationPayload> codec(Type<RegistrationPayload> id) {
		return CustomPacketPayload.codec(RegistrationPayload::write, buf -> new RegistrationPayload(id, buf));
	}
}
