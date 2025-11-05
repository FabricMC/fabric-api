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

package net.fabricmc.fabric.api.datagen.v1.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

/**
 * Extend this class and implement {@link FabricRecipeProvider#createRecipeProvider(HolderLookup.Provider, RecipeOutput)}.
 *
 * <p>Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 */
public abstract class FabricRecipeProvider extends RecipeProvider.Runner {
	protected final FabricDataOutput output;
	private final CompletableFuture<HolderLookup.Provider> registriesFuture;

	public FabricRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
		this.output = output;
		this.registriesFuture = registriesFuture;
	}

	/**
	 * Implement this method and then use the range of methods in {@link RecipeProvider} or from one of the recipe json factories such as {@link ShapedRecipeBuilder} or {@link ShapelessRecipeBuilder}.
	 */
	@Override
	protected abstract RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter);

	/**
	 * Return a new exporter that applies the specified conditions to any recipe json provider it receives.
	 */
	protected RecipeOutput withConditions(RecipeOutput exporter, ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
		return new RecipeOutput() {
			@Override
			public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, @Nullable AdvancementHolder advancementEntry) {
				FabricDataGenHelper.addConditions(recipe, conditions);
				exporter.accept(key, recipe, advancementEntry);
			}

			@Override
			public Advancement.Builder advancement() {
				return exporter.advancement();
			}

			@Override
			public void includeRootAdvancement() {
			}

			@Override
			public Identifier getRecipeIdentifier(Identifier recipeId) {
				return exporter.getRecipeIdentifier(recipeId);
			}
		};
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer) {
		return registriesFuture.thenCompose((wrapperLookup -> {
			Set<Identifier> generatedRecipes = Sets.newHashSet();
			List<CompletableFuture<?>> list = new ArrayList<>();
			RecipeProvider recipeGenerator = createRecipeProvider(wrapperLookup, new RecipeOutput() {
				@Override
				public void accept(ResourceKey<Recipe<?>> recipeKey, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
					Identifier identifier = recipeKey.identifier();

					if (!generatedRecipes.add(identifier)) {
						throw new IllegalStateException("Duplicate recipe " + identifier);
					}

					RegistryOps<JsonElement> registryOps = wrapperLookup.createSerializationContext(JsonOps.INSTANCE);
					JsonObject recipeJson = Recipe.CODEC.encodeStart(registryOps, recipe).getOrThrow(IllegalStateException::new).getAsJsonObject();
					ResourceCondition[] conditions = FabricDataGenHelper.consumeConditions(recipe);
					FabricDataGenHelper.addConditions(recipeJson, conditions);

					final PackOutput.PathProvider recipesPathResolver = output.createRegistryElementsPathProvider(Registries.RECIPE);
					final PackOutput.PathProvider advancementsPathResolver = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);

					list.add(DataProvider.saveStable(writer, recipeJson, recipesPathResolver.json(identifier)));

					if (advancement != null) {
						JsonObject advancementJson = Advancement.CODEC.encodeStart(registryOps, advancement.value()).getOrThrow(IllegalStateException::new).getAsJsonObject();
						FabricDataGenHelper.addConditions(advancementJson, conditions);
						list.add(DataProvider.saveStable(writer, advancementJson, advancementsPathResolver.json(advancement.id())));
					}
				}

				@Override
				public Advancement.Builder advancement() {
					//noinspection removal
					return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
				}

				@Override
				public void includeRootAdvancement() {
				}

				@Override
				public Identifier getRecipeIdentifier(Identifier recipeId) {
					return FabricRecipeProvider.this.getRecipeIdentifier(recipeId);
				}
			});
			recipeGenerator.buildRecipes();
			return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
		}));
	}

	/**
	 * Override this method to change the recipe identifier. The default implementation normalizes the namespace to the mod ID.
	 */
	protected Identifier getRecipeIdentifier(Identifier identifier) {
		return Identifier.fromNamespaceAndPath(output.getModId(), identifier.getPath());
	}
}
