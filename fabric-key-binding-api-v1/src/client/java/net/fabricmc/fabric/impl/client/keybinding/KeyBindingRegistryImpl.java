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

package net.fabricmc.fabric.impl.client.keybinding;

import java.util.List;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public final class KeyBindingRegistryImpl {
	private static final List<KeyBinding> MODDED_KEY_BINDINGS = new ReferenceArrayList<>(); // ArrayList with identity based comparisons for contains/remove/indexOf etc., required for correctly handling duplicate keybinds

	private KeyBindingRegistryImpl() {
	}

	public static KeyBinding registerKeyBinding(KeyBinding binding) {
		if (MinecraftClient.getInstance().options != null) {
			throw new IllegalStateException("GameOptions has already been initialised");
		}

		for (KeyBinding existingKeyBindings : MODDED_KEY_BINDINGS) {
			if (existingKeyBindings == binding) {
				throw new IllegalArgumentException("Attempted to register a key binding twice: " + binding.getId());
			} else if (existingKeyBindings.getId().equals(binding.getId())) {
				throw new IllegalArgumentException("Attempted to register two key bindings with equal ID: " + binding.getId() + "!");
			}
		}

		MODDED_KEY_BINDINGS.add(binding);
		return binding;
	}

	/**
	 * Processes the keybindings array for our modded ones by first removing existing modded keybindings and readding them,
	 * we can make sure that there are no duplicates this way.
	 */
	public static KeyBinding[] process(KeyBinding[] keysAll) {
		List<KeyBinding> newKeysAll = Lists.newArrayList(keysAll);
		newKeysAll.removeAll(MODDED_KEY_BINDINGS);
		newKeysAll.addAll(MODDED_KEY_BINDINGS);
		return newKeysAll.toArray(new KeyBinding[0]);
	}
}
