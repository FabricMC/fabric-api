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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface ScreenOpeningCallback {
	Event<ScreenOpeningCallback> EVENT = EventFactory.createArrayBacked(ScreenOpeningCallback.class,
			callbacks -> (oldScreen, newScreen) -> {
				for (ScreenOpeningCallback callback : callbacks) {
					Screen screen = callback.onScreenOpening(oldScreen, newScreen);

					if (screen != newScreen) {
						return screen;
					}
				}

				return newScreen;
			});

	/**
	 * Called just before a new screen is set to {@link net.minecraft.client.MinecraftClient#currentScreen} in
	 * {@link net.minecraft.client.MinecraftClient#setScreen(Screen)}, allows for exchanging the new screen with a
	 * different one, or can prevent a new screen from opening, by returning the original screen.
	 *
	 * <p>Note that the old screen has already been removed by calling {@link Screen#removed()}, and the new screen
	 * will always be initialized via {@link Screen#init(MinecraftClient, int, int)}.
	 *
	 * @param oldScreen the screen that is being removed, which may be {@code null} when opening the screen from
	 *                  {@link net.minecraft.client.gui.hud.InGameHud}, like
	 *                  {@link net.minecraft.client.gui.screen.GameMenuScreen}
	 * @param newScreen the new screen that is being set, which may be {@code null} when closing a screen and returning
	 *                  to the in-game hud
	 * @return the screen to be opened, by default the new screen
	 */
	@Nullable Screen onScreenOpening(@Nullable Screen oldScreen, @Nullable Screen newScreen);
}
