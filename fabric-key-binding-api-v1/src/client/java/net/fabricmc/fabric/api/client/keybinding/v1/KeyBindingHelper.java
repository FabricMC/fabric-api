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

package net.fabricmc.fabric.api.client.keybinding.v1;

import java.util.Objects;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.fabric.mixin.client.keybinding.KeyBindingAccessor;

/**
 * Helper for registering {@link KeyBinding}s.
 *
 * <pre>{@code
 * KeyBinding left = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.example.left", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, KeyBinding.Category.MISC));
 * KeyBinding right = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.example.right", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, KeyBinding.Category.MISC));
 * }</pre>
 *
 * @see KeyBinding
 * @see net.minecraft.client.option.StickyKeyBinding
 */
public final class KeyBindingHelper {
	private KeyBindingHelper() {
	}

	/**
	 * Registers the keybinding and add the keybinding category if required.
	 *
	 * @param keyBinding the keybinding
	 * @return the keybinding itself
	 * @throws IllegalArgumentException when a key binding with the same ID is already registered
	 */
	public static KeyBinding registerKeyBinding(KeyBinding keyBinding) {
		Objects.requireNonNull(keyBinding, "key binding cannot be null");
		return KeyBindingRegistryImpl.registerKeyBinding(keyBinding);
	}

	/**
	 * Returns the configured KeyCode bound to the KeyBinding from the player's settings.
	 *
	 * @param keyBinding the keybinding
	 * @return configured KeyCode
	 */
	public static InputUtil.Key getBoundKeyOf(KeyBinding keyBinding) {
		return ((KeyBindingAccessor) keyBinding).fabric_getBoundKey();
	}
}
