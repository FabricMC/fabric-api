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

package net.fabricmc.fabric.mixin.client.gametest;

import java.io.IOException;
import java.nio.file.Files;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.world.level.LevelProperties;

import net.fabricmc.fabric.impl.client.gametest.ClientGameTestImpl;
import net.fabricmc.fabric.impl.client.gametest.DedicatedServerImplUtil;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
	@Inject(method = "createLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServerLoader;tryLoad(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Runnable;Z)V"), cancellable = true)
	private void createLevelDataForServers(CallbackInfo ci, @Local CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, @Local LevelProperties levelProperties) {
		if (DedicatedServerImplUtil.saveLevelDataTo != null) {
			NbtCompound levelDatInner = levelProperties.cloneWorldNbt(dynamicRegistries.getCombinedRegistryManager(), null);
			NbtCompound levelDat = new NbtCompound();
			levelDat.put("Data", levelDatInner);

			try {
				Files.createDirectories(DedicatedServerImplUtil.saveLevelDataTo);
				NbtIo.writeCompressed(levelDat, DedicatedServerImplUtil.saveLevelDataTo.resolve("level.dat"));
			} catch (IOException e) {
				ClientGameTestImpl.LOGGER.error("Failed to save dedicated server level data", e);
			}

			ci.cancel();
		}
	}
}
