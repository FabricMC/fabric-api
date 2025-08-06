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

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;

import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(DataPackContents.class)
public class DataPackContentsMixin {
	@Inject(
			method = "reload",
			at = @At("HEAD")
	)
	private static void hookReload(ResourceManager manager, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, List<Registry.PendingTagLoad<?>> pendingTagLoads, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, int functionPermissionLevel, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<DataPackContents>> cir) {
		ResourceConditionsImpl.currentFeatures = enabledFeatures;
	}
}
