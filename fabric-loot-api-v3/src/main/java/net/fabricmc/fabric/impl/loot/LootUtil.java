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

package net.fabricmc.fabric.impl.loot;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.impl.resource.pack.BuiltinModPackSource;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

public final class LootUtil {
	private static final Map<ResourceManager, HolderLookup.Provider> RELOAD_PROVIDERS = Collections.synchronizedMap(new WeakHashMap<>());

	public static void startReload(ResourceManager resourceManager, HolderLookup.Provider provider) {
		RELOAD_PROVIDERS.put(resourceManager, provider);
	}

	public static void endReload(ResourceManager resourceManager) {
		RELOAD_PROVIDERS.remove(resourceManager);
	}

	public static HolderLookup.@Nullable Provider getActiveReloadProvider(ResourceManager resourceManager) {
		return RELOAD_PROVIDERS.get(resourceManager);
	}

	public static LootTable modifyLootTable(ResourceKey<LootTable> key, LootTable table, LootTableSource source, HolderLookup.Provider provider) {
		LootTable replacement = LootTableEvents.REPLACE.invoker().replaceLootTable(key, table, source, provider);

		if (replacement != null) {
			table = replacement;
			source = LootTableSource.REPLACED;
		}

		LootTable.Builder builder = FabricLootTableBuilder.copyOf(table);
		LootTableEvents.MODIFY.invoker().modifyLootTable(key, builder, source, provider);
		return builder.build();
	}

	public static LootTableSource determineSource(Resource resource) {
		if (resource != null) {
			PackSource packSource = resource.getFabricPackSource();

			if (packSource == PackSource.BUILT_IN) {
				return LootTableSource.VANILLA;
			} else if (packSource == ModResourcePackCreator.RESOURCE_PACK_SOURCE || packSource instanceof BuiltinModPackSource) {
				return LootTableSource.MOD;
			}
		}

		// If not builtin or mod, assume external data pack.
		// It might also be a virtual loot table injected via mixin instead of being loaded
		// from a resource, but we can't determine that here.
		return LootTableSource.DATA_PACK;
	}

	public static Holder<LootTable> getEntryOrDirect(ServerLevel level, LootTable table) {
		HolderLookup.Provider provider = level
				.getServer()
				.reloadableRegistries()
				.lookup();

		HolderLookup<LootTable> lootTableHolderLookup = provider
				.lookup(Registries.LOOT_TABLE)
				.orElseThrow(() -> new IllegalStateException("Failed to fetch LootTable provider from HolderLookup.Provider"));

		return lootTableHolderLookup
				.listElements()
				.filter(it -> it.value().equals(table))
				.findFirst()
				.map(Function.<Holder<LootTable>>identity())
				.orElseGet(() -> Holder.direct(table));
	}
}
