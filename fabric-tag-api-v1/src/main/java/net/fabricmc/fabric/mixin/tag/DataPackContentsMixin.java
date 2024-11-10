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

package net.fabricmc.fabric.mixin.tag;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;

import net.fabricmc.fabric.impl.tag.TagAliasLoader;

/**
 * Applies pending tag aliases to dynamic (including reloadable) registries.
 */
@Mixin(DataPackContents.class)
abstract class DataPackContentsMixin {
	@Unique
	private CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistriesByType;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void storeDynamicRegistries(CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, RegistryWrapper.WrapperLookup registries, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, List<?> pendingTagLoads, int functionPermissionLevel, CallbackInfo info) {
		dynamicRegistriesByType = dynamicRegistries;
	}

	// HEAD because we have to run before the lifecycle event injected into the same method.
	@Inject(method = "applyPendingTagLoads", at = @At("HEAD"))
	private void applyDynamicTagAliases(CallbackInfo info) {
		TagAliasLoader.applyToDynamicRegistries(dynamicRegistriesByType, ServerDynamicRegistryType.WORLDGEN);
		TagAliasLoader.applyToDynamicRegistries(dynamicRegistriesByType, ServerDynamicRegistryType.RELOADABLE);
	}
}
