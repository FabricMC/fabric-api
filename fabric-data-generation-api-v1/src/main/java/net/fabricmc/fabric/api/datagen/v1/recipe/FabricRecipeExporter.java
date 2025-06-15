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

package net.fabricmc.fabric.api.datagen.v1.recipe;

import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.util.Identifier;

/**
 * Injected to all {@link RecipeExporter} instances.
 */
public interface FabricRecipeExporter {
	/**
	 * Override this method to change the recipe identifier.
	 *
	 * <p>The default implementation returns the ID unchanged.
	 * Fabric API implementations automatically apply the corresponding method in
	 * {@link net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider FabricRecipeProvider}.
	 *
	 * @see net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider#getRecipeIdentifier(Identifier)
	 */
	default Identifier getRecipeIdentifier(Identifier recipeId) {
		return recipeId;
	}
}
