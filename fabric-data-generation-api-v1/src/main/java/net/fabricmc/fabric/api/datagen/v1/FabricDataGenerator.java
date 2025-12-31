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

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.loader.api.ModContainer;

/// An extension to vanilla's [DataGenerator] providing mod specific data, and helper functions.
public final class FabricDataGenerator extends DataGenerator {
	private final ModContainer modContainer;
	private final boolean strictValidation;
	private final FabricPackOutput fabricOutput;
	private final CompletableFuture<HolderLookup.Provider> registriesFuture;

	@ApiStatus.Internal
	public FabricDataGenerator(Path output, ModContainer mod, boolean strictValidation, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, SharedConstants.getCurrentVersion(), true);
		this.modContainer = Objects.requireNonNull(mod);
		this.strictValidation = strictValidation;
		this.fabricOutput = new FabricPackOutput(mod, output, strictValidation);
		this.registriesFuture = registriesFuture;
	}

	/// Create a default [Pack] instance for generating a mod's data.
	public Pack createPack() {
		return new Pack(true, modContainer.getMetadata().getName(), this.fabricOutput);
	}

	/// Create a new [Pack] instance for generating a builtin resource pack.
	///
	/// To be used in conjunction with [net.fabricmc.fabric.api.resource.ResourceManagerHelper#registerBuiltinResourcePack]
	///
	/// The path in which the resource pack is generated is `"resourcepacks/<id path>"`. `id path` being the path specified
	/// in the identifier.
	public Pack createBuiltinResourcePack(Identifier id) {
		Path path = this.vanillaPackOutput.getOutputFolder().resolve("resourcepacks").resolve(id.getPath());
		return new Pack(true, id.toString(), new FabricPackOutput(modContainer, path, strictValidation));
	}

	/// Returns the [ModContainer] for the mod that this data generator has been created for.
	///
	/// @return a [ModContainer] instance
	public ModContainer getModContainer() {
		return modContainer;
	}

	/// Returns the mod ID for the mod that this data generator has been created for.
	///
	/// @return a mod ID
	public String getModId() {
		return getModContainer().getMetadata().getId();
	}

	/// When enabled data providers can do strict validation to ensure that all entries have data generated for them.
	///
	/// @return if strict validation should be enabled
	public boolean isStrictValidationEnabled() {
		return strictValidation;
	}

	/// Get a future returning the default registries produced by [VanillaRegistries] and
	/// [DataGeneratorEntrypoint#buildRegistry(RegistrySetBuilder)].
	///
	/// Generally one does not need direct access to the registries, and instead can pass them directly to a
	/// [DataProvider] by using [Pack#addProvider(Pack.RegistryDependentFactory)]. However, this method may
	/// be useful when extending the vanilla registries (such as with [RegistryPatchGenerator]).
	///
	/// @return A future containing the builtin registries.
	public CompletableFuture<HolderLookup.Provider> getRegistries() {
		return registriesFuture;
	}

	/// @deprecated Please use [FabricDataGenerator#createPack()]
	@Override
	@Deprecated
	public DataGenerator.PackGenerator getVanillaPack(boolean shouldRun) {
		throw new UnsupportedOperationException();
	}

	/// @deprecated Please use [FabricDataGenerator#createBuiltinResourcePack(Identifier)]
	@Override
	@Deprecated
	public DataGenerator.PackGenerator getBuiltinDatapack(boolean shouldRun, String packName) {
		throw new UnsupportedOperationException();
	}

	/// Represents a pack of generated data (i.e. data pack or resource pack). Providers are added to a pack.
	public final class Pack extends DataGenerator.PackGenerator {
		private Pack(boolean shouldRun, String name, FabricPackOutput output) {
			super(shouldRun, name, output);
		}

		/// Registers a constructor of [DataProvider] which takes a [FabricPackOutput].
		///
		/// @return the [DataProvider]
		public <T extends DataProvider> T addProvider(Factory<T> factory) {
			return super.addProvider(output -> factory.create((FabricPackOutput) output));
		}

		/// Registers a constructor of [DataProvider] which takes a [FabricPackOutput] and the registries.
		/// This is used, for example, with [FabricTagsProvider].
		///
		/// @return the [DataProvider]
		public <T extends DataProvider> T addProvider(RegistryDependentFactory<T> factory) {
			return super.addProvider(output -> factory.create((FabricPackOutput) output, registriesFuture));
		}

		/// A factory of a data provider. This is usually the constructor.
		@FunctionalInterface
		public interface Factory<T extends DataProvider> {
			T create(FabricPackOutput output);
		}

		/// A factory of a data provider. This is usually the constructor.
		/// The provider has access to the registries.
		@FunctionalInterface
		public interface RegistryDependentFactory<T extends DataProvider> {
			T create(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture);
		}
	}
}
