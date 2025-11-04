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

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;

public record SynchronizedRecipesImpl(RecipeMap preparedRecipes) implements SynchronizedRecipes {
	public static final SynchronizedRecipesImpl EMPTY = new SynchronizedRecipesImpl(RecipeMap.EMPTY);

	public static SynchronizedRecipesImpl of(Iterable<RecipeHolder<?>> recipes) {
		return new SynchronizedRecipesImpl(RecipeMap.create(recipes));
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeType<T> type, I input, Level world) {
		return this.preparedRecipes.getRecipesFor(type, input, world);
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeType<T> type) {
		return this.preparedRecipes.byType(type);
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatch(RecipeType<T> type, I input, Level world) {
		return this.preparedRecipes.getRecipesFor(type, input, world).findFirst();
	}

	@Override
	public @Nullable RecipeHolder<?> get(ResourceKey<Recipe<?>> key) {
		return this.preparedRecipes.byKey(key);
	}

	@Override
	public Collection<RecipeHolder<?>> recipes() {
		return this.preparedRecipes.values();
	}
}
