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
import net.minecraft.client.gui.screen.Screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

@Mixin(Mouse.class)
abstract class MouseMixin {
	@WrapOperation(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"))
	private boolean invokeMouseClickedEvents(Screen screen, double mouseX, double mouseY, int button, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, mouseX, mouseY, button)) {
				// Set this press action as handled
				return true;
			}

			ScreenMouseEvents.beforeMouseClick(screen).invoker().beforeMouseClick(screen, mouseX, mouseY, button);
		}

		boolean result = operation.call(screen, mouseX, mouseY, button);

		if (screen != null) {
			ScreenMouseEvents.afterMouseClick(screen).invoker().afterMouseClick(screen, mouseX, mouseY, button);
		}

		return result;
	}

	@WrapOperation(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseReleased(DDI)Z"))
	private boolean invokeMousePressedEvents(Screen screen, double mouseX, double mouseY, int button, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenMouseEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, mouseX, mouseY, button)) {
				// Set this release action as handled
				return true;
			}

			ScreenMouseEvents.beforeMouseRelease(screen).invoker().beforeMouseRelease(screen, mouseX, mouseY, button);
		}

		boolean result = operation.call(screen, mouseX, mouseY, button);

		if (screen != null) {
			ScreenMouseEvents.afterMouseRelease(screen).invoker().afterMouseRelease(screen, mouseX, mouseY, button);
		}

		return result;
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseDragged(DDIDD)Z"))
	private boolean invokeMouseDragEvents(Screen screen, double mouseX, double mouseY, int button, double horizontalAmount, double verticalAmount, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenMouseEvents.allowMouseDrag(screen).invoker().allowMouseDrag(screen, mouseX, mouseY, button, horizontalAmount, verticalAmount)) {
				// Set this drag action as handled
				return true;
			}

			ScreenMouseEvents.beforeMouseDrag(screen).invoker().beforeMouseDrag(screen, mouseX, mouseY, button, horizontalAmount, verticalAmount);
		}

		boolean result = operation.call(screen, mouseX, mouseY, button, horizontalAmount, verticalAmount);

		if (screen != null) {
			ScreenMouseEvents.afterMouseDrag(screen).invoker().afterMouseDrag(screen, mouseX, mouseY, button, horizontalAmount, verticalAmount);
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
			ScreenMouseEvents.afterMouseScroll(screen).invoker().afterMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
		}

		return result;
	}
}
