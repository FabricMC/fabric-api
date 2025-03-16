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
import org.jetbrains.annotations.NotNull;

import net.minecraft.data.DataWriter;
import net.minecraft.data.loottable.EntityLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaEntityLootTableGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.Identifier;

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
public abstract class FabricEntityLootTableProvider extends EntityLootTableGenerator implements FabricLootTableProvider {
	private final FabricDataOutput output;
	private final Set<Identifier> excludedFromStrictValidation = new HashSet<>();
	private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;

	protected FabricEntityLootTableProvider(FabricDataOutput output, @NotNull CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		super(FeatureFlags.FEATURE_MANAGER.getFeatureSet(), registryLookup.join());

		this.output = output;
		this.registryLookupFuture = registryLookup;
	}

	/**
	 * Implement this method to add entity drops.
	 *
	 * <p>Use the {@link EntityLootTableGenerator#register} methods to generate entity drops.
	 *
	 * <p>See {@link VanillaEntityLootTableGenerator#generate()} for examples of vanilla entity loot tables.
	 */
	@Override
	public abstract void generate();

	/**
	 * Disable strict validation for the given entity type.
	 */
	public void excludeFromStrictValidation(EntityType<?> entityType) {
		this.excludedFromStrictValidation.add(Registries.ENTITY_TYPE.getId(entityType));
	}

	@Override
	public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> biConsumer) {
		this.generate();

		for (Map<RegistryKey<LootTable>, LootTable.Builder> tables : this.lootTables.values()) {
			// Register each of this particular entity type's loot tables
			for (Map.Entry<RegistryKey<LootTable>, LootTable.Builder> entry : tables.entrySet()) {
				biConsumer.accept(entry.getKey(), entry.getValue());
			}
		}

		if (this.output.isStrictValidationEnabled()) {
			Set<Identifier> missing = Sets.newHashSet();

			// Find any entity types from this mod that are missing their main loot table
			for (Identifier entityTypeId : Registries.ENTITY_TYPE.getIds()) {
				if (entityTypeId.getNamespace().equals(this.output.getModId())) {
					EntityType<?> entityType = Registries.ENTITY_TYPE.get(entityTypeId);

					entityType.getLootTableKey().ifPresent(mainLootTableKey -> {
						if (mainLootTableKey.getValue().getNamespace().equals(this.output.getModId())) {
							Map<RegistryKey<LootTable>, LootTable.Builder> tables = this.lootTables.get(entityType);

							if (tables == null || !tables.containsKey(mainLootTableKey)) {
								missing.add(entityTypeId);
							}
						}
					});
				}
			}

			missing.removeAll(this.excludedFromStrictValidation);

			if (!missing.isEmpty()) {
				throw new IllegalStateException("Missing loot table(s) for %s".formatted(missing));
			}
		}
	}

	@Override
	public CompletableFuture<?> run(DataWriter writer) {
		return FabricLootTableProviderImpl.run(writer, this, LootContextTypes.ENTITY, this.output, this.registryLookupFuture);
	}

	@Override
	public String getName() {
		return "Entity Loot Tables";
	}
}
