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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingContext;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class KeyBindingsTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeyBinding binding1 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fabric-key-binding-api-v1-testmod.test_keybinding_1", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, "key.category.first.test"));
		KeyBinding binding2 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fabric-key-binding-api-v1-testmod.test_keybinding_2", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "key.category.second.test"));
		KeyBinding stickyBinding = KeyBindingHelper.registerKeyBinding(new StickyKeyBinding("key.fabric-key-binding-api-v1-testmod.test_keybinding_sticky", GLFW.GLFW_KEY_R, "key.category.first.test", () -> true));
		KeyBinding duplicateBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fabric-key-binding-api-v1-testmod.test_keybinding_duplicate", GLFW.GLFW_KEY_RIGHT_SHIFT, "key.category.first.test"));

		KeyBinding inGameBinding = register("in_game_keybinding", GLFW.GLFW_KEY_EQUAL, "context", KeyBindingContext.IN_GAME);
		KeyBinding allBinding = register("all_keybinding", GLFW.GLFW_KEY_BACKSLASH, "context", KeyBindingContext.ALL);

		// context1 won't conflict with context2
		// therefore, one key from context1 and context2 will both be registered as pressed
		CustomKeyBindingContext context1 = new CustomKeyBindingContext();
		KeyBinding customCtxBinding1 = register("custom_context_1", GLFW.GLFW_KEY_SEMICOLON, "context", context1);
		KeyBinding customCtxBinding2 = register("custom_context_2", GLFW.GLFW_KEY_SEMICOLON, "context", context1);

		CustomKeyBindingContext context2 = new CustomKeyBindingContext();
		KeyBinding customCtxBinding3 = register("custom_context_3", GLFW.GLFW_KEY_SEMICOLON, "context", context2);
		KeyBinding customCtxBinding4 = register("custom_context_4", GLFW.GLFW_KEY_SEMICOLON, "context", context2);

		KeyBinding diamondSwordBinding = register("diamond_sword", GLFW.GLFW_KEY_I, "context", new ItemKeyBindingContext(Items.DIAMOND_SWORD));
		KeyBinding netheriteSwordBinding = register("netherite_sword", GLFW.GLFW_KEY_I, "context", new ItemKeyBindingContext(Items.NETHERITE_SWORD));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
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

			// When ALL binding is set to the same key, it then will conflict with all other bindings
			// only one binding will be marked as pressed, based on what binding registered first.
			// Vanilla bindings are registered last as it is made on GameOptions ctor instead of statically.
			// - allBinding & inGameBinding conflicts -> inGameBinding will be called
			// - allBinding & customCtxBinding conflicts -> allBinding will be called
			// - allBinding & vanilla conflicts -> allBinding will be called
			sendMessageWhenPressed(client, inGameBinding, "In-game key was pressed");
			sendMessageWhenPressed(client, allBinding, "ALL context key was pressed!");

			// 1 and 2 will conflict, 3 and 4 will conflict.
			// 1 and 3 should be called at the same time.
			sendMessageWhenPressed(client, customCtxBinding1, "Custom Context Key 1 was pressed!");
			sendMessageWhenPressed(client, customCtxBinding2, "Custom Context Key 2 was pressed!");
			sendMessageWhenPressed(client, customCtxBinding3, "Custom Context Key 3 was pressed!");
			sendMessageWhenPressed(client, customCtxBinding4, "Custom Context Key 4 was pressed!");

			// Won't conflict with each other, hold the respective item and press.
			sendMessageWhenPressed(client, diamondSwordBinding, "Diamond Sword Key was pressed!");
			sendMessageWhenPressed(client, netheriteSwordBinding, "Netherite Sword Key was pressed!");
		});
	}

	private static KeyBinding register(String key, int code, String category, KeyBindingContext context) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fabric-key-binding-api-v1-testmod." + key, code, "key.category." + category), context);
	}

	private static void sendMessageWhenPressed(MinecraftClient client, KeyBinding binding, String message) {
		while (binding.wasPressed()) {
			client.player.sendMessage(Text.literal(message));
		}
	}
}
