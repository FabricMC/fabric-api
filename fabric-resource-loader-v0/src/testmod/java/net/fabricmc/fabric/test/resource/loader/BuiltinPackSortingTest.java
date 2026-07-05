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

import net.minecraft.recipe.RecipeManager;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public class BuiltinPackSortingTest {
	private static final String MOD_ID = "fabric-resource-loader-v0-testmod";

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void builtinPackSorting(TestContext context) {
		RecipeManager manager = context.getWorld().getRecipeManager();

		if (manager.get(Identifier.of(MOD_ID, "disabled_by_b")).isPresent()) {
			throw new GameTestException("disabled_by_b recipe should not have been loaded.");
		}

		if (manager.get(Identifier.of(MOD_ID, "disabled_by_c")).isPresent()) {
			throw new GameTestException("disabled_by_c recipe should not have been loaded.");
		}

		if (manager.get(Identifier.of(MOD_ID, "enabled_by_c")).isEmpty()) {
			throw new GameTestException("enabled_by_c recipe should have been loaded.");
		}

		long loadedRecipes = manager.values().stream().filter(r -> r.id().getNamespace().equals(MOD_ID)).count();
		context.assertTrue(loadedRecipes == 1, "Unexpected loaded recipe count: " + loadedRecipes);
		context.complete();
	}
}
