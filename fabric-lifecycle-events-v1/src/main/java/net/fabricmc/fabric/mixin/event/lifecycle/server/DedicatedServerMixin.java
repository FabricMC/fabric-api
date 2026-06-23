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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.notifications.NotificationManager;

import net.fabricmc.fabric.mixin.event.lifecycle.MinecraftServerMixin;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin extends MinecraftServerMixin {
	@Redirect(method = "initServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/notifications/NotificationManager;serverStarted()V"))
	private void deferServerStartedNotification(NotificationManager instance) {
		// Delay the JSON RPC server started notification until the ServerLifecycleEvents.SERVER_STARTED event is fired.
	}

	@Unique
	@Override
	public void afterServerStartedEvent() {
		super.afterServerStartedEvent();
		this.notificationManager().serverStarted();
	}
}
