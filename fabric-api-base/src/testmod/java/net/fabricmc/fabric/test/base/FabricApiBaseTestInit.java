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

package net.fabricmc.fabric.test.base;

import static net.minecraft.commands.Commands.literal;

import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.network.chat.Component;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class FabricApiBaseTestInit implements ModInitializer {
	@Override
	public void onInitialize() {
		// Command to call audit the mixin environment
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("audit_mixins").executes(context -> {
				context.getSource().sendSuccess(() -> Component.literal("Auditing mixin environment"), false);

				try {
					MixinEnvironment.getCurrentEnvironment().audit();
				} catch (Exception e) {
					// Use an assertion error to bypass error checking in CommandManager
					throw new AssertionError("Failed to audit mixin environment", e);
				}

				context.getSource().sendSuccess(() -> Component.literal("Successfully audited mixin environment"), false);

				return 1;
			}));
		});

		EventTests.run();
	}
}
