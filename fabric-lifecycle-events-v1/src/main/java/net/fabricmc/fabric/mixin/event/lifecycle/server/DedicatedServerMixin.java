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

import java.util.Locale;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.Util;

import net.fabricmc.fabric.mixin.event.lifecycle.MinecraftServerMixin;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin extends MinecraftServerMixin {
	@Shadow
	@Final
	private static Logger LOGGER;
	@Unique
	private long levelNanoTime = Long.MIN_VALUE;

	@WrapOperation(method = "initServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getNanos()J"), slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/UserNameToIdResolver;resolveOfflineUsers(Z)V")))
	private long captureDoneLogStartNanos(Operation<Long> original) {
		return this.levelNanoTime = original.call();
	}

	@Redirect(method = "initServer", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"), to = @At(value = "FIELD", target = "Lnet/minecraft/server/dedicated/DedicatedServerProperties;announcePlayerAchievements:Ljava/lang/Boolean;", opcode = Opcodes.GETFIELD)))
	private void deferDoneLog(Logger logger, String message, Object arg) {
		// Delay the dedicated server's readiness log until after ServerLifecycleEvents.SERVER_STARTED has been fired.
	}

	@Unique
	@Override
	public void afterServerStartedEvent() {
		super.afterServerStartedEvent();

		if (this.levelNanoTime == Long.MIN_VALUE) {
			throw new IllegalStateException();
		}

		double elapsed = Util.getNanos() - levelNanoTime;
		String time = String.format(Locale.ROOT, "%.3fs", elapsed / 1.0E9);
		LOGGER.info("Done ({})! For help, type \"help\"", time);
		this.levelNanoTime = Long.MIN_VALUE;
	}
}
