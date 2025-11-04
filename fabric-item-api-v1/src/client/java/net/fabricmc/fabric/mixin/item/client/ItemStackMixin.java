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

package net.fabricmc.fabric.mixin.item.client;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	// Only target the second RETURN, the first RETURN is for no tooltip
	@Inject(method = "getTooltipLines", at = @At(value = "RETURN", ordinal = 1))
	private void getTooltip(Item.TooltipContext tooltipContext, @Nullable Player entity, TooltipFlag tooltipType, CallbackInfoReturnable<List<Component>> info) {
		ItemTooltipCallback.EVENT.invoker().getTooltip((ItemStack) (Object) this, tooltipContext, tooltipType, info.getReturnValue());
	}
}
