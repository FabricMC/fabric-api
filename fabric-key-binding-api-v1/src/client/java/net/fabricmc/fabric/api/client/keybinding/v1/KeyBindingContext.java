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

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingContextImpl;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingExtensions;

/**
 * {@link KeyBindingContext} decides how {@link KeyBinding} with same bounded key behaves in regard to each other.
 *
 * <p>Bindings with different context will not conflict with each other even if they have the same bounded key.
 *
 * <p>Along with the provided generic contexts, mods can also create its own context by implementing this interface.
 */
public interface KeyBindingContext {
	/**
	 * {@link KeyBinding} that used in-game. All vanilla key binds have this context.
	 */
	KeyBindingContext IN_GAME = KeyBindingContextImpl.IN_GAME;

	/**
	 * {@link KeyBinding} that used when a screen is open.
	 */
	KeyBindingContext IN_SCREEN = KeyBindingContextImpl.IN_SCREEN;

	/**
	 * {@link KeyBinding} that might be used in any context. This context conflicts with any other context.
	 */
	KeyBindingContext ALL = KeyBindingContextImpl.ALL;

	/**
	 * Returns the context of the key binding.
	 */
	static KeyBindingContext of(KeyBinding binding) {
		return ((KeyBindingExtensions) binding).fabric_getContext();
	}

	/**
	 * Returns whether one context conflicts with the other.
	 */
	static boolean conflicts(KeyBindingContext left, KeyBindingContext right) {
		return left.conflicts(right) || right.conflicts(left);
	}

	/**
	 * Returns whether the key bind can be activated in the current state of the game.
	 * If not, {@link KeyBinding#isPressed()} and {@link KeyBinding#wasPressed()} will always return {@code false}.
	 */
	boolean isActive(MinecraftClient client);

	/**
	 * Returns whether this context conflict with the other.
	 *
	 * <p>Do not call! Use {@link #conflicts(KeyBindingContext, KeyBindingContext)} instead.
	 *
	 * <p>Along with the same instance, most custom context implementation should probably conflict with either
	 * {@link #IN_GAME} or {@link #IN_SCREEN}, unless it needs to be called alongside the generic context.
	 * <pre>return this == other || other == IN_GAME;</pre>
	 */
	@ApiStatus.OverrideOnly
	boolean conflicts(KeyBindingContext other);
}
