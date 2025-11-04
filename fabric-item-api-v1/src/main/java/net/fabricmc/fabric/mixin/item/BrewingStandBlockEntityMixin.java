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

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {
	@Unique
	private static final ThreadLocal<ItemStack> REMAINDER_STACK = new ThreadLocal<>();

	@Inject(method = "doBrew", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", shift = At.Shift.AFTER))
	private static void captureItemStack(Level world, BlockPos pos, NonNullList<ItemStack> slots, CallbackInfo ci, @Local ItemStack itemStack) {
		REMAINDER_STACK.set(itemStack.getRecipeRemainder());
	}

	/**
	 * Wrap the {@link Item#getRecipeRemainder} call to replace the old remainder with the new one.
	 */
	@Redirect(method = "doBrew", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getCraftingRemainder()Lnet/minecraft/world/item/ItemStack;"))
	private static ItemStack createStackRecipeRemainder(Item item) {
		ItemStack remainder = REMAINDER_STACK.get();
		REMAINDER_STACK.remove();
		return remainder;
	}
}
