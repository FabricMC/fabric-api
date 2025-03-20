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

package net.fabricmc.fabric.api.datagen.v1.provider;

import java.util.function.BiConsumer;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.data.DataProvider;
import net.minecraft.data.loottable.LootTableGenerator;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;

import net.fabricmc.fabric.api.datagen.v1.loot.FabricBlockLootTableGenerator;
import net.fabricmc.fabric.api.datagen.v1.loot.FabricEntityLootTableGenerator;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

/**
 * A base interface for Loot table providers. You should not implement this class directly.
 *
 * <p>{@link FabricBlockLootTableProvider} provides additional features specific to block drop loot tables.
 *
 * <p>Use {@link SimpleFabricLootTableProvider} for a simple abstract class that you can implement to handle standard loot table functions.
 */
@ApiStatus.NonExtendable
public interface FabricLootTableProvider extends LootTableGenerator, DataProvider {
	/**
	 * Return a new exporter that applies the specified conditions to any loot table it receives.
	 *
	 * <p>For block and entity loot tables, use {@link FabricBlockLootTableGenerator#withConditions} or
	 * {@link FabricEntityLootTableGenerator#withConditions} instead, respectively.
	 */
	default BiConsumer<RegistryKey<LootTable>, LootTable.Builder> withConditions(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> exporter, ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
		return (id, table) -> {
			FabricDataGenHelper.addConditions(table, conditions);
			exporter.accept(id, table);
		};
	}
}
