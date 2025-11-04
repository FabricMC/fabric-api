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

package net.fabricmc.fabric.test.networking.client.channeltest;

import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class NetworkingChannelClientTest implements ClientModInitializer {
	public static final KeyMapping OPEN = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.fabric-networking-api-v1-testmod.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_MENU, KeyMapping.Category.MISC));
	static final Set<Identifier> SUPPORTED_C2S_CHANNELS = new HashSet<>();

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				if (OPEN.consumeClick()) {
					client.setScreen(new ChannelScreen(this));
				}
			}
		});

		C2SPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
			SUPPORTED_C2S_CHANNELS.addAll(channels);

			if (Minecraft.getInstance().screen instanceof ChannelScreen) {
				((ChannelScreen) Minecraft.getInstance().screen).refresh();
			}
		});

		C2SPlayChannelEvents.UNREGISTER.register((handler, sender, client, channels) -> {
			SUPPORTED_C2S_CHANNELS.removeAll(channels);

			if (Minecraft.getInstance().screen instanceof ChannelScreen) {
				((ChannelScreen) Minecraft.getInstance().screen).refresh();
			}
		});

		// State destruction on disconnection:
		ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> {
			SUPPORTED_C2S_CHANNELS.clear();
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			SUPPORTED_C2S_CHANNELS.clear();
		});
	}
}
