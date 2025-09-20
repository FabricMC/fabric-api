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

package net.fabricmc.fabric.test.client.keybinding;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class KeyBindingsTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register 2 before 1, but in-game 1 should appear before 2 due to sorting. Both should appear after all vanilla categories.
		KeyBinding.Category category2 = KeyBinding.Category.create(Identifier.of("fabric-key-binding-api-v1-testmod", "test_category_2"));
		KeyBinding.Category category1 = KeyBinding.Category.create(Identifier.of("fabric-key-binding-api-v1-testmod", "test_category_1"));

		KeyBinding binding1 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fabric-key-binding-api-v1-testmod.test_keybinding_1", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, category1));
		KeyBinding binding2 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fabric-key-binding-api-v1-testmod.test_keybinding_2", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, category1));
		KeyBinding stickyBinding = KeyBindingHelper.registerKeyBinding(new StickyKeyBinding("key.fabric-key-binding-api-v1-testmod.test_keybinding_sticky", GLFW.GLFW_KEY_R, category2, () -> true, false));
		KeyBinding duplicateBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fabric-key-binding-api-v1-testmod.test_keybinding_duplicate", GLFW.GLFW_KEY_RIGHT_SHIFT, category2));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			while (binding1.wasPressed()) {
				client.player.sendMessage(Text.literal("Key 1 was pressed!"), false);
			}

			while (binding2.wasPressed()) {
				client.player.sendMessage(Text.literal("Key 2 was pressed!"), false);
			}

			if (stickyBinding.isPressed()) {
				client.player.sendMessage(Text.literal("Sticky Key was pressed!"), false);
			}

			while (duplicateBinding.wasPressed()) {
				client.player.sendMessage(Text.literal("Duplicate Key was pressed!"), false);
			}
		});
	}
}
