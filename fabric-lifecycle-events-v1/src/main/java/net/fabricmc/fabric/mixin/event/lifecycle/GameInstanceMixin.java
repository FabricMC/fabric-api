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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.class_10961;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

@Mixin(class_10961.class)
public abstract class GameInstanceMixin {
	@Shadow
	public abstract MinecraftServer method_68961();

	@Shadow
	public abstract ResourceManager method_69006();

	@WrapOperation(method = "method_68968", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private <K, V> V onLoadWorld(Map<K, V> worlds, K registryKey, V serverWorld, Operation<V> original) {
		final V result = original.call(worlds, registryKey, serverWorld);
		ServerWorldEvents.LOAD.invoker().onWorldLoad(this.method_68961(), (ServerWorld) serverWorld);

		return result;
	}

	@Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;close()V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void onUnloadWorldAtShutdown(CallbackInfo ci, Iterator<ServerWorld> worlds, ServerWorld world) {
		ServerWorldEvents.UNLOAD.invoker().onWorldUnload(this.method_68961(), world);
	}

	@Inject(method = "method_68979", at = @At("HEAD"))
	private void startResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		ServerLifecycleEvents.START_DATA_PACK_RELOAD.invoker().startDataPackReload(this.method_68961(), (LifecycledResourceManager) this.method_69006());
	}

	@Inject(method = "method_68979", at = @At("TAIL"))
	private void endResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		cir.getReturnValue().handleAsync((value, throwable) -> {
			// Hook into fail
			ServerLifecycleEvents.END_DATA_PACK_RELOAD.invoker().endDataPackReload(this.method_68961(), (LifecycledResourceManager) this.method_69006(), throwable == null);
			return value;
		}, this.method_68961());
	}

	@Inject(method = "method_68984", at = @At("HEAD"))
	private void startSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
		ServerLifecycleEvents.BEFORE_SAVE.invoker().onBeforeSave(this.method_68961(), flush, force);
	}

	@Inject(method = "method_68984", at = @At("TAIL"))
	private void endSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
		ServerLifecycleEvents.AFTER_SAVE.invoker().onAfterSave(this.method_68961(), flush, force);
	}
}
