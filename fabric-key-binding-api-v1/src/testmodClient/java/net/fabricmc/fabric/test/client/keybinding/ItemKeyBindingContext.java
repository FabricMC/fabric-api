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

package net.fabricmc.fabric.test.client.keybinding;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingContext;

public class ItemKeyBindingContext implements KeyBindingContext {
	private final Item item;

	public ItemKeyBindingContext(Item item) {
		this.item = item;
	}

	@Override
	public boolean isActive(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		return IN_GAME.isActive(client) && player != null && player.getStackInHand(Hand.MAIN_HAND).isOf(item);
	}

	@Override
	public boolean conflicts(KeyBindingContext other) {
		return this == other || IN_GAME == other;
	}
}
