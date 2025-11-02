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

package net.fabricmc.fabric.mixin.recipe.client.sync;

import java.util.HashSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;
import net.fabricmc.fabric.impl.recipe.sync.SupportedRecipeSerializersPayloadC2S;

@Mixin(ClientConfigurationNetworkHandler.class)
public class ClientConfigurationNetworkHandlerMixin {
	@Inject(method = "onSelectKnownPacks", at = @At("TAIL"))
	private void sendSupportedRecipeSerializers(SelectKnownPacksS2CPacket packet, CallbackInfo ci) {
		if (!ClientConfigurationNetworking.canSend(SupportedRecipeSerializersPayloadC2S.ID)) {
			return;
		}

		var ids = new HashSet<Identifier>();

		for (RecipeSerializer<?> serializer : RecipeSyncImpl.getSyncedSerializers()) {
			ids.add(Registries.RECIPE_SERIALIZER.getId(serializer));
		}

		// No need to send empty requests, it's the default state anyway.
		if (ids.isEmpty()) {
			return;
		}

		ClientConfigurationNetworking.send(new SupportedRecipeSerializersPayloadC2S(ids));
	}
}
