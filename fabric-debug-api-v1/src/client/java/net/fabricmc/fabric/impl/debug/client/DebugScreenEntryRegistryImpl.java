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

package net.fabricmc.fabric.impl.debug.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;

public class DebugScreenEntryRegistryImpl {
	private static final Map<Identifier, DebugScreenEntry> ADDITIONAL_DEBUG_SCREEN_ENTRIES = new ConcurrentHashMap<>();

	public static void register(Identifier identifier, DebugScreenEntry debugScreenEntry) {
		if (ADDITIONAL_DEBUG_SCREEN_ENTRIES.containsKey(identifier)) {
			throw new IllegalStateException(
					"Identifier `" + identifier.toString() + "` is already registered"
			);
		}

		ADDITIONAL_DEBUG_SCREEN_ENTRIES.put(identifier, debugScreenEntry);
	}

	public static void addEntries(Map<Identifier, DebugScreenEntry> entries) {
		entries.putAll(ADDITIONAL_DEBUG_SCREEN_ENTRIES);
	}
}
