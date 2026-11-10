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

package net.fabricmc.fabric.mixin.command.client;

import com.mojang.brigadier.CommandDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionContext;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.world.flag.FeatureFlagSet;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;
import net.fabricmc.fabric.impl.command.client.ClientSuggestionProviderExtensions;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
	@Shadow
	@Final
	private ClientSuggestionContext suggestionsProvider;

	@Shadow
	@Final
	private ClientSuggestionContext restrictedSuggestionsProvider;

	@Final
	@Shadow
	private FeatureFlagSet enabledFeatures;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void init(Minecraft minecraft, Connection connection, CommonListenerCookie cookie, CallbackInfo ci) {
		((ClientSuggestionProviderExtensions) this.suggestionsProvider).fabric_markAttended();
	}

	@Inject(method = "handleLogin", at = @At("RETURN"))
	private void onGameJoin(ClientboundLoginPacket packet, CallbackInfo info) {
		final CommandDispatcher<FabricClientCommandSource> dispatcher = new CommandDispatcher<>();
		ClientCommandInternals.setActiveDispatcher(dispatcher);
		ClientCommandRegistrationCallback.EVENT.invoker().register(dispatcher, CommandBuildContext.simple(this.enabledFeatures));
		ClientCommandInternals.finalizeInit();
		ClientCommandInternals.addCommands((ClientPacketListener) (Object) this);
	}

	@Inject(method = "sendUnattendedCommand", at = @At("HEAD"), cancellable = true)
	private void onSendCommand(String command, Screen screen, CallbackInfo info) {
		if (ClientCommandInternals.executeCommand(command, (FabricClientCommandSource) suggestionsProvider, (FabricClientCommandSource) restrictedSuggestionsProvider)) {
			info.cancel();
		}
	}

	@Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
	private void onSendCommand(String command, CallbackInfo info) {
		if (ClientCommandInternals.executeCommand(command, (FabricClientCommandSource) suggestionsProvider, null)) {
			info.cancel();
		}
	}
}
