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

import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;

import net.fabricmc.fabric.impl.tag.TagAliasLoader;

/**
 * Applies pending tag aliases to dynamic (including reloadable) registries.
 * The priority is 999 because it must apply the injection to applyPendingTagLoads before the tag loaded lifecycle event.
 */
@Mixin(value = ReloadableServerResources.class, priority = 999)
abstract class ReloadableServerResourcesMixin {
	@Unique
	private LayeredRegistryAccess<RegistryLayer> dynamicRegistriesByType;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void storeDynamicRegistries(LayeredRegistryAccess<RegistryLayer> dynamicRegistries, HolderLookup.Provider registries, FeatureFlagSet enabledFeatures, Commands.CommandSelection environment, List<?> pendingTagLoads, PermissionSet permissionPredicate, CallbackInfo ci) {
		dynamicRegistriesByType = dynamicRegistries;
	}

	@Inject(method = "updateStaticRegistryTags", at = @At("RETURN"))
	private void applyDynamicTagAliases(CallbackInfo info) {
		// Note: when using /reload, dynamic registry tag reloading goes through the same system that is also used
		// for static registries. Luckily, it doesn't break anything to run the code below even in that case,
		// since the map of pending tag alias groups is cleared after they're applied in the first round.
		// This code also needs to run after the vanilla code so the pending tag reloads don't override
		// the alias groups for dynamic registries.
		TagAliasLoader.applyToDynamicRegistries(dynamicRegistriesByType, RegistryLayer.WORLDGEN);
		TagAliasLoader.applyToDynamicRegistries(dynamicRegistriesByType, RegistryLayer.RELOADABLE);
	}
}
