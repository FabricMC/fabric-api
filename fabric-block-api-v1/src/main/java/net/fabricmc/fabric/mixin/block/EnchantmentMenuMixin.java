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

package net.fabricmc.fabric.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.level.Level;

@Mixin(EnchantmentMenu.class)
public class EnchantmentMenuMixin {
	@WrapOperation(method = "lambda$slotsChanged$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/EnchantingTableBlock;isValidBookShelf(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean addEnchantingPower(Level level, BlockPos pos, BlockPos offset, Operation<Boolean> original, @Share("power") LocalFloatRef power) {
		if (original.call(level, pos, offset)) {
			power.set(
					power.get()
					+ level.getBlockState(pos.offset(offset)).getProvidedEnchantmentPower(level, pos.offset(offset))
			);
			return true;
		}

		return false;
	}

	@ModifyArg(method = "lambda$slotsChanged$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"), index = 2)
	int replaceBookcasesWithPower(int bookcases, @Share("power") LocalFloatRef power) {
		// Round x.5 to x
		return Math.max(-Math.round(-power.get()), 0);
	}
}
