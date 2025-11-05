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

import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;
import net.fabricmc.fabric.impl.recipe.sync.SupportedRecipeSerializersPayloadC2S;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public class ClientConfigurationPacketListenerImplMixin {
	@Inject(method = "handleSelectKnownPacks", at = @At("TAIL"))
	private void sendSupportedRecipeSerializers(ClientboundSelectKnownPacks packet, CallbackInfo ci) {
		if (!ClientConfigurationNetworking.canSend(SupportedRecipeSerializersPayloadC2S.ID)) {
			return;
		}

		var ids = new HashSet<Identifier>();

		for (RecipeSerializer<?> serializer : RecipeSyncImpl.getSyncedSerializers()) {
			ids.add(BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer));
		}

		// No need to send empty requests, it's the default state anyway.
		if (ids.isEmpty()) {
			return;
		}

		ClientConfigurationNetworking.send(new SupportedRecipeSerializersPayloadC2S(ids));
	}
}
