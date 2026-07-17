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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class RecipeBookImpl implements ModInitializer {
	public static final Map<RecipeBookType, Identifier> TYPE_TO_ID = new HashMap<>();

	private static final Codec<RecipeBookType> REGISTERED_RECIPE_BOOK_ID_CODEC = Identifier.CODEC.flatXmap(id -> {
		RecipeBookType type = fromId(id);
		return DataResult.success(type);
	}, type -> {
		if (!TYPE_TO_ID.containsKey(type)) {
			return DataResult.error(() -> "Type " + type + " was not registered");
		}

		return DataResult.success(TYPE_TO_ID.get(type));
	});
	private static final Codec<Map<RecipeBookType, RecipeBookSettings.TypeSettings>> FABRIC_SETTINGS_CODEC = Codec.unboundedMap(REGISTERED_RECIPE_BOOK_ID_CODEC, RecipeBookSettings.TypeSettings.CRAFTING_MAP_CODEC.codec());

	public static final StreamCodec<ByteBuf, RecipeBookType> REGISTERED_RECIPE_BOOK_ID_STREAM_CODEC = Identifier.STREAM_CODEC.map(
			RecipeBookImpl::fromId,
			RecipeBookImpl.TYPE_TO_ID::get
	);
	public static final StreamCodec<ByteBuf, Map<RecipeBookType, RecipeBookSettings.TypeSettings>> FABRIC_SETTINGS_STREAM_CODEC = ByteBufCodecs.map(
			HashMap::new,
			REGISTERED_RECIPE_BOOK_ID_STREAM_CODEC,
			RecipeBookSettings.TypeSettings.STREAM_CODEC
	);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.clientboundPlay().register(ClientboundRecipeBookSyncPayload.TYPE, ClientboundRecipeBookSyncPayload.CODEC);
	}

	public static void registerRecipeBookType(RecipeBookType type, Identifier id) {
		if (isVanillaType(type)) { // Safe guard from vanilla recipe book types.
			throw new IllegalArgumentException("Unable to register non-modded recipe book type");
		}

		if (TYPE_TO_ID.containsValue(id)) {
			throw new IllegalArgumentException("Duplicate recipe book type " + id);
		}

		TYPE_TO_ID.put(type, id);
	}

	public static RecipeBookType fromId(Identifier id) {
		return TYPE_TO_ID.entrySet()
				.stream()
				.filter(entry -> entry.getValue().equals(id))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Could not find registered recipe book type " + id));
	}

	public static MapCodec<RecipeBookSettings> modifyRecipeBookSettingsCodec(MapCodec<RecipeBookSettings> originalCodec) {
		return RecordCodecBuilder.mapCodec(i -> i.group(
				originalCodec.forGetter(Function.identity()),
				FABRIC_SETTINGS_CODEC
						.optionalFieldOf("fabric:recipe_book_settings", Collections.emptyMap())
						.forGetter(RecipeBookImpl::filterTypeSettingsToNonDefault)
		).apply(i, (settings, fabricSettings) -> {
			((RecipeBookSettingsHooks) (Object) settings).fabric_getTypeSettings().putAll(fabricSettings);
			return settings;
		}));
	}

	// Avoids encoding empty data within the fabric:recipe_book_settings field.
	private static Map<RecipeBookType, RecipeBookSettings.TypeSettings> filterTypeSettingsToNonDefault(RecipeBookSettings recipeBookSettings) {
		Map<RecipeBookType, RecipeBookSettings.TypeSettings> fabricTypeSettings = new HashMap<>(((RecipeBookSettingsHooks) (Object) recipeBookSettings).fabric_getTypeSettings());
		fabricTypeSettings.values().removeIf(typeSettings -> !typeSettings.open() && !typeSettings.filtering());
		return Collections.unmodifiableMap(fabricTypeSettings);
	}

	private static boolean isVanillaType(RecipeBookType type) {
		return type == RecipeBookType.CRAFTING || type == RecipeBookType.FURNACE || type == RecipeBookType.BLAST_FURNACE || type == RecipeBookType.SMOKER;
	}
}
