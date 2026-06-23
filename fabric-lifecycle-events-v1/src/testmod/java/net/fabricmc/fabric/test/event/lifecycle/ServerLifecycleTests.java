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

package net.fabricmc.fabric.test.event.lifecycle;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

/**
 * Tests related to the lifecycle of a server.
 */
public final class ServerLifecycleTests implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("LifecycleEventsTest");
	private static final boolean SLOW_START_TEST = Boolean.getBoolean("fabric-lifecycle-events-v1.test.slow-start");

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (SLOW_START_TEST) {
				LOGGER.info("Simulating slow server start...");

				try {
					Thread.sleep(Duration.ofSeconds(10));
				} catch (InterruptedException e) {
					LOGGER.warn("Sleep was interrupted", e);
				}
			}

			LOGGER.info("Started Server!");
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			LOGGER.info("Stopping Server!");
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			LOGGER.info("Stopped Server!");
		});

		ServerLevelEvents.LOAD.register((server, level) -> {
			LOGGER.info("Loaded level " + level.dimension().identifier().toString());
		});

		ServerLevelEvents.UNLOAD.register((server, level) -> {
			LOGGER.info("Unloaded level " + level.dimension().identifier().toString());
		});

		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
			LOGGER.info("SyncDataPackContents received for {}", joined ? "join" : "reload");
		});

		ServerLifecycleEvents.BEFORE_SAVE.register((server, flush, force) -> {
			LOGGER.info("Starting Save with settings: Flush:{} Force:{}", flush, force);
		});

		ServerLifecycleEvents.AFTER_SAVE.register((server, flush, force) -> {
			LOGGER.info("Save Finished with settings: Flush:{} Force:{}", flush, force);
		});
	}
}
