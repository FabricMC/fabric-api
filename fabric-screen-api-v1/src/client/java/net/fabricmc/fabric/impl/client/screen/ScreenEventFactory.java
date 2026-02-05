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

	public static Event<ScreenEvents.AfterBackground> createAfterBackgroundEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.AfterBackground.class, callbacks -> (screen, matrices, mouseX, mouseY, tickDelta) -> {
			for (ScreenEvents.AfterBackground callback : callbacks) {
				callback.afterBackground(screen, matrices, mouseX, mouseY, tickDelta);
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
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AllowKeyPress.class, callbacks -> (screen, context) -> {
			for (ScreenKeyboardEvents.AllowKeyPress callback : callbacks) {
				if (!callback.allowKeyPress(screen, context)) {
					return false;
				}
			}

			return true;
		});
	}

	public static Event<ScreenKeyboardEvents.BeforeKeyPress> createBeforeKeyPressEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.BeforeKeyPress.class, callbacks -> (screen, context) -> {
			for (ScreenKeyboardEvents.BeforeKeyPress callback : callbacks) {
				callback.beforeKeyPress(screen, context);
			}
		});
	}

	public static Event<ScreenKeyboardEvents.AfterKeyPress> createAfterKeyPressEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AfterKeyPress.class, callbacks -> (screen, context) -> {
			for (ScreenKeyboardEvents.AfterKeyPress callback : callbacks) {
				callback.afterKeyPress(screen, context);
			}
		});
	}

	public static Event<ScreenKeyboardEvents.AllowKeyRelease> createAllowKeyReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AllowKeyRelease.class, callbacks -> (screen, context) -> {
			for (ScreenKeyboardEvents.AllowKeyRelease callback : callbacks) {
				if (!callback.allowKeyRelease(screen, context)) {
					return false;
				}
			}

			return true;
		});
	}

	public static Event<ScreenKeyboardEvents.BeforeKeyRelease> createBeforeKeyReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.BeforeKeyRelease.class, callbacks -> (screen, context) -> {
			for (ScreenKeyboardEvents.BeforeKeyRelease callback : callbacks) {
				callback.beforeKeyRelease(screen, context);
			}
		});
	}

	public static Event<ScreenKeyboardEvents.AfterKeyRelease> createAfterKeyReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AfterKeyRelease.class, callbacks -> (screen, context) -> {
			for (ScreenKeyboardEvents.AfterKeyRelease callback : callbacks) {
				callback.afterKeyRelease(screen, context);
			}
		});
	}

	// Mouse Events

	public static Event<ScreenMouseEvents.AllowMouseClick> createAllowMouseClickEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseClick.class, callbacks -> (screen, context) -> {
			for (ScreenMouseEvents.AllowMouseClick callback : callbacks) {
				if (!callback.allowMouseClick(screen, context)) {
					return false;
				}
			}

			return true;
		});
	}

	public static Event<ScreenMouseEvents.BeforeMouseClick> createBeforeMouseClickEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseClick.class, callbacks -> (screen, context) -> {
			for (ScreenMouseEvents.BeforeMouseClick callback : callbacks) {
				callback.beforeMouseClick(screen, context);
			}
		});
	}

	public static Event<ScreenMouseEvents.AfterMouseClick> createAfterMouseClickEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseClick.class, callbacks -> (screen, context, consumed) -> {
			boolean consume = false;

			for (ScreenMouseEvents.AfterMouseClick callback : callbacks) {
				consume |= callback.afterMouseClick(screen, context, consume | consumed);
			}

			return consume;
		});
	}

	public static Event<ScreenMouseEvents.AllowMouseRelease> createAllowMouseReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseRelease.class, callbacks -> (screen, context) -> {
			for (ScreenMouseEvents.AllowMouseRelease callback : callbacks) {
				if (!callback.allowMouseRelease(screen, context)) {
					return false;
				}
			}

			return true;
		});
	}

	public static Event<ScreenMouseEvents.BeforeMouseRelease> createBeforeMouseReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseRelease.class, callbacks -> (screen, context) -> {
			for (ScreenMouseEvents.BeforeMouseRelease callback : callbacks) {
				callback.beforeMouseRelease(screen, context);
			}
		});
	}

	public static Event<ScreenMouseEvents.AfterMouseRelease> createAfterMouseReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseRelease.class, callbacks -> (screen, context, consumed) -> {
			boolean consume = false;

			for (ScreenMouseEvents.AfterMouseRelease callback : callbacks) {
				consume |= callback.afterMouseRelease(screen, context, consume | consumed);
			}

			return consume;
		});
	}

	public static Event<ScreenMouseEvents.AllowMouseDrag> createAllowMouseDragEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseDrag.class, callbacks -> (screen, context, horizontalAmount, verticalAmount) -> {
			for (ScreenMouseEvents.AllowMouseDrag callback : callbacks) {
				if (!callback.allowMouseDrag(screen, context, horizontalAmount, verticalAmount)) {
					return false;
				}
			}

			return true;
		});
	}

	public static Event<ScreenMouseEvents.BeforeMouseDrag> createBeforeMouseDragEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseDrag.class, callbacks -> (screen, context, horizontalAmount, verticalAmount) -> {
			for (ScreenMouseEvents.BeforeMouseDrag callback : callbacks) {
				callback.beforeMouseDrag(screen, context, horizontalAmount, verticalAmount);
			}
		});
	}

	public static Event<ScreenMouseEvents.AfterMouseDrag> createAfterMouseDragEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseDrag.class, callbacks -> (screen, context, horizontalAmount, verticalAmount, consumed) -> {
			boolean consume = false;

			for (ScreenMouseEvents.AfterMouseDrag callback : callbacks) {
				consume |= callback.afterMouseDrag(screen, context, horizontalAmount, verticalAmount, consume | consumed);
			}

			return consume;
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
		return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseScroll.class, callbacks -> (screen, mouseX, mouseY, horizontalAmount, verticalAmount, consumed) -> {
			boolean consume = false;

			for (ScreenMouseEvents.AfterMouseScroll callback : callbacks) {
				consume |= callback.afterMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount, consume | consumed);
			}

			return consume;
		});
	}

	private ScreenEventFactory() {
	}
}
