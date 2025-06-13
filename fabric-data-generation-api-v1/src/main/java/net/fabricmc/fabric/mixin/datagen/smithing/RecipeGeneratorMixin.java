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

package net.fabricmc.fabric.mixin.datagen.smithing;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.component.ComponentChanges;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;

import net.fabricmc.fabric.api.datagen.v1.smithing.FabricRecipeGenerator;

@Mixin(RecipeGenerator.class)
abstract class RecipeGeneratorMixin implements FabricRecipeGenerator {
	@Override
	@Unique
	public SmithingTransformRecipeJsonBuilder createSmithingTransformRecipe(Ingredient template, Ingredient input, Ingredient addition, RecipeCategory category, Item result, int count, @Nullable ComponentChanges componentChanges) {
		SmithingTransformRecipeJsonBuilder builder = SmithingTransformRecipeJsonBuilder.create(template, input, addition, category, result);
		builder.setCount(count);
		builder.setComponentChanges(componentChanges != null ? componentChanges : ComponentChanges.EMPTY);
		return builder;
	}
}
