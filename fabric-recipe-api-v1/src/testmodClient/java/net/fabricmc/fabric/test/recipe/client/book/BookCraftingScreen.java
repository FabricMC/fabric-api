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

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import net.fabricmc.fabric.test.recipe.book.BookCraftingMenu;
import net.fabricmc.fabric.test.recipe.book.RecipeBookTestContent;

public class BookCraftingScreen extends AbstractRecipeBookScreen<BookCraftingMenu> {
	private static final Identifier BOOK_CRAFTER_LOCATION = RecipeBookTestContent.id("textures/gui/container/book_crafter.png");

	public BookCraftingScreen(final BookCraftingMenu menu, final Inventory inventory, final Component title) {
		super(menu, new BookCraftingRecipeBookComponent(menu), inventory, title);
	}

	protected void init() {
		super.init();
		this.titleLabelX = 29;
	}

	protected ScreenPosition getRecipeBookButtonPosition() {
		return new ScreenPosition(this.leftPos + 5, this.height / 2 - 49);
	}

	public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		int xo = this.leftPos;
		int yo = (this.height - this.imageHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK_CRAFTER_LOCATION, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
	}
}
