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

import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.impl.resource.loader.BuiltinModResourcePackSource;
import net.fabricmc.fabric.impl.resource.loader.FabricResource;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;

public final class LootUtil {
	public static final ThreadLocal<Map<Identifier, LootTableSource>> SOURCES = ThreadLocal.withInitial(HashMap::new);

	public static LootTableSource determineSource(Resource resource) {
		if (resource != null) {
			ResourcePackSource packSource = ((FabricResource) resource).getFabricPackSource();

			if (packSource == ResourcePackSource.BUILTIN) {
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

	public static RegistryEntry<LootTable> getEntryOrDirect(ServerWorld world, LootTable table) {
		RegistryWrapper.WrapperLookup wrapperLookup = world
				.getServer()
				.getReloadableRegistries()
				.createRegistryLookup();

		RegistryWrapper<LootTable> lootTableRegistryWrapper = wrapperLookup
				.getOptional(RegistryKeys.LOOT_TABLE)
				.orElseThrow(() -> new IllegalStateException("Failed to fetch LootTable wrapper from WrapperLookup"));

		return lootTableRegistryWrapper
				.streamEntries()
				.filter(it -> it.value().equals(table))
				.findFirst()
				.map(Function.<RegistryEntry<LootTable>>identity())
				.orElseGet(() -> RegistryEntry.of(table));
	}
}
