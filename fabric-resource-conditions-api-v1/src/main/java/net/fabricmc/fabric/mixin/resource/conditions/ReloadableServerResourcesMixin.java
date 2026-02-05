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

package net.fabricmc.fabric.mixin.resource.conditions;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;

import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
	@Inject(
			method = "loadResources",
			at = @At("HEAD")
	)
	private static void hookReload(ResourceManager resourceManager, LayeredRegistryAccess<RegistryLayer> dynamicRegistries, List<Registry.PendingTags<?>> pendingTagLoads, FeatureFlagSet enabledFeatures, Commands.CommandSelection environment, PermissionSet permissionPredicate, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
		ResourceConditionsImpl.currentFeatures = enabledFeatures;
	}
}
