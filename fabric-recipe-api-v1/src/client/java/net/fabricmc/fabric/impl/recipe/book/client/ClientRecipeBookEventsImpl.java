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

package net.fabricmc.fabric.impl.recipe.book.client;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.crafting.RecipeBookCategory;

import net.fabricmc.fabric.api.client.recipe.v1.book.ClientRecipeBookEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class ClientRecipeBookEventsImpl {
	private static final Map<RecipeBookCategory, Event<ClientRecipeBookEvents.ModifyClientRecipeList>> GROUP_RECIPE_ORDER_EVENT_MAP = new HashMap<>();

	public static Event<ClientRecipeBookEvents.ModifyClientRecipeList> getOrCreateModifyClientRecipeListEvent(RecipeBookCategory category) {
		return GROUP_RECIPE_ORDER_EVENT_MAP.computeIfAbsent(category, (c ->
				EventFactory.createArrayBacked(ClientRecipeBookEvents.ModifyClientRecipeList.class, callbacks -> (entries) -> {
					for (ClientRecipeBookEvents.ModifyClientRecipeList callback : callbacks) {
						callback.modifyClientRecipeBookList(entries);
					}
				}))
		);
	}

	@Nullable
	public static Event<ClientRecipeBookEvents.ModifyClientRecipeList> getModifyClientRecipeBookListEvent(RecipeBookCategory category) {
		return GROUP_RECIPE_ORDER_EVENT_MAP.get(category);
	}
}
