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

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SimpleResourceReload;

import net.fabricmc.fabric.impl.resource.loader.FabricLifecycledResourceManager;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;

@Mixin(SimpleResourceReload.class)
public class SimpleResourceReloadMixin {
	@ModifyArg(method = "start(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/resource/ResourceReload;", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/SimpleResourceReload;create(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;)Lnet/minecraft/resource/ResourceReload;"))
	private static List<ResourceReloader> sortSimple(List<ResourceReloader> reloaders, @Local(argsOnly = true) ResourceManager resourceManager) {
		if (resourceManager instanceof FabricLifecycledResourceManager flrm) {
			return ResourceManagerHelperImpl.sort(flrm.fabric_getResourceType(), reloaders);
		}

		return reloaders;
	}

	@ModifyArg(method = "start(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/resource/ResourceReload;", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ProfiledResourceReload;start(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;)Lnet/minecraft/resource/ResourceReload;"))
	private static List<ResourceReloader> sortProfiled(List<ResourceReloader> reloaders, @Local(argsOnly = true) ResourceManager resourceManager) {
		if (resourceManager instanceof FabricLifecycledResourceManager flrm) {
			return ResourceManagerHelperImpl.sort(flrm.fabric_getResourceType(), reloaders);
		}

		return reloaders;
	}
}
