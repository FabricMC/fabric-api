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
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class RecipeBookImpl implements ModInitializer {
	public static final Map<RecipeBookType, RecipeBookEntry> ENTRIES = new HashMap<>();
	private static final Codec<RecipeBookType> RECIPE_BOOK_ID_CODEC = Identifier.CODEC.flatXmap(id -> {
		RecipeBookType type = fromId(id);

		if (type == null) {
			return DataResult.error(() -> "Could not find registered recipe book type " + id);
		}

		return DataResult.success(type);
	}, type -> {
		if (!ENTRIES.containsKey(type)) {
			return DataResult.error(() -> "Type " + type + " was not registered");
		}

		return DataResult.success(ENTRIES.get(type).id());
	});
	// TODO: Rename me from fabric:settings to a more user friendly name.
	public static final MapCodec<Map<RecipeBookType, RecipeBookSettings.TypeSettings>> FABRIC_SETTINGS_MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.unboundedMap(RECIPE_BOOK_ID_CODEC, RecipeBookSettings.TypeSettings.CRAFTING_MAP_CODEC.codec())
					.fieldOf("fabric:settings")
					.forGetter(Function.identity())
	).apply(inst, Function.identity()));

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.clientboundPlay().register(ClientboundRecipeBookSyncPayload.TYPE, ClientboundRecipeBookSyncPayload.CODEC);
	}

	public static void registerRecipeBookType(RecipeBookType type, Identifier id) {
		if (isVanillaType(type)) { // Safe guard from vanilla recipe book types.
			throw new IllegalArgumentException("Unable to register non-modded recipe book type");
		}

		ENTRIES.put(type, new RecipeBookEntry(id));
	}

	@Nullable
	public static RecipeBookType fromId(Identifier id) {
		return ENTRIES.entrySet()
				.stream()
				.filter(entry -> entry.getValue().id().equals(id))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(null);
	}

	private static boolean isVanillaType(RecipeBookType type) {
		return type == RecipeBookType.CRAFTING || type == RecipeBookType.FURNACE || type == RecipeBookType.BLAST_FURNACE || type == RecipeBookType.SMOKER;
	}
}
