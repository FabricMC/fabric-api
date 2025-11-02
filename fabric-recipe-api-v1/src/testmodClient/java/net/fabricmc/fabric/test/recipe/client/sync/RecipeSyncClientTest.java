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

import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;

public class RecipeSyncClientTest implements ClientModInitializer {
	private static void compareWithLocalServer(MinecraftClient client, SynchronizedRecipes synchronizedRecipes) {
		if (client.getServer() == null) {
			return;
		}

		ServerRecipeManager recipeManager = client.getServer().getRecipeManager();

		for (RecipeEntry<?> recipeEntry : synchronizedRecipes.recipes()) {
			RecipeEntry<?> serverRecipeEntry = recipeManager.get(recipeEntry.id()).orElseThrow(() -> new IllegalStateException("Server is missing client recipe '" + recipeEntry.id().getValue() + "'!"));

			if (serverRecipeEntry.value().getSerializer() != recipeEntry.value().getSerializer()) {
				throw new IllegalStateException("Client and server have mismatched serializer for recipe '" + recipeEntry.id().getValue() + "'!");
			}

			if (serverRecipeEntry.value().getType() != recipeEntry.value().getType()) {
				throw new IllegalStateException("Client and server have mismatched type for recipe '" + recipeEntry.id().getValue() + "'!");
			}

			// This should be valid case when we include other mods, just invalid for vanilla sync.
			if (serverRecipeEntry.value().getClass() != recipeEntry.value().getClass()) {
				throw new IllegalStateException("Client and server have mismatched class for recipe '" + recipeEntry.id().getValue() + "'!");
			}
		}
	}

	@Override
	public void onInitializeClient() {
		ClientRecipeSynchronizedEvent.EVENT.register(RecipeSyncClientTest::compareWithLocalServer);
	}
}
