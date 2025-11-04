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

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.test.attachment.AttachmentTestMod;

public class AttachmentClientTestMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof LocalPlayer) {
				entity.onAttachedSet(AttachmentTestMod.SYNCED_RENDER_DISTANCE).register((oldValue, newValue) -> {
					OptionInstance<Integer> viewDistance = Minecraft.getInstance().options.renderDistance();

					if (viewDistance.get() < newValue) {
						viewDistance.set(newValue);
						Minecraft.getInstance().gui.getChat().addMessage(Component.nullToEmpty("The server requested to up the render distance to " + newValue));
					}
				});
			}
		});
	}
}
