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

import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

import net.fabricmc.fabric.test.recipe.book.BookCraftingMenu;
import net.fabricmc.fabric.test.recipe.book.BookRecipeDisplay;
import net.fabricmc.fabric.test.recipe.book.RecipeBookTestContent;

public class BookCraftingRecipeBookComponent extends RecipeBookComponent<BookCraftingMenu> {
	private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(Identifier.withDefaultNamespace("recipe_book/filter_enabled"), Identifier.withDefaultNamespace("recipe_book/filter_disabled"), Identifier.withDefaultNamespace("recipe_book/filter_enabled_highlighted"), Identifier.withDefaultNamespace("recipe_book/filter_disabled_highlighted"));
	private static final Component ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");
	private static final List<RecipeBookComponent.TabInfo> TAB_INFOS = List.of(
			new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.FABRIC_RECIPE_API_V1_TESTMOD_BOOK_CRAFTING),
			new RecipeBookComponent.TabInfo(new ItemStack(Items.BOOK), Optional.empty(), RecipeBookTestContent.BOOK_CATEGORY),
			new RecipeBookComponent.TabInfo(new ItemStack(Items.ENCHANTED_BOOK), Optional.empty(), RecipeBookTestContent.ENCHANTED_BOOK_CATEGORY),
			new RecipeBookComponent.TabInfo(new ItemStack(Items.KNOWLEDGE_BOOK), Optional.empty(), RecipeBookTestContent.KNOWLEDGE_BOOK_CATEGORY)
	);

	public BookCraftingRecipeBookComponent(BookCraftingMenu menu) {
		super(menu, TAB_INFOS);
		setOverlay(new BookCraftingOverlayRecipeComponent(getSlotSelectTime()));
	}

	@Override
	protected boolean isCraftingSlot(Slot slot) {
		return this.menu.getResultSlot() == slot || this.menu.getInputSlot() == slot;
	}

	@Override
	protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipe, ContextMap context) {
		if (recipe instanceof BookRecipeDisplay recipeDisplay) {
			ghostSlots.setInput(this.menu.getInputSlot(), context, recipeDisplay.ingredient());
			ghostSlots.setResult(this.menu.getResultSlot(), context, recipe.result());
		}
	}

	@Override
	protected WidgetSprites getFilterButtonTextures() {
		return FILTER_BUTTON_SPRITES;
	}

	@Override
	protected Component getRecipeFilterName() {
		return ONLY_CRAFTABLES_TOOLTIP;
	}

	@Override
	protected void selectMatchingRecipes(RecipeCollection collection, StackedItemContents stackedContents) {
		collection.selectRecipes(stackedContents, recipeDisplay -> recipeDisplay instanceof BookRecipeDisplay);
	}
}
