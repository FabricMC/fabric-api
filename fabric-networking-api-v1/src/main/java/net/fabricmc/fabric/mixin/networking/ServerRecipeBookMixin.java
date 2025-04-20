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

package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.RecipeBookAddS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerRecipeBook;

import net.fabricmc.fabric.impl.networking.RecipeBookAddPacketSplitter;

@Mixin(ServerRecipeBook.class)
public abstract class ServerRecipeBookMixin {
	@Unique
	private static final boolean DISABLE_SPLIT = System.getProperty("fabric-networking-api-v1.disableRecipeBookPacketSplitter") != null;

	@WrapOperation(method = {"unlockRecipes", "sendInitRecipesPacket"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
	private void splitRecipeBookAddPackets(ServerPlayNetworkHandler instance, Packet<?> packet, Operation<Void> original) {
		if (!DISABLE_SPLIT && packet instanceof RecipeBookAddS2CPacket recipeBookAddPacket) {
			RecipeBookAddPacketSplitter.split(
					recipeBookAddPacket,
					instance,
					newPacket -> original.call(instance, newPacket)
			);

			return;
		}

		original.call(instance, packet);
	}
}
