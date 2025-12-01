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

import java.util.EnumSet;

import com.mojang.serialization.Lifecycle;

import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.mixin.registry.sync.BuiltInRegistriesAccessor;

/**
 * Used to create custom registries, with specified registry attributes.
 *
 * <p>See the following example for creating a {@link Registry} of String objects.
 *
 * <pre>
 * {@code
 *  RegistryKey<Registry<String>> registryKey = RegistryKey.ofRegistry(Identifier.fromNamespaceAndPath("modid", "custom_registry"));
 *  Registry<String> registry = FabricRegistryBuilder.createSimple(registryKey)
 * 													.attribute(RegistryAttribute.SYNCED)
 * 													.buildAndRegister();
 * 	}
 * </pre>
 *
 * <p>Tags for the entries of a custom registry must be placed in
 * {@code /tags/<registry namespace>/<registry path>/}. For example, the tags for the example
 * registry above would be placed in {@code /tags/modid/registry_name/}.
 *
 * @param <T> The type stored in the Registry
 * @param <R> The registry type
 */
public final class FabricRegistryBuilder<T, R extends WritableRegistry<T>> {
	/**
	 * Create a new {@link FabricRegistryBuilder}, the registry has the {@link RegistryAttribute#MODDED} attribute by default.
	 *
	 * @param registry The base registry type such as {@link net.minecraft.core.MappedRegistry} or {@link net.minecraft.core.DefaultedRegistry}
	 * @param <T> The type stored in the Registry
	 * @param <R> The registry type
	 * @return An instance of FabricRegistryBuilder
	 */
	public static <T, R extends WritableRegistry<T>> FabricRegistryBuilder<T, R> from(R registry) {
		return new FabricRegistryBuilder<>(registry);
	}

	/**
	 * Create a new {@link FabricRegistryBuilder} using a {@link MappedRegistry}, the registry has the {@link RegistryAttribute#MODDED} attribute by default.
	 *
	 * @param registryKey The registry {@link ResourceKey}
	 * @param <T> The type stored in the Registry
	 * @return An instance of FabricRegistryBuilder
	 */
	public static <T> FabricRegistryBuilder<T, MappedRegistry<T>> createSimple(ResourceKey<Registry<T>> registryKey) {
		return from(new MappedRegistry<>(registryKey, Lifecycle.stable(), false));
	}

	/**
	 * Create a new {@link FabricRegistryBuilder} using a {@link DefaultedRegistry}, the registry has the {@link RegistryAttribute#MODDED} attribute by default.
	 *
	 * @param registryKey The registry {@link ResourceKey}
	 * @param defaultId The default registry id
	 * @param <T> The type stored in the Registry
	 * @return An instance of FabricRegistryBuilder
	 */
	public static <T> FabricRegistryBuilder<T, DefaultedMappedRegistry<T>> createDefaulted(ResourceKey<Registry<T>> registryKey, Identifier defaultId) {
		return from(new DefaultedMappedRegistry<T>(defaultId.toString(), registryKey, Lifecycle.stable(), false));
	}

	/**
	 * Create a new {@link FabricRegistryBuilder} using a {@link MappedRegistry}, the registry has the {@link RegistryAttribute#MODDED} attribute by default.
	 *
	 * @param registryId The registry {@link Identifier} used as the registry id
	 * @param <T> The type stored in the Registry
	 * @return An instance of FabricRegistryBuilder
	 * @deprecated Please migrate to {@link FabricRegistryBuilder#createSimple(ResourceKey)}
	 */
	@Deprecated
	public static <T> FabricRegistryBuilder<T, MappedRegistry<T>> createSimple(Class<T> type, Identifier registryId) {
		return createSimple(ResourceKey.createRegistryKey(registryId));
	}

	/**
	 * Create a new {@link FabricRegistryBuilder} using a {@link DefaultedRegistry}, the registry has the {@link RegistryAttribute#MODDED} attribute by default.
	 *
	 * @param registryId The registry {@link Identifier} used as the registry id
	 * @param defaultId The default registry id
	 * @param <T> The type stored in the Registry
	 * @return An instance of FabricRegistryBuilder
	 * @deprecated Please migrate to {@link FabricRegistryBuilder#createDefaulted(ResourceKey, Identifier)}
	 */
	@Deprecated
	public static <T> FabricRegistryBuilder<T, DefaultedMappedRegistry<T>> createDefaulted(Class<T> type, Identifier registryId, Identifier defaultId) {
		return createDefaulted(ResourceKey.createRegistryKey(registryId), defaultId);
	}

	private final R registry;
	private final EnumSet<RegistryAttribute> attributes = EnumSet.noneOf(RegistryAttribute.class);

	private FabricRegistryBuilder(R registry) {
		this.registry = registry;
		attribute(RegistryAttribute.MODDED);
	}

	/**
	 * Add a {@link RegistryAttribute} to the registry.
	 *
	 * @param attribute the {@link RegistryAttribute} to add to the registry
	 * @return the instance of {@link FabricRegistryBuilder}
	 */
	public FabricRegistryBuilder<T, R> attribute(RegistryAttribute attribute) {
		attributes.add(attribute);
		return this;
	}

	/**
	 * Applies the attributes to the registry and registers it.
	 * @return the registry instance with the attributes applied
	 */
	public R buildAndRegister() {
		final ResourceKey<?> key = registry.key();

		for (RegistryAttribute attribute : attributes) {
			RegistryAttributeHolder.get(key).addAttribute(attribute);
		}

		//noinspection unchecked
		BuiltInRegistriesAccessor.getWRITABLE_REGISTRY().register((ResourceKey<WritableRegistry<?>>) key, registry, RegistrationInfo.BUILT_IN);

		return registry;
	}
}
