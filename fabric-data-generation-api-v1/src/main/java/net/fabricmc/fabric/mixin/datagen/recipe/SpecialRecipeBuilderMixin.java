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

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

@Mixin(SpecialRecipeBuilder.class)
abstract class SpecialRecipeBuilderMixin {
	@ModifyVariable(method = "save(Lnet/minecraft/data/recipes/RecipeOutput;Lnet/minecraft/resources/ResourceKey;)V", at = @At("HEAD"), argsOnly = true)
	private ResourceKey<Recipe<?>> modifyRecipeKey(ResourceKey<Recipe<?>> recipeKey, RecipeOutput exporter) {
		return ResourceKey.create(recipeKey.registryKey(), exporter.getRecipeIdentifier(recipeKey.identifier()));
	}
}
