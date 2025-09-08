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

import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;

import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;

@Mixin(Keyboard.class)
abstract class KeyboardMixin {
	@WrapOperation(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;keyPressed(Lnet/minecraft/client/input/KeyInput;)Z"))
	private boolean invokeKeyPressedEvents(Screen screen, KeyInput ctx, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenKeyboardEvents.allowKeyPress(screen).invoker().allowKeyPress(screen, ctx)) {
				// Set this press action as handled
				return true;
			}

			ScreenKeyboardEvents.beforeKeyPress(screen).invoker().beforeKeyPress(screen, ctx);
		}

		boolean result = operation.call(screen, ctx);

		if (screen != null) {
			ScreenKeyboardEvents.afterKeyPress(screen).invoker().afterKeyPress(screen, ctx);
		}

		return result;
	}

	@WrapOperation(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;keyReleased(Lnet/minecraft/client/input/KeyInput;)Z"))
	private boolean invokeKeyReleasedEvents(Screen screen, KeyInput ctx, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenKeyboardEvents.allowKeyRelease(screen).invoker().allowKeyRelease(screen, ctx)) {
				// Set this release action as handled
				return true;
			}

			ScreenKeyboardEvents.beforeKeyRelease(screen).invoker().beforeKeyRelease(screen, ctx);
		}

		boolean result = operation.call(screen, ctx);

		if (screen != null) {
			ScreenKeyboardEvents.afterKeyRelease(screen).invoker().afterKeyRelease(screen, ctx);
		}

		return result;
	}
}
