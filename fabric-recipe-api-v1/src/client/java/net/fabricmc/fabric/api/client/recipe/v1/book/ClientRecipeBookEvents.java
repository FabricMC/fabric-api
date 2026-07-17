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

import java.util.List;

import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.recipe.book.client.ClientRecipeBookEventsImpl;

/**
 * Holds events related to the {@link net.minecraft.client.ClientRecipeBook}.
 */
public class ClientRecipeBookEvents {
	public static final Event<ModifyClientRecipeListAll> MODIFY_CLIENT_RECIPE_LIST_ALL = EventFactory.createArrayBacked(ModifyClientRecipeListAll.class, callbacks -> (category, recipes) -> {
		for (ModifyClientRecipeListAll callback : callbacks) {
			callback.modifyClientRecipeBookList(category, recipes);
		}
	});

	private ClientRecipeBookEvents() {
	}

	/**
	 * Modifies the recipe book's recipe list for a specific category.
	 *
	 * <p>Operations such as re-ordering the recipe book's entries may be done using this.
	 *
	 * @param category The recipe book category to modify.
	 * @return The event.
	 */
	public static Event<ModifyClientRecipeList> modifyClientRecipeList(RecipeBookCategory category) {
		return ClientRecipeBookEventsImpl.getOrCreateModifyClientRecipeListEvent(category);
	}

	@FunctionalInterface
	public interface ModifyClientRecipeList {
		/**
		 * Modifies the entries of a recipe category.
		 *
		 * @param recipes A list of all individual recipe slots, with recipes of the
		 *                same group contained inside the inner list.
		 * @see ClientRecipeListHelper
		 */
		void modifyClientRecipeBookList(List<List<RecipeDisplayEntry>> recipes);
	}

	@FunctionalInterface
	public interface ModifyClientRecipeListAll {
		/**
		 * Modifies the entries of all recipe categories.
		 *
		 * @param category The category that is being modified.
		 * @param recipes  A list of all individual recipe slots, with recipes of the
		 *                 same group contained inside the inner list.
		 * @see ClientRecipeListHelper
		 */
		void modifyClientRecipeBookList(RecipeBookCategory category, List<List<RecipeDisplayEntry>> recipes);
	}
}
