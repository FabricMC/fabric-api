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

import net.minecraft.client.MinecraftClient;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingContext;

public class KeyBindingContextImpl {
	public static final KeyBindingContext IN_GAME = new KeyBindingContext() {
		@Override
		public boolean isActive(MinecraftClient client) {
			return client.currentScreen == null;
		}

		@Override
		public boolean conflicts(KeyBindingContext other) {
			return this == other;
		}
	};

	public static final KeyBindingContext IN_SCREEN = new KeyBindingContext() {
		@Override
		public boolean isActive(MinecraftClient client) {
			return client.currentScreen != null;
		}

		@Override
		public boolean conflicts(KeyBindingContext other) {
			return this == other;
		}
	};

	public static final KeyBindingContext ALL = new KeyBindingContext() {
		@Override
		public boolean isActive(MinecraftClient client) {
			return true;
		}

		@Override
		public boolean conflicts(KeyBindingContext other) {
			return true;
		}
	};
}
