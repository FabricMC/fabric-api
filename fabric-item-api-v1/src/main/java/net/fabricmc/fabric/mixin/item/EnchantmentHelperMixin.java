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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {
	@Redirect(
			method = "method_60143",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;isPrimaryItem(Lnet/minecraft/world/item/ItemStack;)Z")
	)
	private static boolean useCustomEnchantingChecks(Enchantment instance, ItemStack stack, ItemStack itemStack, boolean bl, Holder<Enchantment> registryEntry) {
		return stack.canBeEnchantedWith(registryEntry, EnchantingContext.PRIMARY);
	}
}
