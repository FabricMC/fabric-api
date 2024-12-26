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

package net.fabricmc.fabric.api.client.gametest.v1;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * The client gametest input handler used to simulate inputs to the client.
 */
@ApiStatus.NonExtendable
public interface TestInput {
	/**
	 * Starts holding down a key binding. The key binding will be held until it is released. The key binding must be
	 * bound. Does nothing if the key binding is already being held.
	 *
	 * @param keyBinding The key binding to hold
	 * @see #releaseKey(KeyBinding)
	 * @see #pressKey(KeyBinding)
	 * @see #holdKey(Function)
	 */
	void holdKey(KeyBinding keyBinding);

	/**
	 * Starts holding down a key binding. The key binding will be held until it is released. The key binding must be
	 * bound. Does nothing if the key binding is already being held.
	 *
	 * @param keyBindingGetter The function to get the key binding from the game options
	 * @see #releaseKey(Function)
	 * @see #pressKey(Function)
	 * @see #holdKey(KeyBinding)
	 */
	void holdKey(Function<GameOptions, KeyBinding> keyBindingGetter);

	/**
	 * Starts holding down a key or mouse button. The key will be held until it is released. Does nothing if the key or
	 * mouse button is already being held.
	 *
	 * @param key The key or mouse button to hold
	 * @see #releaseKey(InputUtil.Key)
	 * @see #pressKey(InputUtil.Key)
	 */
	void holdKey(InputUtil.Key key);

	/**
	 * Starts holding down a key. The key will be held until it is released. Does nothing if the key is already being
	 * held.
	 *
	 * @param keyCode The key code of the key to hold
	 * @see #releaseKey(int)
	 * @see #pressKey(int)
	 */
	void holdKey(int keyCode);

	/**
	 * Starts holding down a mouse button. The mouse button will be held until it is released. Does nothing if the mouse
	 * button is already being held.
	 *
	 * @param button The mouse button to hold
	 * @see #releaseMouse(int)
	 * @see #pressMouse(int)
	 */
	void holdMouse(int button);

	/**
	 * Starts holding down left control, or left super on macOS. Suitable for triggering
	 * {@link Screen#hasControlDown()}. The key will be held until it is released. Does nothing if the key is already
	 * being held.
	 *
	 * @see #releaseControl()
	 */
	void holdControl();

	/**
	 * Starts holding down left shift. Suitable for triggering {@link Screen#hasShiftDown()}. The key will be held until
	 * it is released. Does nothing if the key is already being held.
	 *
	 * @see #releaseShift()
	 */
	void holdShift();

	/**
	 * Starts holding down left alt. Suitable for triggering {@link Screen#hasAltDown()}. The key will be held until it
	 * is released. Does nothing if the key is already being held.
	 *
	 * @see #releaseAlt()
	 */
	void holdAlt();

	/**
	 * Releases a key binding. The key binding must be bound. Does nothing if the key binding is not being held.
	 *
	 * @param keyBinding The key binding to release
	 * @see #holdKey(KeyBinding)
	 * @see #releaseKey(Function)
	 */
	void releaseKey(KeyBinding keyBinding);

	/**
	 * Releases a key binding. The key binding must be bound. Does nothing if the key binding is not being held.
	 *
	 * @param keyBindingGetter The function to get the key binding from the game options
	 * @see #holdKey(Function)
	 * @see #releaseKey(KeyBinding)
	 */
	void releaseKey(Function<GameOptions, KeyBinding> keyBindingGetter);

	/**
	 * Releases a key or mouse button. Does nothing if the key or mouse button is not being held.
	 *
	 * @param key The key or mouse button to release
	 * @see #holdKey(InputUtil.Key)
	 */
	void releaseKey(InputUtil.Key key);

	/**
	 * Releases a key. Does nothing if the key is not being held.
	 *
	 * @param keyCode The GLFW key code of the key to release
	 * @see #holdKey(int)
	 */
	void releaseKey(int keyCode);

	/**
	 * Releases a mouse button. Does nothing if the mouse button is not being held.
	 *
	 * @param button The GLFW mouse button to release
	 * @see #holdMouse(int)
	 */
	void releaseMouse(int button);

	/**
	 * Releases left control, or left super on macOS. Suitable for un-triggering {@link Screen#hasControlDown()}. Does
	 * nothing if the key is not being held.
	 *
	 * @see #holdControl()
	 */
	void releaseControl();

	/**
	 * Releases left shift. Suitable for un-triggering {@link Screen#hasShiftDown()}. Does nothing if the key is not
	 * being held.
	 *
	 * @see #holdShift()
	 */
	void releaseShift();

	/**
	 * Releases left alt. Suitable for un-triggering {@link Screen#hasAltDown()}. Does nothing if the key is not being
	 * held.
	 *
	 * @see #holdAlt()
	 */
	void releaseAlt();

	/**
	 * Presses and releases a key binding. The key binding must be bound.
	 *
	 * @param keyBinding The key binding to press
	 * @see #holdKey(KeyBinding)
	 * @see #pressKey(Function)
	 */
	void pressKey(KeyBinding keyBinding);

	/**
	 * Presses and releases a key binding. The key binding must be bound.
	 *
	 * @param keyBindingGetter The function to get the key binding from the game options
	 * @see #holdKey(Function)
	 * @see #pressKey(KeyBinding)
	 */
	void pressKey(Function<GameOptions, KeyBinding> keyBindingGetter);

	/**
	 * Presses and releases a key or mouse button.
	 *
	 * @param key The key or mouse button to press.
	 * @see #holdKey(InputUtil.Key)
	 */
	void pressKey(InputUtil.Key key);

	/**
	 * Presses and releases a key.
	 *
	 * <p>For sending Unicode text input (e.g. into text boxes), use {@link #typeChar(int)} or
	 * {@link #typeChars(String)} instead.
	 *
	 * @param keyCode The GLFW key code of the key to press
	 * @see #holdKey(int)
	 */
	void pressKey(int keyCode);

	/**
	 * Presses and releases a mouse button.
	 *
	 * @param button The GLFW mouse button to press
	 * @see #holdMouse(int)
	 */
	void pressMouse(int button);

	/**
	 * Holds a key binding for the specified number of ticks and then releases it. Waits until this process is finished.
	 * The key binding must be bound.
	 *
	 * @param keyBinding The key binding to hold
	 * @param ticks The number of ticks to hold the key binding for
	 * @see #holdKey(KeyBinding)
	 * @see #holdKeyFor(Function, int)
	 */
	void holdKeyFor(KeyBinding keyBinding, int ticks);

	/**
	 * Holds a key binding for the specified number of ticks and then releases it. Waits until this process is finished.
	 * The key binding must be bound.
	 *
	 * @param keyBindingGetter The key binding to hold
	 * @param ticks The number of ticks to hold the key binding for
	 * @see #holdKey(Function)
	 * @see #holdKeyFor(Function, int)
	 */
	void holdKeyFor(Function<GameOptions, KeyBinding> keyBindingGetter, int ticks);

	/**
	 * Holds a key or mouse button for the specified number of ticks and then releases it. Waits until this process is
	 * finished.
	 *
	 * @param key The key or mouse button to hold
	 * @param ticks The number of ticks to hold the key or mouse button for
	 * @see #holdKey(InputUtil.Key)
	 */
	void holdKeyFor(InputUtil.Key key, int ticks);

	/**
	 * Holds a key for the specified number of ticks and then releases it. Waits until this process is finished.
	 *
	 * @param keyCode The GLFW key code of the key to hold
	 * @param ticks The number of ticks to hold the key for
	 * @see #holdKey(int)
	 */
	void holdKeyFor(int keyCode, int ticks);

	/**
	 * Holds a mouse button for the specified number of ticks and then releases it. Waits until this process is
	 * finished.
	 *
	 * @param button The GLFW mouse button to hold
	 * @param ticks The number of ticks to hold the mouse button for
	 * @see #holdMouse(int)
	 */
	void holdMouseFor(int button, int ticks);

	/**
	 * Types a code point (character). Useful for typing in text boxes.
	 *
	 * <p>This method is for sending Unicode text input, <em>not</em> for pressing keys on the keyboard for other
	 * purposes, such as pressing {@code W} for moving the player. For those use cases, use one of the {@code pressKey}
	 * overloads instead.
	 *
	 * @param codePoint The code point to type
	 * @see #typeChars(String)
	 * @see #pressKey(int)
	 * @see #pressKey(KeyBinding)
	 * @see #pressKey(Function)
	 */
	void typeChar(int codePoint);

	/**
	 * Types a sequence of code points (characters) one after the other. Useful for typing in text boxes.
	 *
	 * @param chars The code points to type
	 */
	void typeChars(String chars);

	/**
	 * Scrolls the mouse vertically.
	 *
	 * @param amount The amount to scroll by
	 * @see #scroll(double, double)
	 */
	void scroll(double amount);

	/**
	 * Scrolls the mouse horizontally and vertically.
	 *
	 * @param xAmount The horizontal amount to scroll by
	 * @param yAmount The vertical amount to scroll by
	 * @see #scroll(double)
	 */
	void scroll(double xAmount, double yAmount);

	/**
	 * Sets the cursor position.
	 *
	 * @param x The x position of the new cursor position
	 * @param y The y position of the new cursor position
	 * @see #moveCursor(double, double)
	 */
	void setCursorPos(double x, double y);

	/**
	 * Moves the cursor position.
	 *
	 * @param deltaX The amount to add to the x position of the cursor
	 * @param deltaY The amount to add to the y position of the cursor
	 * @see #setCursorPos(double, double)
	 */
	void moveCursor(double deltaX, double deltaY);

	/**
	 * Resizes the window to match the given size. Also attempts to resize the physical window, but whether the physical
	 * window was successfully resized or not, the window size accessible by the game will always be changed to the
	 * value specified, causing widget layouts and screenshots to work as expected.
	 *
	 * @param width The new window width
	 * @param height The new window height
	 */
	void resizeWindow(int width, int height);
}
