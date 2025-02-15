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

package net.fabricmc.fabric.test.resource.loader;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class BuiltinPackSortingTest {
	private static final String MOD_ID = "fabric-resource-loader-v0-testmod";

	private static RegistryKey<Recipe<?>> recipe(String path) {
		return RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, path));
	}

	@GameTest
	public void builtinPackSorting(TestContext context) {
		ServerRecipeManager manager = context.getWorld().getRecipeManager();

		if (manager.get(recipe("disabled_by_b")).isPresent()) {
			throw context.createError(Text.literal("disabled_by_b recipe should not have been loaded."));
		}

		if (manager.get(recipe("disabled_by_c")).isPresent()) {
			throw context.createError(Text.literal("disabled_by_c recipe should not have been loaded."));
		}

		if (manager.get(recipe("enabled_by_c")).isEmpty()) {
			throw context.createError(Text.literal("enabled_by_c recipe should have been loaded."));
		}

		long loadedRecipes = manager.values().stream().filter(r -> r.id().getValue().getNamespace().equals(MOD_ID)).count();
		context.assertTrue(loadedRecipes == 1, Text.literal("Unexpected loaded recipe count: " + loadedRecipes));
		context.complete();
	}
}
