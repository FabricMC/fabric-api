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

package net.fabricmc.fabric.api.event.registry;

import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.serialization.Lifecycle;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public final class BootstrapHelper {
	private BootstrapHelper() {
	}

	private static final class UniversalOwner<T> implements HolderOwner<T> {
		@Override
		public boolean canSerializeIn(HolderOwner<T> context) {
			return true;
		}
	}

	/// creates a {@link Holder.Reference} to stand in for a {@link Holder.Reference} not available when bootstrapping
	/// @param <T> the type of the backing registry
	/// @param key the key of the {@link Holder.Reference}
	public static <T> Holder.Reference<T> createStandInReference(ResourceKey<T> key) {
		return Holder.Reference.createStandAlone(new UniversalOwner<>(), key);
	}

	/// creates a {@link HolderSet.Named} to stand in for a {@link HolderSet.Named} not available when bootstrapping
	/// @param <T> the type of the backing registry
	/// @param key the key of the {@link HolderSet.Named}
	public static <T> HolderSet.Named<T> createStandInHolderSet(TagKey<T> key) {
		return HolderSet.emptyNamed(new UniversalOwner<>(), key);
	}

	/// Creates a lookup from a registry set builder.
	/// the created lookup includes the vanilla bootstraps, as well as any bootstraps introduced by the setup function
	/// @param setup the setup to run on the builder
	public static HolderLookup.Provider createBootstrappingLookup(Consumer<RegistrySetBuilder> setup) {
		return createEmptyBootstrappingLookup(registries -> setup.accept(registries.withVanillaBootstraps()));
	}

	/// Creates a lookup from a Consumer operating on a registry set builder.
	/// the created lookup includes the result of any bootstraps introduced by the setup function
	/// @param setup the setup to run on the builder
	public static HolderLookup.Provider createEmptyBootstrappingLookup(Consumer<RegistrySetBuilder> setup) {
		RegistrySetBuilder builder = new RegistrySetBuilder();

		for (RegistryDataLoader.RegistryData<?> entry : DynamicRegistries.getBootstrappingRegistries()) {
			builder.add(entry.key(), Lifecycle.stable(), Function.identity()::apply);
		}

		var setupBuilder = new RegistrySetBuilder();
		setup.accept(setupBuilder);
		builder.withBootstrapsFrom(setupBuilder);

		HolderLookup.Provider registryLookup = builder.build(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

		if (registryLookup.lookup(Registries.BIOME).isPresent() && registryLookup.lookup(Registries.PLACED_FEATURE).isPresent()) {
			VanillaRegistries.validateThatAllBiomeFeaturesHaveBiomeFilter(registryLookup);
		}

		return registryLookup;
	}
}
