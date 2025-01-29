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

package net.fabricmc.fabric.mixin.client.gametest.lifecycle;

import java.util.concurrent.CompletableFuture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;

import net.fabricmc.fabric.impl.client.gametest.util.DedicatedServerImplUtil;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {
	@Inject(method = "setupServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/MinecraftDedicatedServer;loadWorld()V"))
	private void captureServerInstance(CallbackInfoReturnable<Boolean> cir) {
		// Capture the server instance once the server is ready to be connected to
		CompletableFuture<MinecraftDedicatedServer> serverFuture = DedicatedServerImplUtil.serverFuture;

		if (serverFuture != null) {
			serverFuture.complete((MinecraftDedicatedServer) (Object) this);
		}
	}

	// Don't call shutdownExecutors as we are running the dedi server within the client process.
	@WrapOperation(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;shutdownExecutors()V"))
	private void dontStopExecutors(Operation<Void> original) {
		// Never needed, as this is a client mixin.
	}
}
