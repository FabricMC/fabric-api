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

package net.fabricmc.fabric.mixin.client.gametest.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.RegistryLayer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.SavedDataStorage;

import net.fabricmc.fabric.impl.client.gametest.util.ClientGameTestImpl;
import net.fabricmc.fabric.impl.client.gametest.util.DedicatedServerImplUtil;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
	@Inject(method = "onCreate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;confirmWorldCreation(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Runnable;Z)V"), cancellable = true)
	private void createLevelDataForServers(CallbackInfo ci, @Local(name = "finalLayers") LayeredRegistryAccess<RegistryLayer> finalLayers, @Local(name = "worldData") PrimaryLevelData worldData, @Local(name = "worldGenSettings") WorldGenSettings worldGenSettings, @Local(name = "gameRules") GameRules gameRules) {
		if (DedicatedServerImplUtil.saveLevelDataTo != null) {
			try {
				Path worldPath = DedicatedServerImplUtil.saveLevelDataTo;

				CompoundTag levelDatInner = worldData.createTag(null);
				var levelDat = new CompoundTag();
				levelDat.put("Data", levelDatInner);

				Files.createDirectories(worldPath);
				NbtIo.writeCompressed(levelDat, worldPath.resolve("level.dat"));

				try (var savedDataStorage = new SavedDataStorage(worldPath.resolve("data"), DataFixers.getDataFixer(), finalLayers.compositeAccess())) {
					savedDataStorage.set(WorldGenSettings.TYPE, worldGenSettings);
					savedDataStorage.set(GameRuleMap.TYPE, ((GameRulesAccessor) gameRules).getRules());
					savedDataStorage.saveAndJoin();
				}
			} catch (IOException e) {
				ClientGameTestImpl.LOGGER.error("Failed to save dedicated server level data", e);
			}

			ci.cancel();
		}
	}
}
