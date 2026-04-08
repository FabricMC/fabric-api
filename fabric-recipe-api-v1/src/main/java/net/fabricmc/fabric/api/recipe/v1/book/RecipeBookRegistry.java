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

package net.fabricmc.fabric.api.recipe.v1.book;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.RecipeBookType;

import net.fabricmc.fabric.impl.recipe.book.RecipeBookImpl;

// TODO: Document me!
public class RecipeBookRegistry {
	public static void registerRecipeBookType(RecipeBookType type, Identifier id) {
		RecipeBookImpl.registerRecipeBookType(type, id);
	}

	/**
	 * Obtains a {@link RecipeBookType} that was registered by Fabric from an ID.
	 * @param id The id of the recipe book type.
	 * @see RecipeBookRegistry#registerRecipeBookType(RecipeBookType, Identifier)
	 */
	public static RecipeBookType recipeBookTypeFromId(Identifier id) {
		return RecipeBookImpl.fromId(id);
	}
}
