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

import java.io.File;

import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourceType;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
	@Shadow
	private ResourcePackManager packManager;

	private CreateWorldScreenMixin() {
		super(null);
	}

	@ModifyVariable(method = "show(Lnet/minecraft/client/MinecraftClient;Ljava/lang/Runnable;Ljava/util/function/Function;Lnet/minecraft/client/world/GeneratorOptionsFactory;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/gui/screen/world/CreateWorldCallback;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;createServerConfig(Lnet/minecraft/resource/ResourcePackManager;Lnet/minecraft/resource/DataConfiguration;)Lnet/minecraft/server/SaveLoading$ServerConfig;"))
	private static ResourcePackManager onCreateResManagerInit(ResourcePackManager manager) {
		// Add mod data packs to the initial res pack manager so they are active even if the user doesn't use custom data packs
		manager.providers.add(new ModResourcePackCreator(ResourceType.SERVER_DATA));
		return manager;
	}

	@Redirect(method = "show(Lnet/minecraft/client/MinecraftClient;Ljava/lang/Runnable;Ljava/util/function/Function;Lnet/minecraft/client/world/GeneratorOptionsFactory;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/gui/screen/world/CreateWorldCallback;)V",
			at = @At(value = "FIELD", target = "Lnet/minecraft/resource/DataConfiguration;SAFE_MODE:Lnet/minecraft/resource/DataConfiguration;", ordinal = 0))
	private static DataConfiguration replaceDefaultSettings() {
		return ModResourcePackUtil.createDefaultDataConfiguration();
	}

	@Inject(method = "getScannedPack",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackManager;scanPacks()V", shift = At.Shift.BEFORE))
	private void onScanPacks(CallbackInfoReturnable<Pair<File, ResourcePackManager>> cir) {
		// Allow to display built-in data packs in the data pack selection screen at world creation.
		this.packManager.providers.add(new ModResourcePackCreator(ResourceType.SERVER_DATA));
	}
}
