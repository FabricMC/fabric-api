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

package net.fabricmc.fabric.test.recipe.book;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.recipe.v1.book.RecipeBookRegistry;

public class RecipeBookTestContent implements ModInitializer {
	private static final String MOD_ID = "fabric-recipe-api-v1-testmod";

	public static final RecipeSerializer<BookRecipe> BOOK_RECIPE_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("book"), new RecipeSerializer<>(BookRecipe.MAP_CODEC, BookRecipe.STREAM_CODEC));

	public static final RecipeBookCategory BOOK_CATEGORY = Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, id("book"), new RecipeBookCategory());
	public static final RecipeBookCategory KNOWLEDGE_BOOK_CATEGORY = Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, id("knowledge_book"), new RecipeBookCategory());
	public static final RecipeBookCategory ENCHANTED_BOOK_CATEGORY = Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, id("enchanted_book"), new RecipeBookCategory());

	public static final BookCrafterBlock BOOK_CRAFTER_BLOCK = Registry.register(BuiltInRegistries.BLOCK, id("book_crafter"), new BookCrafterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).setId(ResourceKey.create(Registries.BLOCK, id("book_crafter")))));
	public static final BlockItem BOOK_CRAFTER_ITEM = Registry.register(BuiltInRegistries.ITEM, id("book_crafter"), new BlockItem(BOOK_CRAFTER_BLOCK, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id("book_crafter")))));
	public static final BlockEntityType<BookCrafterBlockEntity> BOOK_CRAFTER_BLOCK_ENTITY_TYPE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("book_crafter"), FabricBlockEntityTypeBuilder.create(BookCrafterBlockEntity::new, BOOK_CRAFTER_BLOCK).build());

	public static final MenuType<BookCraftingMenu> BOOK_CRAFTING_MENU_TYPE = Registry.register(BuiltInRegistries.MENU, id("book_crafting"), new MenuType<>(BookCraftingMenu::new, FeatureFlags.DEFAULT_FLAGS)); // Can't use default MenuType because transitive access wideners aren't working here

	public static final RecipeType<BookRecipe> BOOK_RECIPE_TYPE = Registry.register(BuiltInRegistries.RECIPE_TYPE, id("book"), new RecipeType<BookRecipe>() {
		@Override
		public String toString() {
			return MOD_ID + ":book";
		}
	});

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.RECIPE_DISPLAY, id("book"), BookRecipeDisplay.TYPE);
		RecipeBookRegistry.registerRecipeBookType(RecipeBookType.FABRIC_RECIPE_API_V1_TESTMOD_BOOK_CRAFTING, id("book_crafting"));
		RecipeBookRegistry.registerRecipeBookType(RecipeBookType.FABRIC_RECIPE_API_V1_TESTMOD_UNUSED, id("unused"));
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
