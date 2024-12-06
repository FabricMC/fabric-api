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

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * General-purpose Fabric-provided extensions for {@link Registry} objects.
 *
 * <p>Note: This interface is automatically implemented on all registries via Mixin and interface injection.</p>
 */
@ApiStatus.NonExtendable
public interface FabricRegistry {
	/**
	 * Adds an alias for an entry in this registry. Once added, all queries to this registry that refer to the {@code old}
	 * {@link Identifier} will be redirected towards {@code newId}. This is useful if a mod wants to change an ID without
	 * breaking compatibility with existing worlds.
	 * @param old the {@link Identifier} that will become an alias for {@code newId}
	 * @param newId the {@link Identifier} for which {@code old} will become an alias
	 */
	default void addAlias(Identifier old, Identifier newId) {
		throw new UnsupportedOperationException("implemented via mixin");
	}
}
