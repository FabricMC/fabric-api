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

package net.fabricmc.fabric.test.event.lifecycle.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.test.event.lifecycle.ServerLifecycleTests;

public final class ClientTickTests implements ClientModInitializer {
	private boolean tagsLoadedCalled;
	private final Map<ResourceKey<Level>, Integer> tickTracker = new HashMap<>();
	private int ticks;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			this.ticks++; // Just track our own tick since the client doesn't have a ticks value.

			if (this.ticks % 200 == 0) {
				ServerLifecycleTests.LOGGER.info("Ticked Client at " + this.ticks + " ticks.");
			}
		});

		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
			if (client) tagsLoadedCalled = true;
		});

		ClientTickEvents.END_WORLD_TICK.register(world -> {
			if (!tagsLoadedCalled) {
				throw new IllegalStateException("TAGS_LOADED was not invoked during configuration!");
			}

			final int worldTicks = this.tickTracker.computeIfAbsent(world.dimension(), k -> 0);

			if (worldTicks % 200 == 0) { // Log every 200 ticks to verify the tick callback works on the client world
				ServerLifecycleTests.LOGGER.info("Ticked Client World - " + worldTicks + " ticks:" + world.dimension());
			}

			this.tickTracker.put(world.dimension(), worldTicks + 1);
		});
	}
}
