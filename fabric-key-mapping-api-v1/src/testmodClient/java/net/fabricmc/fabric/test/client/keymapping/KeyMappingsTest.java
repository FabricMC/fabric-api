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
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class KeyMappingsTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register 2 before 1, but in-game 1 should appear before 2 due to sorting. Both should appear after all vanilla categories.
		KeyMapping.Category category2 = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("fabric-key-mapping-api-v1-testmod", "test_category_2"));
		KeyMapping.Category category1 = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("fabric-key-mapping-api-v1-testmod", "test_category_1"));

		KeyMapping binding1 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fabric-key-mapping-api-v1-testmod.test_keymapping_1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, category1));
		KeyMapping binding2 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fabric-key-mapping-api-v1-testmod.test_keymapping_2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, category1));
		KeyMapping stickyBinding = KeyMappingHelper.registerKeyMapping(new ToggleKeyMapping("key.fabric-key-mapping-api-v1-testmod.test_keymapping_sticky", GLFW.GLFW_KEY_R, category2, () -> true, false));
		KeyMapping duplicateBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fabric-key-mapping-api-v1-testmod.test_keymapping_duplicate", GLFW.GLFW_KEY_RIGHT_SHIFT, category2));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			while (binding1.consumeClick()) {
				client.player.sendSystemMessage(Component.literal("Key 1 was pressed!"));
			}

			while (binding2.consumeClick()) {
				client.player.sendSystemMessage(Component.literal("Key 2 was pressed!"));
			}

			if (stickyBinding.isDown()) {
				client.player.sendSystemMessage(Component.literal("Sticky Key was pressed!"));
			}

			while (duplicateBinding.consumeClick()) {
				client.player.sendSystemMessage(Component.literal("Duplicate Key was pressed!"));
			}
		});
	}
}
