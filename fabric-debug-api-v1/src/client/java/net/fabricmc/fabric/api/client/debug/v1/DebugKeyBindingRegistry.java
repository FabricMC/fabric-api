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

import net.minecraft.client.KeyMapping;

import net.fabricmc.fabric.impl.debug.client.DebugKeyBindingRegistryImpl;

/// A registry for debug keybindings on the client, allowing the
/// creation keybindings only accessed when the debug modifier key is
/// pressed (f3 is the default debug modifier key).
public final class DebugKeyBindingRegistry {
	/// @param keyMapping the [KeyMapping] that when pressed along with the debug modifier key runs the handler's function.
	/// @param handler the logic to run when the [KeyMapping] is pressed along with the debug modifier key.
	public static void register(KeyMapping keyMapping, DebugKeyHandler handler) {
		DebugKeyBindingRegistryImpl.register(keyMapping, handler);
	}

	/// Returning `true` indicates that the key press was handled and performed
	/// an action. When `true`, the game will consume the debug key combination,
	/// and the key will retain its normal behavior when pressed without the
	/// debug modifier.
	@FunctionalInterface
	public interface DebugKeyHandler {
		boolean onDebugKey();
	}
}
