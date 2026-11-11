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

package net.fabricmc.fabric.test.gamerule.client;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.client.ClientGameRuleEvents;
import net.fabricmc.fabric.test.gamerule.GameRulesTestMod;

public class ClientGameRulesTestMod implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientGameRulesTestMod.class);
	private static final AtomicBoolean SYNCED_VALUE = new AtomicBoolean(false);

	@Override
	public void onInitializeClient() {
		ClientGameRuleEvents.syncCallback(GameRulesTestMod.SYNCED_BOOLEAN).register((value, _) -> {
			LOGGER.info("Boolean game rule value {} synced to client!", value);
			SYNCED_VALUE.set(value);
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.level != null && SYNCED_VALUE.get()) {
				LocalPlayer player = Objects.requireNonNull(client.player);
				client.level.addAlwaysVisibleParticle(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getY(), player.getZ(), 0.0, 0.0, 0.0);
			}
		});
	}
}
