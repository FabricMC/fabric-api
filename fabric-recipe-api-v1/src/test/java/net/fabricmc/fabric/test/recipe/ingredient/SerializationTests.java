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

package net.fabricmc.fabric.test.recipe.ingredient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientInit;

public class SerializationTests {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		new CustomIngredientInit().onInitialize();
	}

	/**
	 * Check that trying to use a custom ingredient inside an array ingredient fails.
	 */
	@Test
	public void testArrayDeserialization() {
		String ingredientJson = """
[
	{
		"fabric:type": "fabric:all",
		"ingredients": [
			{
				"item": "minecraft:stone"
			}
		]
	}, {
		"item": "minecraft:dirt"
	}
]
				""";
		JsonElement json = JsonParser.parseString(ingredientJson);

		assertThrows(JsonParseException.class, () -> {
			Ingredient.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(JsonParseException::new);
		});
	}

	/**
	 * Check that we can serialise and deserialize a custom ingredient.
	 */
	@Test
	public void testCustomIngredientSerialization() {
		RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

		String ingredientJson = """
					{"ingredients":["minecraft:stone"],"fabric:type":"fabric:all"}
					""".trim();

		Ingredient ingredient = DefaultCustomIngredients.all(
				Ingredient.of(Items.STONE)
		);
		JsonObject json = Ingredient.CODEC.encodeStart(registryOps, ingredient).getOrThrow(IllegalStateException::new).getAsJsonObject();
		assertEquals(json.toString(), ingredientJson, "Unexpected json: " + json);

		// Make sure that we can deserialize it
		Ingredient deserialized = Ingredient.CODEC.parse(registryOps, json).getOrThrow(JsonParseException::new);
		assertNotNull(deserialized.getCustomIngredient(), "Custom ingredient was not deserialized");
		assertSame(deserialized.getCustomIngredient().getSerializer(), ingredient.getCustomIngredient().getSerializer(), "Serializer did not match");
	}
}
