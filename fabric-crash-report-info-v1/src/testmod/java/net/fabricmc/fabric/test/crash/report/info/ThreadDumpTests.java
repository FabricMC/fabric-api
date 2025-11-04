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

package net.fabricmc.fabric.test.crash.report.info;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.context.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.ServerWatchdog;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ThreadDumpTests implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadDumpTests.class);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(literal("print_thread_dump_test_command").executes(this::executeDumpCommand)));
	}

	private int executeDumpCommand(CommandContext<CommandSourceStack> context) {
		final CommandSourceStack source = context.getSource();
		CrashReport crashReport = ServerWatchdog.createWatchdogCrashReport("Watching Server", context.getSource().getServer().getRunningThread().threadId());
		LOGGER.info(crashReport.getFriendlyReport(ReportType.CRASH));
		source.sendSuccess(() -> Component.literal("Thread Dump printed to console."), false);
		return 1;
	}
}
