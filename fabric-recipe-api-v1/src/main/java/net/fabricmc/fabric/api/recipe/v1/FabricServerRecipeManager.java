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

package net.fabricmc.fabric.api.recipe.v1;

import java.util.Collection;
import java.util.stream.Stream;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.world.World;

/**
 * General-purpose Fabric-provided extensions for {@link ServerRecipeManager} class.
 */
public interface FabricServerRecipeManager extends FabricRecipeManager {
	/**
	 * Creates a stream of all recipe entries of the given {@code type} that match the
	 * given {@code input} and {@code world}.
	 *
	 * <p>If {@code input.isEmpty()} returns true, the returned stream will be always empty.
	 *
	 * @return the stream of matching recipes
	 */
	default <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeEntry<T>> getAllMatches(RecipeType<T> type, I input, World world) {
		throw new AssertionError("Implemented in Mixin");
	}

	/**
	 * @return the collection of recipe entries of given type
	 */
	default <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(RecipeType<T> type) {
		throw new AssertionError("Implemented in Mixin");
	}
}
