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

package net.fabricmc.fabric.test.recipe.client.book;

import java.util.Comparator;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.client.Minecraft;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.recipe.v1.book.ClientRecipeBookEvents;
import net.fabricmc.fabric.api.client.recipe.v1.book.ClientRecipeListHelper;
import net.fabricmc.fabric.test.recipe.book.RecipeBookTestContent;

public class RecipeBookTestClientEvents implements ClientModInitializer {


	@Override
	public void onInitializeClient() {
		ClientRecipeBookEvents.MODIFY_CLIENT_RECIPE_LIST_ALL.register((category, recipes) -> {
			if (isCraftingBookCategories(category)) {
				ContextMap context = getContext();
				recipes.sort(Comparator.comparing(entries -> {
					int index = CreativeModeTabs.searchTab()
							.getDisplayItems()
							.stream()
							.toList()
							.indexOf(entries.getFirst().resultItems(context).getFirst());
					if (index == -1) {
						return Integer.MAX_VALUE;
					}
					return index;
				}));
			}
		});
		ClientRecipeBookEvents.modifyClientRecipeList(RecipeBookTestContent.ENCHANTED_BOOK_CATEGORY).register(recipes -> {
			ContextMap context = getContext();
			ClientRecipeListHelper.sortRecipeGroups(recipes,
					Comparator.comparingInt(value ->
							value.resultItems(context)
									.stream()
									.flatMapToInt(stack -> {
										ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
										return enchantments.entrySet()
												.stream()
												.mapToInt(Object2IntMap.Entry::getIntValue);
									})
									.max()
									.orElse(0)
					)
			);
		});
	}

	private static ContextMap getContext() {
		Minecraft client = Minecraft.getInstance();
		assert client.level != null;
		return SlotDisplayContext.fromLevel(client.level);
	}

	private static boolean isCraftingBookCategories(RecipeBookCategory category) {
		return category == RecipeBookCategories.CRAFTING_BUILDING_BLOCKS
				|| category == RecipeBookCategories.CRAFTING_EQUIPMENT
				|| category == RecipeBookCategories.CRAFTING_REDSTONE
				|| category == RecipeBookCategories.CRAFTING_MISC;
	}
}
