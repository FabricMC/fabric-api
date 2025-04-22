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

package net.fabricmc.fabric.test.attachment.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.test.attachment.AttachmentTestMod;

public class AttachmentClientTestMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AttachmentTestMod.SYNCED_REQUESTED_SIMULATION_DISTANCE.onModify().register((newValue, target, isClient) -> {
			if (isClient) {
				MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Server requested that the simulation distance be set to " + newValue));
				MinecraftClient.getInstance().options.getSimulationDistance().setValue(newValue);
			}

			return newValue;
		});
	}
}
