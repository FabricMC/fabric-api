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

package net.fabricmc.fabric.api.recipe.v1.ingredient;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Fabric-provided extensions for {@link Ingredient}.
 * This interface is automatically implemented on all ingredients via Mixin and interface injection.
 */
public interface FabricIngredient {
	/**
	 * {@return the backing {@link CustomIngredient} of this ingredient if it's custom, {@code null} otherwise}.
	 */
	@Nullable
	default CustomIngredient getCustomIngredient() {
		return null;
	}

	/**
	 * Returns whether this ingredient always requires {@linkplain Ingredient#test direct stack testing}.
	 * Vanilla ingredients will always return {@code false},
	 * and custom ingredients need to {@linkplain CustomIngredient#requiresTesting() provide this information}.
	 *
	 * <p>If {@code false}, {@linkplain Ingredient#test testing this ingredient} with an item stack must be equivalent to checking whether
	 * the item stack's item is included in the ingredient's {@linkplain Ingredient#items()}  list of matching stacks}.
	 * In that case, optimized matching logic can be used.
	 *
	 * <p>If {@code true}, the ingredient must always be tested using {@link Ingredient#test(ItemStack)}.
	 * Note that Fabric patches some vanilla systems such as shapeless recipes to account for this.
	 *
	 * @return {@code false} if this ingredient ignores NBT data when matching stacks, {@code true} otherwise
	 */
	default boolean requiresTesting() {
		return getCustomIngredient() != null && getCustomIngredient().requiresTesting();
	}
}
