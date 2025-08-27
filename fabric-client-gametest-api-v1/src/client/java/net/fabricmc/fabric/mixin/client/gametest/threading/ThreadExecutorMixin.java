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

package net.fabricmc.fabric.mixin.client.gametest.threading;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.thread.ThreadExecutor;

import net.fabricmc.fabric.impl.client.gametest.threading.NetworkSynchronizer;

@Mixin(ThreadExecutor.class)
public class ThreadExecutorMixin {
	@Inject(method = "send", at = @At("HEAD"))
	private void onPacketHandlerSchedule(Runnable task, CallbackInfo ci) {
		switch ((Object) this) {
		case MinecraftClient $ -> NetworkSynchronizer.CLIENTBOUND.preTaskAdded(task);
		case MinecraftServer $ -> NetworkSynchronizer.SERVERBOUND.preTaskAdded(task);
		default -> {
		}
		}
	}

	@Inject(method = "executeTask", at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V", shift = At.Shift.AFTER))
	private void onPacketHandlerRun(Runnable task, CallbackInfo ci) {
		switch ((Object) this) {
		case MinecraftClient $ -> NetworkSynchronizer.CLIENTBOUND.postTaskRun(task);
		case MinecraftServer $ -> NetworkSynchronizer.SERVERBOUND.postTaskRun(task);
		default -> {
		}
		}
	}
}
