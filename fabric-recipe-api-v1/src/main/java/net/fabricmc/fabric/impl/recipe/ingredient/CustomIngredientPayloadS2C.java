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

package net.fabricmc.fabric.impl.recipe.ingredient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CustomIngredientPayloadS2C(int protocolVersion) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, CustomIngredientPayloadS2C> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, CustomIngredientPayloadS2C::protocolVersion,
			CustomIngredientPayloadS2C::new
	);
	public static final CustomPacketPayload.Type<CustomIngredientPayloadS2C> ID = new Type<>(CustomIngredientSync.PACKET_ID);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
