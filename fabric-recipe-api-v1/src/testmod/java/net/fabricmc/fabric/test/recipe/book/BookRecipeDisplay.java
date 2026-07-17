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

package net.fabricmc.fabric.test.recipe.book;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record BookRecipeDisplay(SlotDisplay ingredient, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {
	public static final MapCodec<BookRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			SlotDisplay.CODEC.fieldOf("ingredient").forGetter(BookRecipeDisplay::ingredient),
			SlotDisplay.CODEC.fieldOf("result").forGetter(BookRecipeDisplay::result),
			SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(BookRecipeDisplay::craftingStation)
	).apply(inst, BookRecipeDisplay::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, BookRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
			SlotDisplay.STREAM_CODEC, BookRecipeDisplay::ingredient,
			SlotDisplay.STREAM_CODEC, BookRecipeDisplay::result,
			SlotDisplay.STREAM_CODEC, BookRecipeDisplay::craftingStation,
			BookRecipeDisplay::new
	);
	public static final RecipeDisplay.Type<BookRecipeDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

	@Override
	public RecipeDisplay.Type<BookRecipeDisplay> type() {
		return TYPE;
	}

	@Override
	public boolean isEnabled(final FeatureFlagSet enabledFeatures) {
		return this.ingredient.isEnabled(enabledFeatures) && RecipeDisplay.super.isEnabled(enabledFeatures);
	}
}
