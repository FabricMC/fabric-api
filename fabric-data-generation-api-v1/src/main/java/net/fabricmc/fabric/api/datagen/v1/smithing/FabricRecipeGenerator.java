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

package net.fabricmc.fabric.api.datagen.v1.smithing;

import org.jetbrains.annotations.Nullable;

import net.minecraft.component.ComponentChanges;
import net.minecraft.data.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;

/**
 * Fabric-provided extensions for {@link net.minecraft.data.recipe.RecipeGenerator}.
 *
 * <p>Adds recipe types that the vanilla class does not implement.
 */
public interface FabricRecipeGenerator {
	default SmithingTransformRecipeJsonBuilder createSmithingTransformRecipe(Ingredient template, Ingredient input, Ingredient addition, RecipeCategory category, Item result, int count, @Nullable ComponentChanges componentChanges) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default SmithingTransformRecipeJsonBuilder createSmithingTransformRecipe(Ingredient template, Ingredient input, Ingredient addition, RecipeCategory category, ItemStack result) {
		return createSmithingTransformRecipe(template, input, addition, category, result.getItem(), result.getCount(), result.getComponentChanges());
	}

	default SmithingTransformRecipeJsonBuilder createSmithingTransformRecipe(Item template, Item input, Item addition, RecipeCategory category, ItemStack result) {
		return createSmithingTransformRecipe(Ingredient.ofItem(template), Ingredient.ofItem(input), Ingredient.ofItem(addition), category, result);
	}
}
