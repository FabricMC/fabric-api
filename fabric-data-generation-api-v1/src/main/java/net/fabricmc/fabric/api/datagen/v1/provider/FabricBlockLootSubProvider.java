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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.common.collect.Sets;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.impl.datagen.loot.FabricLootTableProviderImpl;

/**
 * Extend this class and implement {@link FabricBlockLootSubProvider#generate}.
 *
 * <p>Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 */
public abstract class FabricBlockLootSubProvider extends BlockLootSubProvider implements FabricLootTableSubProvider {
	private final FabricPackOutput output;
	private final Set<Identifier> excludedFromStrictValidation = new HashSet<>();
	private final CompletableFuture<HolderLookup.Provider> registriesFuture;

	protected FabricBlockLootSubProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registriesFuture.join());
		this.output = packOutput;
		this.registriesFuture = registriesFuture;
	}

	/**
	 * Implement this method to add block drops.
	 *
	 * <p>Use the range of {@link BlockLootSubProvider#add} methods to generate block drops.
	 */
	@Override
	public abstract void generate();

	/**
	 * Disable strict validation for the passed block.
	 */
	public void excludeFromStrictValidation(Block block) {
		excludedFromStrictValidation.add(BuiltInRegistries.BLOCK.getKey(block));
	}

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
		generate();

		for (Map.Entry<ResourceKey<LootTable>, LootTable.Builder> entry : map.entrySet()) {
			ResourceKey<LootTable> resourceKey = entry.getKey();

			biConsumer.accept(resourceKey, entry.getValue());
		}

		if (output.isStrictValidationEnabled()) {
			Set<Identifier> missing = Sets.newHashSet();

			for (Identifier blockId : BuiltInRegistries.BLOCK.keySet()) {
				if (blockId.getNamespace().equals(output.getModId())) {
					Optional<ResourceKey<LootTable>> blockLootTableId = BuiltInRegistries.BLOCK.getValue(blockId).getLootTable();

					if (blockLootTableId.isPresent() && blockLootTableId.get().identifier().getNamespace().equals(output.getModId())) {
						if (!map.containsKey(blockLootTableId.get())) {
							missing.add(blockId);
						}
					}
				}
			}

			missing.removeAll(excludedFromStrictValidation);

			if (!missing.isEmpty()) {
				throw new IllegalStateException("Missing loot table(s) for %s".formatted(missing));
			}
		}
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return FabricLootTableProviderImpl.run(output, this, LootContextParamSets.BLOCK, this.output, registriesFuture);
	}

	@Override
	public String getName() {
		return "Block Loot Tables";
	}
}
