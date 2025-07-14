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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.test.networking.configuration.NetworkingConfigurationTest;

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
	}
}
