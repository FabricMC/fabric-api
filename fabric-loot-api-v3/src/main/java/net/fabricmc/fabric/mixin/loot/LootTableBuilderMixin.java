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

package net.fabricmc.fabric.mixin.loot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder;

/**
 * The implementation of the injected interface {@link FabricLootTableBuilder}.
 * Simply implements the new methods by adding the relevant objects inside the lists.
 */
@Mixin(LootTable.Builder.class)
abstract class LootTableBuilderMixin implements FabricLootTableBuilder {
	@Shadow
	@Final
	@Mutable
	private ImmutableList.Builder<LootPool> pools;

	@Shadow
	@Final
	private ImmutableList.Builder<LootItemFunction> functions;

	@Unique
	private LootTable.Builder self() {
		// noinspection ConstantConditions
		return (LootTable.Builder) (Object) this;
	}

	@Override
	public LootTable.Builder pool(LootPool pool) {
		this.pools.add(pool);
		return self();
	}

	@Override
	public LootTable.Builder apply(LootItemFunction function) {
		this.functions.add(function);
		return self();
	}

	@Override
	public LootTable.Builder pools(Collection<? extends LootPool> pools) {
		this.pools.addAll(pools);
		return self();
	}

	@Override
	public LootTable.Builder apply(Collection<? extends LootItemFunction> functions) {
		this.functions.addAll(functions);
		return self();
	}

	@Override
	public LootTable.Builder modifyPools(Consumer<? super LootPool.Builder> modifier) {
		var list = new ArrayList<>(pools.build());
		ListIterator<LootPool> iterator = list.listIterator();

		while (iterator.hasNext()) {
			LootPool.Builder poolBuilder = FabricLootPoolBuilder.copyOf(iterator.next());
			modifier.accept(poolBuilder);
			iterator.set(poolBuilder.build());
		}

		this.pools = ImmutableList.builder();
		this.pools.addAll(list);

		return self();
	}
}
