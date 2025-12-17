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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class ServerResourceReloadTests implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("LifecycleEventsTest");

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> {
			LOGGER.info("PREPARING FOR RELOAD");
		});

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (success) {
				LOGGER.info("FINISHED RELOAD on {}", Thread.currentThread());
			} else {
				// Failure can be tested by trying to disable the vanilla datapack
				LOGGER.error("FAILED TO RELOAD on {}", Thread.currentThread());
			}
		});
	}
}
