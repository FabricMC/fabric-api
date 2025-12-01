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

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class BuiltinPackSortingTest {
	private static ResourceKey<Recipe<?>> recipe(String path) {
		return ResourceKey.create(Registries.RECIPE, Constants.id(path));
	}

	@GameTest
	public void builtinPackSorting(GameTestHelper context) {
		RecipeManager manager = context.getLevel().recipeAccess();

		if (manager.byKey(recipe("disabled_by_b")).isPresent()) {
			throw context.assertionException(Component.literal("disabled_by_b recipe should not have been loaded."));
		}

		if (manager.byKey(recipe("disabled_by_c")).isPresent()) {
			throw context.assertionException(Component.literal("disabled_by_c recipe should not have been loaded."));
		}

		if (manager.byKey(recipe("enabled_by_c")).isEmpty()) {
			throw context.assertionException(Component.literal("enabled_by_c recipe should have been loaded."));
		}

		long loadedRecipes = manager.getRecipes().stream().filter(r -> r.id().identifier().getNamespace().equals(Constants.NAMESPACE)).count();
		context.assertTrue(loadedRecipes == 1, Component.literal("Unexpected loaded recipe count: " + loadedRecipes));
		context.succeed();
	}
}
