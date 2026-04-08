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
 *
 * <p>Note: You should only be using these contained methods on your own recipe book components.
 *
 * <p>TODO: Figure out what to open to developers within {@link net.minecraft.client.gui.screens.recipebook.RecipeBookComponent} and dependent classes.
 * TODO: Implement these via mixin.
 */
public interface FabricRecipeBookComponent {
	default SlotSelectTime getSlotSelectTime() {
		throw new AssertionError("Implemented via mixin");
	}

	default void setOverlayRecipeComponent(OverlayRecipeComponent overlayComponent) {
		throw new AssertionError("Implemented via mixin");
	}
}
