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

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.world.level.ItemLike;

@Mixin(RecipeProvider.class)
abstract class RecipeProviderMixin {
	// The default `RecipeProvider` outputs all stonecutting recipes
	// in the `minecraft` namespace. Override this method to place
	// them in the output item’s namespace instead.
	@ModifyArg(method = "stonecutterResultFromBase(Lnet/minecraft/data/recipes/RecipeCategory;Lnet/minecraft/world/level/ItemLike;Lnet/minecraft/world/level/ItemLike;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/recipes/SingleItemRecipeBuilder;save(Lnet/minecraft/data/recipes/RecipeOutput;Ljava/lang/String;)V"), index = 1)
	private String adjustId(String path, @Local(name = "result", argsOnly = true) ItemLike result) {
		if ((Object) this instanceof VanillaRecipeProvider) {
			return path;
		}

		return BuiltInRegistries.ITEM.getKey(result.asItem()).getNamespace() + ":" + path;
	}
}
