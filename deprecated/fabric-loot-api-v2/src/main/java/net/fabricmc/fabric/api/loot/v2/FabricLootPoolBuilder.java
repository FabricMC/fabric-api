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

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunction;

/**
 * Convenience extensions to {@link LootPool.Builder}
 * for adding pre-built objects or collections.
 *
 * <p>This interface is automatically injected to {@link LootPool.Builder}.
 * @deprecated Please use {@link net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder} instead.
 */
@ApiStatus.NonExtendable
@Deprecated
public interface FabricLootPoolBuilder {
	/**
	 * Adds an entry to this builder.
	 *
	 * @param entry the added loot entry
	 * @return this builder
	 * @deprecated Please use {@link net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder#with(LootPoolEntry)} instead.
	 */
	@Deprecated
	default LootPool.Builder with(LootPoolEntry entry) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Adds entries to this builder.
	 *
	 * @param entries the added loot entries
	 * @return this builder
	 * @deprecated Please use {@link net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder#with(LootPoolEntry)} instead.
	 */
	@Deprecated
	default LootPool.Builder with(Collection<? extends LootPoolEntry> entries) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Adds a condition to this builder.
	 *
	 * @param condition the added condition
	 * @return this builder
	 * @deprecated Please use {@link net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder#conditionally(LootCondition)} instead.
	 */
	@Deprecated
	default LootPool.Builder conditionally(LootCondition condition) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Adds conditions to this builder.
	 *
	 * @param conditions the added conditions
	 * @return this builder
	 * @deprecated Please use {@link net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder#conditionally(LootCondition)} instead.
	 */
	@Deprecated
	default LootPool.Builder conditionally(Collection<? extends LootCondition> conditions) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Applies a function to this builder.
	 *
	 * @param function the applied loot function
	 * @return this builder
	 * @deprecated Please use {@link net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder#apply(LootFunction)} instead.
	 */
	@Deprecated
	default LootPool.Builder apply(LootFunction function) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Applies loot functions to this builder.
	 *
	 * @param functions the applied loot functions
	 * @return this builder
	 * @deprecated Please use {@link net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder#apply(LootFunction)} instead.
	 */
	@Deprecated
	default LootPool.Builder apply(Collection<? extends LootFunction> functions) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Creates a builder copy of a loot pool.
	 *
	 * @param pool the loot pool
	 * @return the copied builder
	 * @deprecated Please use {@link net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder#copyOf(LootPool)} instead.
	 */
	@Deprecated
	static LootPool.Builder copyOf(LootPool pool) {
		return net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder.copyOf(pool);
	}
}
