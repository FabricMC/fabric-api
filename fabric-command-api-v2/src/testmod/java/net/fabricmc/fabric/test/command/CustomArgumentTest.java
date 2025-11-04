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

package net.fabricmc.fabric.test.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.test.command.argument.SmileyArgument;
import net.fabricmc.fabric.test.command.argument.SmileyArgumentType;

public class CustomArgumentTest implements ModInitializer {
	private static final String ARG_NAME = "smiley_value";

	@Override
	public void onInitialize() {
		ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath("fabric-command-test", "smiley"), SmileyArgumentType.class, SingletonArgumentInfo.contextFree(SmileyArgumentType::smiley));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					literal("fabric_custom_argument_test").then(
							argument(ARG_NAME, SmileyArgumentType.smiley())
								.executes(CustomArgumentTest::executeSmileyCommand)));
		});
	}

	private static int executeSmileyCommand(CommandContext<CommandSourceStack> context) {
		SmileyArgument smiley = context.getArgument(ARG_NAME, SmileyArgument.class);
		String feedback = switch (smiley) {
		case SAD -> "Oh no, here is a heart: <3";
		case HAPPY -> "Nice to see that you are having a good day :)";
		};
		context.getSource().sendSuccess(() -> Component.literal(feedback), false);

		return 1;
	}
}
