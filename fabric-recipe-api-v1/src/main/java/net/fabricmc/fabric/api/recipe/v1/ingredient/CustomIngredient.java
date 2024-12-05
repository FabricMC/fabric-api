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

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.entry.RegistryEntry;

import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;

/**
 * Interface that modders can implement to create new behaviors for {@link Ingredient}s.
 *
 * <p>This is not directly implemented on vanilla {@link Ingredient}s, but conversions are possible:
 * <ul>
 *     <li>{@link #toVanilla()} converts a custom ingredient to a vanilla {@link Ingredient}.</li>
 *     <li>{@link FabricIngredient} can be used to check if a vanilla {@link Ingredient} is custom,
 *     and retrieve the custom ingredient in that case.</li>
 * </ul>
 *
 * <p>The format for custom ingredients is as follows:
 * <pre>{@code
 * {
 *     "fabric:type": "<identifier of the serializer>",
 *     // extra ingredient data, dependent on the serializer
 * }
 * }</pre>
 *
 * <p>Implementors of this interface are strongly encouraged to also implement
 * {@link Object#equals(Object)} and {@link Object#hashCode()}.
 *
 * @see CustomIngredientSerializer
 */
public interface CustomIngredient {
	/**
	 * Checks if a stack matches this ingredient.
	 * The stack <strong>must not</strong> be modified in any way.
	 *
	 * @param stack the stack to test
	 * @return {@code true} if the stack matches this ingredient, {@code false} otherwise
	 */
	boolean test(ItemStack stack);

	/**
	 * {@return the list of stacks that match this ingredient.}
	 *
	 * <p>The following guidelines should be followed for good compatibility:
	 * <ul>
	 *     <li>These stacks are generally used for display purposes, and need not be exhaustive or perfectly accurate.</li>
	 *     <li>An exception is ingredients that {@linkplain #requiresTesting() don't require testing},
	 *     for which it is important that the returned stacks correspond exactly to all the accepted {@link Item}s.</li>
	 *     <li>The ingredient should try to return at least one stack with each accepted {@link Item}.
	 *     This allows mods that inspect the ingredient to figure out which stacks it might accept.</li>
	 * </ul>
	 *
	 * <p>Note: no caching needs to be done by the implementation, this is already handled by the ingredient itself.
	 */
	Stream<RegistryEntry<Item>> getMatchingItems();

	/**
	 * Returns whether this ingredient always requires {@linkplain #test direct stack testing}.
	 *
	 * @return {@code false} if this ingredient ignores NBT data when matching stacks, {@code true} otherwise
	 * @see FabricIngredient#requiresTesting()
	 */
	boolean requiresTesting();

	/**
	 * {@return the serializer for this ingredient}
	 *
	 * <p>The serializer must have been registered using {@link CustomIngredientSerializer#register}.
	 */
	CustomIngredientSerializer<?> getSerializer();

	/**
	 * Returns a {@link SlotDisplay} representing this ingredient, this is synced to the client to display in the recipe book.
	 *
	 * @return a {@link SlotDisplay} instance.
	 */
	default SlotDisplay toDisplay() {
		// Matches the vanilla logic in Ingredient.toDisplay()
		return new SlotDisplay.CompositeSlotDisplay(getMatchingItems().map(Ingredient::createDisplayWithRemainder).toList());
	}

	/**
	 * {@return a new {@link Ingredient} behaving as defined by this custom ingredient}.
	 */
	@ApiStatus.NonExtendable
	default Ingredient toVanilla() {
		return new CustomIngredientImpl(this);
	}
}
