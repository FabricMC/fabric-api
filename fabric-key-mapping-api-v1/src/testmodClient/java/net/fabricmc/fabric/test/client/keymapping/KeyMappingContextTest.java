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

package net.fabricmc.fabric.test.client.keymapping;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingContext;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class KeyMappingContextTest implements ClientModInitializer {
	static final MutableContext CUSTOM_CONTEXT = new MutableContext();
	static KeyMapping inGame;
	static KeyMapping inScreen;
	static KeyMapping screenSpecific;
	static KeyMapping always;
	static KeyMapping custom;
	static KeyMapping shared;
	static KeyMapping legacy;
	static ToggleKeyMapping toggle;
	static ToggleKeyMapping screenTransitionToggle;

	@Override
	public void onInitializeClient() {
		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("fabric-key-mapping-api-v1-testmod", "contexts"));
		inGame = register("in_game", category, KeyMappingContext.IN_GAME);
		inScreen = register("in_screen", category, KeyMappingContext.IN_SCREEN);
		screenSpecific = register("screen_specific", category, KeyMappingContext.forScreen(ContextScreen.class, OtherContextScreen.class));
		always = register("all", category, KeyMappingContext.ALL);
		custom = register("custom", category, CUSTOM_CONTEXT);
		shared = register("shared", category, CUSTOM_CONTEXT);
		legacy = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fabric-key-mapping-api-v1-testmod.legacy", InputConstants.KEY_F16, category));
		toggle = new ToggleKeyMapping("key.fabric-key-mapping-api-v1-testmod.context_toggle", InputConstants.KEY_F18, category, () -> true, true);
		KeyMappingHelper.registerKeyMapping(toggle, CUSTOM_CONTEXT);
		screenTransitionToggle = new ToggleKeyMapping("key.fabric-key-mapping-api-v1-testmod.screen_transition_toggle", InputConstants.KEY_F17, category, () -> true, true);
		KeyMappingHelper.registerKeyMapping(screenTransitionToggle, KeyMappingContext.ALL);
	}

	private static KeyMapping register(String name, KeyMapping.Category category, KeyMappingContext context) {
		return KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fabric-key-mapping-api-v1-testmod." + name, InputConstants.KEY_F20, category), context);
	}

	static class ContextScreen extends Screen {
		ContextScreen() {
			super(Component.literal("Screen-specific key mapping test"));
		}
	}

	static class OtherContextScreen extends Screen {
		OtherContextScreen() {
			super(Component.literal("Other screen-specific key mapping test"));
		}
	}

	static class MutableContext implements KeyMappingContext {
		boolean active;

		@Override
		public boolean isActive(Minecraft client) {
			return active;
		}

		@Override
		public boolean conflicts(KeyMappingContext other) {
			return this == other || other == IN_GAME;
		}
	}
}
