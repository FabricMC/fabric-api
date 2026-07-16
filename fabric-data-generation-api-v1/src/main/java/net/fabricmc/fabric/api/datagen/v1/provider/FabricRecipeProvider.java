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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BootstrapRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.recipe.FabricRecipeOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

/**
 * Extend this class and implement {@link FabricRecipeProvider#createRecipeProvider(HolderLookup.Provider, BootstrapContext, BootstrapContext)}.
 *
 * <p>Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 */
public abstract class FabricRecipeProvider implements DataProvider {
	protected final FabricPackOutput output;
	private final CompletableFuture<HolderLookup.Provider> registriesFuture;

	public FabricRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		this.output = output;
		this.registriesFuture = registriesFuture;
	}

	/**
	 * Implement this method and then use the range of methods in {@link RecipeProvider} or from one of the recipe json factories such as {@link ShapedRecipeBuilder} or {@link ShapelessRecipeBuilder}.
	 */
	protected abstract RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements);

	/**
	 * Return a new exporter that applies the specified conditions to any recipe json provider it receives.
	 */
	protected RecipeOutput withConditions(RecipeOutput output, ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
		return new RecipeOutput() {
			@Override
			public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder) {
				FabricDataGenHelper.addConditions(recipe, conditions);

				if (advancementHolder != null) {
					FabricDataGenHelper.addConditions(advancementHolder.value(), conditions);
				}

				output.accept(key, recipe, advancementHolder);
			}

			@Override
			public Advancement.Builder advancement() {
				return output.advancement();
			}

			public void includeRootAdvancement() {
			}

			public Identifier getRecipeIdentifier(Identifier recipeId) {
				return output.getRecipeIdentifier(recipeId);
			}

			@Override
			public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
				return output.lookup(key);
			}

			@Override
			public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
				return output.listContextElements(key);
			}
		};
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return registriesFuture.thenCompose((registries -> {
			List<CompletableFuture<?>> list = new ArrayList<>();
			FabricBootstrapContext<Recipe<?>> recipes = new RecipeBootstrapContext(registries, this::getRecipeIdentifier);
			FabricBootstrapContext<Advancement> advancements = new FabricBootstrapContext<>(registries, Registries.ADVANCEMENT);
			RecipeProvider recipeProvider = createRecipeProvider(registries, recipes, advancements);
			recipeProvider.buildRecipes();

			RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, new RegistryOps.RegistryInfoLookup() {
				@Override
				@SuppressWarnings("unchecked")
				public <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
					if (key.equals(Registries.RECIPE)) {
						return Optional.of((HolderGetter<T>) recipes.entryLookup);
					} else if (key.equals(Registries.ADVANCEMENT)) {
						return Optional.of((HolderGetter<T>) advancements.entryLookup);
					}

					return registries.lookup(key).map(lookup -> lookup);
				}
			});
			PackOutput.PathProvider recipesPathResolver = FabricRecipeProvider.this.output.createRegistryElementsPathProvider(Registries.RECIPE);
			PackOutput.PathProvider advancementsPathResolver = FabricRecipeProvider.this.output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);

			recipes.entries().forEach((recipeKey, recipe) -> {
				JsonObject recipeJson = Recipe.DIRECT_CODEC.encodeStart(registryOps, recipe).getOrThrow(IllegalStateException::new).getAsJsonObject();
				ResourceCondition[] conditions = FabricDataGenHelper.consumeConditions(recipe);
				FabricDataGenHelper.addConditions(recipeJson, conditions);
				list.add(DataProvider.saveStable(output, recipeJson, recipesPathResolver.json(recipeKey.identifier())));
			});
			advancements.entries().forEach((advancementKey, advancement) -> {
				JsonObject advancementJson = Advancement.CODEC.encodeStart(registryOps, advancement).getOrThrow(IllegalStateException::new).getAsJsonObject();
				ResourceCondition[] conditions = FabricDataGenHelper.consumeConditions(advancement);
				FabricDataGenHelper.addConditions(advancementJson, conditions);
				list.add(DataProvider.saveStable(output, advancementJson, advancementsPathResolver.json(advancementKey.identifier())));
			});

			return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
		}));
	}

	private static class FabricBootstrapContext<T> implements BootstrapContext<T> {
		private final HolderLookup.Provider registries;
		private final ResourceKey<? extends Registry<T>> registryKey;
		private final Map<ResourceKey<T>, T> entries = new LinkedHashMap<>();
		private final BootstrapRegistry<T> entryLookup;

		private FabricBootstrapContext(HolderLookup.Provider registries, ResourceKey<? extends Registry<T>> registryKey) {
			this.registries = registries;
			this.registryKey = registryKey;
			this.entryLookup = new BootstrapRegistry<>(registryKey, Lifecycle.stable());
		}

		@Override
		public Holder.Reference<T> register(ResourceKey<T> key, T value) {
			if (entries.putIfAbsent(key, value) != null) {
				throw new IllegalStateException("Duplicate registration for " + key);
			}

			return entryLookup.getOrThrow(key);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
			if (key.equals(registryKey)) {
				return (HolderGetter<S>) entryLookup;
			}

			return registries.lookupOrThrow(key);
		}

		@Override
		public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
			return registries.lookupOrThrow(key).listElements();
		}

		private Map<ResourceKey<T>, T> entries() {
			return entries;
		}
	}

	private static final class RecipeBootstrapContext extends FabricBootstrapContext<Recipe<?>> implements FabricRecipeOutput {
		private final Function<Identifier, Identifier> recipeIdentifier;

		private RecipeBootstrapContext(HolderLookup.Provider registries, Function<Identifier, Identifier> recipeIdentifier) {
			super(registries, Registries.RECIPE);
			this.recipeIdentifier = recipeIdentifier;
		}

		@Override
		public Identifier getRecipeIdentifier(Identifier recipeId) {
			return recipeIdentifier.apply(recipeId);
		}
	}

	/**
	 * Override this method to change the recipe identifier. The default implementation normalizes the namespace to the mod ID.
	 */
	protected Identifier getRecipeIdentifier(Identifier identifier) {
		return Identifier.fromNamespaceAndPath(output.getModId(), identifier.getPath());
	}

	@Override
	public String getName() {
		return "Recipes";
	}
}
