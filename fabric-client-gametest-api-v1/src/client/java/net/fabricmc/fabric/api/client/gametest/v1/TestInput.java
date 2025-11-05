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

import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

/**
 * The client gametest input handler used to simulate inputs to the client.
 */
@ApiStatus.NonExtendable
public interface TestInput {
	/**
	 * Starts holding down a key binding. The key binding will be held until it is released. The key binding must be
	 * bound. Does nothing if the key binding is already being held.
	 *
	 * <p><strong>Most key bindings will only start reacting to this when a tick is waited.</strong>
	 *
	 * @param keyBinding The key binding to hold
	 * @see #releaseKey(KeyMapping)
	 * @see #pressKey(KeyMapping)
	 * @see #holdKey(Function)
	 */
	void holdKey(KeyMapping keyBinding);

	/**
	 * Starts holding down a key binding. The key binding will be held until it is released. The key binding must be
	 * bound. Does nothing if the key binding is already being held.
	 *
	 * <p><strong>Most key bindings will only start reacting to this when a tick is waited.</strong>
	 *
	 * @param keyBindingGetter The function to get the key binding from the game options
	 * @see #releaseKey(Function)
	 * @see #pressKey(Function)
	 * @see #holdKey(KeyMapping)
	 */
	void holdKey(Function<Options, KeyMapping> keyBindingGetter);

	/**
	 * Starts holding down a key or mouse button. The key will be held until it is released. Does nothing if the key or
	 * mouse button is already being held.
	 *
	 * <p><strong>Most key bindings will only start reacting to this when a tick is waited.</strong>
	 *
	 * @param key The key or mouse button to hold
	 * @see #releaseKey(InputConstants.Key)
	 * @see #pressKey(InputConstants.Key)
	 */
	void holdKey(InputConstants.Key key);

	/**
	 * Starts holding down a key. The key will be held until it is released. Does nothing if the key is already being
	 * held.
	 *
	 * <p><strong>Most key bindings will only start reacting to this when a tick is waited.</strong>
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
	 * <p><strong>Most key bindings will only start reacting to this when a tick is waited.</strong>
	 *
	 * @param button The mouse button to hold
	 * @see #releaseMouse(int)
	 * @see #pressMouse(int)
	 */
	void holdMouse(int button);

	/**
	 * Starts holding down left control, or left super on macOS. Suitable for triggering
	 * {@link Minecraft#hasControlDown()}. The key will be held until it is released. Does nothing if the key is already
	 * being held.
	 *
	 * @see #releaseControl()
	 */
	void holdControl();

	/**
	 * Starts holding down left shift. Suitable for triggering {@link Minecraft#hasShiftDown()}. The key will be held until
	 * it is released. Does nothing if the key is already being held.
	 *
	 * @see #releaseShift()
	 */
	void holdShift();

	/**
	 * Starts holding down left alt. Suitable for triggering {@link Minecraft#hasAltDown()}. The key will be held until it
	 * is released. Does nothing if the key is already being held.
	 *
	 * @see #releaseAlt()
	 */
	void holdAlt();

	/**
	 * Releases a key binding. The key binding must be bound. Does nothing if the key binding is not being held.
	 *
	 * <p><strong>Most key bindings will only react to this when a tick is waited.</strong>
	 *
	 * @param keyBinding The key binding to release
	 * @see #holdKey(KeyMapping)
	 * @see #releaseKey(Function)
	 */
	void releaseKey(KeyMapping keyBinding);

	/**
	 * Releases a key binding. The key binding must be bound. Does nothing if the key binding is not being held.
	 *
	 * <p><strong>Most key bindings will only react to this when a tick is waited.</strong>
	 *
	 * @param keyBindingGetter The function to get the key binding from the game options
	 * @see #holdKey(Function)
	 * @see #releaseKey(KeyMapping)
	 */
	void releaseKey(Function<Options, KeyMapping> keyBindingGetter);

	/**
	 * Releases a key or mouse button. Does nothing if the key or mouse button is not being held.
	 *
	 * <p><strong>Most key bindings will only react to this when a tick is waited.</strong>
	 *
	 * @param key The key or mouse button to release
	 * @see #holdKey(InputConstants.Key)
	 */
	void releaseKey(InputConstants.Key key);

	/**
	 * Releases a key. Does nothing if the key is not being held.
	 *
	 * <p><strong>Most key bindings will only react to this when a tick is waited.</strong>
	 *
	 * @param keyCode The GLFW key code of the key to release
	 * @see #holdKey(int)
	 */
	void releaseKey(int keyCode);

	/**
	 * Releases a mouse button. Does nothing if the mouse button is not being held.
	 *
	 * <p><strong>Most key bindings will only react to this when a tick is waited.</strong>
	 *
	 * @param button The GLFW mouse button to release
	 * @see #holdMouse(int)
	 */
	void releaseMouse(int button);

	/**
	 * Releases left control, or left super on macOS. Suitable for un-triggering {@link Minecraft#hasControlDown()}. Does
	 * nothing if the key is not being held.
	 *
	 * @see #holdControl()
	 */
	void releaseControl();

	/**
	 * Releases left shift. Suitable for un-triggering {@link Minecraft#hasShiftDown()}. Does nothing if the key is not
	 * being held.
	 *
	 * @see #holdShift()
	 */
	void releaseShift();

	/**
	 * Releases left alt. Suitable for un-triggering {@link Minecraft#hasAltDown()}. Does nothing if the key is not being
	 * held.
	 *
	 * @see #holdAlt()
	 */
	void releaseAlt();

	/**
	 * Presses and releases a key binding, then waits a tick. The key binding must be bound.
	 *
	 * <p>A tick is waited because most key bindings need a tick to happen to react to the press. If you don't want the
	 * delay, use {@link #holdKeyFor(KeyMapping, int) holdKeyFor} with a tick count of {@code 0}.
	 *
	 * @param keyBinding The key binding to press
	 * @see #holdKey(KeyMapping)
	 * @see #pressKey(Function)
	 */
	void pressKey(KeyMapping keyBinding);

	/**
	 * Presses and releases a key binding, then waits a tick. The key binding must be bound.
	 *
	 * <p>A tick is waited because most key bindings need a tick to happen to react to the press. If you don't want the
	 * delay, use {@link #holdKeyFor(Function, int) holdKeyFor} with a tick count of {@code 0}.
	 *
	 * @param keyBindingGetter The function to get the key binding from the game options
	 * @see #holdKey(Function)
	 * @see #pressKey(KeyMapping)
	 */
	void pressKey(Function<Options, KeyMapping> keyBindingGetter);

	/**
	 * Presses and releases a key or mouse button, then waits a tick.
	 *
	 * <p>A tick is waited because most key bindings need a tick to happen to react to the press. If you don't want the
	 * delay, use {@link #holdKeyFor(InputConstants.Key, int) holdKeyFor} with a tick count of {@code 0}.
	 *
	 * @param key The key or mouse button to press.
	 * @see #holdKey(InputConstants.Key)
	 */
	void pressKey(InputConstants.Key key);

	/**
	 * Presses and releases a key, then waits a tick.
	 *
	 * <p>A tick is waited because most key bindings need a tick to happen to react to the press. If you don't want the
	 * delay, use {@link #holdKeyFor(int, int) holdKeyFor} with a tick count of {@code 0}.
	 *
	 * <p>For sending Unicode text input (e.g. into text boxes), use {@link #typeChar(int)} or
	 * {@link #typeChars(String)} instead.
	 *
	 * @param keyCode The GLFW key code of the key to press
	 * @see #holdKey(int)
	 */
	void pressKey(int keyCode);

	/**
	 * Presses and releases a mouse button, then waits a tick.
	 *
	 * <p>A tick is waited because most key bindings need a tick to happen to react to the press. If you don't want the
	 * delay, use {@link #holdMouseFor(int, int) holdMouseFor} with a tick count of {@code 0}.
	 *
	 * @param button The GLFW mouse button to press
	 * @see #holdMouse(int)
	 */
	void pressMouse(int button);

	/**
	 * Holds a key binding for the specified number of ticks and then releases it. Waits until this process is finished.
	 * The key binding must be bound.
	 *
	 * <p>Although the key will be released when this method returns, <strong>most key bindings will only react to this
	 * when a tick is waited.</strong>
	 *
	 * @param keyBinding The key binding to hold
	 * @param ticks The number of ticks to hold the key binding for
	 * @see #holdKey(KeyMapping)
	 * @see #holdKeyFor(Function, int)
	 */
	void holdKeyFor(KeyMapping keyBinding, int ticks);

	/**
	 * Holds a key binding for the specified number of ticks and then releases it. Waits until this process is finished.
	 * The key binding must be bound.
	 *
	 * <p>Although the key will be released when this method returns, <strong>most key bindings will only react to this
	 * when a tick is waited.</strong>
	 *
	 * @param keyBindingGetter The key binding to hold
	 * @param ticks The number of ticks to hold the key binding for
	 * @see #holdKey(Function)
	 * @see #holdKeyFor(Function, int)
	 */
	void holdKeyFor(Function<Options, KeyMapping> keyBindingGetter, int ticks);

	/**
	 * Holds a key or mouse button for the specified number of ticks and then releases it. Waits until this process is
	 * finished.
	 *
	 * <p>Although the key or mouse button will be released when this method returns, <strong>most key bindings will
	 * only react to this when a tick is waited.</strong>
	 *
	 * @param key The key or mouse button to hold
	 * @param ticks The number of ticks to hold the key or mouse button for
	 * @see #holdKey(InputConstants.Key)
	 */
	void holdKeyFor(InputConstants.Key key, int ticks);

	/**
	 * Holds a key for the specified number of ticks and then releases it. Waits until this process is finished.
	 *
	 * <p>Although the key will be released when this method returns, <strong>most key bindings will only react to this
	 * when a tick is waited.</strong>
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
	 * <p>Although the mouse button will be released when this method returns, <strong>most key bindings will only react
	 * to this when a tick is waited.</strong>
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
	 * @see #pressKey(KeyMapping)
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
