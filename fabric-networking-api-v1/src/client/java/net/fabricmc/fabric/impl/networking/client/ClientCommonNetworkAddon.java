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

package net.fabricmc.fabric.impl.networking.client;

import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;

abstract class ClientCommonNetworkAddon<H, T extends ClientCommonPacketListenerImpl> extends AbstractChanneledNetworkAddon<H> {
	protected final T handler;
	protected final Minecraft client;

	protected boolean isServerReady = false;

	protected ClientCommonNetworkAddon(GlobalReceiverRegistry<H> receiver, Connection connection, String description, T handler, Minecraft client) {
		super(receiver, connection, description);
		this.handler = handler;
		this.client = client;
	}

	public void onServerReady() {
		this.isServerReady = true;
	}

	@Override
	protected void handleRegistration(Identifier channelName) {
		// If we can already send packets, immediately send the register packet for this channel
		if (this.isServerReady) {
			final RegistrationPayload payload = this.createRegistrationPayload(RegistrationPayload.REGISTER, Collections.singleton(channelName));

			if (payload != null) {
				this.sendPacket(payload);
			}
		}
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
		// If we can already send packets, immediately send the unregister packet for this channel
		if (this.isServerReady) {
			final RegistrationPayload payload = this.createRegistrationPayload(RegistrationPayload.UNREGISTER, Collections.singleton(channelName));

			if (payload != null) {
				this.sendPacket(payload);
			}
		}
	}

	@Override
	protected boolean isReservedChannel(Identifier channelName) {
		return NetworkingImpl.isReservedCommonChannel(channelName);
	}

	@Override
	protected void schedule(Runnable task) {
		client.execute(task);
	}
}
