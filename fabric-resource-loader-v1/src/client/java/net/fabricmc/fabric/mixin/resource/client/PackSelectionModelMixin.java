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

package net.fabricmc.fabric.mixin.resource.client;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

import net.fabricmc.fabric.impl.resource.pack.FabricPack;

@Mixin(PackSelectionModel.class)
public class PackSelectionModelMixin {
	@Shadow
	@Final
	List<Pack> selected;

	@Shadow
	@Final
	List<Pack> unselected;

	/**
	 * Do not list hidden packs in either enabledPacks or disabledPacks.
	 * They are managed entirely by ResourcePackManager on save, and are invisible to client.
	 */
	@Inject(method = "<init>", at = @At("TAIL"))
	private void removeHiddenPacksInit(Consumer<PackSelectionModel.EntryBase> updateCallback, Function iconIdSupplier, PackRepository resourcePackManager, Consumer applier, CallbackInfo ci) {
		this.selected.removeIf(profile -> ((FabricPack) profile).fabric$isHidden());
		this.unselected.removeIf(profile -> ((FabricPack) profile).fabric$isHidden());
	}

	@Inject(method = "findNewPacks", at = @At("TAIL"))
	private void removeHiddenPacksRefresh(CallbackInfo ci) {
		this.selected.removeIf(profile -> ((FabricPack) profile).fabric$isHidden());
		this.unselected.removeIf(profile -> ((FabricPack) profile).fabric$isHidden());
	}
}
