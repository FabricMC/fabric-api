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

/**
 * Helper methods related to registering contents to player recipe book data.
 *
 * @see net.minecraft.stats.RecipeBook
 * @see net.minecraft.stats.RecipeBookSettings
 */
public class RecipeBookRegistry {
	/**
	 * Registers a recipe book type within Fabric API, which adds the recipe book type to
	 * the {@link net.minecraft.stats.RecipeBookSettings}, allowing it to store the open in gui
	 * and filtering state for your book type.
	 *
	 * <p>Adding a {@link RecipeBookType} is handled using enum extending via a class tweaker of v2 or higher.
	 *
	 * @param type A recipe book type.
	 * @param id The ID to store the recipe book type's state under in player nbt.
	 *
	 * @throws IllegalArgumentException If a vanilla recipe book is attempted to be registered using this method.
	 */
	public static void registerRecipeBookType(RecipeBookType type, Identifier id) {
		RecipeBookImpl.registerRecipeBookType(type, id);
	}
}
