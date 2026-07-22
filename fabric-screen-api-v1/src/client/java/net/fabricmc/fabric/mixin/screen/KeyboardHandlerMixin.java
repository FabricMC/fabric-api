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

package net.fabricmc.fabric.mixin.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;

@Mixin(KeyboardHandler.class)
abstract class KeyboardHandlerMixin {
	@WrapOperation(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z"))
	private boolean invokeKeyPressedEvents(Screen screen, KeyEvent event, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenKeyboardEvents.allowKeyPress(screen).invoker().allowKeyPress(screen, event)) {
				// Set this press action as handled
				return true;
			}

			ScreenKeyboardEvents.beforeKeyPress(screen).invoker().beforeKeyPress(screen, event);
		}

		boolean result = operation.call(screen, event);

		if (screen != null) {
			ScreenKeyboardEvents.afterKeyPress(screen).invoker().afterKeyPress(screen, event);
		}

		return result;
	}

	@WrapOperation(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyReleased(Lnet/minecraft/client/input/KeyEvent;)Z"))
	private boolean invokeKeyReleasedEvents(Screen screen, KeyEvent event, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenKeyboardEvents.allowKeyRelease(screen).invoker().allowKeyRelease(screen, event)) {
				// Set this release action as handled
				return true;
			}

			ScreenKeyboardEvents.beforeKeyRelease(screen).invoker().beforeKeyRelease(screen, event);
		}

		boolean result = operation.call(screen, event);

		if (screen != null) {
			ScreenKeyboardEvents.afterKeyRelease(screen).invoker().afterKeyRelease(screen, event);
		}

		return result;
	}

	@WrapOperation(method = "charTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;charTyped(Lnet/minecraft/client/input/CharacterEvent;)Z"))
	private boolean invokeCharTypedEvents(Screen screen, CharacterEvent event, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenKeyboardEvents.allowCharType(screen).invoker().allowCharType(screen, event)) {
				// Set this type action as handled
				return true;
			}

			ScreenKeyboardEvents.beforeCharType(screen).invoker().beforeCharType(screen, event);
		}

		boolean result = operation.call(screen, event);

		if (screen != null) {
			ScreenKeyboardEvents.afterCharType(screen).invoker().afterCharType(screen, event);
		}

		return result;
	}
}
