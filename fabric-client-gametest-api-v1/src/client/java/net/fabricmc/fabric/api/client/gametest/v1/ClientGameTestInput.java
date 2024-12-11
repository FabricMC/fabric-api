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

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * The client gametest input handler used to simulate inputs to the client.
 */
public interface ClientGameTestInput {
	// TODO: document all these methods
	void pressKey(KeyBinding keyBinding);

	void pressKey(Function<GameOptions, KeyBinding> keyBindingGetter);

	void pressKey(InputUtil.Key key);

	void pressKey(int keyCode);

	void pressMouse(int button);

	void pressControl();

	void pressShift();

	void pressAlt();

	void releaseKey(KeyBinding keyBinding);

	void releaseKey(Function<GameOptions, KeyBinding> keyBindingGetter);

	void releaseKey(InputUtil.Key key);

	void releaseKey(int keyCode);

	void releaseMouse(int button);

	void releaseControl();

	void releaseShift();

	void releaseAlt();

	void pressReleaseKey(KeyBinding keyBinding);

	void pressReleaseKey(Function<GameOptions, KeyBinding> keyBindingGetter);

	void pressReleaseKey(InputUtil.Key key);

	void pressReleaseKey(int keyCode);

	void pressReleaseMouse(int button);

	void holdKey(KeyBinding keyBinding, int ticks);

	void holdKey(Function<GameOptions, KeyBinding> keyBindingGetter, int ticks);

	void holdKey(InputUtil.Key key, int ticks);

	void holdKey(int keyCode, int ticks);

	void holdMouse(int button, int ticks);

	void typeChar(int codePoint);

	void typeChars(String chars);

	void scroll(double amount);

	void scroll(double xAmount, double yAmount);

	void setCursorPos(double x, double y);

	void moveCursor(double deltaX, double deltaY);
}
