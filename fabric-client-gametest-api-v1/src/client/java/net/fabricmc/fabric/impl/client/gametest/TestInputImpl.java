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
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.Util;

import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.util.WindowHooks;
import net.fabricmc.fabric.mixin.client.gametest.input.KeyMappingAccessor;
import net.fabricmc.fabric.mixin.client.gametest.input.KeyboardHandlerAccessor;
import net.fabricmc.fabric.mixin.client.gametest.input.MouseHandlerAccessor;

public final class TestInputImpl implements TestInput {
	private static final Set<InputConstants.Key> KEYS_DOWN = new HashSet<>();
	private static final boolean IS_MACOS = Util.getPlatform() == Util.OS.OSX;
	private final ClientGameTestContext context;

	public TestInputImpl(ClientGameTestContext context) {
		this.context = context;
	}

	public static boolean isKeyDown(int keyCode) {
		return KEYS_DOWN.contains(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
	}

	public void clearKeysDown() {
		for (InputConstants.Key key : new ArrayList<>(KEYS_DOWN)) {
			releaseKey(key);
		}
	}

	@Override
	public void holdKey(KeyMapping keyBinding) {
		ThreadingImpl.checkOnGametestThread("holdKey");
		Preconditions.checkNotNull(keyBinding, "keyBinding");

		holdKey(getBoundKey(keyBinding, "hold"));
	}

	@Override
	public void holdKey(Function<Options, KeyMapping> keyBindingGetter) {
		ThreadingImpl.checkOnGametestThread("holdKey");
		Preconditions.checkNotNull(keyBindingGetter, "keyBindingGetter");

		KeyMapping keyBinding = context.computeOnClient(client -> keyBindingGetter.apply(client.options));
		holdKey(keyBinding);
	}

	@Override
	public void holdKey(InputConstants.Key key) {
		ThreadingImpl.checkOnGametestThread("holdKey");
		Preconditions.checkNotNull(key, "key");

		if (KEYS_DOWN.add(key)) {
			context.runOnClient(client -> pressOrReleaseKey(client, key, GLFW.GLFW_PRESS));
		}
	}

	@Override
	public void holdKey(int keyCode) {
		ThreadingImpl.checkOnGametestThread("holdKey");

		holdKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
	}

	@Override
	public void holdMouse(int button) {
		ThreadingImpl.checkOnGametestThread("holdMouse");

		holdKey(InputConstants.Type.MOUSE.getOrCreate(button));
	}

	@Override
	public void holdControl() {
		ThreadingImpl.checkOnGametestThread("holdControl");

		holdKey(IS_MACOS ? InputConstants.KEY_LSUPER : InputConstants.KEY_LCONTROL);
	}

	@Override
	public void holdShift() {
		ThreadingImpl.checkOnGametestThread("holdShift");

		holdKey(InputConstants.KEY_LSHIFT);
	}

	@Override
	public void holdAlt() {
		ThreadingImpl.checkOnGametestThread("holdAlt");

		holdKey(InputConstants.KEY_LALT);
	}

	@Override
	public void releaseKey(KeyMapping keyBinding) {
		ThreadingImpl.checkOnGametestThread("releaseKey");
		Preconditions.checkNotNull(keyBinding, "keyBinding");

		releaseKey(getBoundKey(keyBinding, "release"));
	}

	@Override
	public void releaseKey(Function<Options, KeyMapping> keyBindingGetter) {
		ThreadingImpl.checkOnGametestThread("releaseKey");
		Preconditions.checkNotNull(keyBindingGetter, "keyBindingGetter");

		KeyMapping keyBinding = context.computeOnClient(client -> keyBindingGetter.apply(client.options));
		releaseKey(keyBinding);
	}

	@Override
	public void releaseKey(InputConstants.Key key) {
		ThreadingImpl.checkOnGametestThread("releaseKey");
		Preconditions.checkNotNull(key, "key");

		if (KEYS_DOWN.remove(key)) {
			context.runOnClient(client -> pressOrReleaseKey(client, key, GLFW.GLFW_RELEASE));
		}
	}

	@Override
	public void releaseKey(int keyCode) {
		ThreadingImpl.checkOnGametestThread("releaseKey");

		releaseKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
	}

	@Override
	public void releaseMouse(int button) {
		ThreadingImpl.checkOnGametestThread("releaseMouse");

		releaseKey(InputConstants.Type.MOUSE.getOrCreate(button));
	}

	@Override
	public void releaseControl() {
		ThreadingImpl.checkOnGametestThread("releaseControl");

		releaseKey(IS_MACOS ? InputConstants.KEY_LSUPER : InputConstants.KEY_LCONTROL);
	}

	@Override
	public void releaseShift() {
		ThreadingImpl.checkOnGametestThread("releaseShift");

		releaseKey(InputConstants.KEY_LSHIFT);
	}

	@Override
	public void releaseAlt() {
		ThreadingImpl.checkOnGametestThread("releaseAlt");

		releaseKey(InputConstants.KEY_LALT);
	}

	private static void pressOrReleaseKey(Minecraft client, InputConstants.Key key, int action) {
		switch (key.getType()) {
		case KEYSYM -> ((KeyboardHandlerAccessor) client.keyboardHandler).invokeOnKey(client.getWindow().handle(), action, new KeyEvent(key.getValue(), 0, 0));
		case SCANCODE -> ((KeyboardHandlerAccessor) client.keyboardHandler).invokeOnKey(client.getWindow().handle(), action, new KeyEvent(GLFW.GLFW_KEY_UNKNOWN, key.getValue(), 0));
		case MOUSE -> ((MouseHandlerAccessor) client.mouseHandler).invokeOnMouseButton(client.getWindow().handle(), new MouseButtonInfo(key.getValue(), 0), action);
		}
	}

	@Override
	public void pressKey(KeyMapping keyBinding) {
		ThreadingImpl.checkOnGametestThread("pressKey");
		Preconditions.checkNotNull(keyBinding, "keyBinding");

		pressKey(getBoundKey(keyBinding, "press"));
	}

	@Override
	public void pressKey(Function<Options, KeyMapping> keyBindingGetter) {
		ThreadingImpl.checkOnGametestThread("pressKey");
		Preconditions.checkNotNull(keyBindingGetter, "keyBindingGetter");

		KeyMapping keyBinding = context.computeOnClient(client -> keyBindingGetter.apply(client.options));
		pressKey(keyBinding);
	}

	@Override
	public void pressKey(InputConstants.Key key) {
		ThreadingImpl.checkOnGametestThread("pressKey");
		Preconditions.checkNotNull(key, "key");

		holdKey(key);
		releaseKey(key);
		context.waitTick();
	}

	@Override
	public void pressKey(int keyCode) {
		ThreadingImpl.checkOnGametestThread("pressKey");

		pressKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
	}

	@Override
	public void pressMouse(int button) {
		ThreadingImpl.checkOnGametestThread("pressMouse");

		pressKey(InputConstants.Type.MOUSE.getOrCreate(button));
	}

	@Override
	public void holdKeyFor(KeyMapping keyBinding, int ticks) {
		ThreadingImpl.checkOnGametestThread("holdKeyFor");
		Preconditions.checkNotNull(keyBinding, "keyBinding");
		Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");

		holdKeyFor(getBoundKey(keyBinding, "hold"), ticks);
	}

	@Override
	public void holdKeyFor(Function<Options, KeyMapping> keyBindingGetter, int ticks) {
		ThreadingImpl.checkOnGametestThread("holdKeyFor");
		Preconditions.checkNotNull(keyBindingGetter, "keyBindingGetter");
		Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");

		KeyMapping keyBinding = context.computeOnClient(client -> keyBindingGetter.apply(client.options));
		holdKeyFor(keyBinding, ticks);
	}

	@Override
	public void holdKeyFor(InputConstants.Key key, int ticks) {
		ThreadingImpl.checkOnGametestThread("holdKeyFor");
		Preconditions.checkNotNull(key, "key");
		Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");

		holdKey(key);
		context.waitTicks(ticks);
		releaseKey(key);
	}

	@Override
	public void holdKeyFor(int keyCode, int ticks) {
		ThreadingImpl.checkOnGametestThread("holdKeyFor");
		Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");

		holdKeyFor(InputConstants.Type.KEYSYM.getOrCreate(keyCode), ticks);
	}

	@Override
	public void holdMouseFor(int button, int ticks) {
		ThreadingImpl.checkOnGametestThread("holdMouseFor");
		Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");

		holdKeyFor(InputConstants.Type.MOUSE.getOrCreate(button), ticks);
	}

	@Override
	public void typeChar(int codePoint) {
		ThreadingImpl.checkOnGametestThread("typeChar");

		context.runOnClient(client -> ((KeyboardHandlerAccessor) client.keyboardHandler).invokeOnChar(client.getWindow().handle(), new CharacterEvent(codePoint, 0)));
	}

	@Override
	public void typeChars(String chars) {
		ThreadingImpl.checkOnGametestThread("typeChars");

		context.runOnClient(client -> {
			chars.chars().forEach(codePoint -> {
				((KeyboardHandlerAccessor) client.keyboardHandler).invokeOnChar(client.getWindow().handle(), new CharacterEvent(codePoint, 0));
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

		context.runOnClient(client -> ((MouseHandlerAccessor) client.mouseHandler).invokeOnMouseScroll(client.getWindow().handle(), xAmount, yAmount));
	}

	@Override
	public void setCursorPos(double x, double y) {
		ThreadingImpl.checkOnGametestThread("setCursorPos");

		context.runOnClient(client -> ((MouseHandlerAccessor) client.mouseHandler).invokeOnCursorPos(client.getWindow().handle(), x, y));
	}

	@Override
	public void moveCursor(double deltaX, double deltaY) {
		ThreadingImpl.checkOnGametestThread("moveCursor");

		context.runOnClient(client -> {
			double newX = client.mouseHandler.xpos() + deltaX;
			double newY = client.mouseHandler.ypos() + deltaY;
			((MouseHandlerAccessor) client.mouseHandler).invokeOnCursorPos(client.getWindow().handle(), newX, newY);
		});
	}

	@Override
	public void resizeWindow(int width, int height) {
		ThreadingImpl.checkOnGametestThread("resizeWindow");
		Preconditions.checkArgument(width > 0, "width must be positive");
		Preconditions.checkArgument(height > 0, "height must be positive");

		context.runOnClient(client -> ((WindowHooks) (Object) client.getWindow()).fabric_resize(width, height));
	}

	private static InputConstants.Key getBoundKey(KeyMapping keyBinding, String action) {
		InputConstants.Key boundKey = ((KeyMappingAccessor) keyBinding).getBoundKey();

		if (boundKey == InputConstants.UNKNOWN) {
			throw new AssertionError("Cannot %s binding '%s' because it isn't bound to a key".formatted(action, keyBinding.getName()));
		}

		return boundKey;
	}
}
