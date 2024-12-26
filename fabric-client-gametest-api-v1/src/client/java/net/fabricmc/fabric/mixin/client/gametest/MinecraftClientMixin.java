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

import com.google.common.base.Preconditions;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.world.level.storage.LevelStorage;

import net.fabricmc.fabric.impl.client.gametest.FabricClientGameTestRunner;
import net.fabricmc.fabric.impl.client.gametest.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.WindowHooks;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Unique
	private boolean startedClientGametests = false;
	@Unique
	private Runnable deferredTask = null;

	@Shadow
	@Nullable
	private Overlay overlay;

	@Shadow
	@Final
	private Window window;

	@WrapMethod(method = "run")
	private void onRun(Operation<Void> original) throws Throwable {
		if (ThreadingImpl.isClientRunning) {
			throw new IllegalStateException("Client is already running");
		}

		ThreadingImpl.isClientRunning = true;
		ThreadingImpl.PHASER.register();

		try {
			original.call();
		} finally {
			deregisterClient();

			if (ThreadingImpl.testFailureException != null) {
				throw ThreadingImpl.testFailureException;
			}
		}
	}

	@Inject(method = "cleanUpAfterCrash", at = @At("HEAD"))
	private void deregisterAfterCrash(CallbackInfo ci) {
		// Deregister a bit earlier than normal to allow for the integrated server to stop without waiting for the client
		ThreadingImpl.setGameCrashed();
		deregisterClient();
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		if (!startedClientGametests && overlay == null) {
			startedClientGametests = true;
			FabricClientGameTestRunner.start();
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;runTasks()V"))
	private void preRunTasks(CallbackInfo ci) {
		ThreadingImpl.enterPhase(ThreadingImpl.PHASE_SERVER_TASKS);
		// server tasks happen here
		ThreadingImpl.enterPhase(ThreadingImpl.PHASE_CLIENT_TASKS);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;runTasks()V", shift = At.Shift.AFTER))
	private void postRunTasks(CallbackInfo ci) {
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

	@Inject(method = "startIntegratedServer", at = @At("HEAD"), cancellable = true)
	private void deferStartIntegratedServer(LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, boolean newWorld, CallbackInfo ci) {
		if (ThreadingImpl.taskToRun != null) {
			// don't start the integrated server (which busywaits) inside a task
			deferredTask = () -> MinecraftClient.getInstance().startIntegratedServer(session, dataPackManager, saveLoader, newWorld);
			ci.cancel();
		}
	}

	@Inject(method = "startIntegratedServer", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V", remap = false))
	private void onStartIntegratedServerBusyWait(CallbackInfo ci) {
		// give the server a chance to tick too
		preRunTasks(ci);
		postRunTasks(ci);
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"), cancellable = true)
	private void deferDisconnect(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
		if (MinecraftClient.getInstance().getServer() != null && ThreadingImpl.taskToRun != null) {
			// don't disconnect (which busywaits) inside a task
			deferredTask = () -> MinecraftClient.getInstance().disconnect(disconnectionScreen, transferring);
			ci.cancel();
		}
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;render(Z)V", shift = At.Shift.AFTER))
	private void onDisconnectBusyWait(CallbackInfo ci) {
		// give the server a chance to tick too
		preRunTasks(ci);
		postRunTasks(ci);
	}

	@Inject(method = "getInstance", at = @At("HEAD"))
	private static void checkThreadOnGetInstance(CallbackInfoReturnable<MinecraftClient> cir) {
		Preconditions.checkState(
				Thread.currentThread() != ThreadingImpl.testThread,
				"MinecraftClient.getInstance() cannot be called from the gametest thread. Try using ClientGameTestContext.runOnClient or ClientGameTestContext.computeOnClient"
		);
	}

	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;hasZeroWidthOrHeight()Z"))
	private boolean hasZeroRealWidthOrHeight(boolean original) {
		WindowHooks windowHooks = (WindowHooks) (Object) window;
		return windowHooks.fabric_getRealFramebufferWidth() == 0 || windowHooks.fabric_getRealFramebufferHeight() == 0;
	}

	@Unique
	private static void deregisterClient() {
		if (ThreadingImpl.isClientRunning) {
			ThreadingImpl.clientCanAcceptTasks = false;
			ThreadingImpl.PHASER.arriveAndDeregister();
			ThreadingImpl.isClientRunning = false;
		}
	}
}
