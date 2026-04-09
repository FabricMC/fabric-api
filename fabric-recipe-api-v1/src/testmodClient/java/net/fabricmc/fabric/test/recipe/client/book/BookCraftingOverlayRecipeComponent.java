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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import net.fabricmc.fabric.test.recipe.book.BookRecipeDisplay;
import net.fabricmc.fabric.test.recipe.book.RecipeBookTestContent;
import net.fabricmc.fabric.test.recipe.client.mixin.OverlayRecipeComponentAccessor;
import net.fabricmc.fabric.test.recipe.client.mixin.OverlayRecipeComponentOverlayRecipeButtonAccessor;

public class BookCraftingOverlayRecipeComponent extends OverlayRecipeComponent {
	public BookCraftingOverlayRecipeComponent(SlotSelectTime slotSelectTime) {
		super(slotSelectTime, false);
	}

	@Override
	public OverlayRecipeButton createOverlayButton(int x, int y, RecipeDisplayEntry recipe, ContextMap context, boolean canCraft) {
		return new BookCraftingOverlayRecipeButton(x, y, recipe.id(), recipe.display(), canCraft, context);
	}

	public class BookCraftingOverlayRecipeButton extends OverlayRecipeButton {
		private static final Identifier ENABLED_SPRITE = RecipeBookTestContent.id("recipe_book/book_crafting_overlay");
		private static final Identifier HIGHLIGHTED_ENABLED_SPRITE = RecipeBookTestContent.id("recipe_book/book_crafting_overlay_highlighted");
		private static final Identifier DISABLED_SPRITE = RecipeBookTestContent.id("recipe_book/book_crafting_overlay_disabled");
		private static final Identifier HIGHLIGHTED_DISABLED_SPRITE = RecipeBookTestContent.id("recipe_book/book_crafting_overlay_disabled_highlighted");

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
						new OverlayRecipeButton.Pos(0, 0, bookRecipeDisplay.ingredient()
								.resolve(context, SlotDisplay.ItemStackContentsFactory.INSTANCE)
								.toList()
						)
				);
			}
			return Collections.emptyList();
		}

		@Override
		public void extractWidgetRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getSprite(((OverlayRecipeComponentOverlayRecipeButtonAccessor) this).getIsCraftable()), this.getX(), this.getY(), this.width, this.height);
			float gridPosX = (float) (this.getX() + 2);
			float gridPosY = (float) (this.getY() + 2);

			if (((OverlayRecipeComponentOverlayRecipeButtonAccessor) this).getSlots().isEmpty())
				return;

			Pos pos = ((OverlayRecipeComponentOverlayRecipeButtonAccessor) this).getSlots().getFirst();

			graphics.pose().pushMatrix();
			graphics.pose().translate(gridPosX + (float) pos.x(), gridPosY + (float) pos.y());
			graphics.pose().translate(2.0F, 2.0F);
			graphics.item(pos.selectIngredient(((OverlayRecipeComponentAccessor) BookCraftingOverlayRecipeComponent.this).getSlotSelectTime().currentIndex()), 0, 0);
			graphics.pose().popMatrix();

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
