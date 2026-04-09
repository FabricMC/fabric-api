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
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;

/**
 * Fabric-provided extensions for {@link net.minecraft.client.gui.screens.recipebook.RecipeBookComponent}.
 */
public interface FabricRecipeBookComponent {
	default SlotSelectTime getSlotSelectTime() {
		throw new AssertionError("Implemented via mixin");
	}

	/**
	 * Sets this book component's overlay.
	 * <p>This is used for modifying the overlay upon right-clicking a recipe item button.
	 *
	 * @apiNote This should most commonly be called within the init method of your recipe book overlay.
	 *
	 * @param overlay An overlay recipe component.
	 */
	default void setOverlay(OverlayRecipeComponent overlay) {
		throw new AssertionError("Implemented via mixin");
	}
}
