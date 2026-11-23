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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.Holder;
import net.minecraft.data.recipes.BrewingProvider;
import net.minecraft.data.recipes.BrewingRecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

@Mixin(BrewingProvider.class)
abstract class BrewingProviderMixin {
	@Shadow
	@Final
	private RecipeOutput output;

	@WrapOperation(method = "buildTransformations", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/recipes/BrewingProvider;save(Lnet/minecraft/data/recipes/BrewingRecipeBuilder;)V"))
	private void preventDuplicatingDefaultTransformations(BrewingProvider instance, BrewingRecipeBuilder builder, Operation<Void> original, @Local(name = "transformation") BrewingProvider.ContainerTransformation transformation, @Local(name = "potion") Holder<Potion> potion) {
		Identifier potionId = potion.unwrapKey().orElseThrow().identifier();
		Identifier recipeId = output.getRecipeIdentifier(builder.defaultId().identifier());
		boolean otherPotion = !potionId.getNamespace().equals(recipeId.getNamespace());
		boolean defaultLingeringRecipe = transformation.container().equals(Items.SPLASH_POTION) && transformation.reagent().equals(Items.DRAGON_BREATH) && transformation.output().equals(Items.LINGERING_POTION);
		boolean defaultSplashRecipe = transformation.container().equals(Items.POTION) && transformation.reagent().equals(Items.GUNPOWDER) && transformation.output().equals(Items.SPLASH_POTION);

		// Skip standard splash and lingering conversions already supplied by vanilla or another mod.
		if ((otherPotion || potion.equals(Potions.WATER)) && (defaultSplashRecipe || defaultLingeringRecipe)) {
			return;
		}

		original.call(instance, builder);
	}
}
