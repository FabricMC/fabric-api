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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.handler.PacketDecoderException;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Main packet used to send recipes to the client.
 */
public record RecipeSyncPayloadS2C(List<Entry> entries) implements CustomPayload {
	public static final PacketCodec<RegistryByteBuf, RecipeSyncPayloadS2C> CODEC = Entry.CODEC.collect(PacketCodecs.toList()).xmap(RecipeSyncPayloadS2C::new, RecipeSyncPayloadS2C::entries);

	public static final Id<RecipeSyncPayloadS2C> ID = new Id<>(Identifier.of("fabric", "recipe_sync"));

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public record Entry(RecipeSerializer<?> serializer, List<RecipeEntry<?>> recipes) {
		public static final PacketCodec<RegistryByteBuf, Entry> CODEC = PacketCodec.of(
				Entry::write,
				Entry::read
		);

		private static Entry read(RegistryByteBuf buf) {
			Identifier recipeSerializerId = buf.readIdentifier();
			RecipeSerializer<?> recipeSerializer = Registries.RECIPE_SERIALIZER.get(recipeSerializerId);

			if (recipeSerializer == null || !RecipeSyncImpl.isSynced(recipeSerializer)) {
				throw new PacketDecoderException("Tried syncing unsupported packet serializer '" + recipeSerializerId + "'!");
			}

			int count = buf.readVarInt();
			var list = new ArrayList<RecipeEntry<?>>();

			for (int i = 0; i < count; i++) {
				RegistryKey<Recipe<?>> id = buf.readRegistryKey(RegistryKeys.RECIPE);
				//noinspection deprecation
				Recipe<?> recipe = recipeSerializer.packetCodec().decode(buf);
				list.add(new RecipeEntry<>(id, recipe));
			}

			return new Entry(recipeSerializer, list);
		}

		private void write(RegistryByteBuf buf) {
			buf.writeIdentifier(Registries.RECIPE_SERIALIZER.getId(this.serializer));

			buf.writeVarInt(this.recipes.size());

			//noinspection unchecked,deprecation
			PacketCodec<RegistryByteBuf, Recipe<?>> serializer = ((PacketCodec<RegistryByteBuf, Recipe<?>>) this.serializer.packetCodec());

			for (RecipeEntry<?> recipe : this.recipes) {
				buf.writeRegistryKey(recipe.id());
				serializer.encode(buf, recipe.value());
			}
		}
	}
}
