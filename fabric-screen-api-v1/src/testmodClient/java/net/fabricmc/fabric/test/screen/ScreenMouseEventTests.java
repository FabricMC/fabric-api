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

package net.fabricmc.fabric.test.screen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.gui.screen.ingame.HopperScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

public class ScreenMouseEventTests implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("fabric-screen-api-v1");

	@Override
	public void onInitializeClient() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof HopperScreen) {
				ScreenMouseEvents.allowMouseDrag(screen).register((screen1, mouseX, mouseY, button, horizontalAmount, verticalAmount) -> {
					LOGGER.info("Allow Mouse Drag: Screen {}, Horizontal Amount {}, Vertical Amount {}", screen1.getClass().getSimpleName(), horizontalAmount, verticalAmount);
					return true;
				});
				ScreenMouseEvents.beforeMouseDrag(screen).register((screen1, mouseX, mouseY, button, horizontalAmount, verticalAmount) -> {
					LOGGER.info("Before Mouse Drag: Screen {}, Horizontal Amount {}, Vertical Amount {}", screen1.getClass().getSimpleName(), horizontalAmount, verticalAmount);
				});
				ScreenMouseEvents.afterMouseDrag(screen).register((screen1, mouseX, mouseY, button, horizontalAmount, verticalAmount) -> {
					LOGGER.info("After Mouse Drag: Screen {}, Horizontal Amount {}, Vertical Amount {}", screen1.getClass().getSimpleName(), horizontalAmount, verticalAmount);
				});
			}
		});
	}
}
