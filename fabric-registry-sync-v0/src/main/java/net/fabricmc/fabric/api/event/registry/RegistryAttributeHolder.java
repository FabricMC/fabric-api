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

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.impl.registry.sync.RegistryAttributeImpl;

/// Holds the [RegistryAttribute]s associated with a registry.
@ApiStatus.NonExtendable
public interface RegistryAttributeHolder {
	/// Get the attribute holder for a registry key.
	///
	/// @param key the registry's [ResourceKey]
	/// @return the attribute holder associated with the registry key
	static RegistryAttributeHolder get(ResourceKey<?> key) {
		return RegistryAttributeImpl.getHolder(key);
	}

	/// Get the attribute holder for a registry.
	///
	/// @param registry the registry whose attributes to access
	/// @return the attribute holder associated with the registry's key
	static RegistryAttributeHolder get(Registry<?> registry) {
		return get(registry.key());
	}

	/// Add a [RegistryAttribute] to the registry.
	///
	/// Adding an attribute that is already present has no effect.
	///
	/// @param attribute the [RegistryAttribute] to add to the registry
	/// @return this holder
	/// @throws IllegalArgumentException if an attribute other than [RegistryAttribute#MODDED] is added to a client registry
	RegistryAttributeHolder addAttribute(RegistryAttribute attribute);

	/// Check whether the registry has a [RegistryAttribute].
	///
	/// @param attribute the [RegistryAttribute] to check
	/// @return `true` if the registry has the attribute, otherwise `false`
	boolean hasAttribute(RegistryAttribute attribute);
}
