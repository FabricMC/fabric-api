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

package net.fabricmc.fabric.test.scheduler;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.scheduler.v1.ScheduledTask;
import net.fabricmc.fabric.api.scheduler.v1.ServerScheduler;

public final class SchedulerTestMod implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerTestMod.class);

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ServerScheduler scheduler = ServerScheduler.get(server);

			scheduler.schedule(() -> LOGGER.info("One-shot task ran one second after server start"), 20);

			ScheduledTask mustNotRun = scheduler.schedule(() -> {
				throw new AssertionError("Cancelled task must never run");
			}, 40);
			mustNotRun.cancel();

			AtomicInteger runs = new AtomicInteger();
			ScheduledTask[] repeating = new ScheduledTask[1];
			repeating[0] = scheduler.scheduleRepeating(() -> {
				int count = runs.incrementAndGet();
				LOGGER.info("Repeating task run {}/3", count);

				if (count == 3) {
					repeating[0].cancel();
					LOGGER.info("Repeating task cancelled itself after three runs");
				}
			}, 20, 20);
		});
	}
}
