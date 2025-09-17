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

import net.minecraft.client.Mouse;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

@Mixin(Mouse.class)
abstract class MouseMixin {
	@WrapOperation(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(Lnet/minecraft/client/gui/Click;Z)Z"))
	private boolean invokeMouseClickedEvents(Screen screen, Click ctx, boolean doubleClick, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, ctx)) {
				// Set this press action as handled
				return true;
			}

			ScreenMouseEvents.beforeMouseClick(screen).invoker().beforeMouseClick(screen, ctx);
		}

		boolean result = operation.call(screen, ctx, doubleClick);

		if (screen != null) {
			result |= ScreenMouseEvents.afterMouseClick(screen).invoker().afterMouseClick(screen, ctx, result);
		}

		return result;
	}

	@WrapOperation(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseReleased(Lnet/minecraft/client/gui/Click;)Z"))
	private boolean invokeMousePressedEvents(Screen screen, Click ctx, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenMouseEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, ctx)) {
				// Set this release action as handled
				return true;
			}

			ScreenMouseEvents.beforeMouseRelease(screen).invoker().beforeMouseRelease(screen, ctx);
		}

		boolean result = operation.call(screen, ctx);

		if (screen != null) {
			result |= ScreenMouseEvents.afterMouseRelease(screen).invoker().afterMouseRelease(screen, ctx, result);
		}

		return result;
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseDragged(Lnet/minecraft/client/gui/Click;DD)Z"))
	private boolean invokeMouseDragEvents(Screen screen, Click ctx, double horizontalAmount, double verticalAmount, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenMouseEvents.allowMouseDrag(screen).invoker().allowMouseDrag(screen, ctx, horizontalAmount, verticalAmount)) {
				// Set this drag action as handled
				return true;
			}

			ScreenMouseEvents.beforeMouseDrag(screen).invoker().beforeMouseDrag(screen, ctx, horizontalAmount, verticalAmount);
		}

		boolean result = operation.call(screen, ctx, horizontalAmount, verticalAmount);

		if (screen != null) {
			result |= ScreenMouseEvents.afterMouseDrag(screen).invoker().afterMouseDrag(screen, ctx, horizontalAmount, verticalAmount, result);
		}

		return result;
	}

	@WrapOperation(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDDD)Z"))
	private boolean invokeMouseScrollEvents(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenMouseEvents.allowMouseScroll(screen).invoker().allowMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount)) {
				// Set this scroll action as handled
				return true;
			}

			ScreenMouseEvents.beforeMouseScroll(screen).invoker().beforeMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
		}

		boolean result = operation.call(screen, mouseX, mouseY, horizontalAmount, verticalAmount);

		if (screen != null) {
			result |= ScreenMouseEvents.afterMouseScroll(screen).invoker().afterMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount, result);
		}

		return result;
	}
}
