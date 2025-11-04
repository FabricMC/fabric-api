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

package net.fabricmc.fabric.api.event.client.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * This event fires every tick when the attack key (left mouse button by default) is pressed
 * (including clicking and holding the attack key).
 * If the callback returns true,
 * the vanilla handling (block breaking, entity attacking, hand swing) will be cancelled,
 * and the later callbacks of this event are also cancelled.
 *
 * <p>This event is client-only, which means handling it may require sending custom packets.
 *
 * <p>The event fires both when clicking and holding attack key.
 * To check whether the attack key is just clicked, use {@code clickCount != 0}
 *
 * <p>The vanilla attack cooldown and player game mode does not affect this event.
 * The mod probably needs to check {@link net.minecraft.client.Minecraft#missTime} and the game mode.
 * {@link net.minecraft.world.item.ItemCooldowns} can be used for custom item cooldown handling.
 */
public interface ClientPreAttackCallback {
	Event<ClientPreAttackCallback> EVENT = EventFactory.createArrayBacked(
			ClientPreAttackCallback.class,
			(listeners) -> (client, player, clickCount) -> {
				for (ClientPreAttackCallback event : listeners) {
					if (event.onClientPlayerPreAttack(client, player, clickCount)) {
						return true;
					}
				}

				return false;
			}
	);

	/**
	 * @param player the client player
	 * @param clickCount the click count of the attack key in this tick.
	 * @return whether to intercept attack handling
	 */
	boolean onClientPlayerPreAttack(Minecraft client, LocalPlayer player, int clickCount);
}
