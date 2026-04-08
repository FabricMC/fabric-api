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

package net.fabricmc.fabric.impl.recipe.book;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;

import net.fabricmc.fabric.api.recipe.v1.book.RecipeBookRegistry;

public record ClientboundRecipeBookSyncPayload(Map<RecipeBookType, RecipeBookSettings.TypeSettings> values) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRecipeBookSyncPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.map(
					HashMap::new,
					Identifier.STREAM_CODEC.map(
							RecipeBookRegistry::recipeBookTypeFromId,
							recipeType -> RecipeBookImpl.ENTRIES.get(recipeType).id()
					),
					RecipeBookSettings.TypeSettings.STREAM_CODEC
			),
			ClientboundRecipeBookSyncPayload::values,
			ClientboundRecipeBookSyncPayload::new
	);
	public static final Type<ClientboundRecipeBookSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("fabric", "recipe_book_sync"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
