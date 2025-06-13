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
import org.jetbrains.annotations.Nullable;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

/**
 * Extend this class and implement {@link FabricRecipeProvider#getRecipeGenerator}.
 *
 * <p>Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 */
public abstract class FabricRecipeProvider extends RecipeGenerator.RecipeProvider {
	protected final FabricDataOutput output;
	private final CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture;

	public FabricRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, registriesFuture);
		this.output = output;
		this.registriesFuture = registriesFuture;
	}

	/**
	 * Implement this method and then use the range of methods in {@link RecipeGenerator} or from one of the recipe json factories such as {@link ShapedRecipeJsonBuilder} or {@link ShapelessRecipeJsonBuilder}.
	 */
	@Override
	protected abstract RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter);

	/**
	 * Return a new exporter that applies the specified conditions to any recipe json provider it receives.
	 */
	protected RecipeExporter withConditions(RecipeExporter exporter, ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
		return new RecipeExporter() {
			@Override
			public void accept(RegistryKey<Recipe<?>> key, Recipe<?> recipe, @Nullable AdvancementEntry advancementEntry) {
				FabricDataGenHelper.addConditions(recipe, conditions);
				exporter.accept(key, recipe, advancementEntry);
			}

			@Override
			public Advancement.Builder getAdvancementBuilder() {
				return exporter.getAdvancementBuilder();
			}

			@Override
			public void addRootAdvancement() {
			}

			@Override
			public Identifier getRecipeIdentifier(Identifier recipeId) {
				return exporter.getRecipeIdentifier(recipeId);
			}
		};
	}

	@Override
	public CompletableFuture<?> run(DataWriter writer) {
		return registriesFuture.thenCompose((wrapperLookup -> {
			Set<Identifier> generatedRecipes = Sets.newHashSet();
			List<CompletableFuture<?>> list = new ArrayList<>();
			RecipeGenerator recipeGenerator = getRecipeGenerator(wrapperLookup, new RecipeExporter() {
				@Override
				public void accept(RegistryKey<Recipe<?>> recipeKey, Recipe<?> recipe, @Nullable AdvancementEntry advancement) {
					Identifier identifier = recipeKey.getValue();

					if (!generatedRecipes.add(identifier)) {
						throw new IllegalStateException("Duplicate recipe " + identifier);
					}

					RegistryOps<JsonElement> registryOps = wrapperLookup.getOps(JsonOps.INSTANCE);
					JsonObject recipeJson = Recipe.CODEC.encodeStart(registryOps, recipe).getOrThrow(IllegalStateException::new).getAsJsonObject();
					ResourceCondition[] conditions = FabricDataGenHelper.consumeConditions(recipe);
					FabricDataGenHelper.addConditions(recipeJson, conditions);

					final DataOutput.PathResolver recipesPathResolver = output.getResolver(RegistryKeys.RECIPE);
					final DataOutput.PathResolver advancementsPathResolver = output.getResolver(RegistryKeys.ADVANCEMENT);

					list.add(DataProvider.writeToPath(writer, recipeJson, recipesPathResolver.resolveJson(identifier)));

					if (advancement != null) {
						JsonObject advancementJson = Advancement.CODEC.encodeStart(registryOps, advancement.value()).getOrThrow(IllegalStateException::new).getAsJsonObject();
						FabricDataGenHelper.addConditions(advancementJson, conditions);
						list.add(DataProvider.writeToPath(writer, advancementJson, advancementsPathResolver.resolveJson(getRecipeIdentifier(advancement.id()))));
					}
				}

				@Override
				public Advancement.Builder getAdvancementBuilder() {
					//noinspection removal
					return Advancement.Builder.createUntelemetered().parent(CraftingRecipeJsonBuilder.ROOT);
				}

				@Override
				public void addRootAdvancement() {
				}

				@Override
				public Identifier getRecipeIdentifier(Identifier recipeId) {
					return FabricRecipeProvider.this.getRecipeIdentifier(recipeId);
				}
			});
			recipeGenerator.generate();
			return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
		}));
	}

	/**
	 * Override this method to change the recipe identifier. The default implementation normalizes the namespace to the mod ID.
	 */
	protected Identifier getRecipeIdentifier(Identifier identifier) {
		return Identifier.of(output.getModId(), identifier.getPath());
	}
}
