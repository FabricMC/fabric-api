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

package net.fabricmc.fabric.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

import net.fabricmc.fabric.impl.item.RecipeRemainderHandler;

@Mixin(CraftingRecipe.class)
public interface CraftingRecipeMixin {
	@WrapOperation(method = "defaultCraftingReminder", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
	private static Item captureStack(ItemStack stack, Operation<Item> operation) {
		RecipeRemainderHandler.REMAINDER_STACK.set(stack.getRecipeRemainder());
		return operation.call(stack);
	}

	@Redirect(method = "defaultCraftingReminder", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getCraftingRemainder()Lnet/minecraft/world/item/ItemStack;"))
	private static ItemStack getStackRemainder(Item item) {
		ItemStack remainder = RecipeRemainderHandler.REMAINDER_STACK.get();
		RecipeRemainderHandler.REMAINDER_STACK.remove();
		return remainder;
	}
}
