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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.component.ComponentChanges;
import net.minecraft.data.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.recipe.TransmuteRecipeResult;

import net.fabricmc.fabric.api.datagen.v1.smithing.FabricSmithingTransformRecipeJsonBuilder;

@Mixin(SmithingTransformRecipeJsonBuilder.class)
class SmithingTransformRecipeJsonBuilderMixin implements FabricSmithingTransformRecipeJsonBuilder {
	@Unique
	private int count = 1;

	@Unique
	private ComponentChanges componentChanges = ComponentChanges.EMPTY;

	@ModifyArg(method = "offerTo(Lnet/minecraft/data/recipe/RecipeExporter;Lnet/minecraft/registry/RegistryKey;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/SmithingTransformRecipe;<init>(Ljava/util/Optional;Lnet/minecraft/recipe/Ingredient;Ljava/util/Optional;Lnet/minecraft/recipe/TransmuteRecipeResult;)V"), index = 3)
	private TransmuteRecipeResult editResultParameters(TransmuteRecipeResult result) {
		return new TransmuteRecipeResult(result.itemEntry(), getCount(), getComponentChanges());
	}

	@Override
	public ComponentChanges getComponentChanges() {
		return componentChanges;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public void setComponentChanges(ComponentChanges componentChanges) {
		this.componentChanges = componentChanges;
	}

	@Override
	public void setCount(int count) {
		this.count = count;
	}
}
