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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.recipebook.ClientRecipeManager;

import net.fabricmc.fabric.impl.recipe.sync.client.SynchronizedClientRecipesSetter;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow
	private ClientRecipeManager recipeManager;

	/*
	 * Copies previously synchronized client recipes, as server mods might send the SynchronizeRecipesS2CPacket for custom
	 * vanilla compatible functionality, without actually wanting to (re)synchronize recipes.
	 */
	@WrapOperation(method = "onSynchronizeRecipes", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;recipeManager:Lnet/minecraft/client/recipebook/ClientRecipeManager;", opcode = Opcodes.PUTFIELD))
	private void copyPreviousRecipes(ClientPlayNetworkHandler instance, ClientRecipeManager value, Operation<Void> original) {
		((SynchronizedClientRecipesSetter) value).fabric_setSynchronizedClientRecipes(this.recipeManager.getSynchronizedRecipes());
		original.call(instance, value);
	}
}
