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

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;

/// Interface that modders can implement to create new behaviors for [Ingredient]s.
///
/// This is not directly implemented on vanilla [Ingredient]s, but conversions are possible:
///
///   - [#toVanilla()] converts a custom ingredient to a vanilla [Ingredient].
///   - [FabricIngredient] can be used to check if a vanilla [Ingredient] is custom,
///     and retrieve the custom ingredient in that case.
///
///
/// The format for custom ingredients is as follows:
///
/// ```json
/// {
/// 	"fabric:type": "<identifier of the serializer>",
/// 	// extra ingredient data, dependent on the serializer
/// }
/// ```
///
/// Implementors of this interface are strongly encouraged to also implement
/// [Object#equals(Object)] and [Object#hashCode()].
///
/// @see CustomIngredientSerializer
public interface CustomIngredient {
	/// Checks if a stack matches this ingredient.
	/// The stack **must not** be modified in any way.
	///
	/// @param stack the stack to test
	/// @return `true` if the stack matches this ingredient, `false` otherwise
	boolean test(ItemStack stack);

	/// {@return the list of stacks that match this ingredient.}
	///
	/// The following guidelines should be followed for good compatibility:
	///
	///   - These stacks are generally used for display purposes, and need not be exhaustive or perfectly accurate.
	///   - An exception is ingredients that {@linkplain #requiresTesting() don't require testing},
	///     for which it is important that the returned stacks correspond exactly to all the accepted [Item]s.
	///   - The ingredient should try to return at least one stack with each accepted [Item].
	///     This allows mods that inspect the ingredient to figure out which stacks it might accept.
	///
	///
	/// Note: no caching needs to be done by the implementation, this is already handled by the ingredient itself.
	Stream<Holder<Item>> items();

	/// Returns whether this ingredient always requires {@linkplain #test direct stack testing}.
	///
	/// @return `false` if this ingredient ignores NBT data when matching stacks, `true` otherwise
	/// @see FabricIngredient#requiresTesting()
	boolean requiresTesting();

	/// {@return the serializer for this ingredient}
	///
	/// The serializer must have been registered using [CustomIngredientSerializer#register].
	CustomIngredientSerializer<?> getSerializer();

	/// Returns a [SlotDisplay] representing this ingredient, this is synced to the client to display in the recipe book.
	///
	/// @return a [SlotDisplay] instance.
	default SlotDisplay display() {
		// Matches the vanilla logic in Ingredient.display()
		return new SlotDisplay.Composite(items().map(Ingredient::displayForSingleItem).toList());
	}

	/// {@return a new {@link Ingredient } behaving as defined by this custom ingredient}.
	@ApiStatus.NonExtendable
	default Ingredient toVanilla() {
		return new CustomIngredientImpl(this);
	}
}
