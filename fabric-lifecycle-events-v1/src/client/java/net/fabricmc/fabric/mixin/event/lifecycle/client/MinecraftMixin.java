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

package net.fabricmc.fabric.mixin.event.lifecycle.client;

import java.util.concurrent.CompletableFuture;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow private boolean gameLoadFinished;

	@Inject(at = @At("HEAD"), method = "tick")
	private void onStartTick(CallbackInfo info) {
		ClientTickEvents.START_CLIENT_TICK.invoker().onStartTick((Minecraft) (Object) this);
	}

	@Inject(at = @At("RETURN"), method = "tick")
	private void onEndTick(CallbackInfo info) {
		ClientTickEvents.END_CLIENT_TICK.invoker().onEndTick((Minecraft) (Object) this);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V", shift = At.Shift.AFTER), method = "destroy")
	private void onStopping(CallbackInfo ci) {
		ClientLifecycleEvents.CLIENT_STOPPING.invoker().onClientStopping((Minecraft) (Object) this);
	}

	// We inject after the thread field is set so `ThreadExecutor#getThread` will work
	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;gameThread:Ljava/lang/Thread;", shift = At.Shift.AFTER, ordinal = 0), method = "run")
	private void onStart(CallbackInfo ci) {
		ClientLifecycleEvents.CLIENT_STARTED.invoker().onClientStarted((Minecraft) (Object) this);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;"), method = "<init>")
	private void onStartResourceLoad(CallbackInfo ci) {
		ClientLifecycleEvents.START_RESOURCE_RELOAD.invoker().startResourceReload((Minecraft) (Object) this, true);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ResourceLoadStateTracker;startReload(Lnet/minecraft/client/ResourceLoadStateTracker$ReloadReason;Ljava/util/List;)V"), method = "reloadResourcePacks(ZLnet/minecraft/client/Minecraft$GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;")
	private void onStartResourceReload(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		ClientLifecycleEvents.START_RESOURCE_RELOAD.invoker().startResourceReload((Minecraft) (Object) this, false);
	}

	@WrapMethod(method = "onResourceLoadFinished")
	private void onResourceLoadFinished(@Coerce Object gameLoadCookie, Operation<Void> original) {
		boolean first = !this.gameLoadFinished;
		original.call(gameLoadCookie);
		ClientLifecycleEvents.END_RESOURCE_RELOAD.invoker().endResourceReload((Minecraft) (Object) this, first);
	}

	@Inject(method = "updateLevelInEngines", at = @At("TAIL"))
	private void afterClientWorldChange(ClientLevel world, CallbackInfo ci) {
		if (world != null) {
			Minecraft client = (Minecraft) (Object) this;
			ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.invoker().afterWorldChange(client, world);
		}
	}
}
