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

package net.fabricmc.fabric.impl.recipe.sync.client;

import java.util.ArrayList;
import java.util.Comparator;

import net.minecraft.recipe.RecipeEntry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncPayloadS2C;
import net.fabricmc.fabric.impl.recipe.sync.SynchronizedRecipesImpl;

public class RecipeSyncImplClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(RecipeSyncPayloadS2C.ID, RecipeSyncImplClient::onRecipeSyncPacket);
	}

	private static void onRecipeSyncPacket(RecipeSyncPayloadS2C payload, ClientPlayNetworking.Context context) {
		SynchronizedRecipes recipes;

		if (!payload.entries().isEmpty()) {
			var collectedRecipes = new ArrayList<RecipeEntry<?>>();

			for (RecipeSyncPayloadS2C.Entry entry : payload.entries()) {
				collectedRecipes.addAll(entry.recipes());
			}

			// Sort values by id to match ordering with server ones.
			collectedRecipes.sort(Comparator.comparing(entry -> entry.id().getValue()));
			recipes = SynchronizedRecipesImpl.of(collectedRecipes);
		} else {
			recipes = SynchronizedRecipesImpl.EMPTY;
		}

		((SynchronizedClientRecipesSetter) context.player().networkHandler.getRecipeManager()).fabric_setSynchronizedClientRecipes(recipes);
		ClientRecipeSynchronizedEvent.EVENT.invoker().onRecipesSynchronized(context.client(), recipes);
	}
}
