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

package net.fabricmc.fabric.impl.recipe.sync;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;

public record SynchronizedRecipesImpl(PreparedRecipes preparedRecipes) implements SynchronizedRecipes {
	public static final SynchronizedRecipesImpl EMPTY = new SynchronizedRecipesImpl(PreparedRecipes.EMPTY);

	public static SynchronizedRecipesImpl of(Iterable<RecipeEntry<?>> recipes) {
		return new SynchronizedRecipesImpl(PreparedRecipes.of(recipes));
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeEntry<T>> getAllMatches(RecipeType<T> type, I input, World world) {
		return this.preparedRecipes.find(type, input, world);
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(RecipeType<T> type) {
		return this.preparedRecipes.getAll(type);
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world) {
		return this.preparedRecipes.find(type, input, world).findFirst();
	}

	@Override
	public @Nullable RecipeEntry<?> get(RegistryKey<Recipe<?>> key) {
		return this.preparedRecipes.get(key);
	}

	@Override
	public Collection<RecipeEntry<?>> recipes() {
		return this.preparedRecipes.recipes();
	}
}
