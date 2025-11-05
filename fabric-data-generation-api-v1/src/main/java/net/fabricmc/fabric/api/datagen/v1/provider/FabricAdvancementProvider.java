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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

/**
 * Extend this class and implement {@link FabricAdvancementProvider#generateAdvancement}.
 *
 * <p>Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 */
public abstract class FabricAdvancementProvider implements DataProvider {
	protected final FabricDataOutput output;
	private final PackOutput.PathProvider pathResolver;
	private final CompletableFuture<HolderLookup.Provider> registryLookup;

	protected FabricAdvancementProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
		this.output = output;
		this.pathResolver = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
		this.registryLookup = registryLookup;
	}

	/**
	 * Implement this method to register advancements to generate use the consumer callback to register advancements.
	 *
	 * <p>Use {@link Advancement.Builder#save(Consumer, String)} to help build advancements.
	 */
	public abstract void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer);

	/**
	 * Return a new exporter that applies the specified conditions to any advancement it receives.
	 */
	protected Consumer<AdvancementHolder> withConditions(Consumer<AdvancementHolder> exporter, ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
		return advancement -> {
			FabricDataGenHelper.addConditions(advancement, conditions);
			exporter.accept(advancement);
		};
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer) {
		return this.registryLookup.thenCompose(lookup -> {
			final Set<Identifier> identifiers = Sets.newHashSet();
			final Set<AdvancementHolder> advancements = Sets.newHashSet();

			generateAdvancement(lookup, advancements::add);

			RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
			final List<CompletableFuture<?>> futures = new ArrayList<>();

			for (AdvancementHolder advancement : advancements) {
				if (!identifiers.add(advancement.id())) {
					throw new IllegalStateException("Duplicate advancement " + advancement.id());
				}

				JsonObject advancementJson = Advancement.CODEC.encodeStart(ops, advancement.value()).getOrThrow(IllegalStateException::new).getAsJsonObject();
				FabricDataGenHelper.addConditions(advancementJson, FabricDataGenHelper.consumeConditions(advancement));
				futures.add(DataProvider.saveStable(writer, advancementJson, getOutputPath(advancement)));
			}

			return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
		});
	}

	private Path getOutputPath(AdvancementHolder advancement) {
		return pathResolver.json(advancement.id());
	}

	@Override
	public String getName() {
		return "Advancements";
	}
}
