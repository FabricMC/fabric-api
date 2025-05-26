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

package net.fabricmc.fabric.api.loot.v2;

import java.util.Collection;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.function.LootFunction;

/**
 * Convenience extensions to {@link LootTable.Builder}
 * for adding pre-built objects or collections and modifying loot pools.
 *
 * <p>This interface is automatically injected to {@link LootTable.Builder}.
 *
 * @deprecated use {@link net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder} instead.
 */
@ApiStatus.NonExtendable
@Deprecated
public interface FabricLootTableBuilder {
	/**
	 * Adds a loot pool to this builder.
	 *
	 * @param pool the added pool
	 * @return this builder
	 * @deprecated use {@link net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder#pool(LootPool)} instead.
	 */
	@Deprecated
	default LootTable.Builder pool(LootPool pool) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Applies a loot function to this builder.
	 *
	 * @param function the applied function
	 * @return this builder
	 * @deprecated use {@link net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder#apply(LootFunction)} instead.
	 */
	@Deprecated
	default LootTable.Builder apply(LootFunction function) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Adds loot pools to this builder.
	 *
	 * @param pools the added pools
	 * @return this builder
	 * @deprecated use {@link net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder#pools(Collection)} instead.
	 */
	@Deprecated
	default LootTable.Builder pools(Collection<? extends LootPool> pools) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Applies loot functions to this builder.
	 *
	 * @param functions the applied functions
	 * @return this builder
	 * @deprecated use {@link net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder#apply(Collection)} instead.
	 */
	@Deprecated
	default LootTable.Builder apply(Collection<? extends LootFunction> functions) {
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
	 * @deprecated use {@link net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder#modifyPools(Consumer)} instead.
	 */
	@Deprecated
	default LootTable.Builder modifyPools(Consumer<? super LootPool.Builder> modifier) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Creates a builder copy of a loot table.
	 *
	 * @param table the loot table
	 * @return the copied builder
	 * @deprecated use {@link net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder#copyOf(LootTable)} instead.
	 */
	@Deprecated
	static LootTable.Builder copyOf(LootTable table) {
		return net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder.copyOf(table);
	}
}
