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

package net.fabricmc.fabric.api.client.keymapping.v1;

import java.util.Objects;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

import net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl;
import net.fabricmc.fabric.mixin.client.keymapping.KeyMappingAccessor;

/**
 * Helper for registering {@link KeyMapping}s.
 *
 * <pre>{@code
 * KeyMapping left = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.example.left", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, KeyMapping.Category.MISC));
 * KeyMapping right = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.example.right", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, KeyMapping.Category.MISC));
 * }</pre>
 *
 * @see KeyMapping
 * @see net.minecraft.client.ToggleKeyMapping
 */
public final class KeyMappingHelper {
	private KeyMappingHelper() {
	}

	/**
	 * Registers the keymapping and add the keymapping category if required.
	 *
	 * @param keyMapping the keymapping
	 * @return the keymapping itself
	 * @throws IllegalArgumentException when a key mapping with the same ID is already registered
	 */
	public static KeyMapping registerKeyMapping(KeyMapping keyMapping) {
		Objects.requireNonNull(keyMapping, "key mapping cannot be null");
		return KeyMappingRegistryImpl.registerKeyMapping(keyMapping);
	}

	/**
	 * Returns the configured KeyCode bound to the KeyMapping from the player's settings.
	 *
	 * @param keyMapping the keymapping
	 * @return configured KeyCode
	 */
	public static InputConstants.Key getBoundKeyOf(KeyMapping keyMapping) {
		return ((KeyMappingAccessor) keyMapping).fabric_getBoundKey();
	}
}
