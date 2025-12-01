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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.impl.resource.pack.BuiltinModResourcePackSource;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

public final class LootUtil {
	public static final ThreadLocal<Map<Identifier, LootTableSource>> SOURCES = ThreadLocal.withInitial(HashMap::new);

	public static LootTableSource determineSource(Resource resource) {
		if (resource != null) {
			PackSource packSource = resource.getFabricPackSource();

			if (packSource == PackSource.BUILT_IN) {
				return LootTableSource.VANILLA;
			} else if (packSource == ModResourcePackCreator.RESOURCE_PACK_SOURCE || packSource instanceof BuiltinModResourcePackSource) {
				return LootTableSource.MOD;
			}
		}

		// If not builtin or mod, assume external data pack.
		// It might also be a virtual loot table injected via mixin instead of being loaded
		// from a resource, but we can't determine that here.
		return LootTableSource.DATA_PACK;
	}

	public static Holder<LootTable> getEntryOrDirect(ServerLevel world, LootTable table) {
		HolderLookup.Provider wrapperLookup = world
				.getServer()
				.reloadableRegistries()
				.lookup();

		HolderLookup<LootTable> lootTableRegistryWrapper = wrapperLookup
				.lookup(Registries.LOOT_TABLE)
				.orElseThrow(() -> new IllegalStateException("Failed to fetch LootTable wrapper from WrapperLookup"));

		return lootTableRegistryWrapper
				.listElements()
				.filter(it -> it.value().equals(table))
				.findFirst()
				.map(Function.<Holder<LootTable>>identity())
				.orElseGet(() -> Holder.direct(table));
	}
}
