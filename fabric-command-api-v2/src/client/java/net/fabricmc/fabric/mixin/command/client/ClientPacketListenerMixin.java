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
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.world.flag.FeatureFlagSet;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin implements ClientCommandInternals.LastReceivedCommandsPacketAccessor {
	@Shadow
	private CommandDispatcher<SharedSuggestionProvider> commands;

	@Shadow
	@Final
	private ClientSuggestionProvider suggestionsProvider;

	@Final
	@Shadow
	private FeatureFlagSet enabledFeatures;

	@Final
	@Shadow
	private RegistryAccess.Frozen registryAccess;

	@Unique
	private @Nullable ClientboundCommandsPacket lastReceivedCommandsPacket = null;

	@Inject(method = "handleLogin", at = @At("RETURN"))
	private void onGameJoin(ClientboundLoginPacket packet, CallbackInfo info) {
		final CommandDispatcher<FabricClientCommandSource> dispatcher = new CommandDispatcher<>();
		ClientCommandInternals.setActiveDispatcher(dispatcher);
		ClientCommandRegistrationCallback.EVENT.invoker().register(dispatcher, CommandBuildContext.simple(this.registryAccess, this.enabledFeatures));
		ClientCommandInternals.finalizeInit();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Inject(method = "handleCommands", at = @At("RETURN"))
	private void onOnCommandTree(ClientboundCommandsPacket packet, CallbackInfo info) {
		// Add the commands to the vanilla dispatcher for completion.
		// It's done here because both the server and the client commands have
		// to be in the same dispatcher and completion results.
		ClientCommandInternals.addCommands((CommandDispatcher) commands, (FabricClientCommandSource) suggestionsProvider);
	}

	@Inject(method = "handleCommands", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER))
	private void setLastReceivedCommandsPacket(ClientboundCommandsPacket packet, CallbackInfo ci) {
		this.lastReceivedCommandsPacket = packet;
	}

	@Inject(method = "sendUnattendedCommand", at = @At("HEAD"), cancellable = true)
	private void onSendCommand(String command, Screen screen, CallbackInfo info) {
		if (ClientCommandInternals.executeCommand(command)) {
			info.cancel();
		}
	}

	@Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
	private void onSendCommand(String command, CallbackInfo info) {
		if (ClientCommandInternals.executeCommand(command)) {
			info.cancel();
		}
	}

	@Override
	public @Nullable ClientboundCommandsPacket fabric_api$getLastReceivedCommandsPacket() {
		return this.lastReceivedCommandsPacket;
	}
}
