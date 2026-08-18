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

package net.fabricmc.fabric.api.holder.component.v1;

import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.holder.component.FabricDataComponentInitializersImpl;

/// Allows registration of custom [Fabric Data Component Initializers][net.fabricmc.fabric.api.holder.component.v1.FabricDataComponentInitializer].
///
/// [Fabric Data Component Initializers][FabricDataComponentInitializer] are used to add components to [Holders][net.minecraft.core.Holder]. They are run right before the end of a resource reload.
///
/// [FabricDataComponentInitializer] provides more capabilities than [DataComponentInitializers.Initializer]. For example, it allows adding components dynamically based on registries and/or datapack resources.
/// @see net.minecraft.core.component.DataComponentInitializers DataComponentInitializers
/// @see FabricDataComponentInitializer
// TODO: Finish docs
public final class FabricDataComponentInitializers {
	private FabricDataComponentInitializers() {
	}

	/// Registers a data component initializer.
	/// @param id the identifier of the data component initializer
	/// @param initializer the data component initializer
	/// @see #addInitializerOrdering(Identifier, Identifier)
	public static void registerInitializer(Identifier id, FabricDataComponentInitializer initializer) {
		FabricDataComponentInitializersImpl.registerInitializer(id, initializer);
	}

	/// Requests that data component initializers registered as the first identifier is applied before the other referenced initializer.
	///
	/// Incompatible ordering constraints such as cycles will lead to inconsistent behavior: some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
	///
	/// Please keep in mind that this only takes effect during the application stage!
	/// @param first  the identifier of the reload listener that should run before the other
	/// @param second the identifier of the reload listener that should run after the other
	/// @see #registerInitializer(Identifier, FabricDataComponentInitializer) register a new data component initializer
	public static void addInitializerOrdering(Identifier first, Identifier second) {
		FabricDataComponentInitializersImpl.addInitializerOrdering(first, second);
	}
}
