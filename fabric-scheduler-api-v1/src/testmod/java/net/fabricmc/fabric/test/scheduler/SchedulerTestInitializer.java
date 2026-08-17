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

import com.mojang.brigadier.Command;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.scheduler.v1.Scheduler;

public class SchedulerTestInitializer implements ModInitializer {
	private MinecraftServer server;

	private final Scheduler scheduler = Scheduler.createLoop(
			Scheduler.run(() -> server.getPlayerList().broadcastSystemMessage(Component.literal("Red"), false)),
			Scheduler.delay("red", 100),
			Scheduler.run(() -> server.getPlayerList().broadcastSystemMessage(Component.literal("Green"), false)),
			Scheduler.delay("green", 100),
			Scheduler.run(() -> server.getPlayerList().broadcastSystemMessage(Component.literal("Yellow"), false)),
			Scheduler.delay("yellow", 20)
	);

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> this.server = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> this.server = null);
		ServerTickEvents.START_SERVER_TICK.register(server -> scheduler.tick());

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
			dispatcher.register(Commands.literal("fabric-scheduler")
					.then(Commands.literal("start")
							.executes(ctx -> {
								scheduler.run();
								return Command.SINGLE_SUCCESS;
							})
					)
					.then(Commands.literal("stop")
							.executes(ctx -> {
								scheduler.stop();
								return Command.SINGLE_SUCCESS;
							})
					)
			);
		});
	}
}
