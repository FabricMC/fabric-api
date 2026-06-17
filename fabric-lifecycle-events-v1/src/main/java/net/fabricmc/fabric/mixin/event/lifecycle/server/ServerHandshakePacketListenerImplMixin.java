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

package net.fabricmc.fabric.mixin.event.lifecycle.server;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;

import net.fabricmc.fabric.impl.event.lifecycle.MinecraftServerHooks;

@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakePacketListenerImplMixin {
	@Unique
	private static final Component STARTUP_DISCONNET_REASON = Component.literal("Server is still starting!");

	@Shadow
	@Final
	private Connection connection;
	@Shadow
	@Final
	private MinecraftServer server;

	@Unique
	private boolean hasBecomeReady = false;

	// Reject connections untill after ServerLifecycleEvents.SERVER_STARTED has been fired
	@Inject(method = "handleIntention", at = @At("HEAD"), cancellable = true)
	private void rejectConnectionsDuringStartup(ClientIntentionPacket packet, CallbackInfo ci) {
		if (hasBecomeReady) {
			return;
		}

		if (!this.server.isDedicatedServer() || this.connection.isMemoryConnection() || ((MinecraftServerHooks) this.server).fabric$isStartupReady()) {
			hasBecomeReady = true;
			return;
		}

		this.connection.disconnect(STARTUP_DISCONNET_REASON);
		ci.cancel();
	}
}
