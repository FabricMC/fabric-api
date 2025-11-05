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

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
	@Unique
	private RegistryAccess dynamicRegistryManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(LayeredRegistryAccess<RegistryLayer> combinedDynamicRegistries, HolderLookup.Provider registries, FeatureFlagSet enabledFeatures, Commands.CommandSelection environment, List pendingTagLoads, PermissionSet permissionPredicate, CallbackInfo ci) {
		dynamicRegistryManager = combinedDynamicRegistries.compositeAccess();
	}

	@Inject(method = "updateStaticRegistryTags", at = @At("TAIL"))
	private void hookRefresh(CallbackInfo ci) {
		CommonLifecycleEvents.TAGS_LOADED.invoker().onTagsLoaded(dynamicRegistryManager, false);
	}
}
