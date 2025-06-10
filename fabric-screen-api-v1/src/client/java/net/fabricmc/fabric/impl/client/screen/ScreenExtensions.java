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

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;

public interface ScreenExtensions {
	static ScreenExtensions getExtensions(Screen screen) {
		return (ScreenExtensions) screen;
	}

	List<ClickableWidget> fabric_getButtons();

	Event<ScreenEvents.Remove> fabric_getRemoveEvent();

	Event<ScreenEvents.BeforeTick> fabric_getBeforeTickEvent();

	Event<ScreenEvents.AfterTick> fabric_getAfterTickEvent();

	Event<ScreenEvents.BeforeRender> fabric_getBeforeRenderEvent();

	Event<ScreenEvents.AfterRender> fabric_getAfterRenderEvent();

	// Keyboard

	Event<ScreenKeyboardEvents.AllowKeyPress> fabric_getAllowKeyPressEvent();

	Event<ScreenKeyboardEvents.BeforeKeyPress> fabric_getBeforeKeyPressEvent();

	Event<ScreenKeyboardEvents.AfterKeyPress> fabric_getAfterKeyPressEvent();

	Event<ScreenKeyboardEvents.AllowKeyRelease> fabric_getAllowKeyReleaseEvent();

	Event<ScreenKeyboardEvents.BeforeKeyRelease> fabric_getBeforeKeyReleaseEvent();

	Event<ScreenKeyboardEvents.AfterKeyRelease> fabric_getAfterKeyReleaseEvent();

	// Mouse

	Event<ScreenMouseEvents.AllowMouseClick> fabric_getAllowMouseClickEvent();

	Event<ScreenMouseEvents.BeforeMouseClick> fabric_getBeforeMouseClickEvent();

	Event<ScreenMouseEvents.AfterMouseClick> fabric_getAfterMouseClickEvent();

	Event<ScreenMouseEvents.AllowMouseRelease> fabric_getAllowMouseReleaseEvent();

	Event<ScreenMouseEvents.BeforeMouseRelease> fabric_getBeforeMouseReleaseEvent();

	Event<ScreenMouseEvents.AfterMouseRelease> fabric_getAfterMouseReleaseEvent();

	Event<ScreenMouseEvents.AllowMouseDrag> fabric_getAllowMouseDragEvent();

	Event<ScreenMouseEvents.BeforeMouseDrag> fabric_getBeforeMouseDragEvent();

	Event<ScreenMouseEvents.AfterMouseDrag> fabric_getAfterMouseDragEvent();

	Event<ScreenMouseEvents.AllowMouseScroll> fabric_getAllowMouseScrollEvent();

	Event<ScreenMouseEvents.BeforeMouseScroll> fabric_getBeforeMouseScrollEvent();

	Event<ScreenMouseEvents.AfterMouseScroll> fabric_getAfterMouseScrollEvent();
}
