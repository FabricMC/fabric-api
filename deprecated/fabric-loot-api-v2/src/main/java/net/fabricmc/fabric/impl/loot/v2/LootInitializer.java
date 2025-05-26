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

package net.fabricmc.fabric.impl.loot.v2;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;

public class LootInitializer implements ModInitializer {
	@Override
	public void onInitialize() {
		// Forward the events to the v2 API.
		LootTableEvents.REPLACE.register(((key, original, source, registries) -> net.fabricmc.fabric.api.loot.v2.LootTableEvents.REPLACE.invoker().replaceLootTable(key, original, toV2Source(source))));
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> net.fabricmc.fabric.api.loot.v2.LootTableEvents.MODIFY.invoker().modifyLootTable(key, tableBuilder, toV2Source(source)));
		LootTableEvents.ALL_LOADED.register((resourceManager, lootRegistry) -> net.fabricmc.fabric.api.loot.v2.LootTableEvents.ALL_LOADED.invoker().onLootTablesLoaded(resourceManager, lootRegistry));
	}

	private static net.fabricmc.fabric.api.loot.v2.LootTableSource toV2Source(LootTableSource source) {
		return switch (source) {
		case VANILLA -> net.fabricmc.fabric.api.loot.v2.LootTableSource.VANILLA;
		case MOD -> net.fabricmc.fabric.api.loot.v2.LootTableSource.MOD;
		case DATA_PACK -> net.fabricmc.fabric.api.loot.v2.LootTableSource.DATA_PACK;
		case REPLACED -> net.fabricmc.fabric.api.loot.v2.LootTableSource.REPLACED;
		};
	}
}
