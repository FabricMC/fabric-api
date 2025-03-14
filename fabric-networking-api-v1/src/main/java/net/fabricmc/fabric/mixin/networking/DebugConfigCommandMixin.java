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

package net.fabricmc.fabric.mixin.networking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.command.DebugConfigCommand;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;

@Mixin(DebugConfigCommand.class)
public class DebugConfigCommandMixin {
	// endConfiguration() does not re-run the configuration tasks. This means we loose the state such as what channels we can send on when in the play phase.
	@Redirect(method = "executeUnconfig", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConfigurationNetworkHandler;endConfiguration()V"))
	private static void sendConfigurations(ServerConfigurationNetworkHandler networkHandler) {
		networkHandler.sendConfigurations();
	}
}
