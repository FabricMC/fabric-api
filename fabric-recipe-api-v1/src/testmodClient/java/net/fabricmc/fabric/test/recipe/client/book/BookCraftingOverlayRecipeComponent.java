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

package net.fabricmc.fabric.test.recipe.client.book;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

import net.fabricmc.fabric.test.recipe.book.BookRecipeDisplay;

public class BookCraftingOverlayRecipeComponent extends OverlayRecipeComponent {
	public BookCraftingOverlayRecipeComponent(SlotSelectTime slotSelectTime) {
		super(slotSelectTime, false);
	}

	@Override
	@Nullable
	public OverlayRecipeButton getOverlayButton(int x, int y, RecipeDisplayEntry recipe, boolean isCraftable, ContextMap context) {
		return new BookCraftingOverlayRecipeButton(x, y, recipe.id(), recipe.display(), isCraftable, context);
	}

	public class BookCraftingOverlayRecipeButton extends OverlayRecipeButton {
		private static final Identifier ENABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/furnace_overlay");
		private static final Identifier HIGHLIGHTED_ENABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/furnace_overlay_highlighted");
		private static final Identifier DISABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/furnace_overlay_disabled");
		private static final Identifier HIGHLIGHTED_DISABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/furnace_overlay_disabled_highlighted");

		private final Font font;
		private final ItemStack result;

		public BookCraftingOverlayRecipeButton(int x, int y, RecipeDisplayId id, RecipeDisplay recipe, boolean isCraftable, final ContextMap context) {
			super(x, y, id, isCraftable, calculateIngredientsPositions(recipe, context));
			font = Minecraft.getInstance().font;
			result = recipe.result().resolveForFirstStack(context);
		}

		private static List<OverlayRecipeButton.Pos> calculateIngredientsPositions(final RecipeDisplay recipe, final ContextMap context) {
			if (recipe instanceof BookRecipeDisplay bookRecipeDisplay) {
				return List.of(
						createGridPos(1, 1, bookRecipeDisplay.ingredient().resolveForStacks(context))
				);
			}

			return Collections.emptyList();
		}

		@Override
		public void extractWidgetRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
			super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

			if (isHoveredOrFocused()) {
				graphics.setTooltipForNextFrame(font, result, mouseX, mouseY);
			}
		}

		@Override
		protected Identifier getSprite(boolean isCraftable) {
			if (isCraftable) {
				return this.isHoveredOrFocused() ? HIGHLIGHTED_ENABLED_SPRITE : ENABLED_SPRITE;
			}

			return this.isHoveredOrFocused() ? HIGHLIGHTED_DISABLED_SPRITE : DISABLED_SPRITE;
		}
	}
}
