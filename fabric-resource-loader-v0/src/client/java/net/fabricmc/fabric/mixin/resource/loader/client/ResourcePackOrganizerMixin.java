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

package net.fabricmc.fabric.mixin.resource.loader.client;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;

import net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile;

@Mixin(ResourcePackOrganizer.class)
public class ResourcePackOrganizerMixin {
	@Shadow
	@Final
	List<ResourcePackProfile> enabledPacks;

	@Shadow
	@Final
	List<ResourcePackProfile> disabledPacks;

	/**
	 * Do not list hidden packs in either enabledPacks or disabledPacks.
	 * They are managed entirely by ResourcePackManager on save, and are invisible to client.
	 */
	@Inject(method = "<init>", at = @At("TAIL"))
	private void removeHiddenPacksInit(Consumer<ResourcePackOrganizer.AbstractPack> updateCallback, Function iconIdSupplier, ResourcePackManager resourcePackManager, Consumer applier, CallbackInfo ci) {
		this.enabledPacks.removeIf(profile -> ((FabricResourcePackProfile) profile).fabric_isHidden());
		this.disabledPacks.removeIf(profile -> ((FabricResourcePackProfile) profile).fabric_isHidden());
	}

	@Inject(method = "refresh", at = @At("TAIL"))
	private void removeHiddenPacksRefresh(CallbackInfo ci) {
		this.enabledPacks.removeIf(profile -> ((FabricResourcePackProfile) profile).fabric_isHidden());
		this.disabledPacks.removeIf(profile -> ((FabricResourcePackProfile) profile).fabric_isHidden());
	}
}
