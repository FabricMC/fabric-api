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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
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
		// Sorts the Crafting Recipe Book Categories based on Creative Mode Tab order.
		ClientRecipeBookEvents.MODIFY_CLIENT_RECIPE_LIST_ALL.register((category, recipes) -> {
			if (isCraftingBookCategories(category)) {
				Minecraft minecraft = Minecraft.getInstance();
				ClientLevel level = Objects.requireNonNull(minecraft.level);
				CreativeModeTab.ItemDisplayParameters parameters = new CreativeModeTab.ItemDisplayParameters(
						level.enabledFeatures(),
						false,
						level.registryAccess()
				);

				List<ItemStack> contents = new ArrayList<>();

				for (CreativeModeTab creativeTab : BuiltInRegistries.CREATIVE_MODE_TAB.stream()
						.sorted(Comparator.comparingInt(
								value ->
										(value.row() == CreativeModeTab.Row.TOP ? 0 : 5) + value.column()))
						.toList()) {
					CreativeModeTab.ItemDisplayBuilder displayList = new CreativeModeTab.ItemDisplayBuilder(creativeTab, parameters.enabledFeatures());
					creativeTab.displayItemsGenerator.accept(parameters, displayList);
					contents.addAll(displayList.tabContents);
				}

				ContextMap context = getContext();
				recipes.sort(Comparator.comparing(entries -> {
					Optional<ItemStack> stack = contents.stream()
							.filter(content ->
									ItemStack.isSameItemSameComponents(entries.getFirst().resultItems(context).getFirst(), content))
							.findFirst();

					return stack.map(contents::indexOf).orElse(Integer.MAX_VALUE);
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
		Minecraft minecraft = Minecraft.getInstance();
		return SlotDisplayContext.fromLevel(Objects.requireNonNull(minecraft.level));
	}

	private static boolean isCraftingBookCategories(RecipeBookCategory category) {
		return category == RecipeBookCategories.CRAFTING_BUILDING_BLOCKS
				|| category == RecipeBookCategories.CRAFTING_EQUIPMENT
				|| category == RecipeBookCategories.CRAFTING_REDSTONE
				|| category == RecipeBookCategories.CRAFTING_MISC;
	}
}
