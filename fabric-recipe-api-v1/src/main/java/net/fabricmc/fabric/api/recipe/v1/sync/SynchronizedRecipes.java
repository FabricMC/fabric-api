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

package net.fabricmc.fabric.api.recipe.v1.sync;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * This class provides access to synchronized recipes on the client.
 *
 * <p>You can access SynchronizedClientRecipes by calling getSynchronizedRecipes
 * method on {@link net.minecraft.world.item.crafting.RecipeAccess}
 *
 * <p>See {@link RecipeSynchronization}.
 */
@ApiStatus.NonExtendable
public interface SynchronizedRecipes {
	/**
	 * Creates a stream of all recipe entries of the given {@code type} that match the
	 * given {@code input} and {@code world}.
	 *
	 * <p>If {@code input.isEmpty()} returns true, the returned stream will be always empty.
	 *
	 * @return the stream of matching recipes
	 */
	<I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeType<T> type, I input, Level world);

	/**
	 * @return the collection of recipe entries of given type
	 */
	<I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeType<T> type);

	/**
	 * Finds a first recipe entry (or @{code recipe}, if it matches and isn't null) of the given {@code type} that matches the
	 * given {@code input} and {@code world}.
	 *
	 * @return the optional containing matching recipe entry or empty
	 */
	default <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatch(RecipeType<T> type, I input, Level world, @Nullable ResourceKey<Recipe<?>> recipe) {
		RecipeHolder<T> recipeEntry = recipe != null ? this.get(type, recipe) : null;
		return this.getFirstMatch(type, input, world, recipeEntry);
	}

	/**
	 * Finds a first recipe entry (or @{code recipe}, if it matches and isn't null) of the given {@code type} that matches the
	 * given {@code input} and {@code world}.
	 *
	 * @return the optional containing matching recipe entry or empty
	 */
	default <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatch(RecipeType<T> type, I input, Level world, @Nullable RecipeHolder<T> recipe) {
		return recipe != null && recipe.value().matches(input, world) ? Optional.of(recipe) : this.getFirstMatch(type, input, world);
	}

	/**
	 * Finds a first recipe entry of the given {@code type} that matches the
	 * given {@code input} and {@code world}.
	 *
	 * @return the optional containing matching recipe entry or empty
	 */
	<I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatch(RecipeType<T> type, I input, Level world);

	/**
	 * @return recipe with matching {@code key} or null if not present
	 */
	@Nullable
	RecipeHolder<?> get(ResourceKey<Recipe<?>> key);

	/**
	 * @return recipe with matching {@code key} of type {@code type} or null if not present
	 */
	@Nullable
	default <T extends Recipe<?>> RecipeHolder<T> get(RecipeType<T> type, ResourceKey<Recipe<?>> key) {
		RecipeHolder<?> recipeEntry = this.get(key);
		//noinspection unchecked
		return recipeEntry != null && recipeEntry.value().getType().equals(type) ? (RecipeHolder<T>) recipeEntry : null;
	}

	/**
	 * @return collection of all synchronized recipe types
	 */
	Collection<RecipeHolder<?>> recipes();
}
