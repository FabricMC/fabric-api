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

package net.fabricmc.fabric.mixin.recipe.book;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.inventory.RecipeBookType;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.recipe.book.ClientboundRecipeBookSyncPayload;
import net.fabricmc.fabric.impl.recipe.book.RecipeBookSettingsExtension;

@Mixin(ServerRecipeBook.class)
public class ServerRecipeBookMixin extends RecipeBook {
	@Inject(method = "sendInitialRecipeBook", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;<init>(I)V"))
	private void sendModdedRecipeBookTypeSettings(ServerPlayer player, CallbackInfo ci) {
		RecipeBookSettings settings = getBookSettings();

		Map<RecipeBookType, RecipeBookSettings.TypeSettings> fabricSettings = new HashMap<>(((RecipeBookSettingsExtension) (Object) settings).fabric_getTypeSettings());
		// Remove anything that should be handled by default.
		fabricSettings.values().removeIf(typeSettings -> !typeSettings.open() && !typeSettings.filtering());

		if (!fabricSettings.isEmpty()) {
			ServerPlayNetworking.send(player, new ClientboundRecipeBookSyncPayload(fabricSettings));
		}
	}
}
