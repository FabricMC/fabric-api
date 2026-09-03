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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingContext;
import net.fabricmc.fabric.mixin.client.keybinding.KeyBindingAccessor;

public final class KeyBindingRegistryImpl {
	public static final Map<InputUtil.Key, List<KeyBinding>> KEY_TO_BINDINGS = new HashMap<>();
	private static final List<KeyBinding> MODDED_KEY_BINDINGS = new ReferenceArrayList<>(); // ArrayList with identity based comparisons for contains/remove/indexOf etc., required for correctly handling duplicate keybinds

	private KeyBindingRegistryImpl() {
	}

	private static Map<String, Integer> getCategoryMap() {
		return KeyBindingAccessor.fabric_getCategoryMap();
	}

	public static boolean addCategory(String categoryTranslationKey) {
		Map<String, Integer> map = getCategoryMap();

		if (map.containsKey(categoryTranslationKey)) {
			return false;
		}

		Optional<Integer> largest = map.values().stream().max(Integer::compareTo);
		int largestInt = largest.orElse(0);
		map.put(categoryTranslationKey, largestInt + 1);
		return true;
	}

	public static KeyBinding registerKeyBinding(KeyBinding binding, KeyBindingContext context) {
		if (MinecraftClient.getInstance().options != null) {
			throw new IllegalStateException("GameOptions has already been initialised");
		}

		for (KeyBinding existingKeyBindings : MODDED_KEY_BINDINGS) {
			if (existingKeyBindings == binding) {
				throw new IllegalArgumentException("Attempted to register a key binding twice: " + binding.getTranslationKey());
			} else if (existingKeyBindings.getTranslationKey().equals(binding.getTranslationKey())) {
				throw new IllegalArgumentException("Attempted to register two key bindings with equal ID: " + binding.getTranslationKey() + "!");
			}
		}

		// This will do nothing if the category already exists.
		addCategory(binding.getCategory());
		((KeyBindingExtensions) binding).fabric_setContext(context);
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

	public static void putToMap(InputUtil.Key key, KeyBinding binding) {
		KEY_TO_BINDINGS.computeIfAbsent(key, k -> new ArrayList<>()).add(binding);
	}
}
