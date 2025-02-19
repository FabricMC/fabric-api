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

package net.fabricmc.fabric.mixin.resource.loader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SimpleResourceReload;
import net.minecraft.util.Unit;

import net.fabricmc.fabric.impl.resource.loader.FabricLifecycledResourceManager;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;

@Mixin(SimpleResourceReload.class)
public class SimpleResourceReloadMixin {
	@Unique
	private static final ThreadLocal<ResourceType> fabric_resourceType = new ThreadLocal<>();

	@Inject(method = "start(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/resource/ResourceReload;", at = @At("HEAD"))
	private static void method_40087(ResourceManager resourceManager, List<ResourceReloader> list, Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture, boolean bl, CallbackInfoReturnable<ResourceReload> cir) {
		if (resourceManager instanceof FabricLifecycledResourceManager flrm) {
			fabric_resourceType.set(flrm.fabric_getResourceType());
		}
	}

	@ModifyArg(method = "start(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/resource/ResourceReload;", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/SimpleResourceReload;create(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;)Lnet/minecraft/resource/ResourceReload;"))
	private static List<ResourceReloader> sortSimple(List<ResourceReloader> reloaders) {
		List<ResourceReloader> sorted = ResourceManagerHelperImpl.sort(fabric_resourceType.get(), reloaders);
		fabric_resourceType.remove();
		return sorted;
	}

	@ModifyArg(method = "start(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/resource/ResourceReload;", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/SimpleResourceReload;create(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;)Lnet/minecraft/resource/ResourceReload;"))
	private static List<ResourceReloader> sortProfiled(List<ResourceReloader> reloaders) {
		List<ResourceReloader> sorted = ResourceManagerHelperImpl.sort(fabric_resourceType.get(), reloaders);
		fabric_resourceType.remove();
		return sorted;
	}
}
