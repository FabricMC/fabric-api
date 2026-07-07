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

package net.fabricmc.fabric.mixin.recipe.client.book;

import com.llamalad7.mixinextras.sugar.Local;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

import net.fabricmc.fabric.api.client.recipe.v1.book.FabricOverlayRecipeComponent;

@Mixin(OverlayRecipeComponent.class)
public class OverlayRecipeComponentMixin implements FabricOverlayRecipeComponent {
	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private <E> E modifyRecipeComponentButton(E value, @Local(argsOnly = true) ContextMap context, @Local(name = "canCraft") boolean canCraft, @Local(name = "recipe") RecipeDisplayEntry recipe, @Local(name = "x") int x, @Local(name = "y") int y) {
		OverlayRecipeComponent.OverlayRecipeButton button = getOverlayButton(x, y, recipe, context, canCraft);

		if (button != null) {
			// E will always be OverlayRecipeButton.
			return (E) button;
		}

		return value;
	}

	@Override
	public OverlayRecipeComponent.@Nullable OverlayRecipeButton getOverlayButton(int x, int y, RecipeDisplayEntry recipe, ContextMap context, boolean canCraft) {
		return null;
	}
}
