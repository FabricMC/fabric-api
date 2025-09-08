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

package net.fabricmc.fabric.api.client.screen.v1;

import java.util.Objects;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;

/**
 * Events related to use of the keyboard in a {@link Screen}.
 *
 * <p>All of these events work on top of a specific screen instance.
 * Subscriptions will only last as long as the screen itself, they'll disappear once the screen gets refreshed, closed or replaced.
 * Use {@link ScreenEvents#BEFORE_INIT} to register the desired events every time it is necessary.
 *
 * <p>Events are fired in the following order:
 * <pre>{@code AllowX -> BeforeX -> AfterX}</pre>
 * If the result of the Allow event is false, then Before and After are not called.
 *
 * @see ScreenEvents
 */
public final class ScreenKeyboardEvents {
	/**
	 * An event that checks if a key press should be allowed.
	 *
	 * @return the event
	 */
	public static Event<AllowKeyPress> allowKeyPress(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAllowKeyPressEvent();
	}

	/**
	 * An event that is called before a key press is processed for a screen.
	 *
	 * @return the event
	 */
	public static Event<BeforeKeyPress> beforeKeyPress(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getBeforeKeyPressEvent();
	}

	/**
	 * An event that is called after a key press is processed for a screen.
	 *
	 * @return the event
	 */
	public static Event<AfterKeyPress> afterKeyPress(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAfterKeyPressEvent();
	}

	/**
	 * An event that checks if a pressed key should be allowed to release.
	 *
	 * @return the event
	 */
	public static Event<AllowKeyRelease> allowKeyRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAllowKeyReleaseEvent();
	}

	/**
	 * An event that is called after the release of a key is processed for a screen.
	 *
	 * @return the event
	 */
	public static Event<BeforeKeyRelease> beforeKeyRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getBeforeKeyReleaseEvent();
	}

	/**
	 * An event that is called after the release a key is processed for a screen.
	 *
	 * @return the event
	 */
	public static Event<AfterKeyRelease> afterKeyRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAfterKeyReleaseEvent();
	}

	private ScreenKeyboardEvents() {
	}

	@FunctionalInterface
	public interface AllowKeyPress {
		/**
		 * Checks if a key should be allowed to be pressed.
		 *
		 * @param context the context of the key press, containing the key, scancode and modifiers
		 * @return whether the key press should be processed
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		boolean allowKeyPress(Screen screen, KeyInput context);
	}

	@FunctionalInterface
	public interface BeforeKeyPress {
		/**
		 * Called before a key press is handled.
		 *
		 * @param context the context of the key press, containing the key, scancode and modifiers
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		void beforeKeyPress(Screen screen, KeyInput context);
	}

	@FunctionalInterface
	public interface AfterKeyPress {
		/**
		 * Called after a key press is handled.
		 *
		 * @param context the context of the key press, containing the key, scancode and modifiers
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		void afterKeyPress(Screen screen, KeyInput context);
	}

	@FunctionalInterface
	public interface AllowKeyRelease {
		/**
		 * Checks if a pressed key should be allowed to be released.
		 *
		 * @param context the context of the key press, containing the key, scancode and modifiers
		 * @return whether the key press should be released
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		boolean allowKeyRelease(Screen screen, KeyInput context);
	}

	@FunctionalInterface
	public interface BeforeKeyRelease {
		/**
		 * Called before a pressed key has been released.
		 *
		 * @param context the context of the key press, containing the key, scancode and modifiers
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		void beforeKeyRelease(Screen screen, KeyInput context);
	}

	@FunctionalInterface
	public interface AfterKeyRelease {
		/**
		 * Called after a pressed key has been released.
		 *
		 * @param context the context of the key press, containing the key, scancode and modifiers
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		void afterKeyRelease(Screen screen, KeyInput context);
	}
}
