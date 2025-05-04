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

package net.fabricmc.fabric.mixin.event.lifecycle;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Shadow
	private MinecraftServer.ResourceManagerHolder resourceManagerHolder;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"), method = "runServer")
	private void beforeSetupServer(CallbackInfo info) {
		ServerLifecycleEvents.SERVER_STARTING.invoker().onServerStarting((MinecraftServer) (Object) this);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createMetadata()Lnet/minecraft/server/ServerMetadata;", ordinal = 0), method = "runServer")
	private void afterSetupServer(CallbackInfo info) {
		ServerLifecycleEvents.SERVER_STARTED.invoker().onServerStarted((MinecraftServer) (Object) this);
	}

	@Inject(at = @At("HEAD"), method = "shutdown")
	private void beforeShutdownServer(CallbackInfo info) {
		ServerLifecycleEvents.SERVER_STOPPING.invoker().onServerStopping((MinecraftServer) (Object) this);
	}

	@Inject(at = @At("TAIL"), method = "shutdown")
	private void afterShutdownServer(CallbackInfo info) {
		ServerLifecycleEvents.SERVER_STOPPED.invoker().onServerStopped((MinecraftServer) (Object) this);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickWorlds(Ljava/util/function/BooleanSupplier;)V"), method = "tick")
	private void onStartTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickEvents.START_SERVER_TICK.invoker().onStartTick((MinecraftServer) (Object) this);
	}

	@Inject(at = @At("TAIL"), method = "tick")
	private void onEndTick(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
		ServerTickEvents.END_SERVER_TICK.invoker().onEndTick((MinecraftServer) (Object) this);
	}

	@WrapOperation(method = "createWorlds", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private <K, V> V onLoadWorld(Map<K, V> worlds, K registryKey, V serverWorld, Operation<V> original) {
		final V result = original.call(worlds, registryKey, serverWorld);
		ServerWorldEvents.LOAD.invoker().onWorldLoad((MinecraftServer) (Object) this, (ServerWorld) serverWorld);

		return result;
	}

	@Inject(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;close()V"))
	private void onUnloadWorldAtShutdown(CallbackInfo ci, @Local ServerWorld world) {
		ServerWorldEvents.UNLOAD.invoker().onWorldUnload((MinecraftServer) (Object) this, world);
	}

	@Inject(method = "reloadResources", at = @At("HEAD"))
	private void startResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		ServerLifecycleEvents.START_DATA_PACK_RELOAD.invoker().startDataPackReload((MinecraftServer) (Object) this, this.resourceManagerHolder.resourceManager());
	}

	@Inject(method = "reloadResources", at = @At("TAIL"))
	private void endResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		cir.getReturnValue().handleAsync((value, throwable) -> {
			// Hook into fail
			ServerLifecycleEvents.END_DATA_PACK_RELOAD.invoker().endDataPackReload((MinecraftServer) (Object) this, this.resourceManagerHolder.resourceManager(), throwable == null);
			return value;
		}, (MinecraftServer) (Object) this);
	}

	@Inject(method = "save", at = @At("HEAD"))
	private void startSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
		ServerLifecycleEvents.BEFORE_SAVE.invoker().onBeforeSave((MinecraftServer) (Object) this, flush, force);
	}

	@Inject(method = "save", at = @At("TAIL"))
	private void endSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
		ServerLifecycleEvents.AFTER_SAVE.invoker().onAfterSave((MinecraftServer) (Object) this, flush, force);
	}
}
