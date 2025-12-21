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

package net.fabricmc.fabric.impl.recipe.ingredient.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public class AllIngredient extends CombinedIngredient {
	private static final MapCodec<AllIngredient> CODEC = Ingredient.CODEC
			.listOf()
			.fieldOf("ingredients")
			.xmap(AllIngredient::new, AllIngredient::getIngredients);

	public static final CustomIngredientSerializer<AllIngredient> SERIALIZER =
			new Serializer<>(Identifier.fromNamespaceAndPath("fabric", "all"), AllIngredient::new, CODEC);

	public AllIngredient(List<Ingredient> ingredients) {
		super(ingredients);
	}

	@Override
	public boolean test(ItemStack stack) {
		for (Ingredient ingredient : ingredients) {
			if (!ingredient.test(stack)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public Stream<Holder<Item>> getMatchingItems() {
		// There's always at least one sub ingredient, so accessing ingredients[0] is safe.
		List<Holder<Item>> previewStacks = new ArrayList<>(ingredients.getFirst().items().toList());

		for (int i = 1; i < ingredients.size(); ++i) {
			Ingredient ing = ingredients.get(i);
			previewStacks.removeIf(entry -> !ing.test(entry.value().getDefaultInstance()));
		}

		return previewStacks.stream();
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}
}
