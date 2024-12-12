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

import net.minecraft.block.Block;
import net.minecraft.data.DataWriter;
import net.minecraft.data.loottable.BlockLootTableGenerator;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.impl.datagen.loot.FabricLootTableProviderImpl;

/**
 * Extend this class and implement {@link FabricBlockLootTableProvider#generate}.
 *
 * <p>Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 */
public abstract class FabricBlockLootTableProvider extends BlockLootTableGenerator implements FabricLootTableProvider {
	private final FabricDataOutput output;
	private final Set<Identifier> excludedFromStrictValidation = new HashSet<>();
	private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;

	protected FabricBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		super(Collections.emptySet(), FeatureFlags.FEATURE_MANAGER.getFeatureSet(), registryLookup.join());
		this.output = dataOutput;
		this.registryLookupFuture = registryLookup;
	}

	/**
	 * Implement this method to add block drops.
	 *
	 * <p>Use the range of {@link BlockLootTableGenerator#addDrop} methods to generate block drops.
	 */
	@Override
	public abstract void generate();

	/**
	 * Disable strict validation for the passed block.
	 */
	public void excludeFromStrictValidation(Block block) {
		excludedFromStrictValidation.add(Registries.BLOCK.getId(block));
	}

	@Override
	public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> biConsumer) {
		generate();

		for (Map.Entry<RegistryKey<LootTable>, LootTable.Builder> entry : lootTables.entrySet()) {
			RegistryKey<LootTable> registryKey = entry.getKey();

			biConsumer.accept(registryKey, entry.getValue());
		}

		if (output.isStrictValidationEnabled()) {
			Set<Identifier> missing = Sets.newHashSet();

			for (Identifier blockId : Registries.BLOCK.getIds()) {
				if (blockId.getNamespace().equals(output.getModId())) {
					Optional<RegistryKey<LootTable>> blockLootTableId = Registries.BLOCK.get(blockId).getLootTableKey();

					if (blockLootTableId.isPresent() && blockLootTableId.get().getValue().getNamespace().equals(output.getModId())) {
						if (!lootTables.containsKey(blockLootTableId.get())) {
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
	public CompletableFuture<?> run(DataWriter writer) {
		return FabricLootTableProviderImpl.run(writer, this, LootContextTypes.BLOCK, output, registryLookupFuture);
	}

	@Override
	public String getName() {
		return "Block Loot Tables";
	}
}
