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

package net.fabricmc.fabric.api.client.keybinding;

import net.minecraft.client.option.KeyBinding;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;

/**
 * Interface for registering key bindings.
 *
 * @see KeyBinding
 * @deprecated Please migrate to v1. Please use {@link KeyBindingHelper} instead.
 */
@Deprecated
public interface KeyBindingRegistry {
	KeyBindingRegistry INSTANCE = new KeyBindingRegistry() {
		@Override
		public boolean addCategory(String categoryName) {
			return KeyBindingRegistryImpl.addCategory(categoryName);
		}

		@Override
		public boolean register(FabricKeyBinding binding) {
			return KeyBindingHelper.registerKeyBinding(binding) != null;
		}
	};

	/**
	 * Add a new key binding category.
	 *
	 * @param categoryName The key binding category name.
	 * @return True if a new category was added.
	 */
	boolean addCategory(String categoryName);

	/**
	 * Register a new key binding.
	 *
	 * @param binding The key binding.
	 * @return True if a new key binding was registered.
	 */
	boolean register(FabricKeyBinding binding);
}
