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

package net.fabricmc.fabric.impl.recipe.sync;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Used to notify server which recipes can be synced to the client.
 */
public record SupportedRecipeSerializersPayloadC2S(Set<Identifier> synchronizedSerializers) implements CustomPayload {
	public static final PacketCodec<PacketByteBuf, SupportedRecipeSerializersPayloadC2S> CODEC = PacketCodec.tuple(
			PacketCodecs.collection(HashSet::new, Identifier.PACKET_CODEC), SupportedRecipeSerializersPayloadC2S::synchronizedSerializers,
			SupportedRecipeSerializersPayloadC2S::new
	);
	public static final Id<SupportedRecipeSerializersPayloadC2S> ID = new Id<>(Identifier.of("fabric", "recipe_sync/supported_serializers"));

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
