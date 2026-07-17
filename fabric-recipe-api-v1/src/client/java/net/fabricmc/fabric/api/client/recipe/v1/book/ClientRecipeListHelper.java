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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

public class ClientRecipeListHelper {
	/**
	 * A helper method for sorting all recipe group lists within the current recipes list.
	 *
	 * @param recipes The recipes contained within the current category.
	 * @param comparator A comparator for sorting the individual recipes within the group.
	 *
	 * @see ClientRecipeBookEvents.ModifyClientRecipeList
	 */
	public static void sortRecipeGroups(List<List<RecipeDisplayEntry>> recipes, Comparator<RecipeDisplayEntry> comparator) {
		recipes.forEach(entries -> {
			// Singular entries are always an immutable list, grouped entries are not.
			if (entries instanceof ArrayList<RecipeDisplayEntry>) {
				entries.sort(comparator);
			}
		});
	}
}
