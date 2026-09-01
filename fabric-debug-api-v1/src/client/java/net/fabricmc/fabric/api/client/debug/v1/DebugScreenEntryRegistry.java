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

package net.fabricmc.fabric.api.client.debug.v1;

import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.debug.client.DebugScreenEntryRegistryImpl;

/// A registry for registering [DebugScreenEntry], allowing for additional
/// entries to the Debug Screen Menu, which opens with f3 + f6.
public final class DebugScreenEntryRegistry {
	/// @param identifier the [Identifier] to register the [DebugScreenEntry] under.
	/// @param debugScreenEntry the [DebugScreenEntry] to register.
	public static void register(Identifier identifier, DebugScreenEntry debugScreenEntry) {
		DebugScreenEntryRegistryImpl.register(identifier, debugScreenEntry);
	}
}
