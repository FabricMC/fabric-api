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

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * A utility class that can easily generate and check loot table drops.
 */
public final class LootTableDrops {
	private final GameTestHelper context;
	private final Component name;
	private final List<ItemStack> stacks;

	private LootTableDrops(GameTestHelper context, Component name, List<ItemStack> stacks) {
		this.context = context;
		this.name = name;
		this.stacks = stacks;
	}

	/**
	 * Asserts that the drop list only contains a single expected stack.
	 */
	public void assertEquals(ItemStack expected) {
		assertEquals(List.of(expected));
	}

	/**
	 * Asserts that the drop list matches an expected list.
	 */
	public void assertEquals(List<ItemStack> expected) {
		Component message = Component.translatableEscape("test.error.value_not_equal", name, expected, stacks);
		context.assertTrue(ItemStack.listMatches(expected, stacks), message);
	}

	/**
	 * Asserts that the drop list contains an expected stack.
	 */
	public void assertContains(ItemStack expected) {
		for (ItemStack stack : stacks) {
			if (ItemStack.matches(expected, stack)) {
				// Found a match
				return;
			}
		}

		throw context.assertionException(Component.literal("Expected ").append(name).append(" to contain " + expected + ", but found " + stacks));
	}

	/**
	 * Asserts that the total drop count matches an expected value.
	 */
	public void assertTotalCount(int expected) {
		int actual = stacks.stream().mapToInt(ItemStack::getCount).sum();
		context.assertValueEqual(expected, actual, Component.literal("total drop count"));
	}

	/**
	 * Drops a block loot table.
	 */
	public static Builder block(GameTestHelper context, Block block) {
		Component name = Component.empty().append(block.getName()).append(" drops");
		return new Builder(context, name, LootContextParamSets.BLOCK, block.getLootTable().orElseThrow())
				.set(LootContextParams.BLOCK_STATE, block.defaultBlockState())
				.set(LootContextParams.ORIGIN, Vec3.ZERO)
				.set(LootContextParams.TOOL, ItemStack.EMPTY);
	}

	/**
	 * Drops an entity loot table.
	 */
	public static Builder entity(GameTestHelper context, EntityType<?> type) {
		Component name = Component.empty().append(type.getDescription()).append(" drops");
		Entity contextEntity = context.spawn(type, BlockPos.ZERO);
		return new Builder(context, name, LootContextParamSets.ENTITY, type.getDefaultLootTable().orElseThrow())
				.set(LootContextParams.THIS_ENTITY, contextEntity)
				.set(LootContextParams.ORIGIN, Vec3.ZERO)
				.set(LootContextParams.DAMAGE_SOURCE, context.getLevel().damageSources().generic());
	}

	public static final class Builder {
		private final GameTestHelper testContext;
		private final Component name;
		private final LootParams.Builder contextBuilder;
		private final ContextKeySet contextType;
		private final ResourceKey<LootTable> tableKey;
		private long seed;

		private Builder(GameTestHelper testContext, Component name, ContextKeySet contextType, ResourceKey<LootTable> tableKey) {
			this.testContext = testContext;
			this.name = name;
			this.contextBuilder = new LootParams.Builder(testContext.getLevel());
			this.contextType = contextType;
			this.tableKey = tableKey;
		}

		/**
		 * Sets a loot context parameter.
		 */
		public <T> Builder set(ContextKey<T> parameter, T value) {
			contextBuilder.withParameter(parameter, value);
			return this;
		}

		/**
		 * Sets the loot table seed. This is only needed for tables with random drops.
		 */
		public Builder seed(long seed) {
			this.seed = seed;
			return this;
		}

		/**
		 * Runs the drops.
		 */
		public LootTableDrops drop() {
			LootParams context = contextBuilder.create(contextType);
			LootTable lootTable = testContext.getLevel().getServer().reloadableRegistries().getLootTable(tableKey);
			List<ItemStack> stacks = lootTable.getRandomItems(context, seed);
			return new LootTableDrops(testContext, name, stacks);
		}
	}
}
