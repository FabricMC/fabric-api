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

package net.fabricmc.fabric.impl.client.gametest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.mixin.client.gametest.KeyBindingAccessor;
import net.fabricmc.fabric.mixin.client.gametest.KeyboardAccessor;
import net.fabricmc.fabric.mixin.client.gametest.MouseAccessor;

public final class TestInputImpl implements TestInput {
	private static final Set<InputUtil.Key> KEYS_DOWN = new HashSet<>();
	private final ClientGameTestContext context;

	public TestInputImpl(ClientGameTestContext context) {
		this.context = context;
	}

	public static boolean isKeyDown(int keyCode) {
		return KEYS_DOWN.contains(InputUtil.Type.KEYSYM.createFromCode(keyCode));
	}

	public void clearKeysDown() {
		for (InputUtil.Key key : new ArrayList<>(KEYS_DOWN)) {
			releaseKey(key);
		}
	}

	@Override
	public void holdKey(KeyBinding keyBinding) {
		ThreadingImpl.checkOnGametestThread("holdKey");
		Preconditions.checkNotNull(keyBinding, "keyBinding");

		holdKey(getBoundKey(keyBinding, "hold"));
	}

	@Override
	public void holdKey(Function<GameOptions, KeyBinding> keyBindingGetter) {
		ThreadingImpl.checkOnGametestThread("holdKey");
		Preconditions.checkNotNull(keyBindingGetter, "keyBindingGetter");

		KeyBinding keyBinding = context.computeOnClient(client -> keyBindingGetter.apply(client.options));
		holdKey(keyBinding);
	}

	@Override
	public void holdKey(InputUtil.Key key) {
		ThreadingImpl.checkOnGametestThread("holdKey");
		Preconditions.checkNotNull(key, "key");

		if (KEYS_DOWN.add(key)) {
			context.runOnClient(client -> pressOrReleaseKey(client, key, GLFW.GLFW_PRESS));
		}
	}

	@Override
	public void holdKey(int keyCode) {
		ThreadingImpl.checkOnGametestThread("holdKey");

		holdKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));
	}

	@Override
	public void holdMouse(int button) {
		ThreadingImpl.checkOnGametestThread("holdMouse");

		holdKey(InputUtil.Type.MOUSE.createFromCode(button));
	}

	@Override
	public void holdControl() {
		ThreadingImpl.checkOnGametestThread("holdControl");

		holdKey(MinecraftClient.IS_SYSTEM_MAC ? InputUtil.GLFW_KEY_LEFT_SUPER : InputUtil.GLFW_KEY_LEFT_CONTROL);
	}

	@Override
	public void holdShift() {
		ThreadingImpl.checkOnGametestThread("holdShift");

		holdKey(InputUtil.GLFW_KEY_LEFT_SHIFT);
	}

	@Override
	public void holdAlt() {
		ThreadingImpl.checkOnGametestThread("holdAlt");

		holdKey(InputUtil.GLFW_KEY_LEFT_ALT);
	}

	@Override
	public void releaseKey(KeyBinding keyBinding) {
		ThreadingImpl.checkOnGametestThread("releaseKey");
		Preconditions.checkNotNull(keyBinding, "keyBinding");

		releaseKey(getBoundKey(keyBinding, "release"));
	}

	@Override
	public void releaseKey(Function<GameOptions, KeyBinding> keyBindingGetter) {
		ThreadingImpl.checkOnGametestThread("releaseKey");
		Preconditions.checkNotNull(keyBindingGetter, "keyBindingGetter");

		KeyBinding keyBinding = context.computeOnClient(client -> keyBindingGetter.apply(client.options));
		releaseKey(keyBinding);
	}

	@Override
	public void releaseKey(InputUtil.Key key) {
		ThreadingImpl.checkOnGametestThread("releaseKey");
		Preconditions.checkNotNull(key, "key");

		if (KEYS_DOWN.remove(key)) {
			context.runOnClient(client -> pressOrReleaseKey(client, key, GLFW.GLFW_RELEASE));
		}
	}

	@Override
	public void releaseKey(int keyCode) {
		ThreadingImpl.checkOnGametestThread("releaseKey");

		releaseKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));
	}

	@Override
	public void releaseMouse(int button) {
		ThreadingImpl.checkOnGametestThread("releaseMouse");

		releaseKey(InputUtil.Type.MOUSE.createFromCode(button));
	}

	@Override
	public void releaseControl() {
		ThreadingImpl.checkOnGametestThread("releaseControl");

		releaseKey(MinecraftClient.IS_SYSTEM_MAC ? InputUtil.GLFW_KEY_LEFT_SUPER : InputUtil.GLFW_KEY_LEFT_CONTROL);
	}

	@Override
	public void releaseShift() {
		ThreadingImpl.checkOnGametestThread("releaseShift");

		releaseKey(InputUtil.GLFW_KEY_LEFT_SHIFT);
	}

	@Override
	public void releaseAlt() {
		ThreadingImpl.checkOnGametestThread("releaseAlt");

		releaseKey(InputUtil.GLFW_KEY_LEFT_ALT);
	}

	private static void pressOrReleaseKey(MinecraftClient client, InputUtil.Key key, int action) {
		switch (key.getCategory()) {
		case KEYSYM -> client.keyboard.onKey(client.getWindow().getHandle(), key.getCode(), 0, action, 0);
		case SCANCODE -> client.keyboard.onKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_UNKNOWN, key.getCode(), action, 0);
		case MOUSE -> ((MouseAccessor) client.mouse).invokeOnMouseButton(client.getWindow().getHandle(), key.getCode(), action, 0);
		}
	}

	@Override
	public void pressKey(KeyBinding keyBinding) {
		ThreadingImpl.checkOnGametestThread("pressKey");
		Preconditions.checkNotNull(keyBinding, "keyBinding");

		pressKey(getBoundKey(keyBinding, "press"));
	}

	@Override
	public void pressKey(Function<GameOptions, KeyBinding> keyBindingGetter) {
		ThreadingImpl.checkOnGametestThread("pressKey");
		Preconditions.checkNotNull(keyBindingGetter, "keyBindingGetter");

		KeyBinding keyBinding = context.computeOnClient(client -> keyBindingGetter.apply(client.options));
		pressKey(keyBinding);
	}

	@Override
	public void pressKey(InputUtil.Key key) {
		ThreadingImpl.checkOnGametestThread("pressKey");
		Preconditions.checkNotNull(key, "key");

		holdKey(key);
		releaseKey(key);
	}

	@Override
	public void pressKey(int keyCode) {
		ThreadingImpl.checkOnGametestThread("pressKey");

		pressKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));
	}

	@Override
	public void pressMouse(int button) {
		ThreadingImpl.checkOnGametestThread("pressMouse");

		pressKey(InputUtil.Type.MOUSE.createFromCode(button));
	}

	@Override
	public void holdKeyFor(KeyBinding keyBinding, int ticks) {
		ThreadingImpl.checkOnGametestThread("holdKeyFor");
		Preconditions.checkNotNull(keyBinding, "keyBinding");
		Preconditions.checkArgument(ticks > 0, "ticks must be positive");

		holdKeyFor(getBoundKey(keyBinding, "hold"), ticks);
	}

	@Override
	public void holdKeyFor(Function<GameOptions, KeyBinding> keyBindingGetter, int ticks) {
		ThreadingImpl.checkOnGametestThread("holdKeyFor");
		Preconditions.checkNotNull(keyBindingGetter, "keyBindingGetter");
		Preconditions.checkArgument(ticks > 0, "ticks must be positive");

		KeyBinding keyBinding = context.computeOnClient(client -> keyBindingGetter.apply(client.options));
		holdKeyFor(keyBinding, ticks);
	}

	@Override
	public void holdKeyFor(InputUtil.Key key, int ticks) {
		ThreadingImpl.checkOnGametestThread("holdKeyFor");
		Preconditions.checkNotNull(key, "key");
		Preconditions.checkArgument(ticks > 0, "ticks must be positive");

		holdKey(key);
		context.waitTicks(ticks);
		releaseKey(key);
	}

	@Override
	public void holdKeyFor(int keyCode, int ticks) {
		ThreadingImpl.checkOnGametestThread("holdKeyFor");
		Preconditions.checkArgument(ticks > 0, "ticks must be positive");

		holdKeyFor(InputUtil.Type.KEYSYM.createFromCode(keyCode), ticks);
	}

	@Override
	public void holdMouseFor(int button, int ticks) {
		ThreadingImpl.checkOnGametestThread("holdMouseFor");
		Preconditions.checkArgument(ticks > 0, "ticks must be positive");

		holdKeyFor(InputUtil.Type.MOUSE.createFromCode(button), ticks);
	}

	@Override
	public void typeChar(int codePoint) {
		ThreadingImpl.checkOnGametestThread("typeChar");

		context.runOnClient(client -> ((KeyboardAccessor) client.keyboard).invokeOnChar(client.getWindow().getHandle(), codePoint, 0));
	}

	@Override
	public void typeChars(String chars) {
		ThreadingImpl.checkOnGametestThread("typeChars");

		context.runOnClient(client -> {
			chars.chars().forEach(codePoint -> {
				((KeyboardAccessor) client.keyboard).invokeOnChar(client.getWindow().getHandle(), codePoint, 0);
			});
		});
	}

	@Override
	public void scroll(double amount) {
		ThreadingImpl.checkOnGametestThread("scroll");

		scroll(0, amount);
	}

	@Override
	public void scroll(double xAmount, double yAmount) {
		ThreadingImpl.checkOnGametestThread("scroll");

		context.runOnClient(client -> ((MouseAccessor) client.mouse).invokeOnMouseScroll(client.getWindow().getHandle(), xAmount, yAmount));
	}

	@Override
	public void setCursorPos(double x, double y) {
		ThreadingImpl.checkOnGametestThread("setCursorPos");

		context.runOnClient(client -> ((MouseAccessor) client.mouse).invokeOnCursorPos(client.getWindow().getHandle(), x, y));
	}

	@Override
	public void moveCursor(double deltaX, double deltaY) {
		ThreadingImpl.checkOnGametestThread("moveCursor");

		context.runOnClient(client -> {
			double newX = client.mouse.getX() + deltaX;
			double newY = client.mouse.getY() + deltaY;
			((MouseAccessor) client.mouse).invokeOnCursorPos(client.getWindow().getHandle(), newX, newY);
		});
	}

	@Override
	public void resizeWindow(int width, int height) {
		ThreadingImpl.checkOnGametestThread("resizeWindow");
		Preconditions.checkArgument(width > 0, "width must be positive");
		Preconditions.checkArgument(height > 0, "height must be positive");

		context.runOnClient(client -> ((WindowHooks) (Object) client.getWindow()).fabric_resize(width, height));
	}

	private static InputUtil.Key getBoundKey(KeyBinding keyBinding, String action) {
		InputUtil.Key boundKey = ((KeyBindingAccessor) keyBinding).getBoundKey();

		if (boundKey == InputUtil.UNKNOWN_KEY) {
			throw new AssertionError("Cannot %s binding '%s' because it isn't bound to a key".formatted(action, keyBinding.getTranslationKey()));
		}

		return boundKey;
	}
}
