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

package net.fabricmc.fabric.api.datagen.v1;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.resources.ResourceKey;

/**
 * An entry point for data generation.
 *
 * <p>In {@code fabric.mod.json}, the entrypoint is defined with {@code fabric-datagen} key.</p>
 *
 * @see FabricDataGenerator
 */
@FunctionalInterface
public interface DataGeneratorEntrypoint {
	/**
	 * Register {@link net.minecraft.data.DataProvider} with the {@link FabricDataGenerator} during this entrypoint.
	 *
	 * @param fabricDataGenerator The {@link FabricDataGenerator} instance
	 */
	void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator);

	/**
	 * Returns the mod ID of the mod the data is being generated for.
	 * A {@code null} return will run the data generator using the mod ID that registered the current entrypoint.
	 *
	 * @return a {@link String} or {@code null}
	 * @throws RuntimeException If the mod ID does not exist.
	 */
	@Nullable
	default String getEffectiveModId() {
		return null;
	}

	/**
	 * Builds a registry containing dynamic registry entries to be generated.
	 * Users should call {@link RegistrySetBuilder#add(ResourceKey, RegistrySetBuilder.RegistryBootstrap)}
	 * to register a bootstrap function, which adds registry entries to be generated.
	 *
	 * <p>This is invoked asynchronously.
	 *
	 * @param registryBuilder a {@link RegistrySetBuilder} instance
	 */
	default void buildRegistry(RegistrySetBuilder registryBuilder) {
	}

	/**
	 * Provides a callback for setting the sort priority of object keys in generated JSON files.
	 * @param callback a callback for setting the sort priority for a given key
	 */
	default void addJsonKeySortOrders(JsonKeySortOrderCallback callback) {
	}
}
