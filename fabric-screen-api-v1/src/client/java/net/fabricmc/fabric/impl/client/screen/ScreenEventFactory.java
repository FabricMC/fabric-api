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
import net.fabricmc.fabric.api.event.Combine;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Factory methods for creating event instances used in {@link ScreenExtensions}.
 */
public final class ScreenEventFactory {
	public static Event<ScreenEvents.Remove> createRemoveEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.Remove.class, Combine.CALL_ALL);
	}

	public static Event<ScreenEvents.BeforeExtract> createBeforeExtractEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.BeforeExtract.class, Combine.CALL_ALL);
	}

	public static Event<ScreenEvents.AfterBackground> createAfterBackgroundEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.AfterBackground.class, Combine.CALL_ALL);
	}

	public static Event<ScreenEvents.AfterForeground> createAfterForegroundEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.AfterForeground.class, Combine.CALL_ALL);
	}

	public static Event<ScreenEvents.AfterExtract> createAfterExtractEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.AfterExtract.class, Combine.CALL_ALL);
	}

	public static Event<ScreenEvents.BeforeTick> createBeforeTickEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.BeforeTick.class, Combine.CALL_ALL);
	}

	public static Event<ScreenEvents.AfterTick> createAfterTickEvent() {
		return EventFactory.createArrayBacked(ScreenEvents.AfterTick.class, Combine.CALL_ALL);
	}

	// Keyboard events

	public static Event<ScreenKeyboardEvents.AllowKeyPress> createAllowKeyPressEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AllowKeyPress.class, Combine.STOP_AT_FALSE);
	}

	public static Event<ScreenKeyboardEvents.BeforeKeyPress> createBeforeKeyPressEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.BeforeKeyPress.class, Combine.CALL_ALL);
	}

	public static Event<ScreenKeyboardEvents.AfterKeyPress> createAfterKeyPressEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AfterKeyPress.class, Combine.CALL_ALL);
	}

	public static Event<ScreenKeyboardEvents.AllowKeyRelease> createAllowKeyReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AllowKeyRelease.class, Combine.STOP_AT_FALSE);
	}

	public static Event<ScreenKeyboardEvents.BeforeKeyRelease> createBeforeKeyReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.BeforeKeyRelease.class, Combine.CALL_ALL);
	}

	public static Event<ScreenKeyboardEvents.AfterKeyRelease> createAfterKeyReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AfterKeyRelease.class, Combine.CALL_ALL);
	}

	public static Event<ScreenKeyboardEvents.AllowCharType> createAllowCharTypeEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AllowCharType.class, Combine.STOP_AT_FALSE);
	}

	public static Event<ScreenKeyboardEvents.BeforeCharType> createBeforeCharTypeEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.BeforeCharType.class, Combine.CALL_ALL);
	}

	public static Event<ScreenKeyboardEvents.AfterCharType> createAfterCharTypeEvent() {
		return EventFactory.createArrayBacked(ScreenKeyboardEvents.AfterCharType.class, Combine.CALL_ALL);
	}

	// Mouse Events

	public static Event<ScreenMouseEvents.AllowMouseClick> createAllowMouseClickEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseClick.class, Combine.STOP_AT_FALSE);
	}

	public static Event<ScreenMouseEvents.BeforeMouseClick> createBeforeMouseClickEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseClick.class, Combine.CALL_ALL);
	}

	public static Event<ScreenMouseEvents.AfterMouseClick> createAfterMouseClickEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseClick.class, callbacks -> (screen, event, consumed) -> {
			boolean consume = false;

			for (ScreenMouseEvents.AfterMouseClick callback : callbacks) {
				consume |= callback.afterMouseClick(screen, event, consume | consumed);
			}

			return consume;
		});
	}

	public static Event<ScreenMouseEvents.AllowMouseRelease> createAllowMouseReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseRelease.class, Combine.STOP_AT_FALSE);
	}

	public static Event<ScreenMouseEvents.BeforeMouseRelease> createBeforeMouseReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseRelease.class, Combine.CALL_ALL);
	}

	public static Event<ScreenMouseEvents.AfterMouseRelease> createAfterMouseReleaseEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseRelease.class, callbacks -> (screen, event, consumed) -> {
			boolean consume = false;

			for (ScreenMouseEvents.AfterMouseRelease callback : callbacks) {
				consume |= callback.afterMouseRelease(screen, event, consume | consumed);
			}

			return consume;
		});
	}

	public static Event<ScreenMouseEvents.AllowMouseDrag> createAllowMouseDragEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseDrag.class, Combine.STOP_AT_FALSE);
	}

	public static Event<ScreenMouseEvents.BeforeMouseDrag> createBeforeMouseDragEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseDrag.class, Combine.CALL_ALL);
	}

	public static Event<ScreenMouseEvents.AfterMouseDrag> createAfterMouseDragEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseDrag.class, callbacks -> (screen, event, horizontalAmount, verticalAmount, consumed) -> {
			boolean consume = false;

			for (ScreenMouseEvents.AfterMouseDrag callback : callbacks) {
				consume |= callback.afterMouseDrag(screen, event, horizontalAmount, verticalAmount, consume | consumed);
			}

			return consume;
		});
	}

	public static Event<ScreenMouseEvents.AllowMouseScroll> createAllowMouseScrollEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseScroll.class, Combine.STOP_AT_FALSE);
	}

	public static Event<ScreenMouseEvents.BeforeMouseScroll> createBeforeMouseScrollEvent() {
		return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseScroll.class, Combine.CALL_ALL);
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
