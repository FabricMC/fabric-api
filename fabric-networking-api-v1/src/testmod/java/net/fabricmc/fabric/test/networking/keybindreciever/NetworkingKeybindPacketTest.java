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

package net.fabricmc.fabric.test.networking.keybindreciever;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

// Listens for a packet from the client which is sent to the server when a keybinding is pressed.
// In response the server will send a message containing the keybind text letting the client know it pressed that key.
public final class NetworkingKeybindPacketTest implements ModInitializer {
	private static void receive(KeybindPayload payload, ServerPlayNetworking.Context context) {
		context.player().getEntityWorld().getServer().execute(() -> context.player().sendMessage(Text.literal("So you pressed ").append(Text.keybind("fabric-networking-api-v1-testmod-keybind").styled(style -> style.withFormatting(Formatting.BLUE))), false));
	}

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(KeybindPayload.ID, KeybindPayload.CODEC);
		ServerPlayConnectionEvents.INIT.register((handler, server) -> ServerPlayNetworking.registerReceiver(handler, KeybindPayload.ID, NetworkingKeybindPacketTest::receive));
	}
}
