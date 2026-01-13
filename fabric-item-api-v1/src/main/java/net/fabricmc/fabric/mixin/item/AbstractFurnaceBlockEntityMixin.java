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
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
	// Copy the stack before the .shrink
	@WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
	private static Item copyStack(ItemStack itemStack, Operation<Item> original, @Share("itemStack") LocalRef<ItemStack> copiedStack) {
		copiedStack.set(itemStack.copy());
		return original.call(itemStack);
	}

	@Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getCraftingRemainder()Lnet/minecraft/world/item/ItemStackTemplate;"))
	private static ItemStackTemplate getCraftingRemainder(Item item, @Share("itemStack") LocalRef<ItemStack> stack) {
		return stack.get().getCraftingRemainder();
	}
}
