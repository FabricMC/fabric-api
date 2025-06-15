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

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetEnchantmentsLootFunction;
import net.minecraft.loot.function.SetNameLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

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
			if (Blocks.BLACK_WOOL.getLootTableKey().orElse(null) == key) {
				if (source != LootTableSource.VANILLA) {
					throw new AssertionError("black wool loot table should have LootTableSource.VANILLA, got " + source);
				}

				// Replace black wool drops with an iron ingot
				LootPool pool = LootPool.builder()
						.with(ItemEntry.builder(Items.IRON_INGOT).build())
						.build();

				return LootTable.builder().pool(pool).build();
			}

			return null;
		});

		// Test that the event is stopped when the loot table is replaced
		LootTableEvents.REPLACE.register((key, original, source, registries) -> {
			if (Blocks.BLACK_WOOL.getLootTableKey().orElse(null) == key) {
				throw new AssertionError("Event should have been stopped from replaced loot table");
			}

			return null;
		});

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (Blocks.BLACK_WOOL.getLootTableKey().orElse(null) == key && source != LootTableSource.REPLACED) {
				throw new AssertionError("black wool loot table should have LootTableSource.REPLACED, got " + source);
			}

			if (Blocks.WHITE_WOOL.getLootTableKey().orElse(null) == key) {
				if (source != LootTableSource.VANILLA) {
					throw new AssertionError("white wool loot table should have LootTableSource.VANILLA, got " + source);
				}

				// Add gold ingot with custom name to white wool drops
				LootPool pool = LootPool.builder()
						.with(ItemEntry.builder(Items.GOLD_INGOT).build())
						.conditionally(SurvivesExplosionLootCondition.builder().build())
						.apply(SetNameLootFunction.builder(Text.literal("Gold from White Wool"), SetNameLootFunction.Target.CUSTOM_NAME).build())
						.build();

				tableBuilder.pool(pool);
			}

			// We modify red wool to drop diamonds in the test mod resources.
			if (Blocks.RED_WOOL.getLootTableKey().orElse(null) == key && source != LootTableSource.MOD) {
				throw new AssertionError("red wool loot table should have LootTableSource.MOD, got " + source);
			}

			// Modify yellow wool to drop *either* yellow wool or emeralds by adding
			// emeralds to the same loot pool.
			if (Blocks.YELLOW_WOOL.getLootTableKey().orElse(null) == key) {
				tableBuilder.modifyPools(poolBuilder -> poolBuilder.with(ItemEntry.builder(Items.EMERALD)));
			}
		});

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (EntityType.SALMON.getLootTableKey().orElse(null) == key) {
				Optional<RegistryEntry<Enchantment>> lure = registries.getOptional(RegistryKeys.ENCHANTMENT).flatMap(registry -> registry.getOptional(Enchantments.LURE));

				lure.ifPresent((lureEnchantment) -> tableBuilder.pool(LootPool.builder().with(
						ItemEntry.builder(Items.FISHING_ROD)
				).apply(
						new SetEnchantmentsLootFunction.Builder().enchantment(lureEnchantment, ConstantLootNumberProvider.create(1))
				)));
			}
		});

		LootTableEvents.ALL_LOADED.register((resourceManager, lootRegistry) -> {
			Optional<LootTable> blackWoolTable = lootRegistry.getOptionalValue(Blocks.BLACK_WOOL.getLootTableKey().orElse(null));

			if (blackWoolTable.isEmpty() || blackWoolTable.get() == LootTable.EMPTY) {
				throw new AssertionError("black wool loot table should not be empty");
			}
		});

		ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter = ServerRecipeManager.createCachedMatchGetter(RecipeType.SMELTING);

		// smelt any smeltable drops from blocks broken with a diamond pickaxe
		LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> {
			if (!context.hasParameter(LootContextParameters.TOOL) || !context.hasParameter(LootContextParameters.BLOCK_STATE)) {
				return;
			}

			ItemStack tool = Objects.requireNonNull(context.get(LootContextParameters.TOOL), "LootContext contains tool, but it was null");

			if (!tool.isOf(Items.DIAMOND_PICKAXE)) {
				return;
			}

			ServerWorld world = context.getWorld();
			RegistryWrapper.WrapperLookup lookup = world.getRegistryManager();

			drops.replaceAll(drop -> {
				SingleStackRecipeInput input = new SingleStackRecipeInput(drop);
				return matchGetter.getFirstMatch(input, world).map(RecipeEntry::value)
						.map(recipe -> recipe.craft(input, lookup))
						.orElse(drop);
			});
		});
		LootTableEvents.MODIFY_DROPS.register(new ModifyDropsWithRecGuard((entry, context, drops) -> {
			if (entry.getKey().map(it -> it.toString().contains("red")).orElse(false)) { // all red blocks drop double
				entry.value().generateLoot(context, drops::add);
			}
		}));
		// test inline LootPools
		LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> {
			if (entry.getKey().isEmpty()) {
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
		public void modifyLootTableDrops(RegistryEntry<LootTable> entry, LootContext context, List<ItemStack> drops) {
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
