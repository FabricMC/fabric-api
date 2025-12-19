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

import java.util.List;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;
import net.fabricmc.fabric.mixin.screen.ScreenAccessor;

/**
 * Utility methods related to screens.
 *
 * @see ScreenEvents
 */
public final class Screens {
	/**
	 * Gets all of a screen's widgets.
	 * The provided list allows for addition and removal of widgets from the screen.
	 * This method should be preferred over adding widgets directly to a screen's {@link Screen#children() child elements}.
	 *
	 * @return a list of all of a screen's widgets
	 */
	public static List<AbstractWidget> getWidgets(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).fabric_getButtons();
	}

	/**
	 * Gets a screen's font.
	 *
	 * @return the screen's font.
	 * @deprecated Use {@link Screen#getFont()} directly
	 */
	@Deprecated
	public static Font getFont(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return screen.getFont();
	}

	public static Minecraft getMinecraft(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ((ScreenAccessor) screen).getClient();
	}

	private Screens() {
	}
}
