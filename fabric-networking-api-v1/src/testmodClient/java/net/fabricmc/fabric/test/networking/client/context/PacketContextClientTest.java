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

package net.fabricmc.fabric.test.networking.client.context;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.test.networking.context.PacketContextTest;

public final class PacketContextClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Sending context checking packets from client to server

		ClientConfigurationConnectionEvents.COMPLETE.register((listener, client) -> {
			listener.send(new ServerboundCustomPayloadPacket(new PacketContextTest.ContextCheckPacket("Client Configuration")));
		});

		ClientPlayConnectionEvents.INIT.register((listener, client) -> {
			listener.send(new ServerboundCustomPayloadPacket(new PacketContextTest.ContextCheckPacket("Client Play")));
		});
	}
}
