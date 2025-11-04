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

import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CommonRegisterPayload(int version, String phase, Set<Identifier> channels) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<CommonRegisterPayload> ID = new Type<>(Identifier.parse("c:register"));
	public static final StreamCodec<FriendlyByteBuf, CommonRegisterPayload> CODEC = CustomPacketPayload.codec(CommonRegisterPayload::write, CommonRegisterPayload::new);

	public static final String PLAY_PHASE = "play";
	public static final String CONFIGURATION_PHASE = "configuration";

	private CommonRegisterPayload(FriendlyByteBuf buf) {
		this(
				buf.readVarInt(),
				buf.readUtf(),
				buf.readCollection(HashSet::new, FriendlyByteBuf::readIdentifier)
		);
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(version);
		buf.writeUtf(phase);
		buf.writeCollection(channels, FriendlyByteBuf::writeIdentifier);
	}

	@Override
	public Type<CommonRegisterPayload> type() {
		return ID;
	}
}
