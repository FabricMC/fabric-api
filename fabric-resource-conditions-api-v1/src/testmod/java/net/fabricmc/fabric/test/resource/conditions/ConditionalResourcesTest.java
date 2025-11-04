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

package net.fabricmc.fabric.test.resource.conditions;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class ConditionalResourcesTest {
	private static final String MOD_ID = "fabric-resource-conditions-api-v1-testmod";

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@GameTest
	public void conditionalRecipes(GameTestHelper context) {
		RecipeManager manager = context.getLevel().recipeAccess();

		if (manager.byKey(ResourceKey.create(Registries.RECIPE, id("not_loaded"))).isPresent()) {
			throw new AssertionError("not_loaded recipe should not have been loaded.");
		}

		if (manager.byKey(ResourceKey.create(Registries.RECIPE, id("loaded"))).isEmpty()) {
			throw new AssertionError("loaded recipe should have been loaded.");
		}

		if (manager.byKey(ResourceKey.create(Registries.RECIPE, id("item_tags_populated"))).isEmpty()) {
			throw new AssertionError("item_tags_populated recipe should have been loaded.");
		}

		if (manager.byKey(ResourceKey.create(Registries.RECIPE, id("tags_populated"))).isEmpty()) {
			throw new AssertionError("tags_populated recipe should have been loaded.");
		}

		if (manager.byKey(ResourceKey.create(Registries.RECIPE, id("tags_populated_default"))).isEmpty()) {
			throw new AssertionError("tags_populated_default recipe should have been loaded.");
		}

		if (manager.byKey(ResourceKey.create(Registries.RECIPE, id("tags_not_populated"))).isPresent()) {
			throw new AssertionError("tags_not_populated recipe should not have been loaded.");
		}

		if (manager.byKey(ResourceKey.create(Registries.RECIPE, id("features_enabled"))).isEmpty()) {
			throw new AssertionError("features_enabled recipe should have been loaded.");
		}

		long loadedRecipes = manager.getRecipes().stream().filter(r -> r.id().identifier().getNamespace().equals(MOD_ID)).count();
		if (loadedRecipes != 5) throw new AssertionError("Unexpected loaded recipe count: " + loadedRecipes);

		context.succeed();
	}

	@GameTest
	public void conditionalPredicates(GameTestHelper context) {
		// Predicates are internally handled as a kind of loot data,
		// hence the yarn name "loot condition".

		HolderGetter.Provider registries = context.getLevel().getServer().reloadableRegistries().lookup();

		if (registries.get(ResourceKey.create(Registries.PREDICATE, id("loaded"))).isEmpty()) {
			throw new AssertionError("loaded predicate should have been loaded.");
		}

		if (registries.get(ResourceKey.create(Registries.PREDICATE, id("not_loaded"))).isPresent()) {
			throw new AssertionError("not_loaded predicate should not have been loaded.");
		}

		context.succeed();
	}

	@GameTest
	public void conditionalLootTables(GameTestHelper context) {
		ReloadableServerRegistries.Holder registries = context.getLevel().getServer().reloadableRegistries();

		if (registries.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id("blocks/loaded"))) == LootTable.EMPTY) {
			throw new AssertionError("loaded loot table should have been loaded.");
		}

		if (registries.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id("blocks/not_loaded"))) != LootTable.EMPTY) {
			throw new AssertionError("not_loaded loot table should not have been loaded.");
		}

		context.succeed();
	}

	@GameTest
	public void conditionalDynamicRegistry(GameTestHelper context) {
		Registry<BannerPattern> registry = context.getLevel().registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);

		if (registry.getValue(id("loaded")) == null) {
			throw new AssertionError("loaded banner pattern should have been loaded.");
		}

		if (registry.getValue(id("not_loaded")) != null) {
			throw new AssertionError("not_loaded banner pattern should not have been loaded.");
		}

		context.succeed();
	}

	@GameTest
	public void conditionalOverlays(GameTestHelper context) {
		HolderGetter.Provider registries = context.getLevel().getServer().reloadableRegistries().lookup();

		if (registries.get(ResourceKey.create(Registries.PREDICATE, id("do_overlay"))).isEmpty()) {
			throw new AssertionError("do_overlay predicate should have been overlayed.");
		}

		if (registries.get(ResourceKey.create(Registries.PREDICATE, id("dont_overlay"))).isPresent()) {
			throw new AssertionError("dont_overlay predicate should not have been overlayed.");
		}

		context.succeed();
	}
}
