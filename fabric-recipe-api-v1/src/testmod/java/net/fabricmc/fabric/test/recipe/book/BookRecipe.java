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

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public record BookRecipe(Recipe.CommonInfo commonInfo,
						BookCraftingBookInfo bookInfo,
						ItemStackTemplate result,
						Ingredient ingredient) implements Recipe<CraftingInput> {
	public static final MapCodec<BookRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			CommonInfo.MAP_CODEC.forGetter(BookRecipe::commonInfo),
			BookCraftingBookInfo.MAP_CODEC.forGetter(BookRecipe::bookInfo),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(BookRecipe::result),
			Ingredient.CODEC.fieldOf("ingredient").forGetter(BookRecipe::ingredient)
	).apply(inst, BookRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, BookRecipe> STREAM_CODEC = StreamCodec.composite(
			Recipe.CommonInfo.STREAM_CODEC, BookRecipe::commonInfo,
			BookCraftingBookInfo.STREAM_CODEC, BookRecipe::bookInfo,
			ItemStackTemplate.STREAM_CODEC, BookRecipe::result,
			Ingredient.CONTENTS_STREAM_CODEC, BookRecipe::ingredient,
			BookRecipe::new
	);

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return ingredient.test(input.getItem(0));
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return result.create();
	}

	@Override
	public boolean showNotification() {
		return commonInfo().showNotification();
	}

	@Override
	public String group() {
		return bookInfo().group();
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.create(ingredient);
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of(new BookRecipeDisplay(
				ingredient.display(),
				new SlotDisplay.ItemStackSlotDisplay(result),
				new SlotDisplay.ItemSlotDisplay(RecipeBookTestContent.BOOK_CRAFTER_ITEM)
		));
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return switch (this.bookInfo().category()) {
		case BOOK -> RecipeBookTestContent.BOOK_CATEGORY;
		case ENCHANTED_BOOK -> RecipeBookTestContent.ENCHANTED_BOOK_CATEGORY;
		case KNOWLEDGE_BOOK -> RecipeBookTestContent.KNOWLEDGE_BOOK_CATEGORY;
		};
	}

	@Override
	public RecipeSerializer<BookRecipe> getSerializer() {
		return RecipeBookTestContent.BOOK_RECIPE_SERIALIZER;
	}

	@Override
	public RecipeType<? extends Recipe<CraftingInput>> getType() {
		return RecipeBookTestContent.BOOK_RECIPE_TYPE;
	}

	public record BookCraftingBookInfo(BookCraftingBookCategory category, String group) implements Recipe.BookInfo<BookCraftingBookCategory> {
		public static final MapCodec<BookCraftingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(
				BookCraftingBookCategory.CODEC, BookCraftingBookCategory.BOOK, BookCraftingBookInfo::new
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, BookCraftingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(
				BookCraftingBookCategory.STREAM_CODEC, BookCraftingBookInfo::new
		);
	}
}
