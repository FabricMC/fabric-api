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

package net.fabricmc.fabric.test.base.client.mixin;

import com.google.common.base.Preconditions;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.world.level.storage.LevelStorage;

import net.fabricmc.fabric.test.base.client.FabricApiAutoTestClient;
import net.fabricmc.fabric.test.base.client.ThreadingImpl;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Unique
	private Runnable deferredTask = null;

	@WrapMethod(method = "run")
	private void onRun(Operation<Void> original) {
		if (FabricApiAutoTestClient.IS_AUTO_TEST) {
			if (ThreadingImpl.isClientRunning) {
				throw new IllegalStateException("Client is already running");
			}

			ThreadingImpl.isClientRunning = true;
			ThreadingImpl.PHASER.register();
		}

		try {
			original.call();
		} finally {
			if (FabricApiAutoTestClient.IS_AUTO_TEST) {
				ThreadingImpl.clientCanAcceptTasks = false;
				ThreadingImpl.PHASER.arriveAndDeregister();
				ThreadingImpl.isClientRunning = false;
			}
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;runTasks()V"))
	private void preRunTasks(CallbackInfo ci) {
		if (FabricApiAutoTestClient.IS_AUTO_TEST) {
			ThreadingImpl.enterPhase(ThreadingImpl.PHASE_SERVER_TASKS);
			// server tasks happen here
			ThreadingImpl.enterPhase(ThreadingImpl.PHASE_CLIENT_TASKS);
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;runTasks()V", shift = At.Shift.AFTER))
	private void postRunTasks(CallbackInfo ci) {
		if (FabricApiAutoTestClient.IS_AUTO_TEST) {
			ThreadingImpl.clientCanAcceptTasks = true;
			ThreadingImpl.enterPhase(ThreadingImpl.PHASE_TEST);

			if (ThreadingImpl.testThread != null) {
				while (true) {
					try {
						ThreadingImpl.CLIENT_SEMAPHORE.acquire();
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

			Runnable deferredTask = this.deferredTask;
			this.deferredTask = null;

			if (deferredTask != null) {
				deferredTask.run();
			}
		}
	}

	@Inject(method = "startIntegratedServer", at = @At("HEAD"), cancellable = true)
	private void deferStartIntegratedServer(LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, boolean newWorld, CallbackInfo ci) {
		if (FabricApiAutoTestClient.IS_AUTO_TEST && ThreadingImpl.taskToRun != null) {
			// don't start the integrated server (which busywaits) inside a task
			deferredTask = () -> MinecraftClient.getInstance().startIntegratedServer(session, dataPackManager, saveLoader, newWorld);
			ci.cancel();
		}
	}

	@Inject(method = "startIntegratedServer", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V", remap = false))
	private void onStartIntegratedServerBusyWait(CallbackInfo ci) {
		if (FabricApiAutoTestClient.IS_AUTO_TEST) {
			// give the server a chance to tick too
			preRunTasks(ci);
			postRunTasks(ci);
		}
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"), cancellable = true)
	private void deferDisconnect(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
		if (FabricApiAutoTestClient.IS_AUTO_TEST && MinecraftClient.getInstance().getServer() != null && ThreadingImpl.taskToRun != null) {
			// don't disconnect (which busywaits) inside a task
			deferredTask = () -> MinecraftClient.getInstance().disconnect(disconnectionScreen, transferring);
			ci.cancel();
		}
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;render(Z)V", shift = At.Shift.AFTER))
	private void onDisconnectBusyWait(CallbackInfo ci) {
		if (FabricApiAutoTestClient.IS_AUTO_TEST) {
			// give the server a chance to tick too
			preRunTasks(ci);
			postRunTasks(ci);
		}
	}

	@Inject(method = "getInstance", at = @At("HEAD"))
	private static void checkThreadOnGetInstance(CallbackInfoReturnable<MinecraftClient> cir) {
		if (FabricApiAutoTestClient.IS_AUTO_TEST) {
			// TODO: add suggestion of runOnClient etc when API methods are added
			Preconditions.checkState(Thread.currentThread() != ThreadingImpl.testThread, "MinecraftClient.getInstance() cannot be called from the test thread");
		}
	}
}
