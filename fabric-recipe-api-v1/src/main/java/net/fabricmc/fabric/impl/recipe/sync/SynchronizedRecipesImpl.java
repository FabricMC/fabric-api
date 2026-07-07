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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.jspecify.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;

public record SynchronizedRecipesImpl(Multimap<RecipeType<?>, RecipeHolder<?>> byType, Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey) implements SynchronizedRecipes {
	public static final SynchronizedRecipesImpl EMPTY = of(List.of());

	public SynchronizedRecipesImpl(RecipeMap preparedRecipes) {
		this(indexByType(preparedRecipes.values()), indexByKey(preparedRecipes.values()));
	}

	public static SynchronizedRecipesImpl of(Iterable<RecipeHolder<?>> recipes) {
		List<RecipeHolder<?>> list = new ArrayList<>();
		recipes.forEach(list::add);
		return new SynchronizedRecipesImpl(indexByType(list), indexByKey(list));
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeType<T> type, I input, Level level) {
		return input.isEmpty() ? Stream.empty() : this.getAllOfType(type).stream().filter(recipe -> recipe.value().matches(input, level));
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeType<T> type) {
		return (Collection<RecipeHolder<T>>) (Object) this.byType.get(type);
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatch(RecipeType<T> type, I input, Level level) {
		return this.getAllMatches(type, input, level).findFirst();
	}

	@Override
	public @Nullable RecipeHolder<?> get(ResourceKey<Recipe<?>> key) {
		return this.byKey.get(key);
	}

	@Override
	public Collection<RecipeHolder<?>> recipes() {
		return this.byKey.values();
	}

	private static Multimap<RecipeType<?>, RecipeHolder<?>> indexByType(Iterable<RecipeHolder<?>> recipes) {
		ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> builder = ImmutableMultimap.builder();

		for (RecipeHolder<?> recipe : recipes) {
			builder.put(recipe.value().getType(), recipe);
		}

		return builder.build();
	}

	private static Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> indexByKey(Iterable<RecipeHolder<?>> recipes) {
		ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> builder = ImmutableMap.builder();

		for (RecipeHolder<?> recipe : recipes) {
			builder.put(recipe.id(), recipe);
		}

		return builder.build();
	}
}
