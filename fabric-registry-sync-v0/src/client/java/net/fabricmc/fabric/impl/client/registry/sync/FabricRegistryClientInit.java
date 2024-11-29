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

package net.fabricmc.fabric.impl.client.registry.sync;

import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.text.Text;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.SyncCompletePayload;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistryPacketHandler;

public class FabricRegistryClientInit implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(FabricRegistryClientInit.class);

	@Override
	public void onInitializeClient() {
		registerSyncPacketReceiver(RegistrySyncManager.DIRECT_PACKET_HANDLER);
	}

	private <T extends RegistryPacketHandler.RegistrySyncPayload> void registerSyncPacketReceiver(RegistryPacketHandler<T> packetHandler) {
		ClientConfigurationNetworking.registerGlobalReceiver(packetHandler.getPacketId(), (payload, context) -> {
			ClientRegistrySyncHandler.receivePacket(context.client(), packetHandler, payload, RegistrySyncManager.DEBUG || !context.client().isInSingleplayer())
					.whenComplete((complete, throwable) -> {
						if (throwable != null) {
							LOGGER.error("Registry remapping failed!", throwable);
							context.client().execute(() -> context.responseSender().disconnect(getText(throwable)));
							return;
						}

						if (complete) {
							context.responseSender().sendPacket(SyncCompletePayload.INSTANCE);
						}
					});
		});
	}

	private Text getText(Throwable e) {
		if (e instanceof RemapException remapException) {
			final Text text = remapException.getText();

			if (text != null) {
				return text;
			}
		} else if (e instanceof CompletionException completionException) {
			return getText(completionException.getCause());
		}

		return Text.literal("Registry remapping failed: " + e.getMessage());
	}
}
