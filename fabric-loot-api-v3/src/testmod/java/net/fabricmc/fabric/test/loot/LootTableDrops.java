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

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.context.ContextType;
import net.minecraft.util.math.Vec3d;

/**
 * A utility class that can easily generate and check loot table drops.
 */
public final class LootTableDrops {
	private final TestContext context;
	private final Text name;
	private final List<ItemStack> stacks;

	private LootTableDrops(TestContext context, Text name, List<ItemStack> stacks) {
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
		Text message = Text.stringifiedTranslatable("test.error.value_not_equal", name, expected, stacks);
		context.assertTrue(ItemStack.stacksEqual(expected, stacks), message);
	}

	/**
	 * Asserts that the drop list contains an expected stack.
	 */
	public void assertContains(ItemStack expected) {
		for (ItemStack stack : stacks) {
			if (ItemStack.areEqual(expected, stack)) {
				// Found a match
				return;
			}
		}

		throw context.createError(Text.literal("Expected ").append(name).append(" to contain " + expected + ", but found " + stacks));
	}

	/**
	 * Asserts that the total drop count matches an expected value.
	 */
	public void assertTotalCount(int expected) {
		int actual = stacks.stream().mapToInt(ItemStack::getCount).sum();
		context.assertEquals(expected, actual, Text.literal("total drop count"));
	}

	/**
	 * Drops a block loot table.
	 */
	public static Builder block(TestContext context, Block block) {
		Text name = Text.empty().append(block.getName()).append(" drops");
		return new Builder(context, name, LootContextTypes.BLOCK, block.getLootTableKey().orElseThrow())
				.set(LootContextParameters.BLOCK_STATE, block.getDefaultState())
				.set(LootContextParameters.ORIGIN, Vec3d.ZERO)
				.set(LootContextParameters.TOOL, ItemStack.EMPTY);
	}

	/**
	 * Drops an entity loot table.
	 */
	public static Builder entity(TestContext context, EntityType<?> type) {
		Text name = Text.empty().append(type.getName()).append(" drops");
		Entity contextEntity = context.spawnEntity(type, Vec3d.ZERO);
		return new Builder(context, name, LootContextTypes.ENTITY, type.getLootTableKey().orElseThrow())
				.set(LootContextParameters.THIS_ENTITY, contextEntity)
				.set(LootContextParameters.ORIGIN, Vec3d.ZERO)
				.set(LootContextParameters.DAMAGE_SOURCE, context.getWorld().getDamageSources().generic());
	}

	public static final class Builder {
		private final TestContext testContext;
		private final Text name;
		private final LootWorldContext.Builder contextBuilder;
		private final ContextType contextType;
		private final RegistryKey<LootTable> tableKey;
		private long seed;

		private Builder(TestContext testContext, Text name, ContextType contextType, RegistryKey<LootTable> tableKey) {
			this.testContext = testContext;
			this.name = name;
			this.contextBuilder = new LootWorldContext.Builder(testContext.getWorld());
			this.contextType = contextType;
			this.tableKey = tableKey;
		}

		/**
		 * Sets a loot context parameter.
		 */
		public <T> Builder set(ContextParameter<T> parameter, T value) {
			contextBuilder.add(parameter, value);
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
			LootWorldContext context = contextBuilder.build(contextType);
			LootTable lootTable = testContext.getWorld().getServer().getReloadableRegistries().getLootTable(tableKey);
			List<ItemStack> stacks = lootTable.generateLoot(context, seed);
			return new LootTableDrops(testContext, name, stacks);
		}
	}
}
