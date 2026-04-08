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

package net.fabricmc.fabric.test.recipe.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;

import net.fabricmc.fabric.test.recipe.book.RecipeBookTestContent;

@Mixin(SearchRecipeBookCategory.class)
public enum SearchRecipeBookCategoryMixin {
	FABRIC_RECIPE_API_V1_TESTMOD_BOOK_CRAFTING(new RecipeBookCategory[]{RecipeBookTestContent.BOOK_CATEGORY, RecipeBookTestContent.ENCHANTED_BOOK_CATEGORY, RecipeBookTestContent.KNOWLEDGE_BOOK_CATEGORY});

	@Shadow
	SearchRecipeBookCategoryMixin(RecipeBookCategory[] includedCategories) {
	}
}
