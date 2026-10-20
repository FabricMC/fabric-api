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

package net.fabricmc.fabric.api.client.registry.v0;

import java.util.EnumSet;

import com.mojang.serialization.Lifecycle;

import net.minecraft.client.core.registries.BuiltInClientRegistries;
import net.minecraft.client.core.registries.ClientRegistries;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;

/// Used to create custom client registries, with specified registry attributes.
///
/// See the following example for creating a [Registry] of String objects.
///
/// ```java
///  ResourceKey<Registry<String>> key = ResourceKey.createRegistryKey(
///          ClientRegistries.ROOT_REGISTRY_NAME,
///          Identifier.fromNamespaceAndPath("modid", "custom_registry")
///  );
///  Registry<String> registry = FabricClientRegistryBuilder.create(key)
///          .buildAndRegister();
///  Registry.register(registry, Identifier.fromNamespaceAndPath("modid", "example"), "example");
/// ```
///
/// Registry keys must use [ClientRegistries#ROOT_REGISTRY_NAME] as their root.
///
/// @param <T> The type stored in the Registry
/// @param <R> The registry type
public final class FabricClientRegistryBuilder<T, R extends WritableRegistry<T>> {
	/// Create a new [FabricClientRegistryBuilder], the registry has the [RegistryAttribute#MODDED] attribute by default.
	///
	/// @param registry The base registry type such as [net.minecraft.core.MappedRegistry] or [net.minecraft.core.DefaultedRegistry]
	/// @param <T> The type stored in the Registry
	/// @param <R> The registry type
	/// @return An instance of FabricClientRegistryBuilder
	/// @throws IllegalArgumentException if the registry key does not use the client registry root
	public static <T, R extends WritableRegistry<T>> FabricClientRegistryBuilder<T, R> from(R registry) {
		return new FabricClientRegistryBuilder<>(registry);
	}

	/// Create a new [FabricClientRegistryBuilder] using a [MappedRegistry], the registry has the [RegistryAttribute#MODDED] attribute by default.
	///
	/// @param key The registry's [ResourceKey]
	/// @param <T> The type stored in the Registry
	/// @return An instance of FabricClientRegistryBuilder
	/// @throws IllegalArgumentException if the registry key does not use the client registry root
	public static <T> FabricClientRegistryBuilder<T, MappedRegistry<T>> create(ResourceKey<Registry<T>> key) {
		return from(new MappedRegistry<>(key, Lifecycle.stable(), false));
	}

	/// Create a new [FabricClientRegistryBuilder] using a [net.minecraft.core.DefaultedRegistry], the registry has the [RegistryAttribute#MODDED] attribute by default.
	///
	/// @param key The registry's [ResourceKey]
	/// @param defaultId The default registry id
	/// @param <T> The type stored in the Registry
	/// @return An instance of FabricClientRegistryBuilder
	/// @throws IllegalArgumentException if the registry key does not use the client registry root
	public static <T> FabricClientRegistryBuilder<T, DefaultedMappedRegistry<T>> createDefaulted(ResourceKey<Registry<T>> key, Identifier defaultId) {
		return from(new DefaultedMappedRegistry<>(defaultId.toString(), key, Lifecycle.stable(), false));
	}

	private final R registry;
	private final EnumSet<RegistryAttribute> attributes = EnumSet.noneOf(RegistryAttribute.class);

	private FabricClientRegistryBuilder(R registry) {
		if (!registry.key().registry().equals(ClientRegistries.ROOT_REGISTRY_NAME)) {
			throw new IllegalArgumentException("Client registry key must use " + ClientRegistries.ROOT_REGISTRY_NAME + " as its root: " + registry.key());
		}

		this.registry = registry;
		attribute(RegistryAttribute.MODDED);
	}

	/// Add a [RegistryAttribute] to the registry.
	///
	/// Client registries only support [RegistryAttribute#MODDED].
	///
	/// @param attribute the [RegistryAttribute] to add to the registry
	/// @return the instance of [FabricClientRegistryBuilder]
	public FabricClientRegistryBuilder<T, R> attribute(RegistryAttribute attribute) {
		attributes.add(attribute);
		return this;
	}

	/// Applies the attributes to the registry and registers it.
	/// @return the registry instance with the attributes applied
	/// @throws IllegalArgumentException if an attribute other than [RegistryAttribute#MODDED] was added
	public R buildAndRegister() {
		final ResourceKey<?> key = registry.key();

		for (RegistryAttribute attribute : attributes) {
			RegistryAttributeHolder.get(key).addAttribute(attribute);
		}

		//noinspection unchecked
		BuiltInClientRegistries.BUILDER.registry.register((ResourceKey<WritableRegistry<?>>) key, registry, RegistrationInfo.BUILT_IN);

		return registry;
	}
}
