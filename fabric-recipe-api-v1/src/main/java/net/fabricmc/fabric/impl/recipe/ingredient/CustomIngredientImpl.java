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

package net.fabricmc.fabric.impl.recipe.ingredient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

/**
 * To test this API beyond the unit tests, please refer to the recipe provider in the datagen API testmod.
 * It contains various interesting recipes to test, and explains how to package them in a datapack.
 */
public class CustomIngredientImpl extends Ingredient {
	// Static helpers used by the API

	public static final String TYPE_KEY = "fabric:type";

	static final Map<Identifier, CustomIngredientSerializer<?>> REGISTERED_SERIALIZERS = new ConcurrentHashMap<>();

	public static final Codec<CustomIngredientSerializer<?>> CODEC = Identifier.CODEC.flatXmap(identifier ->
					Optional.ofNullable(REGISTERED_SERIALIZERS.get(identifier))
							.map(DataResult::success)
							.orElseGet(() -> DataResult.error(() -> "Unknown custom ingredient serializer: " + identifier)),
			serializer -> DataResult.success(serializer.getIdentifier())
	);

	public static void registerSerializer(CustomIngredientSerializer<?> serializer) {
		Objects.requireNonNull(serializer.getIdentifier(), "CustomIngredientSerializer identifier may not be null.");

		if (REGISTERED_SERIALIZERS.putIfAbsent(serializer.getIdentifier(), serializer) != null) {
			throw new IllegalArgumentException("CustomIngredientSerializer with identifier " + serializer.getIdentifier() + " already registered.");
		}
	}

	@Nullable
	public static CustomIngredientSerializer<?> getSerializer(Identifier identifier) {
		Objects.requireNonNull(identifier, "Identifier may not be null.");

		return REGISTERED_SERIALIZERS.get(identifier);
	}

	// Actual custom ingredient logic

	private final CustomIngredient customIngredient;
	@Nullable
	private List<RegistryEntry<Item>> customMatchingItems;

	public CustomIngredientImpl(CustomIngredient customIngredient) {
		// We must pass a registry entry list that contains something that isn't air. It doesn't actually get used.
		super(RegistryEntryList.of(Items.STONE.getRegistryEntry()));

		this.customIngredient = customIngredient;
	}

	private List<RegistryEntry<Item>> getCustomMatchingItems() {
		if (customMatchingItems == null) {
			customMatchingItems = customIngredient.getMatchingItems().toList();
		}

		return customMatchingItems;
	}

	@Override
	public CustomIngredient getCustomIngredient() {
		return customIngredient;
	}

	@Override
	public boolean requiresTesting() {
		return customIngredient.requiresTesting();
	}

	@Override
	public Stream<RegistryEntry<Item>> getMatchingItems() {
		return getCustomMatchingItems().stream();
	}

	@Override
	public boolean isEmpty() {
		return getCustomMatchingItems().isEmpty();
	}

	@Override
	public boolean test(ItemStack stack) {
		return customIngredient.test(stack);
	}

	@Override
	public boolean acceptsItem(RegistryEntry<Item> registryEntry) {
		return getCustomMatchingItems().contains(registryEntry);
	}

	@Override
	public SlotDisplay toDisplay() {
		return customIngredient.toDisplay();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CustomIngredientImpl that)) return false;
		return customIngredient.equals(that.customIngredient);
	}

	@Override
	public int hashCode() {
		return customIngredient.hashCode();
	}
}
