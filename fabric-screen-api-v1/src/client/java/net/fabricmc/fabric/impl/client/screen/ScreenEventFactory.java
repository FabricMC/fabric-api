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

package net.fabricmc.fabric.impl.client.screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Factory methods for creating event instances used in {@link ScreenExtensions}.
 */
public final class ScreenEventFactory {
	public static Event<ScreenEvents.Remove> createRemoveEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.Remove.class, callbacks -> screen -> {
			for (ScreenEvents.Remove callback : callbacks) {
				callback.onRemove(screen);
			}
		});
	}

	public static Event<ScreenEvents.BeforeRender> createBeforeRenderEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.BeforeRender.class, callbacks -> (screen, matrices, mouseX, mouseY, tickDelta) -> {
			for (ScreenEvents.BeforeRender callback : callbacks) {
				callback.beforeRender(screen, matrices, mouseX, mouseY, tickDelta);
			}
		});
	}

	public static Event<ScreenEvents.AfterRender> createAfterRenderEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.AfterRender.class, callbacks -> (screen, matrices, mouseX, mouseY, tickDelta) -> {
			for (ScreenEvents.AfterRender callback : callbacks) {
				callback.afterRender(screen, matrices, mouseX, mouseY, tickDelta);
			}
		});
	}

	public static Event<ScreenEvents.BeforeTick> createBeforeTickEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.BeforeTick.class, callbacks -> screen -> {
			for (ScreenEvents.BeforeTick callback : callbacks) {
				callback.beforeTick(screen);
			}
		});
	}

	public static Event<ScreenEvents.AfterTick> createAfterTickEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.AfterTick.class, callbacks -> screen -> {
			for (ScreenEvents.AfterTick callback : callbacks) {
				callback.afterTick(screen);
			}
		});
	}

	// Keyboard events

	public static Event<ScreenKeyboardEvents.AllowKeyPress> createAllowKeyPressEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AllowKeyPress.class, callbacks -> (screen, key, scancode, modifiers) -> {
			for (ScreenKeyboardEvents.AllowKeyPress callback : callbacks) {
				if (!callback.allowKeyPress(screen, key, scancode, modifiers)) {
					return false;
				}
			}

			return true;
		});
	}

	public static Event<ScreenKeyboardEvents.BeforeKeyPress> createBeforeKeyPressEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.BeforeKeyPress.class, callbacks -> (screen, key, scancode, modifiers) -> {
			for (ScreenKeyboardEvents.BeforeKeyPress callback : callbacks) {
				callback.beforeKeyPress(screen, key, scancode, modifiers);
			}
		});
	}

	public static Event<ScreenKeyboardEvents.AfterKeyPress> createAfterKeyPressEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AfterKeyPress.class, callbacks -> (screen, key, scancode, modifiers) -> {
			for (ScreenKeyboardEvents.AfterKeyPress callback : callbacks) {
				callback.afterKeyPress(screen, key, scancode, modifiers);
			}
		});
	}

	public static Event<ScreenKeyboardEvents.AllowKeyRelease> createAllowKeyReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AllowKeyRelease.class, callbacks -> (screen, key, scancode, modifiers) -> {
			for (ScreenKeyboardEvents.AllowKeyRelease callback : callbacks) {
				if (!callback.allowKeyRelease(screen, key, scancode, modifiers)) {
					return false;
				}
			}

			return true;
		});
	}

	public static Event<ScreenKeyboardEvents.BeforeKeyRelease> createBeforeKeyReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.BeforeKeyRelease.class, callbacks -> (screen, key, scancode, modifiers) -> {
			for (ScreenKeyboardEvents.BeforeKeyRelease callback : callbacks) {
				callback.beforeKeyRelease(screen, key, scancode, modifiers);
			}
		});
	}

	public static Event<ScreenKeyboardEvents.AfterKeyRelease> createAfterKeyReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AfterKeyRelease.class, callbacks -> (screen, key, scancode, modifiers) -> {
			for (ScreenKeyboardEvents.AfterKeyRelease callback : callbacks) {
				callback.afterKeyRelease(screen, key, scancode, modifiers);
			}
		});
	}

	// Mouse Events

	public static Event<ScreenMouseEvents.AllowMouseClick> createAllowMouseClickEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseClick.class, callbacks -> (screen, mouseX, mouseY, button) -> {
			for (ScreenMouseEvents.AllowMouseClick callback : callbacks) {
				if (!callback.allowMouseClick(screen, mouseX, mouseY, button)) {
					return false;
				}
			}

			return true;
		});
	}

	public static Event<ScreenMouseEvents.BeforeMouseClick> createBeforeMouseClickEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseClick.class, callbacks -> (screen, mouseX, mouseY, button) -> {
			for (ScreenMouseEvents.BeforeMouseClick callback : callbacks) {
				callback.beforeMouseClick(screen, mouseX, mouseY, button);
			}
		});
	}

	public static Event<ScreenMouseEvents.AfterMouseClick> createAfterMouseClickEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseClick.class, callbacks -> (screen, mouseX, mouseY, button) -> {
			for (ScreenMouseEvents.AfterMouseClick callback : callbacks) {
				callback.afterMouseClick(screen, mouseX, mouseY, button);
			}
		});
	}

	public static Event<ScreenMouseEvents.AllowMouseRelease> createAllowMouseReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseRelease.class, callbacks -> (screen, mouseX, mouseY, button) -> {
			for (ScreenMouseEvents.AllowMouseRelease callback : callbacks) {
				if (!callback.allowMouseRelease(screen, mouseX, mouseY, button)) {
					return false;
				}
			}

			return true;
		});
	}

	public static Event<ScreenMouseEvents.BeforeMouseRelease> createBeforeMouseReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseRelease.class, callbacks -> (screen, mouseX, mouseY, button) -> {
			for (ScreenMouseEvents.BeforeMouseRelease callback : callbacks) {
				callback.beforeMouseRelease(screen, mouseX, mouseY, button);
			}
		});
	}

	public static Event<ScreenMouseEvents.AfterMouseRelease> createAfterMouseReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseRelease.class, callbacks -> (screen, mouseX, mouseY, button) -> {
			for (ScreenMouseEvents.AfterMouseRelease callback : callbacks) {
				callback.afterMouseRelease(screen, mouseX, mouseY, button);
			}
		});
	}

	public static Event<ScreenMouseEvents.AllowMouseDrag> createAllowMouseDragEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseDrag.class, callbacks -> (screen, mouseX, mouseY, button, horizontalAmount, verticalAmount) -> {
			for (ScreenMouseEvents.AllowMouseDrag callback : callbacks) {
				if (!callback.allowMouseDrag(screen, mouseX, mouseY, button, horizontalAmount, verticalAmount)) {
					return false;
				}
			}

			return true;
		});
	}

	public static Event<ScreenMouseEvents.BeforeMouseDrag> createBeforeMouseDragEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseDrag.class, callbacks -> (screen, mouseX, mouseY, button, horizontalAmount, verticalAmount) -> {
			for (ScreenMouseEvents.BeforeMouseDrag callback : callbacks) {
				callback.beforeMouseDrag(screen, mouseX, mouseY, button, horizontalAmount, verticalAmount);
			}
		});
	}

	public static Event<ScreenMouseEvents.AfterMouseDrag> createAfterMouseDragEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseDrag.class, callbacks -> (screen, mouseX, mouseY, button, horizontalAmount, verticalAmount) -> {
			for (ScreenMouseEvents.AfterMouseDrag callback : callbacks) {
				callback.afterMouseDrag(screen, mouseX, mouseY, button, horizontalAmount, verticalAmount);
			}
		});
	}

	public static Event<ScreenMouseEvents.AllowMouseScroll> createAllowMouseScrollEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseScroll.class, callbacks -> (screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
			for (ScreenMouseEvents.AllowMouseScroll callback : callbacks) {
				if (!callback.allowMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount)) {
					return false;
				}
			}

			return true;
		});
	}

	public static Event<ScreenMouseEvents.BeforeMouseScroll> createBeforeMouseScrollEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseScroll.class, callbacks -> (screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
			for (ScreenMouseEvents.BeforeMouseScroll callback : callbacks) {
				callback.beforeMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
			}
		});
	}

	public static Event<ScreenMouseEvents.AfterMouseScroll> createAfterMouseScrollEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseScroll.class, callbacks -> (screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
			for (ScreenMouseEvents.AfterMouseScroll callback : callbacks) {
				callback.afterMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
			}
		});
	}

	private ScreenEventFactory() {
	}
}
