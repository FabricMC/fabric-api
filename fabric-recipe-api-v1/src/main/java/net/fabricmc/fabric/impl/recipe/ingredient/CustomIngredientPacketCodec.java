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

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public class CustomIngredientPacketCodec implements PacketCodec<RegistryByteBuf, Ingredient> {
	static final int PACKET_MARKER = -1;
	private final PacketCodec<RegistryByteBuf, Ingredient> fallback;

	public CustomIngredientPacketCodec(PacketCodec<RegistryByteBuf, Ingredient> fallback) {
		this.fallback = fallback;
	}

	@Override
	public Ingredient decode(RegistryByteBuf buf) {
		int index = buf.readerIndex();

		if (buf.readVarInt() != PACKET_MARKER) {
			// Reset index for vanilla's normal deserialization logic.
			buf.readerIndex(index);
			return this.fallback.decode(buf);
		}

		Identifier type = buf.readIdentifier();
		CustomIngredientSerializer<?> serializer = CustomIngredientSerializer.get(type);

		if (serializer == null) {
			throw new IllegalArgumentException("Cannot deserialize custom ingredient of unknown type " + type);
		}

		return serializer.getPacketCodec().decode(buf).toVanilla();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void encode(RegistryByteBuf buf, Ingredient value) {
		CustomIngredient customIngredient = value.getCustomIngredient();

		if (shouldEncodeFallback(customIngredient)) {
			// The client doesn't support this custom ingredient, so we send the matching stacks as a regular ingredient.
			this.fallback.encode(buf, value);
			return;
		}

		// The client supports this custom ingredient, so we send it as a custom ingredient.
		buf.writeVarInt(PACKET_MARKER);
		buf.writeIdentifier(customIngredient.getSerializer().getIdentifier());
		PacketCodec<RegistryByteBuf, CustomIngredient> packetCodec = (PacketCodec<RegistryByteBuf, CustomIngredient>) customIngredient.getSerializer().getPacketCodec();
		packetCodec.encode(buf, customIngredient);
	}

	static boolean shouldEncodeFallback(@Nullable CustomIngredient customIngredient) {
		if (customIngredient == null) {
			return true;
		}

		// Can be null if we're not writing a packet from the PacketEncoder; in that case, always write the full ingredient.
		// Chances are this is a mod's doing and the client has the Ingredient API with the relevant ingredients.
		Set<Identifier> supportedIngredients = CustomIngredientSync.CURRENT_SUPPORTED_INGREDIENTS.get();
		return supportedIngredients != null && !supportedIngredients.contains(customIngredient.getSerializer().getIdentifier());
	}
}
