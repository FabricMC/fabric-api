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

package net.fabricmc.fabric.test.networking.client.configuration;

import net.fabricmc.fabric.test.networking.NetworkingTestmods;

import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.common.CustomClickActionC2SPacket;
import net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket;
import net.minecraft.text.ClickEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.test.networking.configuration.NetworkingConfigurationTest;

import java.util.Optional;

public class NetworkingConfigurationClientTest implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkingConfigurationTest.class);

	@Override
	public void onInitializeClient() {
		ClientConfigurationNetworking.registerGlobalReceiver(NetworkingConfigurationTest.ConfigurationPacket.ID, (packet, context) -> {
			// Handle stuff here

			// Respond back to the server that the task is complete
			context.responseSender().sendPacket(NetworkingConfigurationTest.ConfigurationCompletePacket.INSTANCE);
		});

		ClientConfigurationConnectionEvents.START.register((handler, client) -> {
			if (!ClientConfigurationNetworking.canSend(NetworkingConfigurationTest.ConfigurationStartPacket.ID)) {
				// This isn't fatal as it will happen when connecting to a vanilla server.
				LOGGER.warn("Not sending configuration start packet; is this a vanilla server?");
				return;
			}

			LOGGER.info("Sending configuration start packet to server");
			ClientConfigurationNetworking.send(NetworkingConfigurationTest.ConfigurationStartPacket.INSTANCE);
		});

		// This is a test of the CONFIGURATION event in CustomClickActionEvents. When this packet is received, a message
		// should appear in the log showing the event has been received by the server, with the payload.
		// Rather than using an actual dialog (which breaks E2E client tests), this mocks the behaviour of clicking on a
		// custom event button by sending the packet that such an action would normally send.
		ClientConfigurationNetworking.registerGlobalReceiver(NetworkingConfigurationTest.MockShowDialogPacket.ID, (packet, context) -> {
			context.responseSender().sendPacket(
					new CustomClickActionC2SPacket(
							NetworkingTestmods.id("configuration_event"),
							Optional.of(NbtString.of("this is a payload"))
					)
			);
		});
	}
}
