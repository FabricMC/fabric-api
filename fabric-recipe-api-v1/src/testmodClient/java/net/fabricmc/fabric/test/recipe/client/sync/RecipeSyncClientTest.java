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

package net.fabricmc.fabric.test.recipe.client.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;

public class RecipeSyncClientTest implements ClientModInitializer {
	private static void compareWithLocalServer(Minecraft minecraft, SynchronizedRecipes synchronizedRecipes) {
		if (minecraft.getSingleplayerServer() == null) {
			return;
		}

		RecipeManager recipeManager = minecraft.getSingleplayerServer().getRecipeManager();

		for (RecipeHolder<?> recipeHolder : synchronizedRecipes.recipes()) {
			RecipeHolder<?> serverRecipeHolder = recipeManager.byKey(recipeHolder.id()).orElseThrow(() -> new IllegalStateException("Server is missing client recipe '" + recipeHolder.id().identifier() + "'!"));

			if (serverRecipeHolder.value().getSerializer() != recipeHolder.value().getSerializer()) {
				throw new IllegalStateException("Client and server have mismatched serializer for recipe '" + recipeHolder.id().identifier() + "'!");
			}

			if (serverRecipeHolder.value().getType() != recipeHolder.value().getType()) {
				throw new IllegalStateException("Client and server have mismatched type for recipe '" + recipeHolder.id().identifier() + "'!");
			}

			// This should be valid case when we include other mods, just invalid for vanilla sync.
			if (serverRecipeHolder.value().getClass() != recipeHolder.value().getClass()) {
				throw new IllegalStateException("Client and server have mismatched class for recipe '" + recipeHolder.id().identifier() + "'!");
			}
		}
	}

	@Override
	public void onInitializeClient() {
		ClientRecipeSynchronizedEvent.EVENT.register(RecipeSyncClientTest::compareWithLocalServer);
	}
}
