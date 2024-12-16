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

package net.fabricmc.fabric.mixin.client.gametest;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.impl.client.gametest.ThreadingImpl;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@WrapMethod(method = "runServer")
	private void onRunServer(Operation<Void> original) {
		if (ThreadingImpl.isServerRunning) {
			throw new IllegalStateException("Server is already running");
		}

		ThreadingImpl.isServerRunning = true;
		ThreadingImpl.PHASER.register();

		try {
			original.call();
		} finally {
			deregisterServer();
		}
	}

	@Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setCrashReport(Lnet/minecraft/util/crash/CrashReport;)V", shift = At.Shift.AFTER))
	protected void onCrash(CallbackInfo ci) {
		if (ThreadingImpl.testFailureException == null) {
			ThreadingImpl.testFailureException = new Throwable("The server crashed");
		}

		MinecraftClient.getInstance().scheduleStop();
		ThreadingImpl.setGameCrashed();
		deregisterServer();
	}

	@Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;runTasksTillTickEnd()V"))
	private void preRunTasks(CallbackInfo ci) {
		ThreadingImpl.enterPhase(ThreadingImpl.PHASE_SERVER_TASKS);
	}

	@Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;runTasksTillTickEnd()V", shift = At.Shift.AFTER))
	private void postRunTasks(CallbackInfo ci) {
		ThreadingImpl.enterPhase(ThreadingImpl.PHASE_CLIENT_TASKS);
		// client tasks happen here

		ThreadingImpl.serverCanAcceptTasks = true;
		ThreadingImpl.enterPhase(ThreadingImpl.PHASE_TEST);

		if (ThreadingImpl.testThread != null) {
			while (true) {
				try {
					ThreadingImpl.SERVER_SEMAPHORE.acquire();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				if (ThreadingImpl.taskToRun != null) {
					ThreadingImpl.taskToRun.run();
				} else {
					break;
				}
			}
		}

		ThreadingImpl.enterPhase(ThreadingImpl.PHASE_TICK);
	}

	@Unique
	private void deregisterServer() {
		ThreadingImpl.serverCanAcceptTasks = false;
		ThreadingImpl.PHASER.arriveAndDeregister();
		ThreadingImpl.isServerRunning = false;
	}
}
