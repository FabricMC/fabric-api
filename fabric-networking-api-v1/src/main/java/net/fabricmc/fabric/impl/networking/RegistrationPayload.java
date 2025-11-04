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

import io.netty.util.AsciiString;

import net.minecraft.IdentifierException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RegistrationPayload(Type<RegistrationPayload> id, List<Identifier> channels) implements CustomPacketPayload {
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
		List<Identifier> ids = new ArrayList<>();
		StringBuilder active = new StringBuilder();

		while (buf.isReadable()) {
			byte b = buf.readByte();

			if (b != 0) {
				active.append(AsciiString.b2c(b));
			} else {
				addId(ids, active);
				active = new StringBuilder();
			}
		}

		addId(ids, active);

		return Collections.unmodifiableList(ids);
	}

	private static void addId(List<Identifier> ids, StringBuilder sb) {
		String literal = sb.toString();

		try {
			ids.add(Identifier.parse(literal));
		} catch (IdentifierException ex) {
			NetworkingImpl.LOGGER.warn("Received invalid channel identifier \"{}\"", literal);
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return id;
	}

	private static StreamCodec<FriendlyByteBuf, RegistrationPayload> codec(Type<RegistrationPayload> id) {
		return CustomPacketPayload.codec(RegistrationPayload::write, buf -> new RegistrationPayload(id, buf));
	}
}
