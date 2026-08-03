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

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;

public class DebugScreenProfilesImpl {
	private static final Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> pending = new HashMap<>();
	private static boolean frozen = false;

	public static void register(DebugScreenProfile profile, Identifier identifier, DebugScreenEntryStatus status) {
		checkNotFrozen();
		pending.computeIfAbsent(profile, _ -> new HashMap<>()).put(identifier, status);
	}

	private static void checkNotFrozen() {
		if (frozen) {
			throw new IllegalStateException(
					"Cannot register debug profile entries after DebugScreenEntries has initialized. Register during mod init.");
		}
	}

	public static Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> invoke(
			Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> base
	) {
		frozen = true;

		Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> result = new HashMap<>();
		base.forEach((profile, entries) -> result.put(profile, new HashMap<>(entries)));

		pending.forEach((profile, extra) ->
				result.computeIfAbsent(profile, _ -> new HashMap<>()).putAll(extra));

		Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> ImmutableCopy = new HashMap<>();
		result.forEach((profile, entries) -> ImmutableCopy.put(profile, Map.copyOf(entries)));

		return Map.copyOf(ImmutableCopy);
	}
}
