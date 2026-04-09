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

package net.fabricmc.fabric.api.client.recipe.v1.book;

import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

/**
 * Fabric-provided extensions for {@link net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent}.
 */
public interface FabricOverlayRecipeComponent {
	/**
	 * Returns the overlay button for an individual recipe entry.
	 * <p>This is returned upon init, when the recipe collection is populated.
	 *
	 * @param x The x pos for the button.
	 * @param y The y pos for the button.
	 * @param recipe The associated recipe entry.
	 * @param context The slot display context map.
	 * @param canCraft Whether the player can craft the associated recipe.
	 *
	 * @return The recipe button for the recipe entry.
	 */
	default OverlayRecipeComponent.OverlayRecipeButton getOverlayButton(int x, int y, RecipeDisplayEntry recipe, ContextMap context, boolean canCraft) {
		throw new AssertionError("Implemented via mixin");
	}
}
