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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;

/// Holds events related to [Screen]s.
///
/// Some events require a screen instance in order to obtain an event instance.
/// The events that require a screen instance can be identified by the use of a method passing a screen instance.
/// All events in [ScreenKeyboardEvents] and [ScreenMouseEvents] require a screen instance.
/// This registration model is used since a screen being (re)initialized will reset the screen to its default state, therefore reverting all changes a mod developer may have applied to a screen.
/// Furthermore, this design was chosen to reduce the amount of wasted iterations of events as a mod developer would only need to register screen events for rendering, ticking, keyboards and mice if needed on a per-instance basis.
///
/// The primary entrypoint into a screen is when it is being opened, this is signified by an event [before][ScreenEvents#BEFORE_INIT] and [after][ScreenEvents#AFTER_INIT] initialization of the screen.
///
/// @see Screens
/// @see ScreenKeyboardEvents
/// @see ScreenMouseEvents
public final class ScreenEvents {
	/// An event that is called before [a screen is initialized][Screen#init(int, int)] to its default state.
	/// It should be noted some methods in [Screens] such as a screen's [font][Screen#getFont()] may not be initialized yet, and as such their use is discouraged.
	/// <!--
	/// Typically this event is used to register screen events such as listening to when child elements are added to the screen. ------ Uncomment when child add/remove event is added for elements-->
	/// You can still use [ScreenEvents#AFTER_INIT] to register events such as keyboard and mouse events.
	///
	/// The [ScreenExtensions] provided by the `info` parameter may be used to register tick, render events, keyboard, mouse, additional and removal of child elements (including buttons).
	/// For example, to register an event on container-like screens after render, the following code could be used:
	/// <pre>
	/// `&#64;Overridepublic void onInitializeClient(){ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) ->{if (screen instanceof AbstractContainerScreen){ScreenEvents.afterRender(screen).register((screen1, graphics, mouseX, mouseY, tickProgress) ->{...});}});}`</pre>
	///
	/// This event indicates a screen has been resized, and therefore is being re-initialized.
	/// This event can also indicate that the previous screen has been changed.
	/// @see ScreenEvents#AFTER_INIT
	public static final Event<BeforeInit> BEFORE_INIT = EventFactory.createArrayBacked(BeforeInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
		for (BeforeInit callback : callbacks) {
			callback.beforeInit(client, screen, scaledWidth, scaledHeight);
		}
	});

	/// An event that is called after [a screen is initialized][Screen#init(int, int)] to its default state.
	///
	/// Typically this event is used to modify a screen after the screen has been initialized.
	/// Modifications such as changing sizes of buttons, removing buttons and adding/removing child elements to the screen can be done safely using this event.
	///
	/// For example, to add a button to the title screen, the following code could be used:
	/// <pre>
	/// `ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->{if (screen instanceof TitleScreen){Screens.getWidgets(screen).add(new Button(...));}});`</pre>
	///
	/// Note that by adding an element to a screen, the element is not automatically [drawn][net.minecraft.client.gui.components.Renderable].
	/// Unless the element is button, you need to call the specific [render][net.minecraft.client.gui.components.Renderable#render(GuiGraphics, int, int, float)] methods in the corresponding screen events.
	///
	/// This event can also indicate that the previous screen has been closed.
	/// @see ScreenEvents#BEFORE_INIT
	public static final Event<AfterInit> AFTER_INIT = EventFactory.createArrayBacked(AfterInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
		for (AfterInit callback : callbacks) {
			callback.afterInit(client, screen, scaledWidth, scaledHeight);
		}
	});

	/// An event that is called after [Screen#removed()] is called.
	/// This event signifies that the screen is now closed.
	///
	/// This event is typically used to undo any screen specific state changes or to terminate threads spawned by a screen.
	/// This event may precede initialization events [ScreenEvents#BEFORE_INIT] but there is no guarantee that event will be called immediately afterwards.
	public static Event<Remove> remove(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getRemoveEvent();
	}

	/// An event that is called before a screen is rendered.
	///
	/// @return the event
	public static Event<BeforeRender> beforeRender(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getBeforeRenderEvent();
	}

	/// An event that is called after a screen's background is rendered.
	///
	/// @return the event
	public static Event<AfterBackground> afterBackground(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAfterBackgroundEvent();
	}

	/// An event that is called after a screen is rendered.
	///
	/// @return the event
	public static Event<AfterRender> afterRender(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAfterRenderEvent();
	}

	/// An event that is called before a screen is ticked.
	///
	/// @return the event
	public static Event<BeforeTick> beforeTick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getBeforeTickEvent();
	}

	/// An event that is called after a screen is ticked.
	///
	/// @return the event
	public static Event<AfterTick> afterTick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getAfterTickEvent();
	}

	@FunctionalInterface
	public interface BeforeInit {
		void beforeInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight);
	}

	@FunctionalInterface
	public interface AfterInit {
		void afterInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight);
	}

	@FunctionalInterface
	public interface Remove {
		void onRemove(Screen screen);
	}

	@FunctionalInterface
	public interface BeforeRender {
		void beforeRender(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickProgress);
	}

	@FunctionalInterface
	public interface AfterBackground {
		void afterBackground(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickProgress);
	}

	@FunctionalInterface
	public interface AfterRender {
		void afterRender(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickProgress);
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
