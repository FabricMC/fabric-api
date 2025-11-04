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

package net.fabricmc.fabric.impl.datagen.loot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

public final class FabricLootTableProviderImpl {
	/**
	 * Shared run logic for {@link FabricBlockLootTableProvider} and {@link SimpleFabricLootTableProvider}.
	 */
	public static CompletableFuture<?> run(
			CachedOutput writer,
			FabricLootTableProvider provider,
			ContextKeySet contextType,
			FabricDataOutput fabricDataOutput,
			CompletableFuture<HolderLookup.Provider> registryLookup) {
		HashMap<Identifier, LootTable> builders = Maps.newHashMap();
		HashMap<Identifier, ResourceCondition[]> conditionMap = new HashMap<>();

		return registryLookup.thenCompose(lookup -> {
			provider.generate((registryKey, builder) -> {
				ResourceCondition[] conditions = FabricDataGenHelper.consumeConditions(builder);
				conditionMap.put(registryKey.identifier(), conditions);

				if (builders.put(registryKey.identifier(), builder.setParamSet(contextType).build()) != null) {
					throw new IllegalStateException("Duplicate loot table " + registryKey.identifier());
				}
			});

			RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
			final List<CompletableFuture<?>> futures = new ArrayList<>();

			for (Map.Entry<Identifier, LootTable> entry : builders.entrySet()) {
				JsonObject tableJson = (JsonObject) LootTable.DIRECT_CODEC.encodeStart(ops, entry.getValue()).getOrThrow(IllegalStateException::new);
				FabricDataGenHelper.addConditions(tableJson, conditionMap.remove(entry.getKey()));
				futures.add(DataProvider.saveStable(writer, tableJson, getOutputPath(fabricDataOutput, entry.getKey())));
			}

			return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
		});
	}

	private static Path getOutputPath(FabricDataOutput dataOutput, Identifier lootTableId) {
		return dataOutput.createRegistryElementsPathProvider(Registries.LOOT_TABLE).json(lootTableId);
	}

	private FabricLootTableProviderImpl() {
	}
}
