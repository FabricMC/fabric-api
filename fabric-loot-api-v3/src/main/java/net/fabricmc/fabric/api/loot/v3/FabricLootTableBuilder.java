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

package net.fabricmc.fabric.api.loot.v3;

import java.util.Collection;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import net.fabricmc.fabric.mixin.loot.LootTableAccessor;

/**
 * Convenience extensions to {@link LootTable.Builder}
 * for adding pre-built objects or collections and modifying loot pools.
 *
 * <p>This interface is automatically injected to {@link LootTable.Builder}.
 */
@ApiStatus.NonExtendable
public interface FabricLootTableBuilder {
	/**
	 * Adds a loot pool to this builder.
	 *
	 * @param pool the added pool
	 * @return this builder
	 */
	default LootTable.Builder pool(LootPool pool) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Applies a loot function to this builder.
	 *
	 * @param function the applied function
	 * @return this builder
	 */
	default LootTable.Builder apply(LootItemFunction function) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Adds loot pools to this builder.
	 *
	 * @param pools the added pools
	 * @return this builder
	 */
	default LootTable.Builder pools(Collection<? extends LootPool> pools) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Applies loot functions to this builder.
	 *
	 * @param functions the applied functions
	 * @return this builder
	 */
	default LootTable.Builder apply(Collection<? extends LootItemFunction> functions) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Modifies all loot pools already present in this builder.
	 *
	 * <p>This method can be used instead of simply adding a new pool
	 * when you want the loot table to only drop items from one of the loot pool entries
	 * instead of both.
	 *
	 * <p>Calling this method turns all pools into builders and rebuilds them back into loot pools afterwards,
	 * so it is more efficient to do all transformations with one {@code modifyPools} call.
	 *
	 * @param modifier the modifying function
	 * @return this builder
	 */
	default LootTable.Builder modifyPools(Consumer<? super LootPool.Builder> modifier) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Creates a builder copy of a loot table.
	 *
	 * @param table the loot table
	 * @return the copied builder
	 */
	static LootTable.Builder copyOf(LootTable table) {
		LootTable.Builder builder = LootTable.lootTable();
		LootTableAccessor accessor = (LootTableAccessor) table;

		builder.setParamSet(table.getParamSet());
		builder.pools(accessor.fabric_getPools());
		builder.apply(accessor.fabric_getFunctions());
		accessor.fabric_getRandomSequenceId().ifPresent(builder::setRandomSequence);

		return builder;
	}
}
