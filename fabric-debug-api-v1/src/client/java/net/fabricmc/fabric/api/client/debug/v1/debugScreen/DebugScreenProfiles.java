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

package net.fabricmc.fabric.api.client.debug.v1.debugScreen;

import net.fabricmc.fabric.impl.debug.client.debugScreen.DebugScreenProfilesImpl;

import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;

/// Allows mods to edit the default profiles inside the Debug Screen.
/// This would allow mods to add a [DebugScreenEntry] to the f3 menu.
public class DebugScreenProfiles {
	/// @apiNote Must be called during mod initialization, before debug screen entries are finalized.
	/// @param identifier the [Identifier] of the [DebugScreenEntry] to set.
	/// @param profile the profile to set the [DebugScreenEntry] under.
	/// @param status the default status, or setting, of the [DebugScreenEntry].
	public static void set(Identifier identifier, DebugScreenProfile profile, DebugScreenEntryStatus status) {
		DebugScreenProfilesImpl.register(profile, identifier, status);
	}
}
