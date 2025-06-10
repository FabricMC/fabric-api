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

package net.fabricmc.fabric.api.client.screen.v1;

import java.util.Objects;

import net.minecraft.client.gui.screen.Screen;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;

/**
 * Events related to use of the mouse in a {@link Screen}.
 *
 * <p>All of these events work on top of a specific screen instance.
 * Subscriptions will only last as long as the screen itself, they'll disappear once the screen gets refreshed, closed or replaced.
 * Use {@link ScreenEvents#BEFORE_INIT} to register the desired events every time it is necessary.
 *
 * <p>Events are fired in the following order:
 * <pre>{@code AllowX -> BeforeX -> AfterX}</pre>
 * If the result of the Allow event is false, then Before and After are not called.
 *
 * @see ScreenEvents
 */
public final class ScreenMouseEvents {
	/**
	 * An event that checks if the mouse click should be allowed.
	 *
	 * @return the event
	 */
	public static Event<AllowMouseClick> allowMouseClick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseClickEvent();
	}

	/**
	 * An event that is called before a mouse click is processed for a screen.
	 *
	 * @return the event
	 */
	public static Event<BeforeMouseClick> beforeMouseClick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseClickEvent();
	}

	/**
	 * An event that is called after a mouse click is processed for a screen.
	 *
	 * @return the event
	 */
	public static Event<AfterMouseClick> afterMouseClick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseClickEvent();
	}

	/**
	 * An event that checks if the mouse click should be allowed to release in a screen.
	 *
	 * @return the event
	 */
	public static Event<AllowMouseRelease> allowMouseRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseReleaseEvent();
	}

	/**
	 * An event that is called before the release of a mouse click is processed for a screen.
	 *
	 * @return the event
	 */
	public static Event<BeforeMouseRelease> beforeMouseRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseReleaseEvent();
	}

	/**
	 * An event that is called after the release of a mouse click is processed for a screen.
	 *
	 * @return the event
	 */
	public static Event<AfterMouseRelease> afterMouseRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseReleaseEvent();
	}

	/**
	 * An event that checks if the mouse should be allowed to drag in a screen.
	 *
	 * <p>This event tracks the amount a mouse was dragged both vertically and horizontally.
	 *
	 * @return the event
	 */
	public static Event<AllowMouseDrag> allowMouseDrag(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseDragEvent();
	}

	/**
	 * An event called after mouse dragging is processed for a screen.
	 *
	 * <p>This event tracks the amount a mouse was dragged both vertically and horizontally.
	 *
	 * @return the event
	 */
	public static Event<BeforeMouseDrag> beforeMouseDrag(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseDragEvent();
	}

	/**
	 * An event called after mouse dragging is processed for a screen.
	 *
	 * <p>This event tracks the amount a mouse was dragged both vertically and horizontally.
	 *
	 * @return the event
	 */
	public static Event<AfterMouseDrag> afterMouseDrag(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseDragEvent();
	}

	/**
	 * An event that checks if the mouse should be allowed to scroll in a screen.
	 *
	 * <p>This event tracks the amount a mouse was scrolled both vertically and horizontally.
	 *
	 * @return the event
	 */
	public static Event<AllowMouseScroll> allowMouseScroll(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseScrollEvent();
	}

	/**
	 * An event called before mouse scrolling is processed for a screen.
	 *
	 * <p>This event tracks the amount a mouse was scrolled both vertically and horizontally.
	 *
	 * @return the event
	 */
	public static Event<BeforeMouseScroll> beforeMouseScroll(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseScrollEvent();
	}

	/**
	 * An event called after mouse scrolling is processed for a screen.
	 *
	 * <p>This event tracks the amount a mouse was scrolled both vertically and horizontally.
	 *
	 * @return the event
	 */
	public static Event<AfterMouseScroll> afterMouseScroll(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseScrollEvent();
	}

	private ScreenMouseEvents() {
	}

	@FunctionalInterface
	public interface AllowMouseClick {
		/**
		 * Checks if the mouse click should be allowed in a screen.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		boolean allowMouseClick(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface BeforeMouseClick {
		/**
		 * Called before a mouse click in a screen.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		void beforeMouseClick(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface AfterMouseClick {
		/**
		 * Called after a mouse click in a screen.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		void afterMouseClick(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface AllowMouseRelease {
		/**
		 * Checks if the mouse click should be allowed to release in a screen.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		boolean allowMouseRelease(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface BeforeMouseRelease {
		/**
		 * Called before a mouse click has released in a screen.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		void beforeMouseRelease(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface AfterMouseRelease {
		/**
		 * Called after a mouse click has released in a screen.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		void afterMouseRelease(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface AllowMouseDrag {
		/**
		 * Checks if the mouse should be allowed to drag in a screen by moving the cursor while a mouse button is held
		 * down.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @param horizontalAmount the horizontal drag amount
		 * @param verticalAmount the vertical drag amount
		 * @return whether the mouse should be allowed to drag
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		boolean allowMouseDrag(Screen screen, double mouseX, double mouseY, int button, double horizontalAmount, double verticalAmount);
	}

	@FunctionalInterface
	public interface BeforeMouseDrag {
		/**
		 * Called before a mouse is dragged on screen.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @param horizontalAmount the horizontal drag amount
		 * @param verticalAmount the vertical drag amount
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		void beforeMouseDrag(Screen screen, double mouseX, double mouseY, int button, double horizontalAmount, double verticalAmount);
	}

	@FunctionalInterface
	public interface AfterMouseDrag {
		/**
		 * Called after a mouse is dragged on screen.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @param horizontalAmount the horizontal drag amount
		 * @param verticalAmount the vertical drag amount
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		void afterMouseDrag(Screen screen, double mouseX, double mouseY, int button, double horizontalAmount, double verticalAmount);
	}

	@FunctionalInterface
	public interface AllowMouseScroll {
		/**
		 * Checks if the mouse should be allowed to scroll in a screen.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param horizontalAmount the horizontal scroll amount
		 * @param verticalAmount the vertical scroll amount
		 * @return whether the mouse should be allowed to scroll
		 */
		boolean allowMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
	}

	@FunctionalInterface
	public interface BeforeMouseScroll {
		/**
		 * Called before a mouse has scrolled on screen.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param horizontalAmount the horizontal scroll amount
		 * @param verticalAmount the vertical scroll amount
		 */
		void beforeMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
	}

	@FunctionalInterface
	public interface AfterMouseScroll {
		/**
		 * Called after a mouse has scrolled on screen.
		 *
		 * @param screen the screen
		 * @param mouseX the x position of the mouse
		 * @param mouseY the y position of the mouse
		 * @param horizontalAmount the horizontal scroll amount
		 * @param verticalAmount the vertical scroll amount
		 */
		void afterMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
	}
}
