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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.common.collect.Sets;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.packs.VanillaEntityLoot;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.impl.datagen.loot.FabricLootTableProviderImpl;

/**
 * Extend this class and implement {@link FabricEntityLootTableProvider#generate()}.
 *
 * <p>Register an instance of this class with {@link FabricDataGenerator.Pack#addProvider} in a
 * {@link DataGeneratorEntrypoint}.
 */
public abstract class FabricEntityLootTableProvider extends EntityLootSubProvider implements FabricLootTableProvider {
	private final FabricDataOutput output;
	private final Set<Identifier> excludedFromStrictValidation = new HashSet<>();
	private final CompletableFuture<HolderLookup.Provider> registryLookupFuture;

	protected FabricEntityLootTableProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(FeatureFlags.REGISTRY.allFlags(), registryLookup.join());

		this.output = output;
		this.registryLookupFuture = registryLookup;
	}

	/**
	 * Implement this method to add entity drops.
	 *
	 * <p>Use the {@link EntityLootSubProvider#add} methods to generate entity drops.
	 *
	 * <p>See {@link VanillaEntityLoot#generate()} for examples of vanilla entity loot tables.
	 */
	@Override
	public abstract void generate();

	/**
	 * Disable strict validation for the given entity type.
	 */
	public void excludeFromStrictValidation(EntityType<?> entityType) {
		this.excludedFromStrictValidation.add(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
	}

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
		this.generate();

		for (Map<ResourceKey<LootTable>, LootTable.Builder> tables : this.map.values()) {
			// Register each of this particular entity type's loot tables
			for (Map.Entry<ResourceKey<LootTable>, LootTable.Builder> entry : tables.entrySet()) {
				biConsumer.accept(entry.getKey(), entry.getValue());
			}
		}

		if (this.output.isStrictValidationEnabled()) {
			Set<Identifier> missing = Sets.newHashSet();

			// Find any entity types from this mod that are missing their main loot table
			for (Identifier entityTypeId : BuiltInRegistries.ENTITY_TYPE.keySet()) {
				if (!entityTypeId.getNamespace().equals(this.output.getModId())) {
					continue;
				}

				EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(entityTypeId);

				entityType.getDefaultLootTable().ifPresent(mainLootTableKey -> {
					if (!mainLootTableKey.identifier().getNamespace().equals(this.output.getModId())) {
						return;
					}

					Map<ResourceKey<LootTable>, LootTable.Builder> tables = this.map.get(entityType);

					if (tables == null || !tables.containsKey(mainLootTableKey)) {
						missing.add(entityTypeId);
					}
				});
			}

			missing.removeAll(this.excludedFromStrictValidation);

			if (!missing.isEmpty()) {
				throw new IllegalStateException("Missing loot table(s) for %s".formatted(missing));
			}
		}
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer) {
		return FabricLootTableProviderImpl.run(writer, this, LootContextParamSets.ENTITY, this.output, this.registryLookupFuture);
	}

	@Override
	public String getName() {
		return "Entity Loot Tables";
	}
}
