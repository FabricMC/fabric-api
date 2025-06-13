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

package net.fabricmc.fabric.mixin.datagen.recipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;

@Mixin(SmithingTransformRecipeJsonBuilder.class)
abstract class SmithingTransformRecipeJsonBuilderMixin {
	@ModifyVariable(method = "offerTo(Lnet/minecraft/data/recipe/RecipeExporter;Lnet/minecraft/registry/RegistryKey;)V", at = @At("HEAD"), argsOnly = true)
	private RegistryKey<Recipe<?>> modifyRecipeKey(RegistryKey<Recipe<?>> recipeKey, RecipeExporter exporter) {
		return RegistryKey.of(recipeKey.getRegistryRef(), exporter.getRecipeIdentifier(recipeKey.getValue()));
	}
}
