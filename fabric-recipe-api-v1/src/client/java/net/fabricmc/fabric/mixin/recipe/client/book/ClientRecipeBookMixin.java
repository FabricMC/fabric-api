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

package net.fabricmc.fabric.mixin.recipe.client.book;

import java.util.List;
import java.util.Map;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

import net.fabricmc.fabric.api.client.recipe.v1.book.ClientRecipeBookEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.recipe.book.client.ClientRecipeBookEventsImpl;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {
	@ModifyReturnValue(method = "categorizeAndGroupRecipes", at = @At("RETURN"))
	private static Map<RecipeBookCategory, List<List<RecipeDisplayEntry>>> modifyRecipeBookCategories(Map<RecipeBookCategory, List<List<RecipeDisplayEntry>>> result) {
		for (Map.Entry<RecipeBookCategory, List<List<RecipeDisplayEntry>>> entry : result.entrySet()) {
			RecipeBookCategory category = entry.getKey();
			List<List<RecipeDisplayEntry>> entries = entry.getValue();

			final Event<ClientRecipeBookEvents.ModifyClientRecipeList> modifyEntriesEvent = ClientRecipeBookEventsImpl.getModifyClientRecipeBookListEvent(category);

			if (modifyEntriesEvent != null) {
				modifyEntriesEvent.invoker().modifyClientRecipeBookList(entries);
			}

			ClientRecipeBookEvents.MODIFY_CLIENT_RECIPE_LIST_ALL.invoker().modifyClientRecipeBookList(category, entries);
		}
		return result;
	}
}
