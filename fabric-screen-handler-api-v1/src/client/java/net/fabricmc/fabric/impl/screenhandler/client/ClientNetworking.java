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

package net.fabricmc.fabric.impl.screenhandler.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.impl.screenhandler.Networking;

public final class ClientNetworking implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-screen-handler-api-v1/client");

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(Networking.OpenScreenPayload.ID, (payload, context) -> {
			this.openScreen(payload);
		});
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <D> void openScreen(Networking.OpenScreenPayload<D> payload) {
		Identifier typeId = payload.identifier();
		int syncId = payload.syncId();
		Component title = payload.title();

		MenuType<?> type = BuiltInRegistries.MENU.getValue(typeId);

		if (type == null || payload.data() == null) {
			LOGGER.warn("Unknown screen handler ID: {}", typeId);
			return;
		}

		if (!(type instanceof ExtendedScreenHandlerType)) {
			LOGGER.warn("Received extended opening packet for non-extended screen handler {}", typeId);
			return;
		}

		MenuScreens.ScreenConstructor screenFactory = MenuScreens.getConstructor(type);

		if (screenFactory != null) {
			Minecraft client = Minecraft.getInstance();
			Player player = client.player;

			Screen screen = screenFactory.create(
					((ExtendedScreenHandlerType<?, D>) type).create(syncId, player.getInventory(), payload.data()),
					player.getInventory(),
					title
			);

			player.containerMenu = ((MenuAccess<?>) screen).getMenu();
			client.setScreen(screen);
		} else {
			LOGGER.warn("Screen not registered for screen handler {}!", typeId);
		}
	}
}
