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

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class NetworkingChannelClientTest implements ClientModInitializer {
	public static final KeyBinding OPEN = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fabric-networking-api-v1-testmod.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_MENU, KeyBinding.Category.MISC));
	static final Set<Identifier> SUPPORTED_C2S_CHANNELS = new HashSet<>();

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				if (OPEN.wasPressed()) {
					client.setScreen(new ChannelScreen(this));
				}
			}
		});

		C2SPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
			SUPPORTED_C2S_CHANNELS.addAll(channels);

			if (MinecraftClient.getInstance().currentScreen instanceof ChannelScreen) {
				((ChannelScreen) MinecraftClient.getInstance().currentScreen).refresh();
			}
		});

		C2SPlayChannelEvents.UNREGISTER.register((handler, sender, client, channels) -> {
			SUPPORTED_C2S_CHANNELS.removeAll(channels);

			if (MinecraftClient.getInstance().currentScreen instanceof ChannelScreen) {
				((ChannelScreen) MinecraftClient.getInstance().currentScreen).refresh();
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
