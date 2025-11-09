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
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;

import net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.api.loot.v3.LootModifier;
import net.fabricmc.fabric.api.loot.v3.LootModifierTarget;

public record LootModifierImpl(LootModifierTarget target, List<LootPool> pools, List<LootItemFunction> functions) implements LootModifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(LootModifierImpl.class);

	public static final Codec<LootModifier> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			LootModifierTarget.CODEC.fieldOf("target").forGetter(LootModifier::target),
			LootPool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(LootModifier::pools),
			LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(LootModifier::functions)
	).apply(builder, LootModifierImpl::new));

	public static void modifyLootTables(ResourceManager resourceManager, RegistryOps<JsonElement> registryOps, HolderLookup.Provider registries, Map<Identifier, LootTable> lootTables) {
		Map<Identifier, LootModifier> modifiers = loadLootModifiers(resourceManager, registryOps);
		lootTables.replaceAll((id, table) -> modifyLootTable(modifiers, registries, id, table));
	}

	private static Map<Identifier, LootModifier> loadLootModifiers(ResourceManager resourceManager, RegistryOps<JsonElement> registryOps) {
		Map<Identifier, LootModifier> modifiers = new HashMap<>();
		SimpleJsonResourceReloadListener.scanDirectory(resourceManager, FileToIdConverter.json(LootModifier.DATA_DIRECTORY), registryOps, CODEC, modifiers);
		LOGGER.debug("Loaded {} loot modifiers", modifiers.size());
		return modifiers;
	}

	private static LootTable modifyLootTable(Map<Identifier, LootModifier> modifiers, HolderLookup.Provider registries, Identifier id, LootTable table) {
		ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, id);
		// Populated inside SimpleJsonResourceReloadListenerMixin
		LootTableSource source = LootUtil.SOURCES.get().getOrDefault(id, LootTableSource.DATA_PACK);
		// Invoke the REPLACE event for the current loot table.
		LootTable replacement = LootTableEvents.REPLACE.invoker().replaceLootTable(key, table, source, registries);

		if (replacement != null) {
			// Set the loot table to MODIFY to be the replacement loot table.
			// The MODIFY event will also see it as a replaced loot table via the source.
			table = replacement;
			source = LootTableSource.REPLACED;
		}

		// Turn the current table into a modifiable builder, apply loot modifiers and invoke the MODIFY event.
		LootTable.Builder builder = FabricLootTableBuilder.copyOf(table);

		for (LootModifier modifier : modifiers.values()) {
			applyModifier(builder, key, source, modifier);
		}

		LootTableEvents.MODIFY.invoker().modifyLootTable(key, builder, source, registries);
		return builder.build();
	}

	private static void applyModifier(LootTable.Builder builder, ResourceKey<LootTable> key, LootTableSource source, LootModifier modifier) {
		if (modifier.target().shouldModify(key, source)) {
			builder.pools(modifier.pools());
			builder.apply(modifier.functions());
		}
	}
}
