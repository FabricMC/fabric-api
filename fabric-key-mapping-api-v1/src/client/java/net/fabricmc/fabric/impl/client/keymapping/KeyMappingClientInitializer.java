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

package net.fabricmc.fabric.impl.client.keymapping;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;

public class KeyMappingClientInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Identifier phase = Identifier.fromNamespaceAndPath("fabric", "key_mappings");
		// Update mappings before mod listeners query them from screen input events.
		ScreenEvents.BEFORE_INIT.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
		ScreenEvents.BEFORE_INIT.register(phase, (client, screen, width, height) -> {
			ScreenKeyboardEvents.beforeKeyPress(screen).register((currentScreen, event) -> onScreenKey(currentScreen, InputConstants.getKey(event)));
			ScreenMouseEvents.beforeMouseClick(screen).register((currentScreen, event) -> onScreenKey(currentScreen, InputConstants.Type.MOUSE.getOrCreate(event.button())));
		});
	}

	private static void onScreenKey(Screen screen, InputConstants.Key key) {
		if (screen instanceof KeyBindsScreen keyBindsScreen && keyBindsScreen.selectedKey != null) {
			return;
		}

		KeyMappingRegistryImpl.onScreenKey(key, true);
	}
}
