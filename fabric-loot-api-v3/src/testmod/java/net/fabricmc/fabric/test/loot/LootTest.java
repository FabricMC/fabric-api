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

package net.fabricmc.fabric.test.loot;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;

public class LootTest implements ModInitializer {
	@Override
	public void onInitialize() {
		// Test loot table load event
		// The LootTable.Builder LootPool.Builder methods here should use
		// prebuilt entries and pools to test the injected methods.
		LootTableEvents.REPLACE.register((key, original, source, registries) -> {
			if (Blocks.BLACK_WOOL.getLootTable().orElse(null) == key) {
				if (source != LootTableSource.VANILLA) {
					throw new AssertionError("black wool loot table should have LootTableSource.VANILLA, got " + source);
				}

				// Replace black wool drops with an iron ingot
				LootPool pool = LootPool.lootPool()
						.with(LootItem.lootTableItem(Items.IRON_INGOT).build())
						.build();

				return LootTable.lootTable().pool(pool).build();
			}

			return null;
		});

		// Test that the event is stopped when the loot table is replaced
		LootTableEvents.REPLACE.register((key, original, source, registries) -> {
			if (Blocks.BLACK_WOOL.getLootTable().orElse(null) == key) {
				throw new AssertionError("Event should have been stopped from replaced loot table");
			}

			return null;
		});

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (Blocks.BLACK_WOOL.getLootTable().orElse(null) == key && source != LootTableSource.REPLACED) {
				throw new AssertionError("black wool loot table should have LootTableSource.REPLACED, got " + source);
			}

			if (Blocks.WHITE_WOOL.getLootTable().orElse(null) == key) {
				if (source != LootTableSource.VANILLA) {
					throw new AssertionError("white wool loot table should have LootTableSource.VANILLA, got " + source);
				}

				// Add gold ingot with custom name to white wool drops
				LootPool pool = LootPool.lootPool()
						.with(LootItem.lootTableItem(Items.GOLD_INGOT).build())
						.conditionally(ExplosionCondition.survivesExplosion().build())
						.apply(SetNameFunction.setName(Component.literal("Gold from White Wool"), SetNameFunction.Target.CUSTOM_NAME).build())
						.build();

				tableBuilder.pool(pool);
			}

			// We modify red wool to drop diamonds in the test mod resources.
			if (Blocks.RED_WOOL.getLootTable().orElse(null) == key && source != LootTableSource.MOD) {
				throw new AssertionError("red wool loot table should have LootTableSource.MOD, got " + source);
			}

			// Modify yellow wool to drop *either* yellow wool or emeralds by adding
			// emeralds to the same loot pool.
			if (Blocks.YELLOW_WOOL.getLootTable().orElse(null) == key) {
				tableBuilder.modifyPools(poolBuilder -> poolBuilder.add(LootItem.lootTableItem(Items.EMERALD)));
			}
		});

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (EntityType.SALMON.getDefaultLootTable().orElse(null) == key) {
				Optional<Holder<Enchantment>> lure = registries.lookup(Registries.ENCHANTMENT).flatMap(registry -> registry.get(Enchantments.LURE));

				lure.ifPresent((lureEnchantment) -> tableBuilder.withPool(LootPool.lootPool().add(
						LootItem.lootTableItem(Items.FISHING_ROD)
				).apply(
						new SetEnchantmentsFunction.Builder().withEnchantment(lureEnchantment, ConstantValue.exactly(1))
				)));
			}
		});

		LootTableEvents.ALL_LOADED.register((resourceManager, lootRegistry) -> {
			Optional<LootTable> blackWoolTable = lootRegistry.getOptional(Blocks.BLACK_WOOL.getLootTable().orElse(null));

			if (blackWoolTable.isEmpty() || blackWoolTable.get() == LootTable.EMPTY) {
				throw new AssertionError("black wool loot table should not be empty");
			}
		});

		RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> matchGetter = RecipeManager.createCheck(RecipeType.SMELTING);

		// smelt any smeltable drops from blocks broken with a diamond pickaxe
		LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> {
			if (!context.hasParameter(LootContextParams.TOOL) || !context.hasParameter(LootContextParams.BLOCK_STATE)) {
				return;
			}

			ItemStack tool = Objects.requireNonNull(context.getOptionalParameter(LootContextParams.TOOL), "LootContext contains tool, but it was null");

			if (!tool.is(Items.DIAMOND_PICKAXE)) {
				return;
			}

			ServerLevel world = context.getLevel();
			HolderLookup.Provider lookup = world.registryAccess();

			drops.replaceAll(drop -> {
				SingleRecipeInput input = new SingleRecipeInput(drop);
				return matchGetter.getRecipeFor(input, world).map(RecipeHolder::value)
						.map(recipe -> recipe.assemble(input, lookup))
						.orElse(drop);
			});
		});
		LootTableEvents.MODIFY_DROPS.register(new ModifyDropsWithRecGuard((entry, context, drops) -> {
			if (entry.unwrapKey().map(it -> it.toString().contains("red")).orElse(false)) { // all red blocks drop double
				entry.value().getRandomItems(context, drops::add);
			}
		}));
		// test inline LootPools
		LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> {
			if (entry.unwrapKey().isEmpty()) {
				LootGameTest.inlineLootTablesSeen++;
			}
		});
	}

	private static final class ModifyDropsWithRecGuard implements LootTableEvents.ModifyDrops {
		private final LootTableEvents.ModifyDrops inner;
		private boolean running = false;

		ModifyDropsWithRecGuard(LootTableEvents.ModifyDrops inner) {
			this.inner = inner;
		}

		@Override
		public void modifyLootTableDrops(Holder<LootTable> entry, LootContext context, List<ItemStack> drops) {
			if (running) return;

			try {
				running = true;
				inner.modifyLootTableDrops(entry, context, drops);
			} finally {
				running = false;
			}
		}
	}
}
