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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.impl.datagen.loot.FabricLootTableProviderImpl;

/**
 * Extend this class and implement {@link #generate}. Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 */
public abstract class SimpleFabricLootTableSubProvider implements FabricLootTableSubProvider {
	protected final FabricPackOutput output;
	private final CompletableFuture<HolderLookup.Provider> registryLookupFuture;
	protected final ContextKeySet contextParamSet;

	public SimpleFabricLootTableSubProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture, ContextKeySet contextParamSet) {
		this.output = output;
		this.registryLookupFuture = registryLookupFuture;
		this.contextParamSet = contextParamSet;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return FabricLootTableProviderImpl.run(cache, this, contextParamSet, output, registryLookupFuture);
	}

	@Override
	public String getName() {
		return Objects.requireNonNull(LootContextParamSets.REGISTRY.inverse().get(contextParamSet), "Could not get id for loot context param set") + " Loot Table";
	}
}
