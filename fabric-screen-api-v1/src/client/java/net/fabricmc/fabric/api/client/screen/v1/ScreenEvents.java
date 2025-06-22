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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;

/**
 * Holds events related to {@link Screen}s.
 *
 * <p>Some events require a screen instance in order to obtain an event instance.
 * The events that require a screen instance can be identified by the use of a method passing a screen instance.
 * All events in {@link ScreenKeyboardEvents} and {@link ScreenMouseEvents} require a screen instance.
 * This registration model is used since a screen being (re)initialized will reset the screen to its default state, therefore reverting all changes a mod developer may have applied to a screen.
 * Furthermore, this design was chosen to reduce the amount of wasted iterations of events as a mod developer would only need to register screen events for rendering, ticking, keyboards and mice if needed on a per-instance basis.
 *
 * <p>The primary entrypoint into a screen is when it is being opened, this is signified by an event {@link ScreenEvents#BEFORE_INIT before} and {@link ScreenEvents#AFTER_INIT after} initialization of the screen.
 *
 * @see Screens
 * @see ScreenKeyboardEvents
 * @see ScreenMouseEvents
 */

public final class ScreenEvents {
	/**
	 * An event that is called before {@link Screen#init(MinecraftClient, int, int) a screen is initialized} to its default state.
	 * It should be noted some methods in {@link Screens} such as a screen's {@link Screen#getTextRenderer text renderer} may not be initialized yet, and as such their use is discouraged.
	 *
	 * <!--<p>Typically this event is used to register screen events such as listening to when child elements are added to the screen. ------ Uncomment when child add/remove event is added for elements-->
	 * You can still use {@link ScreenEvents#AFTER_INIT} to register events such as keyboard and mouse events.
	 *
	 * <p>The {@link ScreenExtensions} provided by the {@code info} parameter may be used to register tick, render events, keyboard, mouse, additional and removal of child elements (including buttons).
	 * For example, to register an event on inventory like screens after render, the following code could be used:
	 * <pre>{@code
	 * &#64;Override
	 * public void onInitializeClient() {
	 * 	ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
	 * 		if (screen instanceof AbstractInventoryScreen) {
	 * 			ScreenEvents.afterRender(screen).register((screen1, matrices, mouseX, mouseY, tickDelta) -> {
	 * 				...
	 * 			});
	 * 		}
	 * 	});
	 * }
	 * }</pre>
	 *
	 * <p>This event indicates a screen has been resized, and therefore is being re-initialized.
	 * This event can also indicate that the previous screen has been changed.
	 * @see ScreenEvents#AFTER_INIT
	 */
	public static final Event<BeforeInit> BEFORE_INIT = EventFactory.createArrayBacked(BeforeInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
		for (BeforeInit callback : callbacks) {
			callback.beforeInit(client, screen, scaledWidth, scaledHeight);
		}
	});

	/**
	 * An event that is called after {@link Screen#init(MinecraftClient, int, int) a screen is initialized} to its default state.
	 *
	 * <p>Typically this event is used to modify a screen after the screen has been initialized.
	 * Modifications such as changing sizes of buttons, removing buttons and adding/removing child elements to the screen can be done safely using this event.
	 *
	 * <p>For example, to add a button to the title screen, the following code could be used:
	 * <pre>{@code
	 * ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
	 * 	if (screen instanceof TitleScreen) {
	 * 		Screens.getButtons(screen).add(new ButtonWidget(...));
	 * 	}
	 * });
	 * }</pre>
	 *
	 * <p>Note that by adding an element to a screen, the element is not automatically {@link net.minecraft.client.gui.Drawable drawn}.
	 * Unless the element is button, you need to call the specific {@link net.minecraft.client.gui.Drawable#render(DrawContext, int, int, float) render} methods in the corresponding screen events.
	 *
	 * <p>This event can also indicate that the previous screen has been closed.
	 * @see ScreenEvents#BEFORE_INIT
	 */
	public static final Event<AfterInit> AFTER_INIT = EventFactory.createArrayBacked(AfterInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
		for (AfterInit callback : callbacks) {
			callback.afterInit(client, screen, scaledWidth, scaledHeight);
		}
	});

	/**
	 * An event that is called after {@link Screen#removed()} is called.
	 * This event signifies that the screen is now closed.
	 *
	 * <p>This event is typically used to undo any screen specific state changes or to terminate threads spawned by a screen.
	 * This event may precede initialization events {@link ScreenEvents#BEFORE_INIT} but there is no guarantee that event will be called immediately afterwards.
	 */
	public static Event<Remove> remove(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getRemoveEvent();
	}

	/**
	 * An event that is called before a screen is rendered.
	 *
	 * @return the event
	 */
	public static Event<BeforeRender> beforeRender(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getBeforeRenderEvent();
	}

	/**
	 * An event that is called after a screen is rendered.
	 *
	 * @return the event
	 */
	public static Event<AfterRender> afterRender(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAfterRenderEvent();
	}

	/**
	 * An event that is called before a screen is ticked.
	 *
	 * @return the event
	 */
	public static Event<BeforeTick> beforeTick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getBeforeTickEvent();
	}

	/**
	 * An event that is called after a screen is ticked.
	 *
	 * @return the event
	 */
	public static Event<AfterTick> afterTick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAfterTickEvent();
	}

	@FunctionalInterface
	public interface BeforeInit {
		void beforeInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight);
	}

	@FunctionalInterface
	public interface AfterInit {
		void afterInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight);
	}

	@FunctionalInterface
	public interface Remove {
		void onRemove(Screen screen);
	}

	@FunctionalInterface
	public interface BeforeRender {
		void beforeRender(Screen screen, DrawContext drawContext, int mouseX, int mouseY, float tickDelta);
	}

	@FunctionalInterface
	public interface AfterRender {
		void afterRender(Screen screen, DrawContext drawContext, int mouseX, int mouseY, float tickDelta);
	}

	@FunctionalInterface
	public interface BeforeTick {
		void beforeTick(Screen screen);
	}

	@FunctionalInterface
	public interface AfterTick {
		void afterTick(Screen screen);
	}

	private ScreenEvents() {
	}
}
